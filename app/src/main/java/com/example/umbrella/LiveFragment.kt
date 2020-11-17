package com.example.umbrella

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LiveFragment : Fragment(), WifiP2pManager.ChannelListener {

    private fun log(text: String) {
        /*activity?.runOnUiThread {
            var t = textView.text.toString()
            if (t.isNotEmpty()) {
                t += System.lineSeparator()
            }
            t += text

            textView.text = t
        }*/

        Log(text)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        if (!initP2p()) {
            Toast.makeText(
                context,
                "Failed to Init P2P.", Toast.LENGTH_LONG
            ).show()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (statusOfThis == WifiP2pDevice.AVAILABLE) {
                discover()
            }
        }, 1000)
    }

    private fun setupActionBar() {
        val activity = activity as MainActivity
        activity.title = "Live"
        activity.setupBackButton(true)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val liveThread = LiveThread

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(receiver, intentFilter)

        if (statusOfThis == WifiP2pDevice.CONNECTED) {
            liveThread.start()
        }
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(receiver)

        liveThread.stop()
    }

    private val intentFilter = object : IntentFilter() {
        init {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    private fun initP2p(): Boolean {
        val wifiManager: WifiManager =
            context?.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isP2pSupported) {
            log("Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.")
            return false
        }
        manager = context?.getSystemService(AppCompatActivity.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(context, activity?.mainLooper, null)
        return true
    }

    @SuppressLint("MissingPermission")
    private fun discover() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("discoverPeers onSuccess")
            }

            override fun onFailure(reasonCode: Int) {
                log("discoverPeers onFailure")
            }
        })
    }

    private var statusOfThis = WifiP2pDevice.AVAILABLE

    private fun deviceStatus(status: Int): String? {
        val mapStatus = mapOf(
            WifiP2pDevice.AVAILABLE to "AVAILABLE",
            WifiP2pDevice.CONNECTED to "CONNECTED",
            WifiP2pDevice.FAILED to "FAILED",
            WifiP2pDevice.INVITED to "INVITED",
            WifiP2pDevice.UNAVAILABLE to "UNAVAILABLE"
        )

        return mapStatus[status]
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action.toString()

            var extra = ""
            when (action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        extra = "enabled"
                    } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        extra = "disabled"
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> run {
                    val deviceList =
                        intent.getParcelableExtra<WifiP2pDeviceList>(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                            ?: return@run

                    val devices = deviceList.deviceList
                    for (device in devices) {
                        if (device.deviceName != "raspberry") {
                            continue
                        }

                        extra += device.deviceName + " -> " + deviceStatus(device.status)

                        if (WifiP2pDevice.AVAILABLE == device.status &&
                            WifiP2pDevice.AVAILABLE == statusOfThis
                        ) {
                            statusOfThis = WifiP2pDevice.INVITED

                            connect(device)
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> run {
                    val info =
                        intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                            ?: return@run
                    val network =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                            ?: return@run
                    //val group = intent.getParcelableExtra<WifiP2pGroup>(WifiP2pManager.EXTRA_WIFI_P2P_GROUP)

                    val isConnected = network.isConnected
                    extra = if (isConnected) {
                        info.groupOwnerAddress.toString()
                    } else {
                        "false"
                    }

                    if (isConnected) {
                        statusOfThis = WifiP2pDevice.CONNECTED

                        liveThread.address = info.groupOwnerAddress
                        liveThread.start()
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> run {
                    val device =
                        intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                            ?: return@run

                    extra = deviceStatus(device.status).toString()
                }
            }

            val label = action.replace("android.net.wifi.p2p.", "")

            log("$label: $extra")
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: WifiP2pDevice) {

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Connect succeeded.")
            }

            override fun onFailure(reason: Int) {
                log("Connect failed. Retry.")
            }
        })
    }

    private fun disconnect() {
        if (statusOfThis != WifiP2pDevice.CONNECTED) return
        statusOfThis = WifiP2pDevice.AVAILABLE

        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                log("Disconnect failed. Reason :$reasonCode")
            }

            override fun onSuccess() {
                log("Disconnect succeeded")
            }
        })
    }

    override fun onChannelDisconnected() {
    }

    /*
    private fun startLive() {
        val address = InetSocketAddress(targetAddress.hostAddress, 8080)

        val socket = Socket()
        val time = measureTimeMillis {
            try {
                socket.connect(address, 1000)

                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println("LIVE=START")

                val input = socket.getInputStream()
                val buffer = ByteArray(128 * 1024)

                fun updateImage(i: Int, size: Int) {
                    val bitmap = BitmapFactory.decodeByteArray(buffer, i, size)
                    activity?.runOnUiThread {
                        imageView2.setImageBitmap(bitmap)
                    }
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
                log(e.toString())
            } finally {
                socket.close()
            }
        }

        log("$time")
    }

    private val runnable = Runnable {
        if (statusOfThis == WifiP2pDevice.CONNECTED) {
            startLive()
        }
    }*/
}