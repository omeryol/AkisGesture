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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Deep Organic Nature Simulation Engine.
 * Complete architectural rewrite featuring procedural fluid dynamics,
 * fractal lightning generation, turbulent plasma fire physics,
 * atmospheric nebula particle fields, and solar corona optics.
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
    private val branchPath = Path()
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
    var animation: FeedbackAnimation = FeedbackAnimation.OCEAN_LIQUID
    var quickIcon: FeedbackIcon = FeedbackIcon.CHEVRON
    var holdIcon: FeedbackIcon = FeedbackIcon.STAR
    var actionSymbol: String = ""
    var animSpeed: Float = 1f
    var animSize: Float = 1f
    var showIndicatorBar: Boolean = false

    // ── Organic Particle Data Class ──
    private class OrganicParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var alpha: Float,
        var life: Float,
        var maxLife: Float
    )

    private val particlePool = Array(18) {
        OrganicParticle(0f, 0f, 0f, 0f, 4f, 1f, 1f, 1f)
    }

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

        // Execute Deep Organic Nature Simulation Engines
        when (animation) {
            FeedbackAnimation.OCEAN_LIQUID, FeedbackAnimation.WATER, FeedbackAnimation.FLUID -> {
                drawOceanLiquidSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.PLASMA_FIRE, FeedbackAnimation.FIRE -> {
                drawPlasmaFireSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.ELECTRIC_STORM, FeedbackAnimation.LIGHTNING -> {
                drawElectricStormSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.ATMOSPHERIC_MIST, FeedbackAnimation.STEAM -> {
                drawAtmosphericMistSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.SOLAR_CORONA, FeedbackAnimation.SUN -> {
                drawSolarCoronaSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
            else -> {
                drawOceanLiquidSimulation(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            }
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
    // 🌊 1. OCEAN LIQUID & METABALL DYNAMICS (Procedural Hydrodynamic Wave)
    // =========================================================================
    private fun drawOceanLiquidSimulation(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val span = halfSpan * (0.85f + (stretch / 280f).coerceIn(0f, 0.35f)) * animSize
        val peakVal = stretch * 0.96f
        val timeSec = System.currentTimeMillis() / 300.0

        // Procedural Wave Path with Sine Wave Interference
        path.reset()
        val segments = 12
        val points = Array(segments + 1) { Pair(0f, 0f) }

        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val waveOffset = sin(timeSec * 2.5 + i * 0.6).toFloat() * (8f * progress)
            val yAlong = touchPos - span + t * (span * 2f)

            // Sine envelop calculation for wave swelling
            val envelope = sin(t * Math.PI).toFloat()
            val xDepth = (peakVal + waveOffset) * envelope

            points[i] = when (edge) {
                Edge.LEFT -> Pair(xDepth, yAlong)
                Edge.RIGHT -> Pair(w - xDepth, yAlong)
                Edge.BOTTOM -> Pair(yAlong, h - xDepth)
            }
        }

        when (edge) {
            Edge.LEFT -> {
                path.moveTo(0f, touchPos - span)
                for (i in 0..segments) path.lineTo(points[i].first, points[i].second)
                path.lineTo(0f, touchPos + span)
            }
            Edge.RIGHT -> {
                path.moveTo(w, touchPos - span)
                for (i in 0..segments) path.lineTo(points[i].first, points[i].second)
                path.lineTo(w, touchPos + span)
            }
            Edge.BOTTOM -> {
                path.moveTo(touchPos - span, h)
                for (i in 0..segments) path.lineTo(points[i].first, points[i].second)
                path.lineTo(touchPos + span, h)
            }
        }
        path.close()

        // Hydrodynamic Multi-Stop Linear Gradient Shader
        val alphaVal = ((80 + progress * 90) * opacity * stateBoost).toInt().coerceIn(0, 245)
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

        // Specular Crest Foam Highlight Rim
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2.6f * animSize
        rimPaint.alpha = ((90 + progress * 120) * opacity * stateBoost).toInt().coerceIn(0, 245)
        canvas.drawPath(path, rimPaint)

        // Floating Buoyant Liquid Droplets (Metaball Physics)
        val dropCount = 5
        sparkPaint.color = intColorWithAlpha(baseColor, (alphaVal * 0.85f).toInt())
        for (i in 0 until dropCount) {
            val wobble = sin(timeSec * 3.0 + i * 1.2).toFloat() * 12f * animSize
            val dropRadius = (5f + (i % 3) * 3.5f) * animSize
            val dist = (cx * (0.6f + i * 0.2f))

            val dropX = when (edge) {
                Edge.LEFT -> cx + dist * 0.35f
                Edge.RIGHT -> cx - dist * 0.35f
                Edge.BOTTOM -> cx + wobble
            }
            val dropY = when (edge) {
                Edge.BOTTOM -> cy - dist * 0.35f
                else -> cy + wobble
            }

            canvas.drawCircle(dropX, dropY, dropRadius, sparkPaint)

            // Micro specular reflection on droplet
            sparkPaint.color = Color.WHITE
            sparkPaint.alpha = (180 * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(dropX - dropRadius * 0.3f, dropY - dropRadius * 0.3f, dropRadius * 0.35f, sparkPaint)
            sparkPaint.color = intColorWithAlpha(baseColor, (alphaVal * 0.85f).toInt())
        }
    }

    // =========================================================================
    // 🔥 2. PLASMA FIRE & VOLCANIC EMBERS (Turbulent Flame Engine)
    // =========================================================================
    private fun drawPlasmaFireSimulation(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val flameR = (26f + progress * 20f) * animSize
        val timeMs = System.currentTimeMillis()

        // Turbulent Flame Path
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

        // Volumetric Multi-Stop Heat Radial Shader
        val flameColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 61, 0)
        val alphaVal = (235 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, flameR * 2.4f,
            intArrayOf(
                Color.rgb(255, 245, 157), // White-hot plasma core
                intColorWithAlpha(flameColor, alphaVal),
                intColorWithAlpha(Color.rgb(183, 28, 28), (alphaVal * 0.2f).toInt())
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = radialShader
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null

        // Volcanic Embers Particle Field
        val rand = Random(timeMs / 120)
        sparkPaint.color = Color.rgb(255, 214, 0)
        for (i in 0 until 8) {
            val driftX = (rand.nextFloat() - 0.5f) * 36f * animSize
            val driftY = -rand.nextFloat() * 40f * animSize
            val emberX = cx + driftX
            val emberY = cy + driftY
            val emberR = (2.5f + rand.nextFloat() * 3f) * animSize

            sparkPaint.alpha = ((180 + rand.nextInt(75)) * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(emberX, emberY, emberR, sparkPaint)
        }
    }

    // =========================================================================
    // ⚡ 3. ELECTRIC STORM & VOLTAGE LIGHTNING (Procedural Fractal Lightning)
    // =========================================================================
    private fun drawElectricStormSimulation(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val timeMs = System.currentTimeMillis()

        // Flash Ionization Plasma Glow
        val lightningColor = if (isLUp || isLDown) lSwipeColor else Color.rgb(0, 229, 255)
        val alphaVal = (220 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, 52f * animSize,
            intColorWithAlpha(lightningColor, alphaVal),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, 52f * animSize, auraPaint)
        auraPaint.shader = null

        // Procedural Fractal Lightning Bolt Generation
        val seed = (timeMs / 35).toInt()
        val rand = Random(seed)

        path.reset()
        branchPath.reset()

        val startX = when (edge) { Edge.LEFT -> 0f; Edge.RIGHT -> w; Edge.BOTTOM -> cx }
        val startY = when (edge) { Edge.LEFT -> cy; Edge.RIGHT -> cy; Edge.BOTTOM -> h }

        var currX = startX
        var currY = startY
        path.moveTo(currX, currY)

        val steps = 6
        for (i in 1..steps) {
            val ratio = i.toFloat() / steps
            val targetX = startX + (cx - startX) * ratio
            val targetY = startY + (cy - startY) * ratio

            val jitterX = (rand.nextFloat() - 0.5f) * 28f * animSize
            val jitterY = (rand.nextFloat() - 0.5f) * 28f * animSize

            currX = if (i == steps) cx else targetX + jitterX
            currY = if (i == steps) cy else targetY + jitterY

            path.lineTo(currX, currY)

            // Generate Fractal Sub-Branch
            if (i == 3 || i == 4) {
                branchPath.moveTo(currX, currY)
                val branchX = currX + (rand.nextFloat() - 0.5f) * 45f * animSize
                val branchY = currY + (rand.nextFloat() - 0.5f) * 45f * animSize
                branchPath.lineTo(branchX, branchY)
            }
        }

        // Draw Lightning Core (White Hot High Voltage)
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = (4.5f + progress * 2.5f) * animSize
        rimPaint.alpha = (255 * opacity * stateBoost).toInt().coerceIn(0, 255)
        canvas.drawPath(path, rimPaint)

        // Draw Sub-Branches (Cyan Ion Glow)
        rimPaint.color = lightningColor
        rimPaint.strokeWidth = 2.2f * animSize
        rimPaint.alpha = (210 * opacity).toInt().coerceIn(0, 255)
        canvas.drawPath(branchPath, rimPaint)

        // Energy Arc Sparks Radiating at Contact Points
        sparkPaint.color = Color.WHITE
        for (i in 0 until 5) {
            val angle = rand.nextDouble() * Math.PI * 2
            val dist = 8f + rand.nextFloat() * 16f * animSize
            val sx = cx + (dist * cos(angle)).toFloat()
            val sy = cy + (dist * sin(angle)).toFloat()
            canvas.drawCircle(sx, sy, (2f + rand.nextFloat() * 2.5f) * animSize, sparkPaint)
        }
    }

    // =========================================================================
    // 💨 4. ATMOSPHERIC MIST & NEBULA (Volumetric Cloudlet Particle Field)
    // =========================================================================
    private fun drawAtmosphericMistSimulation(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (36f + progress * 26f) * animSize
        val timeSec = System.currentTimeMillis() / 400.0

        // 8 Volumetric Atmospheric Nebula Cloudlets Swirling in Orbital Fields
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

        // Ethereal Soft Highlight Core
        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (85 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.4f, highlightPaint)
    }

    // =========================================================================
    // ☀️ 5. SOLAR CORONA & STELLAR RAYS (Radiant Solar Flare Engine)
    // =========================================================================
    private fun drawSolarCoronaSimulation(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val r = (24f + progress * 16f) * animSize
        val timeMs = System.currentTimeMillis()

        // Volumetric Solar Corona Gradient
        val sunColor = if (isLUp || isLDown || holdArmed) baseColor else Color.rgb(255, 179, 0)
        val alphaVal = (235 * opacity * stateBoost).toInt().coerceIn(0, 255)

        val radialShader = RadialGradient(
            cx, cy, r * 2.2f,
            intColorWithAlpha(sunColor, alphaVal),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        auraPaint.shader = radialShader
        canvas.drawCircle(cx, cy, r * 2.2f, auraPaint)
        auraPaint.shader = null

        // 16 Dynamic Rotating Solar Rays
        rimPaint.color = intColorWithAlpha(sunColor, (alphaVal * 0.85f).toInt())
        rimPaint.strokeWidth = 2.4f * animSize
        val rotAngle = (timeMs / 45.0) % 360.0

        for (i in 0 until 16) {
            val angle = Math.toRadians(rotAngle + i * 22.5)
            val rayPulse = sin(timeMs / 80.0 + i).toFloat() * 6f * animSize
            val rx1 = cx + (r * 1.05f * cos(angle)).toFloat()
            val ry1 = cy + (r * 1.05f * sin(angle)).toFloat()
            val rx2 = cx + ((r * 1.55f + rayPulse) * cos(angle)).toFloat()
            val ry2 = cy + ((r * 1.55f + rayPulse) * sin(angle)).toFloat()

            canvas.drawLine(rx1, ry1, rx2, ry2, rimPaint)
        }

        // Intense White-Hot Sun Core Lens Flare
        bodyPaint.color = Color.WHITE
        bodyPaint.alpha = (245 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.72f, bodyPaint)

        // Core lens flare ring
        rimPaint.color = Color.WHITE
        rimPaint.strokeWidth = 2f
        rimPaint.alpha = (200 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.95f, rimPaint)
    }

    // =========================================================================
    // 🎯 6. GESTURE ICON & ACTION SYMBOL INTERACTION
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
