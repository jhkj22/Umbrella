package com.example.umbrella

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class LiveThread {

    var address: InetAddress? = null
    var bitmap: Bitmap? = null

    var onImageUpdated: (() -> Unit)? = null
    var running = false

    private var writer: PrintWriter? = null

    fun start() {
        if (running) return

        Thread {
            loop()
        }.start()
    }

    fun stop() {
        if (!running) return

        Thread {
            writer?.println("LIVE=STOP")
        }.start()
    }

    private fun loop() {
        running = true

        val address = InetSocketAddress(address?.hostAddress, 8080)

        val socket = Socket()
        try {
            socket.connect(address, 1000)

            writer = PrintWriter(socket.getOutputStream(), true)
            writer?.println("LIVE=START")

            val input = socket.getInputStream()
            val buffer = ByteArray(128 * 1024)

            fun updateImage(i: Int, size: Int) {
                bitmap = BitmapFactory.decodeByteArray(buffer, i, size)
                onImageUpdated?.invoke()
            }

            val headerSize = 3
            var index = 0
            val indicesHead = mutableListOf(0)

            while (true) {
                if (index + 4096 > buffer.size) break

                var size = input.read(buffer, index, 4096)

                if (size < 0) break

                val iHead = indicesHead.last()
                if (iHead + headerSize <= index + size) {
                    val dataSize = buffer
                        .slice(iHead until iHead + headerSize)
                        .mapIndexed { i, v -> v.toUByte().toInt().shl(i * 8) }
                        .sum()

                    val nextHead = iHead + dataSize
                    if (nextHead > buffer.size - 10000) {
                        buffer.copyInto(buffer, 0, iHead, index + size)
                        size -= iHead - index
                        index = 0

                        indicesHead.addAll(arrayOf(0, dataSize))
                    } else {
                        indicesHead.add(nextHead)
                    }
                }

                index += size

                while (indicesHead.size > 2) {
                    val i0 = indicesHead[0]
                    val i1 = indicesHead[1]

                    if (i0 >= i1) {
                        indicesHead.removeAt(0)
                    } else {
                        updateImage(i0 + headerSize, i1 - i0 - headerSize)
                        indicesHead.removeAt(0)
                    }
                }
            }

        } catch (e: Exception) {
            Log(e.toString())
        } finally {
            socket.close()
        }

        writer = null
        running = false
    }
}