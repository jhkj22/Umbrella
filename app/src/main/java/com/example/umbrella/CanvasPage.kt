package com.example.umbrella

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View

interface CanvasPage {
    val view: View
    val icMap: ImageCanvasMap

    fun onDraw(canvas: Canvas)
    fun onSizeChanged(w: Int, h: Int) {}
    fun onTouchEvent(event: MotionEvent): Boolean
}