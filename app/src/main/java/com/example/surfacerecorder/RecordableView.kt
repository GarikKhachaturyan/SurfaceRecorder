package com.example.surfacerecorder

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver

class RecordableView : View {

    private var path = Path()
    private val paint = Paint()
    private lateinit var drawingBitmap: Bitmap
    private lateinit var drawingCanvas: Canvas

    private var drawPath = false

    var drawListener: DrawListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        paint.color = Color.BLACK
        paint.strokeWidth = 10f
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                drawingCanvas = Canvas(drawingBitmap)
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                path.lineTo(event.x, event.y)
                drawPath()
            }
        }

        return true
    }

    private fun drawPath() {
        drawPath = true
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawListener?.onDrawFinished()
    }

    public override fun onDraw(canvas: Canvas) {
        if (drawPath) {
            drawingCanvas.drawPath(path, paint)
            path.reset()
            drawPath = false
        }

        canvas.drawBitmap(drawingBitmap, 0f, 0f, null)

        canvas.drawPath(path, paint)
    }

    interface DrawListener {
        fun onDrawFinished()
    }
}