package com.example.umbrella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.umbrella.core.Inspection

class BlobCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val icMap = ImageCanvasMap()

    private var pageIndex = 0
    private var page: CanvasPage? = createPage(pageIndex)

    private fun createPage(index: Int): CanvasPage? {
        return when (index) {
            0 -> BlobCanvasPageArea(this, icMap)
            1 -> BlobCanvasTestRun(this, icMap)
            else -> null
        }
    }

    fun toNext(): Boolean {
        if (pageIndex == 1) return false

        page = createPage(++pageIndex)
        invalidate()
        return true
    }

    fun toPrev(): Boolean {
        if (pageIndex == 0) return false

        page = createPage(--pageIndex)
        invalidate()
        return true
    }

    fun getTitle(): String {
        return when (pageIndex) {
            0 -> "検査領域を決めてください"
            1 -> "テスト実行"
            else -> ""
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event) || (page?.onTouchEvent(event) ?: true)
    }

    init {
        val image = Inspection.image
        icMap.sizeImage.right = image.width
        icMap.sizeImage.bottom = image.height
        icMap.cpCanvas.set(image.width / 2f, image.height / 2f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        icMap.sizeCanvas.right = w
        icMap.sizeCanvas.bottom = h

        icMap.scale = icMap.sizeCanvas.width().toFloat() / icMap.sizeImage.width()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fun drawBitmap() {
            canvas.drawBitmap(Inspection.image, null, icMap.getRectImageC(), null)
        }

        fun drawUI() {
            page?.onDraw(canvas)
        }

        drawBitmap()
        drawUI()
    }
}