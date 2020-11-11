package com.example.umbrella

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View

open class CanvasBase(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)

    val bitmapMatrix = Matrix()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val scale = h.toFloat() / bitmap.height
        bitmapMatrix.setScale(scale, scale)
        bitmapMatrix.postTranslate((w - bitmap.width * scale) / 2f, 0f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        canvas.drawBitmap(bitmap, bitmapMatrix, null)
    }
}