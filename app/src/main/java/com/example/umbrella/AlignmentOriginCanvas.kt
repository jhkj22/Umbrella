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
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.minus

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AlignmentOriginCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val iconPin: Bitmap = ResourceUtil.getBitmap(context, R.drawable.ic_model_center_pin)
    private val posPin = PointF()

    private val alignment = TwoPointAlignment

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
                updateCp()

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
                updateCp()

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

    init {
        alignment.initCp()

        icMap.sizeImage.right = alignment.image.width
        icMap.sizeImage.bottom = alignment.image.height

        icMap.cpCanvas = alignment.model1.cp

        mScaleGestureDetector = ScaleGestureDetector(context, mScaleGestureListener)
        mGestureDetector = GestureDetector(context, mSimpleOnGestureListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        icMap.sizeCanvas.right = w
        icMap.sizeCanvas.bottom = h

        icMap.scale = icMap.sizeCanvas.height().toFloat() / icMap.sizeImage.height()

        posPin.set(w * .5f, h * .4f)

        val dpI = alignment.model1.cp - icMap.mapC2I(posPin)
        icMap.moveI(dpI)
    }

    fun updateCp() {
        alignment.model1.cp.set(icMap.mapC2I(posPin))
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fun drawBitmap() {
            canvas.drawBitmap(alignment.image, null, icMap.getRectImageC(), null)
        }

        fun drawPin() {
            val x = posPin.x - iconPin.width / 2f
            val y = posPin.y - iconPin.height

            canvas.drawBitmap(iconPin, x, y, null)
        }

        drawBitmap()
        drawPin()
    }
}