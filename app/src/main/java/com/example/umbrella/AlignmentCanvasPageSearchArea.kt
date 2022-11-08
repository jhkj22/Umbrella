package com.example.umbrella

import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.minus
import androidx.core.graphics.toRectF
import com.example.umbrella.core.Inspection
import kotlin.math.max
import kotlin.math.min

class AlignmentCanvasPageSearchArea(
    override val view: View, override val icMap: ImageCanvasMap
) : CanvasPage {

    private val alignment = Inspection.alignment

    private val g = Graphics(view.context)

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    private val mScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                icMap.zoom(detector.scaleFactor)

                view.invalidate()

                super.onScale(detector)
                return true
            }
        }

    private var dragDiagonalPoint: PointF? = null
    private var motionLastPoint = PointF()


    private fun getFourCorners(rect: RectF): Array<Pair<Graphics.Corner, PointF>> {
        return arrayOf(
            Graphics.Corner.LT,
            Graphics.Corner.RT,
            Graphics.Corner.RB,
            Graphics.Corner.LB
        )
            .map { c -> Pair(c, g.getCorner(rect, c)) }.toTypedArray()
    }

    private fun onTouchDown(e: MotionEvent) {
        dragDiagonalPoint = null

        val pDown = PointF(e.x, e.y)

        val area = alignment.model1.areaSearch.toRectF()
        val areaC = icMap.mapI2C(area)

        for ((corner, point) in getFourCorners(areaC)) {
            val dp = point - pDown
            if (dp.length() < g.px(20f)) {
                dragDiagonalPoint = g.getCorner(
                    area, g.getDiagonalCorner(corner)
                )
                break
            }
        }

        motionLastPoint = pDown
    }

    private fun onMotion(e: MotionEvent) {
        if (dragDiagonalPoint == null) {
            val p1 = PointF(e.x, e.y)
            val p2 = motionLastPoint
            val dp = p2 - p1
            icMap.moveC(dp)

            motionLastPoint = p1
        } else {
            val p1 = icMap.mapC2I(PointF(e.x, e.y))
            val p2 = dragDiagonalPoint!!

            val left = min(p1.x, p2.x).toInt()
            val right = max(p1.x, p2.x).toInt()
            val top = min(p1.y, p2.y).toInt()
            val bot = max(p1.y, p2.y).toInt()

            alignment.model1.areaSearch.set(left, top, right, bot)
        }

        view.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onTouchDown(event)
            MotionEvent.ACTION_MOVE -> onMotion(event)
        }

        return mScaleGestureDetector!!.onTouchEvent(event)
    }

    init {
        alignment.initAreaSearch()

        mScaleGestureDetector = ScaleGestureDetector(view.context, mScaleGestureListener)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rectModelC = icMap.mapI2C(alignment.model1.areaModel.toRectF())
        val rectC = icMap.mapI2C(alignment.model1.areaSearch.toRectF())

        fun drawModelFrame() {
            val paint = Paint()
            paint.strokeWidth = g.px(1f)
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(178, 0xff, 0xff, 0xff)

            canvas.drawRect(rectModelC, paint)
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

        fun drawKnob() {
            val paint = Paint()
            paint.color = Color.WHITE

            for ((c, p) in getFourCorners(rectC)) {
                canvas.drawCircle(p.x, p.y, g.px(4f), paint)
            }
        }

        fun drawSizeLabel() {
            val paint = Paint()
            paint.color = Color.BLACK

            val paintText = Paint()
            paintText.textSize = g.pxT(12f)
            paintText.color = Color.WHITE

            val text = "%d x %d".format(
                alignment.model1.areaSearch.width(),
                alignment.model1.areaSearch.height()
            )

            val wText = paintText.measureText(text)

            val fontMetrics = paintText.fontMetrics
            val hText = fontMetrics.bottom - fontMetrics.top


            val w = wText + g.px(12f) * 2f
            val h = hText * 1.8f
            val x = rectC.centerX() - w / 2
            val y = rectC.top - h - g.px(10f)
            val rect = RectF(x, y, x + w, y + h)

            canvas.drawRoundRect(rect, g.px(4f), g.px(4f), paint)

            canvas.drawText(
                text,
                x + (w - wText) / 2,
                y + (h - hText) / 2 - fontMetrics.top,
                paintText
            )
        }

        drawModelFrame()
        drawBack()
        drawFrame()
        drawKnob()
        drawSizeLabel()
    }
}