package com.example.umbrella

import android.annotation.SuppressLint
import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.toRectF
import com.example.umbrella.core.Inspection

class AlignmentCanvasPageTestRun(
    override val view: View, override val icMap: ImageCanvasMap
) : CanvasPage {
    private val alignment = Inspection.alignment

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
                || mScaleGestureDetector!!.onTouchEvent(event)
    }

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null

    private val mScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                icMap.zoom(detector.scaleFactor)
                view.invalidate()

                super.onScale(detector)
                return true
            }
        }

    private val mSimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e1.getPointerId(0) == e2.getPointerId(0)) {
                icMap.moveC(PointF(distanceX, distanceY))
                view.invalidate()
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    init {
        mScaleGestureDetector = ScaleGestureDetector(view.context, mScaleGestureListener)
        mGestureDetector = GestureDetector(view.context, mSimpleOnGestureListener)
    }

    private val g = Graphics(view.context)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rectModelC = icMap.mapI2C(alignment.model1.areaModel.toRectF())
        val rectC = icMap.mapI2C(alignment.model1.areaSearch.toRectF())

        fun drawModelFrame() {
            val paint = Paint()

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(13, 0, 140, 70)
            canvas.drawRect(rectModelC, paint)

            paint.strokeWidth = g.px(2f)
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(178, 0, 140, 70)
            canvas.drawRect(rectModelC, paint)
        }

        fun drawJudgeLabel() {
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(178, 0, 140, 70)

            val width = rectModelC.width() * 0.4f
            val height = width * 0.3f
            val left = rectModelC.left - g.px(1f)
            val right = left + width
            val top = rectModelC.top - height
            val bot = rectModelC.top - g.px(1f)
            val rect = RectF(left, top, right, bot)

            canvas.drawRect(rect, paint)

            paint.textSize = height * 0.6f
            paint.color = Color.WHITE
            g.drawLabel(canvas, "OK", rect, paint, Graphics.ALIGN_CENTER)
        }

        fun drawBack() {
            val width = icMap.sizeCanvas.width()
            val height = icMap.sizeCanvas.height()
            // top, bottom, left, right
            val rects = arrayOf(
                RectF(0f, 0f, width.toFloat(), rectC.top),
                RectF(0f, rectC.bottom, width.toFloat(), height.toFloat()),
                RectF(0f, rectC.top, rectC.left, rectC.bottom),
                RectF(rectC.right, rectC.top, width.toFloat(), rectC.bottom)
            )

            val paint = Paint()
            paint.color = Color.argb(89, 0, 0, 0)

            for (rect in rects) {
                canvas.drawRect(rect, paint)
            }
        }

        fun drawFrame() {
            val paint = Paint()
            paint.strokeWidth = g.px(1f)
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(178, 0xff, 0xff, 0xff)

            canvas.drawRect(rectC, paint)
        }

        drawModelFrame()
        drawJudgeLabel()
        drawBack()
        drawFrame()
    }
}