package com.example.umbrella.core

import android.graphics.PointF
import android.graphics.Rect

class Alignment {
    val areaModel = Rect()
    val areaSearch = Rect()
    val cp = PointF(-1f, -1f)
}

class TwoPointAlignment {
    val model1 = Alignment()
    val model2 = Alignment()

    fun initCp(sizeImage: Rect) {
        if (model1.cp.x >= 0 && model1.cp.y >= 0) {
            return
        }

        model1.cp.x = sizeImage.width() / 2f
        model1.cp.y = sizeImage.height() / 2f
    }

    fun initAreaModel(width: Int, height: Int) {
        var s = model1.areaModel.width() / 2
        if (s == 0) s = width / 2

        model1.areaModel.left = model1.cp.x.toInt() - s
        model1.areaModel.right = model1.cp.x.toInt() + s
        model1.areaModel.top = model1.cp.y.toInt() - s
        model1.areaModel.bottom = model1.cp.y.toInt() + s
    }

    fun initAreaSearch() {
        if (model1.areaSearch.width() > 0 || model1.areaSearch.height() > 0) return

        val cx = model1.areaModel.centerX()
        val cy = model1.areaModel.centerY()
        val s = model1.areaModel.width()

        model1.areaSearch.left = cx - s
        model1.areaSearch.right = cx + s
        model1.areaSearch.top = cy - s
        model1.areaSearch.bottom = cy + s
    }
}