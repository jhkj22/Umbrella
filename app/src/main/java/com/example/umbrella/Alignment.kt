package com.example.umbrella

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect

class Alignment {
    val areaModel = Rect()
    val areaSearch = Rect()
    val cp = PointF()
}

object TwoPointAlignment {
    lateinit var image: Bitmap
    val model1 = Alignment()
    val model2 = Alignment()

    fun initCp() {
        model1.cp.x = image.width / 2f
        model1.cp.y = image.height / 2f
    }

    fun initAreaModel() {
        val s = 100

        model1.areaModel.left = model1.cp.x.toInt() - s
        model1.areaModel.right = model1.cp.x.toInt() + s
        model1.areaModel.top = model1.cp.y.toInt() - s
        model1.areaModel.bottom = model1.cp.y.toInt() + s
    }
}