package com.example.umbrella

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import androidx.core.graphics.toRectF

class ImageCanvasMap {
    var sizeImage = Rect(0, 0, 1, 1)
    var sizeCanvas = Rect(0, 0, 1, 1)
    var scale: Float = 1f
    var cpCanvas = PointF(0.5f, 0.5f)

    private val onChangedListeners: ArrayList<() -> Unit> = arrayListOf()

    fun setOnChangedListener(callback: (() -> Unit)) {
        onChangedListeners.add(callback)
    }

    private fun notifyChanges() {
        for (callback in onChangedListeners) {
            callback.invoke()
        }
    }

    fun moveI(dp: PointF) {
        cpCanvas += dp
        notifyChanges()
    }

    fun moveC(dp: PointF) {
        cpCanvas += PointF(dp.x / scale, dp.y / scale)
        notifyChanges()
    }

    fun zoom(ratio: Float) {
        scale *= ratio
        notifyChanges()
    }

    fun mapI2C(pI: PointF): PointF {
        val offset = PointF(sizeCanvas.width() * 0.5f, sizeCanvas.height() * 0.5f)
        val dp = pI - cpCanvas
        return PointF(dp.x * scale, dp.y * scale) + offset
    }

    fun mapC2I(pC: PointF): PointF {
        val offset = PointF(sizeCanvas.width() * 0.5f, sizeCanvas.height() * 0.5f)
        val dp = pC - offset
        return cpCanvas + PointF(dp.x / scale, dp.y / scale)
    }

    fun mapI2C(rI: RectF): RectF {
        val p1 = mapI2C(PointF(rI.left, rI.top))
        val p2 = mapI2C(PointF(rI.right, rI.bottom))
        return RectF(p1.x, p1.y, p2.x, p2.y)
    }

    fun mapC2I(rC: RectF): RectF {
        val p1 = mapC2I(PointF(rC.left, rC.top))
        val p2 = mapC2I(PointF(rC.right, rC.bottom))
        return RectF(p1.x, p1.y, p2.x, p2.y)
    }

    fun getRectImageI(): RectF {
        return sizeImage.toRectF()
    }

    fun getRectImageC(): RectF {
        val rect = getRectImageI()
        val pos = mapI2C(PointF(rect.left, rect.top))
        val w = rect.width() * scale
        val h = rect.height() * scale

        return RectF(pos.x, pos.y, pos.x + w, pos.y + h)
    }
}