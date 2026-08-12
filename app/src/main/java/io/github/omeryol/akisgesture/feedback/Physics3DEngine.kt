package io.github.omeryol.akisgesture.feedback

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/** 3D Perspective Projection, Blinn-Phong Lighting, Gravity Physics, and Drop Shadow Engine. */
object Physics3DEngine {

    data class Point3D(val x: Float, val y: Float, val z: Float)
    data class Point2D(val x: Float, val y: Float, val scale: Float)

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 0, 0, 0)
        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
    }

    // Normalized light vector pointing from top-left front (0.3, -0.8, 1.2)
    private const val LX = 0.228f
    private const val LY = -0.609f
    private const val LZ = 0.759f

    /** Projects a 3D coordinate (x, y, z) to a 2D screen coordinate with scale factor. */
    fun project(x: Float, y: Float, z: Float, focalLength: Float = 320f): Point2D {
        val zClamped = max(-200f, min(600f, z))
        val scale = focalLength / (focalLength + zClamped)
        return Point2D(x * scale, y * scale, scale)
    }

    /** Computes Blinn-Phong specular light intensity (0.0 to 1.0) given a surface normal vector (nx, ny, nz). */
    fun computeSpecularLight(nx: Float, ny: Float, nz: Float, shininess: Float = 16f): Float {
        // Normal vector normalization
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)
        val normX = nx / len
        val normY = ny / len
        val normZ = nz / len

        // Diffuse N dot L
        val nDotL = max(0f, normX * LX + normY * LY + normZ * LZ)

        // View vector V = (0, 0, 1), Halfway vector H = (L + V) / |L + V|
        val hx = LX
        val hy = LY
        val hz = LZ + 1f
        val hLen = sqrt(hx * hx + hy * hy + hz * hz).coerceAtLeast(0.001f)
        val nDotH = max(0f, normX * (hx / hLen) + normY * (hy / hLen) + normZ * (hz / hLen))

        val specular = nDotH.pow(shininess)
        return (0.35f + 0.45f * nDotL + 0.40f * specular).coerceIn(0f, 1f)
    }

    /** Renders a soft offset 3D drop shadow onto canvas for a shape path. */
    fun drawDropShadow(canvas: Canvas, path: Path, dx: Float = 6f, dy: Float = 10f, opacity: Float = 0.5f) {
        shadowPaint.color = Color.argb((110 * opacity).toInt().coerceIn(0, 255), 0, 0, 0)
        canvas.save()
        canvas.translate(dx, dy)
        canvas.drawPath(path, shadowPaint)
        canvas.restore()
    }
}
