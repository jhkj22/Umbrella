package com.example.umbrella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AlignmentOriginCanvas(context: Context, attrs: AttributeSet) : CanvasBase(context, attrs) {
    private val pin: Bitmap = ResourceUtil.getBitmap(context, R.drawable.ic_model_center_pin)

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null

    private val mScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            private var touchPoint = PointF(0f, 0f)

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                touchPoint.x = detector.focusX
                touchPoint.y = detector.focusY
                return super.onScaleBegin(detector)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                bitmapMatrix.postScale(scale, scale, touchPoint.x, touchPoint.y)
                invalidate()
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
                bitmapMatrix.postTranslate(-distanceX, -distanceY)
                invalidate()
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    init {
        mScaleGestureDetector = ScaleGestureDetector(context, mScaleGestureListener)
        mGestureDetector = GestureDetector(context, mSimpleOnGestureListener)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
                || mScaleGestureDetector!!.onTouchEvent(event)
                || super.onTouchEvent(event)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        fun drawPin() {
            val x = (width - pin.width) / 2f
            val y = height / 2f - pin.height - 50

            canvas.drawBitmap(pin, x, y, null)
        }
        drawPin()
    }
}