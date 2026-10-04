package dev.sadakat.qandeel.shared.designsystem.lamp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A point or direction in the lamp's model space (the star's points at radius 1, y down). */
internal data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    fun normalized(): Vec3 {
        val length = sqrt(dot(this))
        return if (length == 0f) this else this * (1f / length)
    }
}

/** One flat face of the prism: its corners and its outward normal, in model space. */
internal class Face(val corners: List<Vec3>, val normal: Vec3)

/**
 * The lamp's glass: the rub el hizb (two squares turned 45° to each other) extruded into a prism.
 * The outline alternates the star's 8 points (radius 1) with the 8 corners where the squares'
 * edges cross (radius [INNER_RADIUS]), starting from the top point and going clockwise on screen.
 */
internal object LampGeometry {

    /** Where two edges of the squares cross: cos 45° / cos 22.5° of the points' radius. */
    val INNER_RADIUS = (cos(PI / 4) / cos(PI / 8)).toFloat()

    /** The star's outline in the z = 0 plane. */
    val outline: List<Vec3> = List(16) { i ->
        val angle = i * PI / 8 - PI / 2
        val radius = if (i % 2 == 0) 1f else INNER_RADIUS
        Vec3((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat(), 0f)
    }

    /** The prism of [outline] extruded [depth] each way: front, back, and the 16 sides. */
    fun prism(depth: Float): List<Face> {
        val front = outline.map { it.copy(z = -depth) }
        val back = outline.map { it.copy(z = depth) }
        val sides = outline.indices.map { i ->
            val j = (i + 1) % outline.size
            val a = outline[i]
            val b = outline[j]
            // The outline runs clockwise on screen (y down), so (dy, -dx) points outward.
            val normal = Vec3(b.y - a.y, -(b.x - a.x), 0f).normalized()
            Face(listOf(front[i], front[j], back[j], back[i]), normal)
        }
        return sides + Face(front, Vec3(0f, 0f, -1f)) + Face(back.reversed(), Vec3(0f, 0f, 1f))
    }
}

/**
 * Turns model space towards the viewer: [yaw] about the vertical axis, then [pitch] about the
 * horizontal one (radians). The camera looks along +z, so a larger z is farther away.
 */
internal class LampRotation(yaw: Float, pitch: Float) {
    private val cy = cos(yaw)
    private val sy = sin(yaw)
    private val cp = cos(pitch)
    private val sp = sin(pitch)

    fun apply(v: Vec3): Vec3 {
        val x1 = v.x * cy + v.z * sy
        val z1 = -v.x * sy + v.z * cy
        return Vec3(x1, v.y * cp - z1 * sp, v.y * sp + z1 * cp)
    }
}

/** Perspective scale of a point at depth [z] for a camera [distance] radii in front of the lamp. */
internal fun perspective(z: Float, distance: Float): Float = distance / (distance + z)
