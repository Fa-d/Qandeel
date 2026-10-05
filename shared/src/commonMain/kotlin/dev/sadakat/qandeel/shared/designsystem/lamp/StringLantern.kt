package dev.sadakat.qandeel.shared.designsystem.lamp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** One string of light: its points in model space, and how brightly it shines (0..1). */
internal class LightString(val points: List<Vec3>, val brightness: Float)

/**
 * The lantern as strings of light, at [time] seconds burning with [energy] (0..1).
 *
 * Its rings are stacked down a qandeel's profile, narrow at the top and bottom and full in the
 * middle, and each ring's cross-section is the rub el hizb softened into an eight-lobed flower.
 * Over time the lobes deepen and relax (star to circle and back), the stack twists as one, and each
 * ring vibrates in its own standing wave, faster as the recitation brightens it. Eight ribs follow
 * the lobes' tips from top to bottom, so the rings read as one woven lantern.
 */
internal object StringLantern {
    const val RINGS = 6
    const val RIBS = 8
    private const val RING_POINTS = 144
    private const val RIB_POINTS = 48
    private const val HALF_HEIGHT = 1.05f
    private const val LOBES = 8
    private const val ORBITS = 2
    private const val ORBIT_POINTS = 160

    /** How deep the star's lobes are now: from almost a circle to a clear star. */
    fun lobeDepth(time: Float): Float = 0.15f + 0.07f * sin(time * 0.45f)

    /** How far the stack has twisted, top to bottom, in radians. */
    fun twist(time: Float): Float = 0.6f * sin(time * 0.31f)

    /** The lantern's radius at [u], 0 at the top to 1 at the bottom: a hanging lamp's swell. */
    fun profile(u: Float): Float = 0.16f + 0.84f * sin(PI.toFloat() * u).pow(0.85f)

    fun strings(time: Float, energy: Float): List<LightString> = rings(time, energy) + ribs(time) + orbits(time, energy)

    private fun rings(time: Float, energy: Float): List<LightString> {
        val depth = lobeDepth(time)
        val twist = twist(time)
        val wave = 0.005f + 0.028f * energy
        return List(RINGS) { i ->
            val u = (i + 0.5f) / RINGS
            val radius = profile(u)
            val y = (u - 0.5f) * 2f * HALF_HEIGHT
            val turn = u * twist
            // Each ring sings its own note: an odd number of waves, travelling at its own speed.
            val mode = 3 + (i % 3) * 2
            val speed = 1.4f + 0.5f * (i % 3) + 2.5f * energy
            val points = List(RING_POINTS + 1) { k ->
                val theta = k * 2f * PI.toFloat() / RING_POINTS
                val lobe = 1f + depth * cos(LOBES * (theta + turn))
                val vibration = wave * sin(mode * theta + time * speed + i * 1.7f)
                val r = radius * (lobe + vibration)
                Vec3(r * cos(theta), y, r * sin(theta))
            }
            LightString(points, brightness = 0.45f + 0.55f * sin(PI.toFloat() * u))
        }
    }

    private fun ribs(time: Float): List<LightString> {
        val depth = lobeDepth(time)
        val twist = twist(time)
        return List(RIBS) { j ->
            val base = j * 2f * PI.toFloat() / RIBS
            val points = List(RIB_POINTS + 1) { k ->
                val u = k.toFloat() / RIB_POINTS
                val theta = base - u * twist
                val r = profile(u) * (1f + depth)
                Vec3(r * cos(theta), (u - 0.5f) * 2f * HALF_HEIGHT, r * sin(theta))
            }
            LightString(points, brightness = 0.26f)
        }
    }

    /**
     * Two closed strings circling the lantern on tilted orbits, each vibrating along its length as
     * it turns: they pass behind the lantern and in front of it, which gives it depth.
     */
    private fun orbits(time: Float, energy: Float): List<LightString> = List(ORBITS) { o ->
        val tilt = 0.9f + o * 0.7f
        val spin = time * (0.35f + 0.15f * o) + o * 2f
        val radius = 1.45f + 0.08f * o
        val mode = 5 + o * 2
        val ct = cos(tilt)
        val st = sin(tilt)
        val points = List(ORBIT_POINTS + 1) { k ->
            val theta = k * 2f * PI.toFloat() / ORBIT_POINTS + spin
            val r = radius * (1f + (0.02f + 0.03f * energy) * sin(mode * theta - time * 2.2f))
            val x = r * cos(theta)
            val z = r * sin(theta)
            // Tilt the loop about the x axis, so it rises in front and dips behind.
            Vec3(x, z * st, z * ct)
        }
        LightString(points, brightness = 0.34f)
    }

    /** Where bead [index] of [count] is on its ring, as it travels round at a pace set by [energy]. */
    fun bead(index: Int, count: Int, time: Float, energy: Float): Vec3 {
        val ring = (index * 3 + 2) % RINGS
        val u = (ring + 0.5f) / RINGS
        val direction = if (index % 2 == 0) 1f else -1f
        val theta = index * 2f * PI.toFloat() / count + direction * time * (0.5f + 1.6f * energy)
        val r = profile(u) * (1f + lobeDepth(time) * cos(LOBES * (theta + u * twist(time))))
        return Vec3(r * cos(theta), (u - 0.5f) * 2f * HALF_HEIGHT, r * sin(theta))
    }
}
