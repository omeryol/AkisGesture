package io.github.omeryol.akisgesture.feedback.animation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ParticleBurstModule {
    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var alpha: Float,
        var color: Int,
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lastTimeMs: Long = 0L

    fun trigger(cx: Float, cy: Float, primaryColor: Int) {
        particles.clear()
        val count = 14
        val random = java.util.Random()

        for (i in 0 until count) {
            val angle = random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = 120f + random.nextFloat() * 240f // px/sec
            val vx = Math.cos(angle.toDouble()).toFloat() * speed
            val vy = Math.sin(angle.toDouble()).toFloat() * speed
            val radius = 4f + random.nextFloat() * 7f
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = vx,
                    vy = vy,
                    radius = radius,
                    alpha = 1f,
                    color = primaryColor,
                ),
            )
        }
        lastTimeMs = System.currentTimeMillis()
    }

    fun draw(canvas: Canvas, currentTimeMs: Long) {
        if (particles.isEmpty()) return
        val dt = if (lastTimeMs > 0L) ((currentTimeMs - lastTimeMs) / 1000f).coerceIn(0.001f, 0.05f) else 0.016f
        lastTimeMs = currentTimeMs

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha -= dt * 3.5f // fades in ~280ms
            p.radius *= 0.96f

            if (p.alpha <= 0f || p.radius <= 0.5f) {
                iterator.remove()
            } else {
                val alphaInt = (p.alpha.coerceIn(0f, 1f) * 255).toInt()
                paint.color = Color.argb(
                    alphaInt,
                    Color.red(p.color),
                    Color.green(p.color),
                    Color.blue(p.color),
                )
                canvas.drawCircle(p.x, p.y, p.radius, paint)
            }
        }
    }

    val isActive: Boolean get() = particles.isNotEmpty()
}
