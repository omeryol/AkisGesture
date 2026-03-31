package com.openswipe.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager.LayoutParams

object OverlayWindowFactory {

    private const val BASE_FLAGS =
        LayoutParams.FLAG_NOT_FOCUSABLE or
        LayoutParams.FLAG_LAYOUT_IN_SCREEN

    /**
     * 创建边缘触摸传感器窗口（Sensor 层）
     */
    fun createEdgeSensor(
        context: Context,
        edge: Edge,
        widthPx: Int,
        heightPx: Int,
        offsetPx: Int = 0,
        onTouchListener: EdgeSensorView.OnEdgeTouchListener? = null,
    ): OverlayWindow {
        val params = LayoutParams(
            widthPx, heightPx,
            LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            BASE_FLAGS or
                LayoutParams.FLAG_NOT_TOUCH_MODAL or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            when (edge) {
                Edge.LEFT -> {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = offsetPx
                }
                Edge.RIGHT -> {
                    gravity = Gravity.TOP or Gravity.END
                    x = 0
                    y = offsetPx
                }
                Edge.BOTTOM -> {
                    gravity = Gravity.BOTTOM or Gravity.START
                    x = offsetPx
                    y = 0
                }
            }
        }
        val view = EdgeSensorView(context, edge).apply {
            this.onEdgeTouchListener = onTouchListener
        }
        return OverlayWindow(view, params)
    }
}
