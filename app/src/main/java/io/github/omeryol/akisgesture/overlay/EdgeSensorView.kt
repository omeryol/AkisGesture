package io.github.omeryol.akisgesture.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

@SuppressLint("ViewConstructor")
class EdgeSensorView(
    context: Context,
    val edge: Edge,
) : View(context) {

    interface OnEdgeTouchListener {
        fun onEdgeTouch(edge: Edge, event: MotionEvent): Boolean
    }

    var onEdgeTouchListener: OnEdgeTouchListener? = null

    private var highlightAlpha = 0f
    private var highlightColor = 0x803D5AFE.toInt()
    private var animator: ValueAnimator? = null

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val rectF = RectF()

    init {
        setBackgroundColor(0x00000000)
    }

    fun triggerHighlight(
        durationMs: Long = 2500L,
        colorArgb: Int = 0x803D5AFE.toInt(),
    ) {
        highlightColor = colorArgb
        animator?.cancel()
        animator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                highlightAlpha = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (highlightAlpha <= 0.01f) return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val baseAlpha = (highlightColor ushr 24) and 0xFF
        val currentAlpha = (baseAlpha * highlightAlpha).toInt().coerceIn(0, 255)
        val rgb = highlightColor and 0x00FFFFFF

        fillPaint.color = (currentAlpha shl 24) or rgb
        strokePaint.color = ((currentAlpha * 0.9f).toInt() shl 24) or 0x00FFFFFF

        val cornerRadius = when (edge) {
            Edge.BOTTOM -> (h / 2f).coerceAtMost(16f)
            else -> (w / 2f).coerceAtMost(16f)
        }

        rectF.set(0f, 0f, w, h)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, fillPaint)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, strokePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onEdgeTouchListener?.onEdgeTouch(edge, event) ?: false
    }
}
