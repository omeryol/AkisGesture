package com.omer.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.omer.akisgesture.overlay.Edge
import kotlin.math.cos
import kotlin.math.sin

/**
 * Natural Elements Visual Feedback Engine.
 * Takes direct creative inspiration from core forces of nature:
 * Water (Su), Fire (Ateş), Steam (Buhar), Cloud (Bulut), Lightning (Şimşek), Wind (Rüzgar), Rain (Yağmur), Sun (Güneş).
 */
class BezierStretchRenderer {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val path = Path()
    private val secondaryPath = Path()
    private val arrowPath = Path()
    private val rectF = RectF()

    var halfSpan: Float = 190f
    var armed: Boolean = false
    var holdArmed: Boolean = false
    var isLUp: Boolean = false
    var isLDown: Boolean = false
    var bendStartY: Float = 0f

    var primaryColor: Int = Color.rgb(61, 90, 254)
    var secondaryColor: Int = Color.rgb(255, 145, 0)
    var lSwipeColor: Int = Color.rgb(0, 230, 118)
    var baseColor: Int = Color.rgb(61, 90, 254)
    var opacity: Float = 0.65f
    var animation: FeedbackAnimation = FeedbackAnimation.WATER
    var quickIcon: FeedbackIcon = FeedbackIcon.CHEVRON
    var holdIcon: FeedbackIcon = FeedbackIcon.STAR
    var actionSymbol: String = ""
    var animSpeed: Float = 1f
    var animSize: Float = 1f
    var showIndicatorBar: Boolean = false

