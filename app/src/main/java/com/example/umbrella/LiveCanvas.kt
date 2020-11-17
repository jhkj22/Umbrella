package com.example.umbrella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class LiveCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val icMap = ImageCanvasMap()

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
                icMap.zoom(detector.scaleFactor)
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
                icMap.moveC(PointF(distanceX, distanceY))
                invalidate()
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
                || mScaleGestureDetector!!.onTouchEvent(event)
                || super.onTouchEvent(event)
    }

    private var canvasIsInitialized = false
    private val liveThread = LiveThread

    init {
        liveThread.onImageUpdated = {
            val bitmap = liveThread.bitmap
            if (bitmap != null && !canvasIsInitialized) {
                icMap.sizeImage.right = bitmap.width
                icMap.sizeImage.bottom = bitmap.height

                icMap.cpCanvas = PointF(bitmap.width / 2f, bitmap.height / 2f)
                icMap.scale = 1f * icMap.sizeCanvas.height() / icMap.sizeImage.height()

                canvasIsInitialized = true
            }

            invalidate()
        }

        mScaleGestureDetector = ScaleGestureDetector(context, mScaleGestureListener)
        mGestureDetector = GestureDetector(context, mSimpleOnGestureListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        icMap.sizeCanvas.right = w
        icMap.sizeCanvas.bottom = h
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fun drawBitmap() {
            val bitmap = liveThread.bitmap ?: return
            canvas.drawBitmap(bitmap, null, icMap.getRectImageC(), null)
        }

        drawBitmap()
    }
}