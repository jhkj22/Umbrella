package com.example.umbrella.core

import android.graphics.Rect

class Blob {
    val area = Rect()

    fun initArea(sizeImage: Rect) {
        if (area.width() > 0 || area.height() > 0) return

        val width = sizeImage.width() / 2
        val height = sizeImage.height() / 2
        val left = (sizeImage.width() - width) / 2
        val top = (sizeImage.height() - height) / 2

        area.set(left, top, left + width, top + height)
    }
}