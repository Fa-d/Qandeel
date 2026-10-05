package dev.sadakat.qandeel.shared.designsystem.lamp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A point in the lantern's model space: y points down, the lantern's widest ring has radius 1. */
internal data class Vec3(val x: Float, val y: Float, val z: Float)

/** The rub el hizb (two squares turned 45° to each other), for the badges drawn in its shape. */
internal object LampGeometry {

    /** Where two edges of the squares cross: cos 45° / cos 22.5° of the points' radius. */
    val INNER_RADIUS = (cos(PI / 4) / cos(PI / 8)).toFloat()

    /**
     * The star's outline: its 8 points (radius 1) alternating with the 8 corners where the squares'
     * edges cross, from the top point clockwise on screen.
     */
    val outline: List<Vec3> = List(16) { i ->
        val angle = i * PI / 8 - PI / 2
        val radius = if (i % 2 == 0) 1f else INNER_RADIUS
        Vec3((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat(), 0f)
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

/** Perspective scale of a point at depth [z] for a camera [distance] radii in front of the lantern. */
internal fun perspective(z: Float, distance: Float): Float = distance / (distance + z)
