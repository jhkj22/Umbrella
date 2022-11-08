package com.example.umbrella

import android.annotation.SuppressLint
import android.graphics.*
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.minus
import androidx.core.graphics.toRectF
import com.example.umbrella.core.Inspection
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class AlignmentCanvasPageModelSize(
    override val view: View, override val icMap: ImageCanvasMap
) : CanvasPage {

    private val alignment = Inspection.alignment

    private fun px(dp: Int): Float {
        val metrics = view.context.resources.displayMetrics
        return dp * metrics.density
    }

    private fun pxT(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, view.context.resources.displayMetrics
        )
    }

    private fun getFourCorners(rect: RectF): Array<PointF> {
        val top = rect.top
        val bot = rect.bottom
        val left = rect.left
        val right = rect.right

        return arrayOf(
            PointF(left, top),
            PointF(right, top),
            PointF(right, bot),
            PointF(left, bot)
        )
    }

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    private val mScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                icMap.zoom(detector.scaleFactor)

                val posC = PointF(
                    icMap.sizeCanvas.width() * .5f,
                    icMap.sizeCanvas.height() * .4f
                )

                val dpC = icMap.mapI2C(alignment.model1.cp) - posC
                icMap.moveC(dpC)

                view.invalidate()

                super.onScale(detector)
                return true
            }
        }

    private var dragging = false

    private fun onTouchDown(e: MotionEvent) {
        dragging = false

        val rectModelC = icMap.mapI2C(alignment.model1.areaModel.toRectF())
        for (corner in getFourCorners(rectModelC)) {
            val dp = corner - PointF(e.x, e.y)
            if (dp.length() < px(20)) {
                dragging = true
                break
            }
        }
    }

    private fun onMotion(e: MotionEvent) {
        if (!dragging) return

        val pCur = icMap.mapC2I(PointF(e.x, e.y))

        val dp = pCur - alignment.model1.cp
        val w = abs(dp.x)
        val h = abs(dp.y)

        val s = max(1, min(w, h).toInt())

        alignment.model1.areaModel.left = alignment.model1.cp.x.toInt() - s
        alignment.model1.areaModel.right = alignment.model1.cp.x.toInt() + s
        alignment.model1.areaModel.top = alignment.model1.cp.y.toInt() - s
        alignment.model1.areaModel.bottom = alignment.model1.cp.y.toInt() + s

        view.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> onTouchDown(event)
            ACTION_MOVE -> onMotion(event)
        }

        return mScaleGestureDetector!!.onTouchEvent(event)
    }

    init {
        val w = (icMap.sizeCanvas.width() / 2 / icMap.scale).toInt()
        val wRound = (w / 50) * 50
        alignment.initAreaModel(wRound, wRound)

        mScaleGestureDetector = ScaleGestureDetector(view.context, mScaleGestureListener)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val rectModelC = icMap.mapI2C(alignment.model1.areaModel.toRectF())

        fun drawBack() {
            val width = icMap.sizeCanvas.width()
            val height = icMap.sizeCanvas.height()
            // top, bottom, left, right
            val rects = arrayOf(
                RectF(0f, 0f, width.toFloat(), rectModelC.top),
                RectF(0f, rectModelC.bottom, width.toFloat(), height.toFloat()),
                RectF(0f, rectModelC.top, rectModelC.left, rectModelC.bottom),
                RectF(rectModelC.right, rectModelC.top, width.toFloat(), rectModelC.bottom)
            )

            val paint = Paint()
            paint.color = Color.argb(89, 0, 0, 0)

            for (rect in rects) {
                canvas.drawRect(rect, paint)
            }
        }

        fun drawFrame() {
            val paint = Paint()
            paint.strokeWidth = px(1)
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(178, 0xff, 0xff, 0xff)

            canvas.drawRect(rectModelC, paint)
        }

        fun drawKnob() {
            val paint = Paint()
            paint.color = Color.WHITE

            for (p in getFourCorners(rectModelC)) {
                canvas.drawCircle(p.x, p.y, px(4), paint)
            }
        }

        fun drawSizeLabel() {
            val paint = Paint()
            paint.color = Color.BLACK

            val paintText = Paint()
            paintText.textSize = pxT(12f)
            paintText.color = Color.WHITE

            val text = "%d x %d".format(
                alignment.model1.areaModel.width(),
                alignment.model1.areaModel.height()
            )

            val wText = paintText.measureText(text)

            val fontMetrics = paintText.fontMetrics
            val hText = fontMetrics.bottom - fontMetrics.top


            val w = wText + px(12) * 2f
            val h = hText * 1.8f
            val x = rectModelC.centerX() - w / 2
            val y = rectModelC.top - h - px(10)
            val rect = RectF(x, y, x + w, y + h)

            canvas.drawRoundRect(rect, px(4), px(4), paint)

            canvas.drawText(
                text,
                x + (w - wText) / 2,
                y + (h - hText) / 2 - fontMetrics.top,
                paintText
            )
        }

        drawBack()
        drawFrame()
        drawKnob()
        drawSizeLabel()
    }
}