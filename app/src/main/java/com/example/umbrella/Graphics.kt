package com.example.umbrella

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.TypedValue

class Graphics(private val context: Context) {
    fun px(dp: Float): Float {
        val metrics = context.resources.displayMetrics
        return dp * metrics.density
    }

    fun pxT(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics
        )
    }

    companion object {
        const val ALIGN_CENTER_V: Int = 0b01
        const val ALIGN_CENTER_H: Int = 0b10
        const val ALIGN_CENTER: Int = ALIGN_CENTER_H + ALIGN_CENTER_V
        const val ALIGN_LEFT: Int = 0b100
        const val ALIGN_RIGHT: Int = 0b1000
        const val ALIGN_TOP: Int = 0b10000
        const val ALIGN_BOTTOM: Int = 0b100000
    }


    fun drawLabel(canvas: Canvas, text: String, rect: RectF, paint: Paint, align: Int) {
        val wText = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val hText = fontMetrics.bottom - fontMetrics.top
        val hBase = fontMetrics.leading - fontMetrics.top

        var left: Float = rect.left
        var top: Float = rect.top

        if (align and ALIGN_CENTER_H > 0) {
            left += (rect.width() - wText) / 2
        } else if (align and ALIGN_RIGHT > 0) {
            left += rect.width() - wText
        }

        if (align and ALIGN_CENTER_V > 0) {
            Log("${rect.height()} $hText $hBase")
            top += (rect.height() - hText) / 2 + hBase
        } else if (align and ALIGN_TOP > 0) {
            top += hBase
        } else if (align and ALIGN_BOTTOM > 0) {
            top += rect.height() - hText + hBase
        }

        canvas.drawText(text, left, top, paint)
    }

    enum class Corner {
        LT, RT, LB, RB
    }

    fun getCorner(rect: RectF, corner: Corner): PointF {
        return when (corner) {
            Corner.LT -> PointF(rect.left, rect.top)
            Corner.RT -> PointF(rect.right, rect.top)
            Corner.LB -> PointF(rect.left, rect.bottom)
            Corner.RB -> PointF(rect.right, rect.bottom)
        }
    }

    fun getDiagonalCorner(corner: Corner): Corner {
        return when (corner) {
            Corner.LT -> Corner.RB
            Corner.RT -> Corner.LB
            Corner.LB -> Corner.RT
            Corner.RB -> Corner.LT
        }
    }
}