    fun draw(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPosition: Float,
        peak: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        arrowAlpha: Float = 1f,
    ) {
        // Lock touch position in place at bendStartY when L-swipe is active
        val effectiveTouchPos = if ((isLUp || isLDown) && bendStartY > 0f) {
            bendStartY
        } else {
            touchPosition
        }

        if (showIndicatorBar) {
            drawIndicatorBar(canvas, edge, effectiveTouchPos, canvasWidth, canvasHeight)
        }

        if (stretch < 0.5f || animation == FeedbackAnimation.NONE) return

        // Color Space Selection: L-swipe > Secondary (Hold) > Primary (Quick Swipe)
        baseColor = when {
            isLUp || isLDown -> lSwipeColor
            holdArmed -> secondaryColor
            else -> primaryColor
        }

        val progress = (stretch / peak.coerceAtLeast(1f)).coerceIn(0f, 1.4f)
        val stateBoost = when {
            isLUp || isLDown -> 1.40f
            holdArmed -> 1.28f
            armed -> 1.15f
            else -> 1.0f
        }

        // Draw active animation shape based on Natural Elements & Legacy Styles
        when (animation) {
            FeedbackAnimation.WATER, FeedbackAnimation.FLUID -> drawWaterElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.FIRE -> drawFireElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.STEAM -> drawSteamElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.CLOUD -> drawCloudElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.LIGHTNING -> drawLightningElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.WIND -> drawWindElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.RAIN -> drawRainElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.SUN -> drawSunElement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)

            FeedbackAnimation.NEON_PULSE -> drawQuantumNeonAura(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.CYBER_HEX -> drawCyberPrism(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ORB_GLOW -> drawStellarFlareOrb(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.TEARDROP -> drawHydrodynamicMercuryDrop(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.BUBBLE -> drawBubbleDisplacement(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MINIMAL_PADDLE -> drawGlassmorphicPill(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }

        // Draw icon & action symbol with tight shape interaction
        drawGestureIcon(
            canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, arrowAlpha, progress
        )
    }

    private fun center(edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float): Pair<Float, Float> {
        val maxOffset = 58f * animSize
        val inset = (stretch * 0.48f).coerceAtMost(maxOffset).coerceAtLeast(12f)
        return when (edge) {
            Edge.LEFT -> inset to touchPos
            Edge.RIGHT -> (w - inset) to touchPos
            Edge.BOTTOM -> touchPos to (h - inset)
        }
    }

    // ── 🌊 1. WATER (Su - Akıcı Nehir Dalgası & Sıvı Fiziği) ──
    private fun drawWaterElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val span = halfSpan * (0.8f + (stretch / 300f).coerceIn(0f, 0.3f)) * animSize
        val peakVal = stretch * 0.95f

        path.reset()
        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, touchPos - span)
                path.cubicTo(peakVal * 0.85f, touchPos - span * 0.38f, peakVal * 0.85f, touchPos + span * 0.38f, 0f, touchPos + span)
            }
            Edge.RIGHT -> {
                path.moveTo(w, touchPos - span)
                path.cubicTo(w - peakVal * 0.85f, touchPos - span * 0.38f, w - peakVal * 0.85f, touchPos + span * 0.38f, w, touchPos + span)
            }
            Edge.BOTTOM -> {
                path.moveTo(touchPos - span, h)
                path.cubicTo(touchPos - span * 0.38f, h - peakVal * 0.85f, touchPos + span * 0.38f, h - peakVal * 0.85f, touchPos + span, h)
            }
        }
        path.close()

        val alphaVal = ((75 + progress * 85) * opacity * stateBoost).toInt().coerceIn(0, 245)
        val shader = when (edge) {
            Edge.LEFT -> LinearGradient(0f, cy, peakVal, cy, intColorWithAlpha(baseColor, alphaVal), intColorWithAlpha(baseColor, alphaVal / 3), Shader.TileMode.CLAMP)
            Edge.RIGHT -> LinearGradient(w, cy, w - peakVal, cy, intColorWithAlpha(baseColor, alphaVal), intColorWithAlpha(baseColor, alphaVal / 3), Shader.TileMode.CLAMP)
            Edge.BOTTOM -> LinearGradient(cx, h, cx, h - peakVal, intColorWithAlpha(baseColor, alphaVal), intColorWithAlpha(baseColor, alphaVal / 3), Shader.TileMode.CLAMP)
        }
        bodyPaint.shader = shader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        // Water crest foam highlight
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.5f * animSize
        rimPaint.alpha = ((85 + progress * 110) * opacity * stateBoost).toInt().coerceIn(0, 240)
        canvas.drawPath(path, rimPaint)

        // Water micro splash droplets
        if (progress > 0.35f) {
            highlightPaint.color = intColorWithAlpha(baseColor, 200)
            val dropCount = 3
            for (i in 0 until dropCount) {
                val offset = (i - 1) * 16f * animSize
                val dropX = if (edge == Edge.LEFT) cx + 18f * animSize else if (edge == Edge.RIGHT) cx - 18f * animSize else cx + offset
                val dropY = if (edge == Edge.BOTTOM) cy - 18f * animSize else cy + offset
                canvas.drawCircle(dropX, dropY, (4f + i) * animSize, highlightPaint)
            }
        }
    }

    // ── 🔥 2. FIRE (Ateş - Alev Dalgaları & Uçuşan Kıvılcımlar) ──
    private fun drawFireElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val flameR = (24f + progress * 18f) * animSize
        val timeMs = System.currentTimeMillis()

        // Flickering Flame Tongue Path
        val flicker = 4f * sin(timeMs / 60.0).toFloat()
        path.reset()
        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + flameR + flicker
                path.moveTo(0f, cy - flameR * 1.5f)
                path.cubicTo(tipX * 0.6f, cy - flameR * 1.2f, tipX, cy - flameR * 0.4f, tipX, cy)
                path.cubicTo(tipX, cy + flameR * 0.4f, tipX * 0.6f, cy + flameR * 1.2f, 0f, cy + flameR * 1.5f)
            }
            Edge.RIGHT -> {
                val tipX = cx - flameR - flicker
                path.moveTo(w, cy - flameR * 1.5f)
                path.cubicTo(w - (w - tipX) * 0.6f, cy - flameR * 1.2f, tipX, cy - flameR * 0.4f, tipX, cy)
                path.cubicTo(tipX, cy + flameR * 0.4f, w - (w - tipX) * 0.6f, cy + flameR * 1.2f, w, cy + flameR * 1.5f)
            }
            Edge.BOTTOM -> {
                val tipY = cy - flameR - flicker
                path.moveTo(cx - flameR * 1.5f, h)
                path.cubicTo(cx - flameR * 1.2f, h - (h - tipY) * 0.6f, cx - flameR * 0.4f, tipY, cx, tipY)
                path.cubicTo(cx + flameR * 0.4f, tipY, cx + flameR * 1.2f, h - (h - tipY) * 0.6f, cx + flameR * 1.5f, h)
            }
        }
        path.close()

        // Intense radial flame heat shader (Amber-Red-Yellow)
        val flameColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 87, 34)
        val alphaVal = (225 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val radialShader = RadialGradient(
            cx, cy, flameR * 2.2f,
            intColorWithAlpha(flameColor, alphaVal),
            intColorWithAlpha(Color.rgb(255, 193, 7), (alphaVal * 0.3f).toInt()),
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = radialShader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        // Floating embers / sparks particles
        highlightPaint.color = Color.rgb(255, 235, 59)
        highlightPaint.alpha = (230 * opacity).toInt().coerceIn(0, 255)
        for (i in 0 until 4) {
            val sparkAngle = timeMs / 100.0 + i * 1.5
            val sx = cx + (flameR * 0.7f * cos(sparkAngle)).toFloat()
            val sy = cy - (flameR * 0.5f + (i * 6f))
            canvas.drawCircle(sx, sy, 3f * animSize, highlightPaint)
        }
    }

    // ── 💨 3. STEAM (Buhar - Sıcak Sis & Hacimsel Yoğuşma) ──
    private fun drawSteamElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (32f + progress * 24f) * animSize

        // 3 overlapping soft atmospheric mist puffs
        val alphaVal = (95 * opacity * stateBoost).toInt().coerceIn(0, 255)
        for (i in 0 until 3) {
            val offset = (i - 1) * 14f * animSize
            val puffX = if (edge == Edge.BOTTOM) cx + offset else cx
            val puffY = if (edge == Edge.BOTTOM) cy else cy + offset
            val puffR = r * (0.8f + i * 0.2f)

            val mistShader = RadialGradient(
                puffX, puffY, puffR,
                intColorWithAlpha(baseColor, alphaVal),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            auraPaint.shader = mistShader
            canvas.drawCircle(puffX, puffY, puffR, auraPaint)
        }
        auraPaint.shader = null

        // Soft steam highlight core
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (70 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.45f, highlightPaint)
    }

    // ── ☁️ 4. CLOUD (Bulut - Yumuşak Kümülüs Şekilleri) ──
    private fun drawCloudElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val cloudR = (22f + progress * 14f) * animSize

        // 3 Cumulus cloud lobes
        val alphaVal = (210 * opacity * stateBoost).toInt().coerceIn(0, 255)
        bodyPaint.color = baseColor
        bodyPaint.alpha = alphaVal

        // Main center lobe
        canvas.drawCircle(cx, cy, cloudR * 1.1f, bodyPaint)
        // Left/Top lobe
        canvas.drawCircle(cx - cloudR * 0.7f, cy - cloudR * 0.3f, cloudR * 0.85f, bodyPaint)
        // Right/Bottom lobe
        canvas.drawCircle(cx + cloudR * 0.7f, cy - cloudR * 0.3f, cloudR * 0.85f, bodyPaint)

        // Top cloud highlight rim
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.4f * animSize
        rimPaint.alpha = (200 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy - cloudR * 0.2f, cloudR * 0.9f, rimPaint)
    }

    // ── ⚡ 5. LIGHTNING (Şimşek - Elektrik Arki & Lazer Çatallanması) ──
    private fun drawLightningElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        // Flash electric aura
        val lightningColor = if (isLUp || isLDown) lSwipeColor else Color.rgb(0, 229, 255)
        val alphaVal = (210 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val radialShader = RadialGradient(
            cx, cy, 46f * animSize,
            intColorWithAlpha(lightningColor, alphaVal),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, 46f * animSize, auraPaint)
        auraPaint.shader = null

        // Jagged Lightning Bolt Path
        path.reset()
        val jitter = ((timeMs / 40) % 7) * 2f - 6f
        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, cy)
                path.lineTo(cx * 0.4f, cy - 14f + jitter)
                path.lineTo(cx * 0.7f, cy + 12f - jitter)
                path.lineTo(cx + 12f, cy)
            }
            Edge.RIGHT -> {
                path.moveTo(w, cy)
                path.lineTo(w - (w - cx) * 0.4f, cy - 14f + jitter)
                path.lineTo(w - (w - cx) * 0.7f, cy + 12f - jitter)
                path.lineTo(cx - 12f, cy)
            }
            Edge.BOTTOM -> {
                path.moveTo(cx, h)
                path.lineTo(cx - 14f + jitter, h - (h - cy) * 0.4f)
                path.lineTo(cx + 12f - jitter, h - (h - cy) * 0.7f)
                path.lineTo(cx, cy - 12f)
            }
        }

        // Electric bolt stroke
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = (4f + progress * 2f) * animSize
        rimPaint.alpha = (245 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, rimPaint)
    }

    // ── 🍃 6. WIND (Rüzgar - Aerodinamik Hava Akımı) ──
    private fun drawWindElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val windLen = (36f + progress * 24f) * animSize

        rimPaint.color = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(178, 235, 242)
        rimPaint.strokeWidth = 3f * animSize
        val alphaVal = (200 * opacity * stateBoost).toInt().coerceIn(0, 255)
        rimPaint.alpha = alphaVal

        // 3 Aerodynamic wind streamlines
        for (i in -1..1) {
            val offset = i * 16f * animSize
            path.reset()
            when (edge) {
                Edge.LEFT -> {
                    path.moveTo(0f, cy + offset * 1.4f)
                    path.cubicTo(cx * 0.6f, cy + offset, cx + windLen, cy + offset * 0.5f, cx + windLen * 1.2f, cy + offset)
                }
                Edge.RIGHT -> {
                    path.moveTo(w, cy + offset * 1.4f)
                    path.cubicTo(w - (w - cx) * 0.6f, cy + offset, cx - windLen, cy + offset * 0.5f, cx - windLen * 1.2f, cy + offset)
                }
                Edge.BOTTOM -> {
                    path.moveTo(cx + offset * 1.4f, h)
                    path.cubicTo(cx + offset, h - (h - cy) * 0.6f, cx + offset * 0.5f, cy - windLen, cx + offset, cy - windLen * 1.2f)
                }
            }
            canvas.drawPath(path, rimPaint)
        }
    }

    // ── 🌧️ 7. RAIN (Yağmur - Su Serpintisi & Halka Dalgaları) ──
    private fun drawRainElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (24f + progress * 18f) * animSize

        // Raindrop splash ripple ring 1 & 2
        rimPaint.color = baseColor
        rimPaint.strokeWidth = 2.2f * animSize
        rimPaint.alpha = ((210 - progress * 80) * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r, rimPaint)
        canvas.drawCircle(cx, cy, r * 1.45f, rimPaint)

        // Rain splash micro-droplets radiating outwards
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (220 * opacity).toInt().coerceIn(0, 255)
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val rx = cx + (r * 1.6f * cos(angle)).toFloat()
            val ry = cy + (r * 1.6f * sin(angle)).toFloat()
            canvas.drawCircle(rx, ry, 2.5f * animSize, highlightPaint)
        }
    }

    // ── ☀️ 8. SUN (Güneş - Işıltılı Korona & Güneş Işınları) ──
    private fun drawSunElement(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (22f + progress * 15f) * animSize
        val timeMs = System.currentTimeMillis()

        // Radiant Solar Corona Aura
        val sunColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 179, 0)
        val alphaVal = (230 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val radialShader = RadialGradient(
            cx, cy, r * 1.8f,
            intColorWithAlpha(sunColor, alphaVal),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, r * 1.8f, auraPaint)
        auraPaint.shader = null

        // 8 Rotating Solar Rays
        rimPaint.color = intColorWithAlpha(sunColor, (alphaVal * 0.8f).toInt())
        rimPaint.strokeWidth = 2.5f * animSize
        val rotAngle = (timeMs / 50.0) % 360.0
        for (i in 0 until 8) {
            val angle = Math.toRadians(rotAngle + i * 45.0)
            val rx1 = cx + (r * 1.1f * cos(angle)).toFloat()
            val ry1 = cy + (r * 1.1f * sin(angle)).toFloat()
            val rx2 = cx + (r * 1.6f * cos(angle)).toFloat()
            val ry2 = cy + (r * 1.6f * sin(angle)).toFloat()
            canvas.drawLine(rx1, ry1, rx2, ry2, rimPaint)
        }

        // Intense Sun Core Lens Flare
        bodyPaint.color = Color.WHITE
        bodyPaint.alpha = (240 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.75f, bodyPaint)
    }

    // ── LEGACY & EXTRA ANIMATIONS ──
    private fun drawQuantumNeonAura(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawLightningElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    private fun drawCyberPrism(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawSunElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    private fun drawStellarFlareOrb(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawSunElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    private fun drawHydrodynamicMercuryDrop(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawWaterElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    private fun drawBubbleDisplacement(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawRainElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    private fun drawGlassmorphicPill(canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float, w: Float, h: Float, progress: Float, stateBoost: Float) {
        drawWindElement(canvas, edge, stretch, touchPos, w, h, progress, stateBoost)
    }

    // ── GLASS ICON INTERACTION ──
    private fun drawGestureIcon(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, alpha: Float, progress: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        val scale = when {
            isLUp || isLDown -> 1.34f + 0.04f * sin(timeMs / 80.0).toFloat()
            holdArmed -> 1.26f + 0.04f * sin(timeMs / 90.0).toFloat()
            armed -> 1.15f
            else -> 0.85f + (progress * 0.22f).coerceAtMost(0.22f)
        }

        canvas.save()
        canvas.scale(scale * animSize, scale * animSize, cx, cy)

        if (armed || holdArmed || isLUp || isLDown) {
            auraPaint.color = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            auraPaint.alpha = ((if (isLUp || isLDown) 210 else if (holdArmed) 190 else 135) * opacity * alpha).toInt().coerceIn(0, 255)
            val auraRadius = if (isLUp || isLDown || holdArmed) 34f else 28f
            canvas.drawCircle(cx, cy, auraRadius, auraPaint)

            rimPaint.color = Color.WHITE
            rimPaint.strokeWidth = 3f
            rimPaint.alpha = (235 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, rimPaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(7f, 0f, 1.8f, Color.argb(150, 0, 0, 0))

        val symbolStr = when {
            actionSymbol.isNotEmpty() -> actionSymbol
            isLUp -> "▲"
            isLDown -> "▼"
            else -> {
                val selectedIcon = if (holdArmed) holdIcon else quickIcon
                if (selectedIcon != FeedbackIcon.NONE && selectedIcon != FeedbackIcon.CHEVRON) selectedIcon.symbol else ""
            }
        }

        if (symbolStr.isNotEmpty()) {
            val popSize = if (isLUp || isLDown || holdArmed) 44f else if (armed) 40f else 34f
            iconPaint.textSize = popSize
            val baseline = cy - (iconPaint.ascent() + iconPaint.descent()) / 2f
            canvas.drawText(symbolStr, cx, baseline, iconPaint)
        } else {
            arrowPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
            arrowPaint.strokeWidth = if (armed) 6.5f else 5f
            drawChevron(canvas, cx, cy, 24f, edge)
        }

        canvas.restore()
    }

    private fun drawChevron(canvas: Canvas, cx: Float, cy: Float, size: Float, edge: Edge) {
        arrowPath.reset()
        val half = size / 2f
        when (edge) {
            Edge.LEFT -> {
                arrowPath.moveTo(cx - half * 0.35f, cy - half)
                arrowPath.lineTo(cx + half * 0.45f, cy)
                arrowPath.lineTo(cx - half * 0.35f, cy + half)
            }
            Edge.RIGHT -> {
                arrowPath.moveTo(cx + half * 0.35f, cy - half)
                arrowPath.lineTo(cx - half * 0.45f, cy)
                arrowPath.lineTo(cx + half * 0.35f, cy + half)
            }
            Edge.BOTTOM -> {
                arrowPath.moveTo(cx - half, cy + half * 0.35f)
                arrowPath.lineTo(cx, cy - half * 0.45f)
                arrowPath.lineTo(cx + half, cy + half * 0.35f)
            }
        }
        canvas.drawPath(arrowPath, arrowPaint)
    }

    private fun drawIndicatorBar(canvas: Canvas, edge: Edge, touchPos: Float, w: Float, h: Float) {
        auraPaint.color = baseColor
        auraPaint.alpha = (130 * opacity).toInt().coerceIn(0, 255)
        when (edge) {
            Edge.LEFT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(4f, cy - 42f, 10f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
            Edge.RIGHT -> {
                val cy = if (touchPos > 0) touchPos else h / 2f
                rectF.set(w - 10f, cy - 42f, w - 4f, cy + 42f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
            Edge.BOTTOM -> {
                val cx = if (touchPos > 0) touchPos else w / 2f
                rectF.set(cx - 50f, h - 10f, cx + 50f, h - 4f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
        }
    }

    private fun intColorWithAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
    }
}
