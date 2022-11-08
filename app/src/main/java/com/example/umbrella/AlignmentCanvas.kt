package com.example.umbrella

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.umbrella.core.Inspection

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AlignmentCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val icMap = ImageCanvasMap()

    private var pageIndex = 0
    private var page: CanvasPage? = createPage(pageIndex)

    private fun createPage(index: Int): CanvasPage? {
        return when (index) {
            0 -> AlignmentCanvasPageModelOrigin(this, icMap)
            1 -> AlignmentCanvasPageModelSize(this, icMap)
            2 -> AlignmentCanvasPageSearchArea(this, icMap)
            3 -> AlignmentCanvasPageTestRun(this, icMap)
            else -> null
        }
    }

    fun toNext(): Boolean {
        if (pageIndex == 3) return false

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
            0 -> "モデル中心を決めてください"
            1 -> "モデルサイズを決めてください"
            2 -> "探索領域を決めてください"
            3 -> "テスト実行"
            else -> ""
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event) || (page?.onTouchEvent(event) ?: true)
    }

    private val alignment = Inspection.alignment

    init {
        icMap.sizeImage.right = Inspection.image.width
        icMap.sizeImage.bottom = Inspection.image.height
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        icMap.sizeCanvas.right = w
        icMap.sizeCanvas.bottom = h

        icMap.scale = icMap.sizeCanvas.height().toFloat() / icMap.sizeImage.height()

        page?.onSizeChanged(w, h)
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