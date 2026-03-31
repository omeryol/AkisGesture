package com.openswipe.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View

@SuppressLint("ViewConstructor")
class EdgeSensorView(
    context: Context,
    val edge: Edge,
) : View(context) {

    interface OnEdgeTouchListener {
        fun onEdgeTouch(edge: Edge, event: MotionEvent): Boolean
    }

    var onEdgeTouchListener: OnEdgeTouchListener? = null

    init {
        // 透明窗口，不绘制任何内容
        setBackgroundColor(0x00000000)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onEdgeTouchListener?.onEdgeTouch(edge, event) ?: false
    }
}
