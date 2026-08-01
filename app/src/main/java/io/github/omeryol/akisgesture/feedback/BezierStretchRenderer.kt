package io.github.omeryol.akisgesture.feedback

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import io.github.omeryol.akisgesture.overlay.Edge
import kotlin.math.abs
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

    private val metalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
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
        val visualScale = (animSize * stateBoost).coerceIn(0.85f, 2.8f)

        // Every style gets a readable volumetric stage so small strokes never
        // disappear behind the action icon.
        drawVolumetricAura(
            canvas = canvas,
            edge = edge,
            stretch = stretch,
            touchPos = effectiveTouchPos,
            w = canvasWidth,
            h = canvasHeight,
            color = baseColor,
            opacity = opacity,
            scale = visualScale,
            progress = progress,
        )

        // Execute 3D Spatial Optics Nature Simulation Engines
        when (animation) {
            FeedbackAnimation.OCEAN_WAVE,
            FeedbackAnimation.HYDRO_WIPE -> drawFluidHydroTide3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.ZIPPER_VOID -> drawRealisticZipperVoid3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.BLACK_HOLE_PULL -> drawAccretionBlackHole3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.PLASMA_FIRE -> drawVolumetricPlasmaFire3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MATRIX_DISSOLVE,
            FeedbackAnimation.STARFIELD -> drawMatrixCyberDissolve3D(canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, progress, stateBoost)
            FeedbackAnimation.MERCURY_TEARDROP,
            FeedbackAnimation.ATMOSPHERIC_MIST,
            FeedbackAnimation.ELECTRIC_STORM,
            FeedbackAnimation.SOLAR_CORONA -> drawSpecialAnimation(canvas, edge, effectiveTouchPos, canvasWidth, canvasHeight, progress, animation)
            FeedbackAnimation.AURORA_RIBBON,
            FeedbackAnimation.GLASS_RIPPLE,
            FeedbackAnimation.NEON_PULSE,
            FeedbackAnimation.STARFIELD,
            FeedbackAnimation.ICE_SHARDS,
            FeedbackAnimation.VORTEX,
            FeedbackAnimation.PRISM_FLOW,
            FeedbackAnimation.EMBER_BLOOM,
            FeedbackAnimation.COMET_TAIL,
            FeedbackAnimation.QUANTUM_RING,
            FeedbackAnimation.INK_FLOW,
            FeedbackAnimation.SOLAR_FLARE,
            FeedbackAnimation.MATRIX_DISSOLVE,
            FeedbackAnimation.HYDRO_WIPE,
            FeedbackAnimation.DEWDROP_GLASS,
            FeedbackAnimation.PRISM_SHATTER -> {
                drawSpecialAnimation(canvas, edge, effectiveTouchPos, canvasWidth, canvasHeight, progress, animation)
            }
            FeedbackAnimation.ICON_ONLY, FeedbackAnimation.NONE -> Unit
        }

        // Draw icon & action symbol with tight shape interaction
        drawGestureIcon(
            canvas, edge, stretch, effectiveTouchPos, canvasWidth, canvasHeight, arrowAlpha, progress
        )
    }

    private fun drawVolumetricAura(
        canvas: Canvas,
        edge: Edge,
        stretch: Float,
        touchPos: Float,
        w: Float,
        h: Float,
        color: Int,
        opacity: Float,
        scale: Float,
        progress: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        // Keep this layer as a quiet depth cue. The selected animation must
        // own the silhouette; a large shared lens makes every mode look alike.
        val radius = (26f + progress * 30f) * scale
        auraPaint.shader = RadialGradient(
            cx,
            cy,
            radius * 2.2f,
            intArrayOf(
                intColorWithAlpha(Color.WHITE, (28 * opacity).toInt()),
                intColorWithAlpha(color, (72 * opacity).toInt()),
                intColorWithAlpha(color, (24 * opacity).toInt()),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.24f, 0.58f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, radius * 2.2f, auraPaint)
        auraPaint.shader = null
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
            if (i == 0) {
                val startPx = when (edge) {
                    Edge.LEFT -> 0f
                    Edge.RIGHT -> w
                    Edge.BOTTOM -> touchPos - span
                }
                val startPy = when (edge) {
                    Edge.LEFT, Edge.RIGHT -> touchPos - span
                    Edge.BOTTOM -> h
                }
                path.moveTo(startPx, startPy)
            }
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
        val timeMs = System.currentTimeMillis()
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

            // Small satellites make the bulb read as a drop breaking away from
            // the stretched wave instead of a second unrelated icon.
            sparkPaint.color = Color.WHITE
            for (i in 0 until 3) {
                val orbit = dropR * (1.35f + i * 0.22f)
                val angle = timeMs / 600.0 + i * 2.1
                val particleX = dropX + cos(angle).toFloat() * orbit
                val particleY = dropY + sin(angle).toFloat() * orbit
                sparkPaint.alpha = (150 * opacity).toInt().coerceIn(0, 255)
                canvas.drawCircle(particleX, particleY, dropR * (0.08f + i * 0.025f), sparkPaint)
            }
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
        val flameR = (36f + progress * 38f) * animSize * stateBoost.coerceAtMost(1.35f)
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

        // Separate hot tongue: a compact yellow-white core keeps the flame
        // readable as fire instead of a generic glowing blob.
        bodyPaint.shader = RadialGradient(
            cx,
            cy - flameR * 0.18f,
            flameR * 0.9f,
            intArrayOf(Color.WHITE, Color.rgb(255, 193, 7), intColorWithAlpha(flameColor, 0)),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy - flameR * 0.12f, flameR * 0.72f, bodyPaint)
        bodyPaint.shader = null

        // A hot inner tongue gives the outer silhouette a readable flame shape.
        val innerColor = Color.rgb(255, 238, 130)
        val innerShader = RadialGradient(
            cx, cy - flameR * 0.18f, flameR * 0.9f,
            intArrayOf(Color.WHITE, innerColor, intColorWithAlpha(flameColor, 0)),
            floatArrayOf(0f, 0.38f, 1f),
            Shader.TileMode.CLAMP,
        )
        bodyPaint.shader = innerShader
        canvas.drawCircle(cx, cy - flameR * 0.12f, flameR * 0.72f, bodyPaint)
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
        val r = (54f + progress * 46f) * animSize
        val timeSec = System.currentTimeMillis() / 400.0

        val alphaVal = (145 * opacity * stateBoost).toInt().coerceIn(0, 255)
        val cloudletCount = 14

        for (i in 0 until cloudletCount) {
            val angle = timeSec * (0.8 + i * 0.2) + i * (Math.PI / 4)
            val orbitDist = (i * 6f) * animSize
            val cloudletX = cx + (orbitDist * cos(angle)).toFloat()
            val cloudletY = cy + (orbitDist * sin(angle)).toFloat()
            val cloudletR = r * (0.42f + (i % 4) * 0.14f)

            val mistShader = RadialGradient(
                cloudletX, cloudletY, cloudletR,
                intColorWithAlpha(baseColor, (alphaVal * 1.45f).toInt()),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            auraPaint.shader = mistShader
            canvas.drawCircle(cloudletX, cloudletY, cloudletR, auraPaint)
        }
        auraPaint.shader = null

        highlightPaint.color = Color.WHITE
        highlightPaint.alpha = (125 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.52f, highlightPaint)
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
        glowStrokePaint.strokeWidth = (14f + progress * 7f) * animSize
        canvas.drawPath(path, glowStrokePaint)

        glowStrokePaint.color = Color.WHITE
        glowStrokePaint.strokeWidth = 5f * animSize
        canvas.drawPath(path, glowStrokePaint)

        glowStrokePaint.color = intColorWithAlpha(lightningColor, (235 * opacity).toInt().coerceIn(0, 255))
        glowStrokePaint.strokeWidth = 7f * animSize
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
        val r = (38f + progress * 34f) * animSize
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
        glowStrokePaint.strokeWidth = 9f * animSize
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

        // Curved prominence arcs make the corona feel like an eruption, not a
        // static radial badge.
        glowStrokePaint.strokeWidth = 8f * animSize
        glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (180 * opacity).toInt())
        for (i in 0 until 4) {
            val arcAngle = Math.toRadians(rotAngle + i * 90.0)
            val arcX = cx + (r * 1.12f * cos(arcAngle)).toFloat()
            val arcY = cy + (r * 1.12f * sin(arcAngle)).toFloat()
            path.reset()
            path.moveTo(arcX, arcY)
            path.cubicTo(
                cx + (r * 1.85f * cos(arcAngle + 0.55)).toFloat(),
                cy + (r * 1.85f * sin(arcAngle + 0.55)).toFloat(),
                cx + (r * 1.85f * cos(arcAngle - 0.55)).toFloat(),
                cy + (r * 1.85f * sin(arcAngle - 0.55)).toFloat(),
                arcX,
                arcY,
            )
            canvas.drawPath(path, glowStrokePaint)
        }

        bodyPaint.color = Color.WHITE
        bodyPaint.alpha = (245 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, r * 0.75f, bodyPaint)
    }

    private fun drawFluidHydroTide3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val span = halfSpan * (1.1f + progress * .3f) * animSize
        val depth = (stretch * 1.15f).coerceAtLeast(45f * animSize)
        val time = System.currentTimeMillis() / 240.0
        path.reset()
        for (i in 0..20) {
            val t = i / 20f
            val wave = (sin(time * 2.8 + i * .45) * 9f + cos(time * 1.4 - i * .3) * 6f).toFloat() * animSize
            val along = touchPos - span + t * span * 2f
            val envelope = sin(t * Math.PI).toFloat()
            val offset = (depth + wave) * envelope
            val point = when (edge) {
                Edge.LEFT -> cx + offset to along
                Edge.RIGHT -> w - (cx + offset) to along
                Edge.BOTTOM -> along to h - offset
            }
            if (i == 0) path.moveTo(point.first, point.second) else path.lineTo(point.first, point.second)
        }
        when (edge) {
            Edge.LEFT -> path.lineTo(0f, touchPos + span)
            Edge.RIGHT -> path.lineTo(w, touchPos + span)
            Edge.BOTTOM -> path.lineTo(touchPos + span, h)
        }
        path.close()
        shadowPaint.color = Color.BLACK
        shadowPaint.alpha = (110 * opacity).toInt().coerceIn(0, 255)
        canvas.save(); canvas.translate(10f * animSize, 13f * animSize); canvas.drawPath(path, shadowPaint); canvas.restore()
        val alpha = ((180 + progress * 70) * opacity * stateBoost).toInt().coerceIn(0, 255)
        bodyPaint.shader = when (edge) {
            Edge.LEFT -> LinearGradient(0f, cy, depth, cy, intColorWithAlpha(baseColor, alpha), intColorWithAlpha(Color.CYAN, (alpha * .7f).toInt()), Shader.TileMode.CLAMP)
            Edge.RIGHT -> LinearGradient(w, cy, w - depth, cy, intColorWithAlpha(baseColor, alpha), intColorWithAlpha(Color.CYAN, (alpha * .7f).toInt()), Shader.TileMode.CLAMP)
            Edge.BOTTOM -> LinearGradient(cx, h, cx, h - depth, intColorWithAlpha(baseColor, alpha), intColorWithAlpha(Color.CYAN, (alpha * .7f).toInt()), Shader.TileMode.CLAMP)
        }
        canvas.drawPath(path, bodyPaint); bodyPaint.shader = null
        glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (210 * opacity).toInt())
        glowStrokePaint.strokeWidth = 6f * animSize
        canvas.drawPath(path, glowStrokePaint)
        sparkPaint.color = Color.WHITE
        for (i in 0 until 6) {
            sparkPaint.alpha = ((150 + i * 15) * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx + cos(time + i).toFloat() * depth * .5f, cy + sin(time + i * .8).toFloat() * span * .3f, (3f + i % 3) * animSize, sparkPaint)
        }
    }

    private fun drawRealisticZipperVoid3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val opening = (35f + progress * 85f) * animSize
        val railLength = (90f + progress * 140f) * animSize
        auraPaint.shader = RadialGradient(cx, cy, opening * 2.6f, intArrayOf(Color.BLACK, intColorWithAlpha(Color.rgb(15, 23, 42), (240 * opacity).toInt()), intColorWithAlpha(baseColor, (120 * opacity).toInt()), Color.TRANSPARENT), floatArrayOf(0f, .4f, .75f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, opening * 2.6f, auraPaint); auraPaint.shader = null
        bodyPaint.color = Color.BLACK; bodyPaint.alpha = (220 * opacity).toInt()
        canvas.drawOval(RectF(cx - opening * .7f, cy - opening * 1.3f, cx + opening * .7f, cy + opening * 1.3f), bodyPaint)
        val side = if (edge == Edge.RIGHT) -1f else 1f
        path.reset()
        when (edge) {
            Edge.LEFT, Edge.RIGHT -> { val ex = if (edge == Edge.LEFT) cx - railLength else cx + railLength; path.moveTo(ex, cy-opening*1.8f); path.quadTo(cx-side*opening*1.2f, cy, ex, cy+opening*1.8f) }
            Edge.BOTTOM -> { path.moveTo(cx-opening*1.8f, cy+railLength); path.quadTo(cx, cy-opening*1.2f, cx+opening*1.8f, cy+railLength) }
        }
        glowStrokePaint.color = intColorWithAlpha(Color.LTGRAY, (220 * opacity).toInt()); glowStrokePaint.strokeWidth = 10f * animSize; canvas.drawPath(path, glowStrokePaint)
        glowStrokePaint.color = Color.WHITE; glowStrokePaint.strokeWidth = 3f * animSize; canvas.drawPath(path, glowStrokePaint)
        for (i in 0..18) {
            val t = i / 18f; val y = cy-opening*1.7f+t*opening*3.4f; val open = (1f-abs(y-cy)/(opening*1.7f)).coerceIn(0f,1f); val x = cx+open*opening*.9f*side
            metalPaint.color = if (i%2==0) Color.rgb(226,232,240) else Color.rgb(71,85,105); metalPaint.alpha = (235*opacity).toInt()
            rectF.set(x-10f*animSize,y-4f*animSize,x+10f*animSize,y+4f*animSize); canvas.drawRoundRect(rectF, 3f*animSize, 3f*animSize, metalPaint)
        }
    }

    private fun drawAccretionBlackHole3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val horizon = (32f + progress * 62f) * animSize
        val time = System.currentTimeMillis() / 1000.0
        auraPaint.shader = RadialGradient(cx, cy, horizon*3.2f, intArrayOf(Color.BLACK, intColorWithAlpha(baseColor, (240*opacity).toInt()), intColorWithAlpha(Color.rgb(245,158,11), (140*opacity).toInt()), Color.TRANSPARENT), floatArrayOf(0f,.35f,.75f,1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx,cy,horizon*3.2f,auraPaint); auraPaint.shader=null
        glowStrokePaint.color=intColorWithAlpha(Color.rgb(251,191,36),(230*opacity).toInt()); glowStrokePaint.strokeWidth=12f*animSize
        rectF.set(cx-horizon*2.3f,cy-horizon*.65f,cx+horizon*2.3f,cy+horizon*.65f); canvas.drawOval(rectF,glowStrokePaint)
        bodyPaint.color=Color.BLACK; bodyPaint.alpha=(255*opacity).toInt(); canvas.drawCircle(cx,cy,horizon,bodyPaint)
        sparkPaint.color=Color.WHITE
        for(i in 0 until 14){ val a=time*2.5+i*.45; val d=horizon*(1.2f+(i%4)*.35f); sparkPaint.alpha=((140+i*8)*opacity).toInt().coerceIn(0,255); canvas.drawCircle(cx+cos(a).toFloat()*d,cy+sin(a).toFloat()*d*.5f,(2f+i%3)*animSize,sparkPaint) }
    }

    private fun drawVolumetricPlasmaFire3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float,
    ) {
        val (cx, cy) = center(edge, stretch, touchPos, w, h)
        val flame = (42f + progress*48f)*animSize
        val time=System.currentTimeMillis()
        path.reset(); path.moveTo(0f,cy-flame*1.7f); path.cubicTo(cx*.5f,cy-flame*1.3f,cx+flame,cy-flame*.4f,cx+flame,cy); path.cubicTo(cx+flame,cy+flame*.4f,cx*.5f,cy+flame*1.3f,0f,cy+flame*1.7f); path.close()
        bodyPaint.shader=RadialGradient(cx,cy,flame*2.5f,intArrayOf(Color.WHITE,Color.YELLOW,intColorWithAlpha(Color.rgb(239,68,68),(240*opacity).toInt()),Color.TRANSPARENT),floatArrayOf(0f,.28f,.7f,1f),Shader.TileMode.CLAMP); canvas.drawPath(path,bodyPaint); bodyPaint.shader=null
        bodyPaint.shader=RadialGradient(cx,cy-flame*.15f,flame*.85f,Color.WHITE,Color.rgb(253,224,71),Shader.TileMode.CLAMP); canvas.drawCircle(cx,cy-flame*.1f,flame*.62f,bodyPaint); bodyPaint.shader=null
        sparkPaint.color=Color.rgb(253,224,71); val rand=Random(time/100)
        for(i in 0 until 14){ sparkPaint.alpha=((180+rand.nextInt(75))*opacity).toInt().coerceIn(0,255); canvas.drawCircle(cx+(rand.nextFloat()-.5f)*flame,cy-rand.nextFloat()*flame,(2.5f+rand.nextFloat()*3f)*animSize,sparkPaint) }
    }

    private fun drawMatrixCyberDissolve3D(
        canvas: Canvas, edge: Edge, stretch: Float, touchPos: Float,
        w: Float, h: Float, progress: Float, stateBoost: Float,
    ) {
        val (cx, cy)=center(edge,stretch,touchPos,w,h); val radius=(45f+progress*55f)*animSize; val time=System.currentTimeMillis()/1000.0; val green=Color.rgb(0,255,102)
        for(col in 0 until 14){ val x=cx-radius*1.6f+col*radius*.25f; val top=cy-radius*1.5f+((time*2.5+col*.6)%1.0).toFloat()*radius*3f; for(row in 0 until 7){ val y=top-row*14f*animSize; sparkPaint.color=if(row==0)Color.WHITE else green; sparkPaint.alpha=((240-row*32)*opacity).toInt().coerceIn(0,255); rectF.set(x,y,x+7f*animSize,y+10f*animSize); canvas.drawRoundRect(rectF,2f*animSize,2f*animSize,sparkPaint) } }
        glowStrokePaint.color=intColorWithAlpha(green,(240*opacity*stateBoost).toInt().coerceIn(0,255)); glowStrokePaint.strokeWidth=7f*animSize; canvas.drawLine(cx,cy-radius*1.6f,cx,cy+radius*1.6f,glowStrokePaint)
    }

    private fun drawSpecialAnimation(
        canvas: Canvas,
        edge: Edge,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
        mode: FeedbackAnimation,
    ) {
        val (cx, cy) = center(edge, progress * 260f + 16f, touchPos, w, h)
        val time = System.currentTimeMillis() / 1000.0
        val radius = (30f + progress * 32f) * animSize
        val alpha = (210f * opacity).toInt().coerceIn(0, 255)
        val colors = intArrayOf(
            Color.rgb(0, 229, 255), Color.rgb(124, 77, 255), Color.rgb(255, 64, 129),
        )

        when (mode) {
            FeedbackAnimation.OCEAN_WAVE -> {
                path.reset()
                val span = radius * 2.7f
                for (i in 0..20) {
                    val t = i / 20f
                    val crest = sin(time * 2.2 + t * 8.0).toFloat() * radius * 0.24f
                    val x = cx - radius * 1.9f + t * radius * 3.8f
                    if (i == 0) path.moveTo(x, cy - span * .32f + crest)
                    else path.lineTo(x, cy - span * .32f + crest)
                }
                for (i in 20 downTo 0) {
                    val t = i / 20f
                    val crest = sin(time * 2.2 + t * 8.0 + .7).toFloat() * radius * .24f
                    path.lineTo(cx - radius * 1.9f + t * radius * 3.8f, cy + span * .32f + crest)
                }
                path.close()
                bodyPaint.shader = LinearGradient(cx - radius, cy, cx + radius, cy, baseColor, secondaryColor, Shader.TileMode.MIRROR)
                canvas.drawPath(path, bodyPaint)
                bodyPaint.shader = null
                glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (150 * opacity).toInt())
                glowStrokePaint.strokeWidth = 6f * animSize
                canvas.drawPath(path, glowStrokePaint)
            }
            FeedbackAnimation.MERCURY_TEARDROP -> {
                val drop = radius * (.72f + progress * .22f)
                bodyPaint.shader = RadialGradient(cx - drop*.35f, cy-drop*.35f, drop*1.5f, Color.WHITE, baseColor, Shader.TileMode.CLAMP)
                canvas.drawOval(RectF(cx-drop*1.3f, cy-drop*.78f, cx+drop*1.1f, cy+drop*.78f), bodyPaint)
                bodyPaint.shader = null
                sparkPaint.color = Color.WHITE
                for (i in 0 until 5) {
                    val angle = time * 1.4 + i * 1.2
                    canvas.drawCircle(cx + cos(angle).toFloat()*drop*1.35f, cy + sin(angle).toFloat()*drop*.7f, (2f+i)*animSize, sparkPaint)
                }
            }
            FeedbackAnimation.PLASMA_FIRE -> {
                path.reset()
                val flame = radius * 1.25f
                path.moveTo(cx-flame*.8f, cy+flame*.8f)
                path.cubicTo(cx-flame*1.2f, cy, cx-flame*.35f, cy-flame*.7f, cx, cy-flame*1.35f)
                path.cubicTo(cx+flame*.25f, cy-flame*.65f, cx+flame*1.15f, cy-.1f, cx+flame*.8f, cy+flame*.8f)
                path.close()
                bodyPaint.shader = RadialGradient(cx, cy-flame*.3f, flame*1.4f, intArrayOf(Color.WHITE, Color.YELLOW, baseColor, Color.TRANSPARENT), floatArrayOf(0f,.25f,.62f,1f), Shader.TileMode.CLAMP)
                canvas.drawPath(path, bodyPaint)
                bodyPaint.shader = null
                sparkPaint.color = secondaryColor
                for (i in 0 until 16) canvas.drawCircle(cx + cos(time*1.5+i).toFloat()*flame*(.7f+i%3*.15f), cy-flame*(.2f+i%5*.16f), (2f+i%4)*animSize, sparkPaint)
            }
            FeedbackAnimation.ATMOSPHERIC_MIST -> {
                for (i in 0 until 11) {
                    val dx = cos(time*.55+i*.72).toFloat()*radius*.7f
                    val dy = sin(time*.72+i*.51).toFloat()*radius*.5f
                    auraPaint.shader = RadialGradient(cx+dx, cy+dy, radius*.9f, intColorWithAlpha(baseColor, (115*opacity).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                    canvas.drawCircle(cx+dx, cy+dy, radius*.9f, auraPaint)
                }
                auraPaint.shader = null
            }
            FeedbackAnimation.ELECTRIC_STORM -> {
                glowStrokePaint.color = Color.WHITE
                glowStrokePaint.strokeWidth = 8f*animSize
                for (i in 0 until 7) {
                    val angle = time*.8+i*Math.PI/3.5
                    path.reset(); path.moveTo(cx,cy)
                    path.lineTo(cx+cos(angle).toFloat()*radius*.65f, cy+sin(angle).toFloat()*radius*.65f)
                    path.lineTo(cx+cos(angle+.15).toFloat()*radius*1.45f, cy+sin(angle+.15).toFloat()*radius*1.45f)
                    canvas.drawPath(path, glowStrokePaint)
                }
            }
            FeedbackAnimation.SOLAR_CORONA -> {
                bodyPaint.shader = RadialGradient(cx,cy,radius*1.7f, intArrayOf(Color.WHITE, Color.YELLOW, baseColor, Color.TRANSPARENT), floatArrayOf(0f,.25f,.6f,1f), Shader.TileMode.CLAMP)
                canvas.drawCircle(cx,cy,radius*1.45f,bodyPaint); bodyPaint.shader=null
                glowStrokePaint.color = intColorWithAlpha(secondaryColor,(190*opacity).toInt()); glowStrokePaint.strokeWidth=9f*animSize
                for(i in 0 until 6){ rectF.set(cx-radius*1.7f,cy-radius*1.7f,cx+radius*1.7f,cy+radius*1.7f); canvas.drawArc(rectF,(time*20+i*60).toFloat(),72f,false,glowStrokePaint) }
            }
            FeedbackAnimation.AURORA_RIBBON,
            FeedbackAnimation.PRISM_FLOW -> {
                for (band in 0 until 3) {
                    path.reset()
                    val offset = band * radius * 0.28f
                    for (step in 0..12) {
                        val t = step / 12f
                        val wave = sin(time * 2.5 + t * 7.0 + band).toFloat() * radius * 0.32f
                        val x = cx - radius * 1.5f + t * radius * 3f
                        val y = cy + wave + offset - radius * 0.28f
                        if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    glowStrokePaint.color = intColorWithAlpha(colors[band], (170 * opacity).toInt())
                    glowStrokePaint.strokeWidth = (14f - band * 2f) * animSize
                    canvas.drawPath(path, glowStrokePaint)
                }
            }
            FeedbackAnimation.GLASS_RIPPLE,
            FeedbackAnimation.QUANTUM_RING -> {
                for (ring in 0 until 4) {
                    val ringRadius = radius * (0.65f + ring * 0.34f + (time % 1.0).toFloat() * 0.16f)
                    glowStrokePaint.color = intColorWithAlpha(colors[ring % colors.size], ((170 - ring * 25) * opacity).toInt())
                    glowStrokePaint.strokeWidth = (7f - ring * 0.7f).coerceAtLeast(3f) * animSize
                    rectF.set(cx - ringRadius, cy - ringRadius * 0.7f, cx + ringRadius, cy + ringRadius * 0.7f)
                    canvas.drawOval(rectF, glowStrokePaint)
                }
            }
            FeedbackAnimation.NEON_PULSE,
            FeedbackAnimation.EMBER_BLOOM -> {
                val pulse = (sin(time * 5.0).toFloat() * 0.16f + 1f)
                auraPaint.shader = RadialGradient(
                    cx, cy, radius * 2.4f,
                    intArrayOf(Color.WHITE, intColorWithAlpha(colors[2], alpha), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.32f, 1f), Shader.TileMode.CLAMP,
                )
                canvas.drawCircle(cx, cy, radius * pulse * 1.35f, auraPaint)
                auraPaint.shader = null
                for (i in 0 until 12) {
                    val angle = time * 1.4 + i * Math.PI / 6.0
                    sparkPaint.color = colors[i % colors.size]
                    sparkPaint.alpha = (180 * opacity).toInt()
                    canvas.drawCircle(
                        cx + cos(angle).toFloat() * radius * 1.55f,
                        cy + sin(angle).toFloat() * radius * 1.55f,
                        (2f + (i % 3)) * animSize,
                        sparkPaint,
                    )
                }
            }
            FeedbackAnimation.STARFIELD -> {
                val rand = Random((time * 8).toInt())
                for (i in 0 until 24) {
                    val angle = rand.nextFloat() * Math.PI * 2
                    val distance = radius * (0.5f + rand.nextFloat() * 1.8f)
                    sparkPaint.color = Color.WHITE
                    sparkPaint.alpha = ((100 + rand.nextInt(155)) * opacity).toInt()
                    canvas.drawCircle(
                        cx + cos(angle).toFloat() * distance,
                        cy + sin(angle).toFloat() * distance,
                        (1.2f + rand.nextFloat() * 2.8f) * animSize,
                        sparkPaint,
                    )
                }
            }
            FeedbackAnimation.ICE_SHARDS -> {
                glowStrokePaint.color = intColorWithAlpha(Color.rgb(128, 222, 234), alpha)
                glowStrokePaint.strokeWidth = 8f * animSize
                for (i in 0 until 8) {
                    val angle = time * 0.5 + i * Math.PI / 4.0
                    val inner = radius * 0.35f
                    val outer = radius * (1.3f + (i % 2) * 0.5f)
                    canvas.drawLine(
                        cx + cos(angle).toFloat() * inner,
                        cy + sin(angle).toFloat() * inner,
                        cx + cos(angle).toFloat() * outer,
                        cy + sin(angle).toFloat() * outer,
                        glowStrokePaint,
                    )
                }
            }
            FeedbackAnimation.VORTEX,
            FeedbackAnimation.INK_FLOW -> {
                path.reset()
                for (step in 0..28) {
                    val t = step / 28f
                    val angle = time * 2.0 + t * Math.PI * 3.2
                    val distance = radius * (0.1f + t * 1.8f)
                    val x = cx + cos(angle).toFloat() * distance
                    val y = cy + sin(angle).toFloat() * distance
                    if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                glowStrokePaint.color = intColorWithAlpha(
                    if (mode == FeedbackAnimation.INK_FLOW) Color.rgb(63, 81, 181) else colors[1],
                    alpha,
                )
                glowStrokePaint.strokeWidth = 13f * animSize
                canvas.drawPath(path, glowStrokePaint)
            }
            FeedbackAnimation.COMET_TAIL -> {
                val headX = cx + cos(time * 2.0).toFloat() * radius * 0.8f
                val headY = cy + sin(time * 2.0).toFloat() * radius * 0.8f
                path.reset()
                path.moveTo(cx, cy)
                path.cubicTo(cx - radius, cy - radius, headX - radius * 0.7f, headY - radius * 0.4f, headX, headY)
                glowStrokePaint.color = intColorWithAlpha(Color.rgb(255, 193, 7), alpha)
                glowStrokePaint.strokeWidth = 16f * animSize
                canvas.drawPath(path, glowStrokePaint)
                sparkPaint.color = Color.WHITE
                sparkPaint.alpha = 240
                canvas.drawCircle(headX, headY, radius * 0.32f, sparkPaint)
            }
            FeedbackAnimation.SOLAR_FLARE -> {
                drawMasterSolarCorona3D(canvas, edge, radius * 3f, touchPos, w, h, progress, 1.35f)
                glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (200 * opacity).toInt())
                glowStrokePaint.strokeWidth = 5f * animSize
                for (i in 0 until 6) {
                    val angle = time * 0.8 + i * Math.PI / 3.0
                    path.reset()
                    path.moveTo(cx, cy)
                    path.quadTo(
                        cx + cos(angle).toFloat() * radius * 1.8f,
                        cy + sin(angle).toFloat() * radius * 1.8f,
                        cx + cos(angle + 0.35).toFloat() * radius * 2.4f,
                        cy + sin(angle + 0.35).toFloat() * radius * 2.4f,
                    )
                    canvas.drawPath(path, glowStrokePaint)
                }
            }
            FeedbackAnimation.ZIPPER_VOID -> drawZipperVoidAnimation(canvas, edge, touchPos, w, h, progress)
            FeedbackAnimation.BLACK_HOLE_PULL -> drawBlackHolePullAnimation(canvas, edge, touchPos, w, h, progress)
            FeedbackAnimation.MATRIX_DISSOLVE -> drawMatrixDissolve(canvas, cx, cy, radius, progress, baseColor, time)
            FeedbackAnimation.HYDRO_WIPE -> drawHydroWipe(canvas, edge, cx, cy, touchPos, w, h, radius, baseColor, secondaryColor, time)
            FeedbackAnimation.DEWDROP_GLASS -> drawDewdropGlass(canvas, cx, cy, radius, baseColor, progress, time)
            FeedbackAnimation.PRISM_SHATTER -> drawPrismShatter(canvas, cx, cy, radius, baseColor, secondaryColor, progress, time)
            else -> Unit
        }
    }

    private fun drawMatrixDissolve(canvas: Canvas, cx: Float, cy: Float, radius: Float, progress: Float, color: Int, time: Double) {
        val columns = 12
        for (column in 0 until columns) {
            val x = cx - radius * 1.5f + column * radius * 0.27f
            val phase = (time * 2.2 + column * 0.7) % 1.0
            val top = cy - radius * 1.4f + (phase.toFloat() * radius * 2.8f)
            for (row in 0 until 6) {
                val y = top - row * 13f * animSize
                sparkPaint.color = if (row == 0) Color.WHITE else color
                sparkPaint.alpha = ((210 - row * 28) * opacity).toInt().coerceIn(0, 255)
                canvas.drawRect(x, y, x + 5f * animSize, y + 9f * animSize, sparkPaint)
            }
        }
        glowStrokePaint.color = intColorWithAlpha(color, (170 * opacity).toInt())
        glowStrokePaint.strokeWidth = 5f * animSize
        canvas.drawLine(cx - radius * progress, cy - radius * 1.5f, cx - radius * progress, cy + radius * 1.5f, glowStrokePaint)
    }

    private fun drawHydroWipe(canvas: Canvas, edge: Edge, cx: Float, cy: Float, touchPos: Float, w: Float, h: Float, radius: Float, primary: Int, secondary: Int, time: Double) {
        path.reset()
        val span = radius * 2.4f
        when (edge) {
            Edge.LEFT, Edge.RIGHT -> {
                val side = if (edge == Edge.LEFT) 0f else w
                path.moveTo(side, cy - span)
                for (i in 0..18) {
                    val t = i / 18f
                    val wave = sin(time * 2.1 + t * 10.0).toFloat() * radius * 0.22f
                    path.lineTo(cx + if (edge == Edge.LEFT) wave else -wave, cy - span + t * span * 2f)
                }
                path.lineTo(side, cy + span)
            }
            Edge.BOTTOM -> {
                path.moveTo(cx - span, h)
                for (i in 0..18) {
                    val t = i / 18f
                    val wave = sin(time * 2.1 + t * 10.0).toFloat() * radius * 0.22f
                    path.lineTo(cx - span + t * span * 2f, cy + wave)
                }
                path.lineTo(cx + span, h)
            }
        }
        path.close()
        bodyPaint.shader = LinearGradient(cx, cy - span, cx, cy + span, primary, secondary, Shader.TileMode.MIRROR)
        canvas.drawPath(path, bodyPaint)
        bodyPaint.shader = null
        sparkPaint.color = Color.WHITE
        for (i in 0 until 8) {
            val x = cx - radius + i * radius * 0.28f
            canvas.drawCircle(x, cy + sin(time * 2 + i).toFloat() * radius * .4f, 3f * animSize, sparkPaint)
        }
    }

    private fun drawDewdropGlass(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, progress: Float, time: Double) {
        val drops = 5
        for (i in 0 until drops) {
            val x = cx - radius * 1.2f + i * radius * .6f + sin(time + i).toFloat() * radius * .12f
            val y = cy - radius * 1.1f + ((time * (0.25 + i * .04)) % 2.0).toFloat() * radius * 1.2f
            val drop = radius * (.18f + progress * .14f)
            bodyPaint.shader = RadialGradient(x - drop * .3f, y - drop * .35f, drop * 1.8f, Color.WHITE, intColorWithAlpha(color, (160 * opacity).toInt()), Shader.TileMode.CLAMP)
            canvas.drawCircle(x, y, drop, bodyPaint)
            bodyPaint.shader = null
        }
        glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (180 * opacity).toInt())
        glowStrokePaint.strokeWidth = 4f * animSize
        rectF.set(cx - radius * 1.4f, cy - radius * .65f, cx + radius * 1.4f, cy + radius * .65f)
        canvas.drawOval(rectF, glowStrokePaint)
    }

    private fun drawPrismShatter(canvas: Canvas, cx: Float, cy: Float, radius: Float, primary: Int, secondary: Int, progress: Float, time: Double) {
        for (i in 0 until 6) {
            val angle = time * .35 + i * Math.PI / 3.0
            val inner = radius * .22f
            val outer = radius * (1.0f + progress * .8f + i % 2 * .2f)
            path.reset()
            path.moveTo(cx + cos(angle).toFloat() * inner, cy + sin(angle).toFloat() * inner)
            path.lineTo(cx + cos(angle - .18).toFloat() * outer, cy + sin(angle - .18).toFloat() * outer)
            path.lineTo(cx + cos(angle + .12).toFloat() * outer * .85f, cy + sin(angle + .12).toFloat() * outer * .85f)
            path.close()
            bodyPaint.color = intColorWithAlpha(if (i % 2 == 0) primary else secondary, (145 * opacity).toInt())
            canvas.drawPath(path, bodyPaint)
            glowStrokePaint.color = intColorWithAlpha(Color.WHITE, (150 * opacity).toInt())
            glowStrokePaint.strokeWidth = 3f * animSize
            canvas.drawPath(path, glowStrokePaint)
        }
    }

    private fun drawBlackHolePullAnimation(
        canvas: Canvas,
        edge: Edge,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
    ) {
        val (cx, cy) = center(edge, progress * 260f + 18f, touchPos, w, h)
        val time = System.currentTimeMillis() / 1000.0
        val eventHorizon = (22f + progress * 56f) * animSize
        val pull = (0.35f + progress * 0.65f)

        // A physically readable dark core surrounded by an abstract color lens.
        auraPaint.shader = RadialGradient(
            cx, cy, eventHorizon * 3.2f,
            intArrayOf(
                Color.BLACK,
                intColorWithAlpha(Color.rgb(42, 16, 78), (220 * opacity).toInt()),
                intColorWithAlpha(baseColor, (125 * opacity).toInt()),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.28f, 0.62f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, eventHorizon * 3.2f, auraPaint)
        auraPaint.shader = null
        // A flattened luminous lens makes the gravitational pull readable even
        // before the event horizon becomes large.
        glowStrokePaint.color = intColorWithAlpha(Color.rgb(255, 112, 67), (170 * opacity).toInt())
        glowStrokePaint.strokeWidth = 11f * animSize
        rectF.set(
            cx - eventHorizon * 2.1f,
            cy - eventHorizon * 0.48f,
            cx + eventHorizon * 2.1f,
            cy + eventHorizon * 0.48f,
        )
        canvas.drawOval(rectF, glowStrokePaint)
        bodyPaint.color = Color.BLACK
        bodyPaint.alpha = (250 * opacity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, eventHorizon, bodyPaint)

        // Accretion disk: light bends into a flattened orbit around the void.
        glowStrokePaint.strokeWidth = 8f * animSize
        for (ring in 0 until 3) {
            val orbit = eventHorizon * (1.35f + ring * 0.32f)
            rectF.set(cx - orbit * 1.7f, cy - orbit * 0.42f, cx + orbit * 1.7f, cy + orbit * 0.42f)
            glowStrokePaint.color = intColorWithAlpha(
                if (ring % 2 == 0) Color.rgb(255, 112, 67) else Color.rgb(124, 77, 255),
                ((185 - ring * 35) * opacity).toInt(),
            )
            canvas.drawOval(rectF, glowStrokePaint)
        }

        // Matter streaks visibly curve inward as the gesture stretches.
        path.reset()
        for (i in 0 until 12) {
            val angle = time * (1.2 + i * 0.03) + i * Math.PI / 6.0
            val outer = eventHorizon * (2.2f + (i % 3) * 0.45f)
            val inner = eventHorizon * (0.75f + pull * 0.35f)
            val startX = cx + cos(angle).toFloat() * outer * 1.5f
            val startY = cy + sin(angle).toFloat() * outer * 0.7f
            val endX = cx + cos(angle + 0.8).toFloat() * inner
            val endY = cy + sin(angle + 0.8).toFloat() * inner
            path.moveTo(startX, startY)
            path.quadTo(cx, cy, endX, endY)
        }
        glowStrokePaint.color = intColorWithAlpha(Color.rgb(255, 213, 79), (150 * opacity).toInt())
        glowStrokePaint.strokeWidth = 4f * animSize
        canvas.drawPath(path, glowStrokePaint)

        sparkPaint.color = Color.WHITE
        for (i in 0 until 10) {
            val angle = time * 2.0 + i * 0.63
            val distance = eventHorizon * (1.15f + ((i * 17) % 100) / 100f * 2.2f)
            sparkPaint.alpha = ((120 + i * 10) * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(
                cx + cos(angle).toFloat() * distance,
                cy + sin(angle).toFloat() * distance * 0.65f,
                (1.5f + i % 3) * animSize,
                sparkPaint,
            )
        }
    }

    private fun drawZipperVoidAnimation(
        canvas: Canvas,
        edge: Edge,
        touchPos: Float,
        w: Float,
        h: Float,
        progress: Float,
    ) {
        val (cx, cy) = center(edge, progress * 260f + 16f, touchPos, w, h)
        val time = System.currentTimeMillis() / 1000.0
        val opening = (24f + progress * 72f) * animSize
        val railLength = (58f + progress * 138f) * animSize
        val metal = Color.rgb(185, 205, 224)

        auraPaint.shader = RadialGradient(
            cx, cy, opening * 2.4f,
            intArrayOf(Color.BLACK, intColorWithAlpha(Color.rgb(12, 22, 42), 230), Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 1f), Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, opening * 2.4f, auraPaint)
        auraPaint.shader = null
        bodyPaint.color = Color.rgb(3, 5, 12)
        bodyPaint.alpha = (235 * opacity).toInt().coerceIn(0, 255)
        canvas.drawOval(RectF(cx - opening * 0.72f, cy - opening * 1.28f, cx + opening * 0.72f, cy + opening * 1.28f), bodyPaint)

        val direction = if (edge == Edge.RIGHT) -1f else 1f
        glowStrokePaint.color = intColorWithAlpha(metal, (210 * opacity).toInt())
        glowStrokePaint.strokeWidth = 7f * animSize
        path.reset()
        when (edge) {
            Edge.LEFT, Edge.RIGHT -> {
                val edgeX = if (edge == Edge.LEFT) cx - railLength else cx + railLength
                path.moveTo(edgeX, cy - opening * 1.8f)
                path.cubicTo(edgeX - direction * railLength * 0.35f, cy - opening, cx - direction * railLength * 0.15f, cy - opening * 0.4f, cx, cy)
                path.cubicTo(cx + direction * railLength * 0.15f, cy + opening * 0.4f, edgeX - direction * railLength * 0.35f, cy + opening, edgeX, cy + opening * 1.8f)
            }
            Edge.BOTTOM -> {
                path.moveTo(cx - opening * 1.8f, cy + railLength)
                path.cubicTo(cx - opening, cy + railLength * 0.35f, cx - opening * 0.4f, cy + railLength * 0.15f, cx, cy)
                path.cubicTo(cx + opening * 0.4f, cy + railLength * 0.15f, cx + opening, cy + railLength * 0.35f, cx + opening * 1.8f, cy + railLength)
            }
        }
        canvas.drawPath(path, glowStrokePaint)
        glowStrokePaint.color = Color.WHITE
        glowStrokePaint.strokeWidth = 2f * animSize
        canvas.drawPath(path, glowStrokePaint)

        for (i in 0 until 16) {
            val t = i / 15f
            val pulse = sin(time * 1.8 + i * 0.42).toFloat() * 2.5f * animSize
            val along = railLength * (1f - t) + opening * 0.7f
            val tooth = 7f * animSize
            glowStrokePaint.color = intColorWithAlpha(if (i % 2 == 0) Color.WHITE else metal, (210 * opacity).toInt())
            glowStrokePaint.strokeWidth = tooth * 1.25f
            when (edge) {
                Edge.LEFT, Edge.RIGHT -> {
                    val x = cx + if (edge == Edge.LEFT) -along else along
                    val y = cy - opening * 1.55f + t * opening * 3.1f + pulse
                    canvas.drawLine(x, y, x + if (edge == Edge.LEFT) tooth else -tooth, y + tooth * 0.7f, glowStrokePaint)
                }
                Edge.BOTTOM -> {
                    val x = cx - opening * 1.55f + t * opening * 3.1f + pulse
                    val y = cy + along
                    canvas.drawLine(x, y, x + tooth * 0.7f, y - tooth, glowStrokePaint)
                }
            }
        }

        sparkPaint.color = Color.rgb(90, 190, 255)
        for (i in 0 until 7) {
            val angle = time * (1.3 + i * 0.08) + i
            val distance = opening * (1.8f - progress * 0.8f) * (0.5f + i / 10f)
            sparkPaint.alpha = (170 * opacity).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx + cos(angle).toFloat() * distance, cy + sin(angle).toFloat() * distance, (1.8f + i % 3) * animSize, sparkPaint)
        }
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

        // A compact luminous core keeps the feedback expressive without turning
        // the action symbol into a cartoon sticker.
        if (armed || holdArmed || isLUp || isLDown || actionSymbol.isNotEmpty()) {
            val auraRadius = if (isLUp || isLDown || holdArmed) 42f else 36f

            // 1. Soft 3D shadow for the icon-friendly glass squircle.
            shadowPaint.color = Color.BLACK
            shadowPaint.alpha = (105 * opacity * alpha).toInt().coerceIn(0, 255)
            rectF.set(
                cx - auraRadius * 1.25f + 5f,
                cy - auraRadius * 1.1f + 7f,
                cx + auraRadius * 1.25f + 5f,
                cy + auraRadius * 1.1f + 7f,
            )
            canvas.drawRoundRect(rectF, auraRadius * 0.42f, auraRadius * 0.42f, shadowPaint)

            val coreColor = when {
                isLUp || isLDown -> lSwipeColor
                holdArmed -> secondaryColor
                else -> baseColor
            }
            auraPaint.shader = RadialGradient(
                cx - auraRadius * 0.28f,
                cy - auraRadius * 0.32f,
                auraRadius * 1.35f,
                intArrayOf(
                    Color.WHITE,
                    intColorWithAlpha(coreColor, (225 * opacity * alpha).toInt()),
                    intColorWithAlpha(coreColor, (125 * opacity * alpha).toInt()),
                ),
                floatArrayOf(0f, 0.28f, 1f),
                Shader.TileMode.CLAMP,
            )
            rectF.set(
                cx - auraRadius * 1.25f,
                cy - auraRadius * 1.1f,
                cx + auraRadius * 1.25f,
                cy + auraRadius * 1.1f,
            )
            canvas.drawRoundRect(rectF, auraRadius * 0.42f, auraRadius * 0.42f, auraPaint)
            auraPaint.shader = null
        }

        iconPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
        iconPaint.strokeWidth = (1.5f * animSize).coerceAtLeast(1.2f)
        iconPaint.setShadowLayer(6f, 0f, 2f, Color.argb(180, 0, 0, 0))

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
            val popSize = if (isLUp || isLDown || holdArmed) 44f else if (armed) 41f else 36f
            iconPaint.textSize = popSize
            val baseline = cy - (iconPaint.ascent() + iconPaint.descent()) / 2f
            canvas.drawText(symbolStr, cx, baseline, iconPaint)
        } else {
            arrowPaint.alpha = (alpha * opacity * 255).toInt().coerceIn(0, 255)
            arrowPaint.strokeWidth = if (armed) 7.5f else 6f
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
