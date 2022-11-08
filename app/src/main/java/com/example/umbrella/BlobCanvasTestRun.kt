package com.example.umbrella

import android.annotation.SuppressLint
import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.*
import com.example.umbrella.core.Inspection
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

class BlobCanvasTestRun(
    override val view: View, override val icMap: ImageCanvasMap
) : CanvasPage {

    private val blob = Inspection.blob

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

    private val contours: MutableList<MatOfPoint> = mutableListOf()

    init {
        val mat = Mat()
        Utils.bitmapToMat(Inspection.image, mat)

        val area = blob.area
        val rect = Rect(area.left, area.top, area.width(), area.height())
        val subImage = mat.submat(rect)

        Imgproc.cvtColor(subImage, subImage, Imgproc.COLOR_RGB2GRAY)

        val matBin = Mat()
        Imgproc.threshold(subImage, matBin, 128.0, 255.0, Imgproc.THRESH_BINARY_INV)

        val hierarchy = Mat()
        Imgproc.findContours(
            matBin,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_TC89_KCOS
        )

        mScaleGestureDetector = ScaleGestureDetector(view.context, mScaleGestureListener)
        mGestureDetector = GestureDetector(view.context, mSimpleOnGestureListener)
    }

    private val g = Graphics(view.context)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rectC = icMap.mapI2C(blob.area.toRectF())

        fun drawContours() {
            val paths = mutableListOf<Path>()

            for (cont in contours) {
                val path = Path()
                val array = cont.toArray().map {
                    icMap.mapI2C(
                        PointF(
                            it.x.toFloat() + blob.area.left,
                            it.y.toFloat() + blob.area.top
                        )
                    )
                }

                path.moveTo(array[0].x, array[0].y)
                for (i in 1 until array.size) {
                    path.lineTo(array[i].x, array[i].y)
                }
                path.close()

                val color = if (array.size > 10) Color.RED else Color.GREEN

                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(60, color.red, color.green, color.blue)
                canvas.drawPath(path, paint)

                paint.strokeWidth = g.px(1f)
                paint.style = Paint.Style.STROKE
                paint.color = Color.argb(178, color.red, color.green, color.blue)
                canvas.drawPath(path, paint)
            }
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

        drawContours()
        drawBack()
        drawFrame()
    }
}