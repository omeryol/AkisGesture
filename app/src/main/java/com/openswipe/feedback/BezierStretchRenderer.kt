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
import kotlin.random.Random

/**
 * Masterclass 3D Spatial Optics Engine with Circular Emoji Integration.
 */
class BezierStretchRenderer {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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
    var animation: FeedbackAnimation = FeedbackAnimation.OCEAN_WAVE
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

        // Execute 3D Spatial Optics Nature Simulation Engines
        when (animation) {
            FeedbackAnimation.OCEAN_WAVE -> {
                drawRefractiveOceanWave3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.MERCURY_TEARDROP -> {
                drawDetachingPinchTeardrop3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.PLASMA_FIRE -> {
                drawPlasmaFireSimulation3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.ATMOSPHERIC_MIST -> {
                drawAtmosphericMistSimulation3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.ELECTRIC_STORM -> {
                drawMultiStageElectricStorm3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.SOLAR_CORONA -> {
                drawMasterSolarCorona3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
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

    // =========================================================================
    // 🌊 1. 3D OCEAN WAVE (Volumetric 3D Refractive Tide & Deep Z-Shadow)
    // =========================================================================
    private fun drawRefractiveOceanWave3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val span = halfSpan * (0.85f + (stretch / 280f).coerceIn(0f, 0.35f)) * animSize
        val peakVal = stretch * 0.96f
        val timeSec = System.currentTimeMillis() / 280.0

        path.reset()
        val segments = 14
        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val waveOffset = sin(timeSec * 2.5 + i * 0.5).toFloat() * (7f * progress)
            val yAlong = touchPos - span + t * (span * 2f)
            val envelope = sin(t * Math.PI).toFloat()
            val xDepth = (peakVal + waveOffset) * envelope

            val (px, py) = when (edge) {
                Edge.LEFT -> Pair(xDepth, yAlong)
                Edge.RIGHT -> Pair(w - xDepth, yAlong)
                Edge.BOTTOM -> Pair(yAlong, h - xDepth)
            }
            if (i == 0) path.moveTo(if (edge == Edge.RIGHT) w else 0f, touchPos - span)
            path.lineTo(px, py)
        }
        if (edge == Edge.LEFT) path.lineTo(0f, touchPos + span)
        else if (edge == Edge.RIGHT) path.lineTo(w, touchPos + span)
        else if (edge == Edge.BOTTOM) path.lineTo(touchPos + span, h)
        path.close()

        val shadowAlpha = (90 * opacity).toInt().coerceIn(0, 255)
        shadowPaint.color = Color.BLACK
        shadowPaint.alpha = shadowAlpha
        canvas.save()
        val shadowOffsetX = 10f * animSize
        val shadowOffsetY = 12f * animSize
        when (edge) {
            Edge.LEFT -> canvas.translate(shadowOffsetX, shadowOffsetY)
            Edge.RIGHT -> canvas.translate(-shadowOffsetX, shadowOffsetY)
            Edge.BOTTOM -> canvas.translate(0f, shadowOffsetY)
        }
        canvas.drawPath(path, shadowPaint)
        canvas.restore()

        val alphaVal = ((95 + progress * 95) * opacity * stateBoost).toInt().coerceIn(0, 245)
        val startColor = intColorWithAlpha(baseColor, alphaVal)
        val endColor = intColorWithAlpha(baseColor, alphaVal / 4)

        val shader = when (edge) {
            Edge.LEFT -> LinearGradient(0f, cy, peakVal, cy, startColor, endColor, Shader.TileMode.CLAMP)
            Edge.RIGHT -> LinearGradient(w, cy, w - peakVal, cy, startColor, endColor, Shader.TileMode.CLAMP)
            Edge.BOTTOM -> LinearGradient(cx, h, cx, h - peakVal, startColor, endColor, Shader.TileMode.CLAMP)
        }
        bodyPaint.shader = shader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        secondaryPath.reset()
        val innerPeak = peakVal * 0.65f
        val innerSpan = span * 0.75f
        when (edge) {
            Edge.LEFT -> {
                secondaryPath.moveTo(0f, touchPos - innerSpan)
                secondaryPath.cubicTo(innerPeak, touchPos - innerSpan * 0.4f, innerPeak, touchPos + innerSpan * 0.4f, 0f, touchPos + innerSpan)
            }
            Edge.RIGHT -> {
                secondaryPath.moveTo(w, touchPos - innerSpan)
                secondaryPath.cubicTo(w - innerPeak, touchPos - innerSpan * 0.4f, w - innerPeak, touchPos + innerSpan * 0.4f, w, touchPos + innerSpan)
            }
            Edge.BOTTOM -> {
                secondaryPath.moveTo(touchPos - innerSpan, h)
                secondaryPath.cubicTo(touchPos - innerSpan * 0.4f, h - innerPeak, touchPos + innerSpan * 0.4f, h - innerPeak, touchPos + innerSpan, h)
            }
        }
        secondaryPath.close()

        bodyPaint.color = Color.WHITE
        bodyPaint.alpha = (55 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(secondaryPath, bodyPaint)
    }

    // =========================================================================
    // 💧 2. 3D MERCURY TEARDROP (Pronounced 3D Spherical Optics & Ambient Shadow)
    // =========================================================================
    private fun drawDetachingPinchTeardrop3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val bulbR = (22f + progress * 16f) * animSize
        val baseSpan = (42f + progress * 24f) * animSize
        val alphaVal = (225 * opacity * stateBoost).toInt().coerceIn(0, 255)

        path.reset()
        val neckPinch = if (progress > 0.45f) (1f - (progress - 0.45f) * 0.8f).coerceIn(0.2f, 1f) else 1f

        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + bulbR * 0.85f
                path.moveTo(0f, cy - baseSpan * neckPinch)
                path.cubicTo(tipX * 0.4f, cy - baseSpan * 0.8f * neckPinch, tipX + bulbR * 0.5f, cy - bulbR * neckPinch, tipX + bulbR, cy)
                path.cubicTo(tipX + bulbR * 0.5f, cy + bulbR * neckPinch, tipX * 0.4f, cy + baseSpan * 0.8f * neckPinch, 0f, cy + baseSpan * neckPinch)
            }
            Edge.RIGHT -> {
                val tipX = cx - bulbR * 0.85f
                path.moveTo(w, cy - baseSpan * neckPinch)
                path.cubicTo(w - (w - tipX) * 0.4f, cy - baseSpan * 0.8f * neckPinch, tipX - bulbR * 0.5f, cy - bulbR * neckPinch, tipX - bulbR, cy)
                path.cubicTo(tipX - bulbR * 0.5f, cy + bulbR * neckPinch, w - (w - tipX) * 0.4f, cy + baseSpan * 0.8f * neckPinch, w, cy + baseSpan * neckPinch)
            }
            Edge.BOTTOM -> {
                val tipY = cy - bulbR * 0.85f
                path.moveTo(cx - baseSpan * neckPinch, h)
                path.cubicTo(cx - baseSpan * 0.8f * neckPinch, h - (h - tipY) * 0.4f, cx - bulbR * neckPinch, tipY - bulbR * 0.5f, cx, tipY - bulbR)
                path.cubicTo(cx + bulbR * neckPinch, tipY - bulbR * 0.5f, cx + baseSpan * 0.8f * neckPinch, h - (h - tipY) * 0.4f, cx + baseSpan * neckPinch, h)
            }
        }
        path.close()

        bodyPaint.color = baseColor
        bodyPaint.alpha = (alphaVal * 0.75f).toInt()
        canvas.drawPath(path, bodyPaint)

        if (progress > 0.4f) {
            val dropDist = (stretch * 0.55f).coerceAtLeast(30f * animSize)
            val dropR = (15f + progress * 14f) * animSize

            val dropX = when (edge) {
                Edge.LEFT -> dropDist
                Edge.RIGHT -> w - dropDist
                Edge.BOTTOM -> cx
            }
            val dropY = when (edge) {
                Edge.BOTTOM -> h - dropDist
                else -> cy
            }

            shadowPaint.color = Color.BLACK
            shadowPaint.alpha = (100 * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(dropX + 6f * animSize, dropY + 9f * animSize, dropR * 1.08f, shadowPaint)

            val radialShader = RadialGradient(
                dropX - dropR * 0.4f, dropY - dropR * 0.4f, dropR * 1.9f,
                intArrayOf(Color.WHITE, intColorWithAlpha(baseColor, alphaVal), intColorWithAlpha(Color.argb(230, 10, 10, 10), (alphaVal * 0.85f).toInt())),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            bodyPaint.shader = radialShader
            canvas.drawCircle(dropX, dropY, dropR, bodyPaint)
            bodyPaint.shader = null

            highlightPaint.color = Color.WHITE
            highlightPaint.alpha = (230 * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(dropX - dropR * 0.38f, dropY - dropR * 0.38f, dropR * 0.36f, highlightPaint)
        }
    }

    // =========================================================================
    // 🔥 3. 3D PLASMA FIRE (Volumetric Flame & 3D Perspective Embers)
    // =========================================================================
    private fun drawPlasmaFireSimulation3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val flameR = (26f + progress * 20f) * animSize
        val timeMs = System.currentTimeMillis()

        path.reset()
        val flicker1 = sin(timeMs / 45.0).toFloat() * 6f * animSize
        val flicker2 = cos(timeMs / 35.0).toFloat() * 6f * animSize

        when (edge) {
            Edge.LEFT -> {
                val tipX = cx + flameR + flicker1
                path.moveTo(0f, cy - flameR * 1.6f)
                path.cubicTo(tipX * 0.5f, cy - flameR * 1.3f + flicker2, tipX + flicker2, cy - flameR * 0.4f, tipX, cy)
                path.cubicTo(tipX + flicker2, cy + flameR * 0.4f, tipX * 0.5f, cy + flameR * 1.3f - flicker2, 0f, cy + flameR * 1.6f)
            }
            Edge.RIGHT -> {
                val tipX = cx - flameR - flicker1
                path.moveTo(w, cy - flameR * 1.6f)
                path.cubicTo(w - (w - tipX) * 0.5f, cy - flameR * 1.3f + flicker2, tipX - flicker2, cy - flameR * 0.4f, tipX, cy)
                path.cubicTo(tipX - flicker2, cy + flameR * 0.4f, w - (w - tipX) * 0.5f, cy + flameR * 1.3f - flicker2, w, cy + flameR * 1.6f)
            }
            Edge.BOTTOM -> {
                val tipY = cy - flameR - flicker1
                path.moveTo(cx - flameR * 1.6f, h)
                path.cubicTo(cx - flameR * 1.3f + flicker2, h - (h - tipY) * 0.5f, cx - flameR * 0.4f, tipY - flicker2, cx, tipY)
                path.cubicTo(cx + flameR * 0.4f, tipY - flicker2, cx + flameR * 1.3f - flicker2, h - (h - tipY) * 0.5f, cx + flameR * 1.6f, h)
            }
        }
        path.close()

        val flameColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 61, 0)
        val alphaVal = (235 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, flameR * 2.4f,
            intArrayOf(
                Color.rgb(255, 245, 157),
                intColorWithAlpha(flameColor, alphaVal),
                intColorWithAlpha(Color.rgb(183, 28, 28), (alphaVal * 0.2f).toInt())
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = radialShader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        val rand = Random(timeMs / 120)
        sparkPaint.color = Color.rgb(255, 214, 0)
        for (i in 0 until 9) {
            val zScale = 0.5f + (i % 4) * 0.25f
            val driftX = (rand.nextFloat() - 0.5f) * 38f * animSize * zScale
            val driftY = -rand.nextFloat() * 42f * animSize * zScale
            val emberX = cx + driftX
            val emberY = cy + driftY
            val emberR = (2.2f + rand.nextFloat() * 2.8f) * animSize * zScale

            sparkPaint.alpha = ((170 + rand.nextInt(85)) * opacity * zScale).toInt().coerceIn(0, 255)
            canvas.drawCircle(emberX, emberY, emberR, sparkPaint)
        }
    }

    // =========================================================================
    // 💨 4. 3D ATMOSPHERIC MIST (Volumetric 3D Cloudlet Particles)
    // =========================================================================
    private fun drawAtmosphericMistSimulation3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (36f + progress * 26f) * animSize
        val timeSec = System.currentTimeMillis() / 400.0

        val alphaVal = (105 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val cloudletCount = 8

        for (i in 0 until cloudletCount) {
            val angle = timeSec * (0.8 + i * 0.2) + i * (Math.PI / 4)
            val orbitDist = (i * 4f) * animSize
            val cloudletX = cx + (orbitDist * cos(angle)).toFloat()
            val cloudletY = cy + (orbitDist * sin(angle)).toFloat()
            val cloudletR = r * (0.6f + (i % 4) * 0.18f)

            val mistShader = RadialGradient(
                cloudletX, cloudletY, cloudletR,
                intColorWithAlpha(baseColor, alphaVal),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            auraPaint.shader = mistShader
            canvas.drawCircle(cloudletX, cloudletY, cloudletR, auraPaint)
        }
        auraPaint.shader = null

        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (85 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.4f, highlightPaint)
    }

    // =========================================================================
    // ⚡ 5. 3D ELECTRIC STORM (Multi-Stage High-Voltage Glowing Lightning)
    // =========================================================================
    private fun drawMultiStageElectricStorm3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        val lightningColor = if (isLUp || isLDown) lSwipeColor else Color.rgb(0, 229, 255)
        val alphaVal = (235 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, 60f * animSize,
            intColorWithAlpha(lightningColor, alphaVal),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, 60f * animSize, auraPaint)
        auraPaint.shader = null

        val seed = (timeMs / 30).toInt()
        val rand = Random(seed)

        path.reset()
        secondaryPath.reset()

        val startX = when (edge) { Edge.LEFT -> 0f; Edge.RIGHT -> w; Edge.BOTTOM -> cx }
        val startY = when (edge) { Edge.LEFT -> cy; Edge.RIGHT -> cy; Edge.BOTTOM -> h }

        var currX = startX
        var currY = startY
        path.moveTo(currX, currY)

        val steps = 7
        for (i in 1..steps) {
            val ratio = i.toFloat() / steps
            val targetX = startX + (cx - startX) * ratio
            val targetY = startY + (cy - startY) * ratio

            val jitterX = (rand.nextFloat() - 0.5f) * 32f * animSize
            val jitterY = (rand.nextFloat() - 0.5f) * 32f * animSize

            currX = if (i == steps) cx else targetX + jitterX
            currY = if (i == steps) cy else targetY + jitterY

            path.lineTo(currX, currY)

            sparkPaint.color = lightningColor
            sparkPaint.alpha = (230 * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(currX, currY, 3.5f * animSize, sparkPaint)

            if (i % 2 == 1 && i < steps) {
                secondaryPath.moveTo(currX, currY)
                val forkAngle = rand.nextDouble() * Math.PI * 2
                val forkLen = (18f + rand.nextFloat() * 30f) * animSize
                val forkX = currX + (forkLen * cos(forkAngle)).toFloat()
                val forkY = currY + (forkLen * sin(forkAngle)).toFloat()
                secondaryPath.lineTo(forkX, forkY)
            }
        }

        glowStrokePaint.color = intColorWithAlpha(lightningColor, (240 * opacity * stateBoost).toInt().coerceIn(0, 255))
        glowStrokePaint.strokeWidth = (5f + progress * 3f) * animSize
        canvas.drawPath(path, glowStrokePaint)

        glowStrokePaint.color = intColorWithAlpha(lightningColor, (200 * opacity).toInt().coerceIn(0, 255))
        glowStrokePaint.strokeWidth = 2.5f * animSize
        canvas.drawPath(secondaryPath, glowStrokePaint)
    }

    // =========================================================================
    // ☀️ 6. 3D MASTER SOLAR CORONA (3D Volumetric Flare Corona Shader)
    // =========================================================================
    private fun drawMasterSolarCorona3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (24f + progress * 16f) * animSize
        val timeMs = System.currentTimeMillis()

        val sunColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 179, 0)
        val alphaVal = (240 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, r * 2.5f,
            intArrayOf(
                Color.WHITE,
                intColorWithAlpha(sunColor, alphaVal),
                intColorWithAlpha(Color.rgb(230, 81, 0), (alphaVal * 0.3f).toInt())
            ),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, r * 2.5f, auraPaint)
        auraPaint.shader = null

        glowStrokePaint.color = intColorWithAlpha(sunColor, (alphaVal * 0.85f).toInt())
        glowStrokePaint.strokeWidth = 2.6f * animSize
        val rotAngle = (timeMs / 40.0) % 360.0

        for (i in 0 until 16) {
            val angle = Math.toRadians(rotAngle + i * 22.5)
            val rayPulse = sin(timeMs / 70.0 + i).toFloat() * 7f * animSize
            val rx1 = cx + (r * 1.05f * cos(angle)).toFloat()
            val ry1 = cy + (r * 1.05f * sin(angle)).toFloat()
            val rx2 = cx + ((r * 1.65f + rayPulse) * cos(angle)).toFloat()
            val ry2 = cy + ((r * 1.65f + rayPulse) * sin(angle)).toFloat()

            canvas.drawLine(rx1, ry1, rx2, ry2, glowStrokePaint)
        }

        bodyPaint.color = Color.WHITE
        bodyPaint.alpha = (245 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.75f, bodyPaint)
    }

    // =========================================================================
    // 🎯 7. GESTURE ICON & ACTION SYMBOL INTERACTION (3D Glass Orb Badge Container)
    // =========================================================================
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

        // 3D Floating Glass Orb Badge with 3D Round Drop Shadow
        if (armed || holdArmed || isLUp || isLDown) {
            val auraRadius = if (isLUp || isLDown || holdArmed) 34f else 28f

            // 1. 3D Round Drop Shadow
            shadowPaint.color = Color.BLACK
            shadowPaint.alpha = (105 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx + 5f, cy + 7f, auraRadius * 1.06f, shadowPaint)

            // 2. 3D Glass Orb Body
            auraPaint.color = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            auraPaint.alpha = ((if (isLUp || isLDown) 215 else if (holdArmed) 195 else 140) * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, auraRadius, auraPaint)

            // 3. Top-Left 3D Specular Light Refraction Lens
            highlightPaint.color = Color.WHITE
            highlightPaint.alpha = (165 * opacity * alpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx - auraRadius * 0.35f, cy - auraRadius * 0.35f, auraRadius * 0.38f, highlightPaint)
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.setShadowLayer(9f, 0f, 2.5f, Color.argb(170, 0, 0, 0))

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
                rectF.set(cx - 54f, h - 11f, cx + 54f, h - 4f)
                canvas.drawRoundRect(rectF, 3.5f, 3.5f, auraPaint)
            }
        }
    }

    private fun intColorWithAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
    }
}
