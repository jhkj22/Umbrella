package com.example.umbrella

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.minus
import com.example.umbrella.core.Inspection

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AlignmentCanvasPageModelOrigin(
    override val view: View, override val icMap: ImageCanvasMap
) : CanvasPage {

    private val alignment = Inspection.alignment

    private val iconPin: Bitmap =
        ResourceUtil.getBitmap(view.context, R.drawable.ic_model_center_pin)
    private val posPin = PointF()

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
                updateCp()

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
                updateCp()

                view.invalidate()
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    init {
        val image = Inspection.image
        alignment.initCp(Rect(0, 0, image.width, image.height))
        updatePin()

        mScaleGestureDetector = ScaleGestureDetector(view.context, mScaleGestureListener)
        mGestureDetector = GestureDetector(view.context, mSimpleOnGestureListener)
    }

    override fun onSizeChanged(w: Int, h: Int) {
        updatePin()
    }

    private fun updatePin() {
        posPin.set(
            icMap.sizeCanvas.width() * .5f,
            icMap.sizeCanvas.height() * .4f
        )

        val dpI = alignment.model1.cp - icMap.mapC2I(posPin)
        icMap.moveI(dpI)
    }

    fun updateCp() {
        alignment.model1.cp.set(icMap.mapC2I(posPin))
    }

    override fun onDraw(canvas: Canvas) {
        fun drawPin() {
            val x = posPin.x - iconPin.width / 2f
            val y = posPin.y - iconPin.height

            canvas.drawBitmap(iconPin, x, y, null)
        }

        drawPin()
    }
}