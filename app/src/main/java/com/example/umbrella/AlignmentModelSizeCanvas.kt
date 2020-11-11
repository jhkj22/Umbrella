package com.example.umbrella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.ScaleGestureDetector
import androidx.core.graphics.minus
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class AlignmentModelSizeCanvas(context: Context, attrs: AttributeSet) : CanvasBase(context, attrs) {
    private val cpModel = PointF()
    private val rectModel = Rect()

    private fun px(dp: Int): Float {
        val metrics = context.resources.displayMetrics
        return dp * metrics.density
    }

    private fun pxT(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics
        )
    }

    private fun getFourCorners(rect: Rect): Array<Point> {
        val top = rect.top
        val bot = rect.bottom
        val left = rect.left
        val right = rect.right

        return arrayOf(
            Point(left, top),
            Point(right, top),
            Point(right, bot),
            Point(left, bot)
        )
    }

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    private val mScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                val cx = rectModel.exactCenterX()
                val cy = rectModel.exactCenterY()
                bitmapMatrix.postScale(scale, scale, cx, cy)
                invalidate()
                super.onScale(detector)
                return true
            }
        }

    init {
        mScaleGestureDetector = ScaleGestureDetector(context, mScaleGestureListener)
    }

    private var dragging = false

    private fun onTouchDown(e: MotionEvent) {
        dragging = false
        for (corner in getFourCorners(rectModel)) {
            val p1 = PointF(corner.x.toFloat(), corner.y.toFloat())
            val p2 = PointF(e.x, e.y)
            val dp = p1 - p2
            if (dp.length() < px(20)) {
                dragging = true
                break
            }
        }
    }

    private fun onMotion(e: MotionEvent) {
        if (!dragging) return

        val pCur = PointF(e.x, e.y)

        val dp = pCur - cpModel
        val w = abs(dp.x)
        val h = abs(dp.y)

        val s = max(100f, min(w, h)).toInt()


        rectModel.left = cpModel.x.toInt() - s
        rectModel.right = cpModel.x.toInt() + s
        rectModel.top = cpModel.y.toInt() - s
        rectModel.bottom = cpModel.y.toInt() + s

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> onTouchDown(event)
            ACTION_MOVE -> onMotion(event)
        }

        return mScaleGestureDetector!!.onTouchEvent(event)
                || super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val wModel = width / 2
        val hModel = wModel
        val x = (width - wModel) / 2
        val y = (height - hModel) / 2 - 200

        cpModel.x = x + wModel / 2f
        cpModel.y = y + hModel / 2f
        rectModel.set(x, y, x + wModel, y + hModel)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        fun drawBack() {
            // top, bottom, left, right
            val rects = arrayOf(
                Rect(0, 0, width, rectModel.top),
                Rect(0, rectModel.bottom, width, height),
                Rect(0, rectModel.top, rectModel.left, rectModel.bottom),
                Rect(rectModel.right, rectModel.top, width, rectModel.bottom)
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

            canvas.drawRect(rectModel, paint)
        }

        fun drawKnob() {
            val paint = Paint()
            paint.color = Color.WHITE

            for (p in getFourCorners(rectModel)) {
                canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), px(4), paint)
            }
        }

        fun drawSizeLabel() {
            val paint = Paint()
            paint.color = Color.BLACK

            val paintText = Paint()
            paintText.textSize = pxT(12f)
            paintText.color = Color.WHITE

            val text = "%d x %d".format(rectModel.width(), rectModel.height())

            val wText = paintText.measureText(text)

            val fontMetrics = paintText.fontMetrics
            val hText = fontMetrics.bottom - fontMetrics.top

            val w = wText + px(12) * 2f
            val h = hText * 1.8f
            val x = rectModel.centerX() - w / 2
            val y = rectModel.top - h - px(10)
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