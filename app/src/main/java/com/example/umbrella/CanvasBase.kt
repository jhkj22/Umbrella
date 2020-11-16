package com.example.umbrella

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.View

open class CanvasBase(context: Context, attrs: AttributeSet) : View(context, attrs) {
    protected val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
}