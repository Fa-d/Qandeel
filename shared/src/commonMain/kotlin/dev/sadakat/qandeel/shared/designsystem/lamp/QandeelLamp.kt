package dev.sadakat.qandeel.shared.designsystem.lamp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.min
import kotlin.math.sin

/**
 * The lantern's light: [string] for the strings facing the viewer, [stringFar] for those behind,
 * [core] the heart of the light, [glow] the warmth around it and the pool it casts below, [spark]
 * the beads travelling the strings. [additive] adds light to the sky (night); otherwise it is
 * painted over it (dawn, where adding light to a pale sky would only wash it out).
 */
@Immutable
data class LampColors(
    val string: Color,
    val stringFar: Color,
    val core: Color,
    val glow: Color,
    val spark: Color,
    val additive: Boolean,
)

/**
 * Where the lantern is in its motion: [time] in seconds drives every movement; [energy] (0..1) is
 * how brightly it burns, which a recitation raises on every word; [tilt] (-1..1) leans it towards
 * or away from the viewer, as the page scrolls.
 */
@Immutable
data class LampMotion(val time: Float = 0f, val energy: Float = 0f, val tilt: Float = 0f)

/**
 * Qandeel's lamp: a lantern woven from strings of light around a glowing heart, floating free.
 * Its star-shaped rings breathe between star and circle, twist, and vibrate like plucked strings
 * ([StringLantern]); beads of light run along them, faster as the recitation goes on.
 *
 * Drawn in 3D on a Canvas every frame from [motion], read only while drawing: the lantern redraws
 * as it moves but never recomposes.
 */
@Composable
fun QandeelLamp(motion: () -> LampMotion, colors: LampColors, modifier: Modifier = Modifier) {
    Spacer(modifier.drawBehind { drawLamp(motion(), colors) })
}

private const val CAMERA_DISTANCE = 5f
private const val BEADS = 12

internal fun DrawScope.drawLamp(motion: LampMotion, colors: LampColors) {
    val t = motion.time
    val energy = motion.energy.coerceIn(0f, 1f)
    val unit = min(size.width * 0.27f, size.height * 0.28f)
    // It floats: a slow bob, and a slower lean, as if on warm air.
    val center = Offset(size.width / 2f, size.height * 0.46f + sin(t * 0.8f) * unit * 0.06f)
    val rotation = LampRotation(
        yaw = t * 0.22f,
        pitch = 0.5f + sin(t * 0.27f) * 0.07f + motion.tilt * 0.2f,
    )
    val blend = if (colors.additive) BlendMode.Plus else BlendMode.SrcOver
    val project = { v: Vec3 -> projected(v, rotation, center, unit) }

    drawPool(center, unit, colors.glow, energy)
    // The heart breathes, and swells with each recited word.
    drawHeart(center, unit, breath = 1f + 0.06f * sin(t * 1.9f) + 0.18f * energy, colors, blend)
    StringLantern.strings(t, energy).forEach { string -> drawString(string, project, unit, colors, blend) }
    drawBeads(t, energy, project, colors, blend)
}

/** A point of model space on screen, with its depth (positive is behind the lantern's centre). */
private class Projected(val offset: Offset, val depth: Float)

private fun projected(v: Vec3, rotation: LampRotation, center: Offset, unit: Float): Projected {
    val p = rotation.apply(v)
    val scale = perspective(p.z, CAMERA_DISTANCE) * unit
    return Projected(Offset(center.x + p.x * scale, center.y + p.y * scale), p.z)
}

/**
 * One string, in two passes: the stretch behind the lantern's middle, faint and thin, then the
 * stretch in front, bright. Each is drawn as a wide soft glow, a narrower halo, and a fine core,
 * which is what makes a line read as light rather than as wire.
 */
private fun DrawScope.drawString(
    string: LightString,
    project: (Vec3) -> Projected,
    unit: Float,
    colors: LampColors,
    blend: BlendMode,
) {
    val near = Path()
    val far = Path()
    var previous: Projected? = null
    string.points.forEach { point ->
        val p = project(point)
        val last = previous
        if (last != null) {
            val path = if ((last.depth + p.depth) / 2f < 0f) near else far
            path.moveTo(last.offset.x, last.offset.y)
            path.lineTo(p.offset.x, p.offset.y)
        }
        previous = p
    }
    val width = unit / 100f
    glowStroke(far, colors.stringFar, string.brightness * 0.45f, width * 0.7f, blend)
    glowStroke(near, colors.string, string.brightness, width, blend)
}

private fun DrawScope.glowStroke(path: Path, color: Color, brightness: Float, width: Float, blend: BlendMode) {
    listOf(7f to 0.06f, 3f to 0.16f, 1.1f to 0.85f).forEach { (thickness, alpha) ->
        drawPath(
            path,
            color.copy(alpha = color.alpha * alpha * brightness),
            style = Stroke(width = width * thickness, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = blend,
        )
    }
}

/** The heart of the light: a soft orb, [breath] times its resting size, in a wide warm halo. */
private fun DrawScope.drawHeart(center: Offset, unit: Float, breath: Float, colors: LampColors, blend: BlendMode) {
    val halo = unit * 2.3f * breath
    drawCircle(
        Brush.radialGradient(
            0f to colors.glow.copy(alpha = colors.glow.alpha * 0.32f),
            0.4f to colors.glow.copy(alpha = colors.glow.alpha * 0.1f),
            1f to Color.Transparent,
            center = center,
            radius = halo,
        ),
        radius = halo,
        center = center,
        blendMode = blend,
    )
    val orb = unit * 0.42f * breath
    drawCircle(
        Brush.radialGradient(
            0f to colors.core,
            0.25f to colors.core.copy(alpha = 0.85f),
            0.6f to colors.glow.copy(alpha = colors.glow.alpha * 0.45f),
            1f to Color.Transparent,
            center = center,
            radius = orb,
        ),
        radius = orb,
        center = center,
        blendMode = blend,
    )
}

/** The pool of light it casts below, which is what makes it look as if it floats. */
private fun DrawScope.drawPool(center: Offset, unit: Float, glow: Color, energy: Float) {
    val pool = Offset(center.x, center.y + unit * 1.6f)
    val radius = unit * 1.5f
    scale(scaleX = 1f, scaleY = 0.16f, pivot = pool) {
        drawCircle(
            Brush.radialGradient(
                0f to glow.copy(alpha = glow.alpha * (0.35f + 0.2f * energy)),
                1f to Color.Transparent,
                center = pool,
                radius = radius,
            ),
            radius = radius,
            center = pool,
        )
    }
}

/** Beads of light running along the rings, the strings' energy made visible. */
private fun DrawScope.drawBeads(
    t: Float,
    energy: Float,
    project: (Vec3) -> Projected,
    colors: LampColors,
    blend: BlendMode,
) {
    val size = this.size.minDimension / 140f
    repeat(BEADS) { i ->
        val p = project(StringLantern.bead(i, BEADS, t, energy))
        val behind = p.depth > 0f
        val strength = if (behind) 0.3f else 1f
        val glowRadius = size * 4f
        drawCircle(
            Brush.radialGradient(
                0f to colors.spark.copy(alpha = 0.7f * strength),
                1f to Color.Transparent,
                center = p.offset,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = p.offset,
            blendMode = blend,
        )
        drawCircle(colors.core.copy(alpha = strength), radius = size * 0.8f, center = p.offset, blendMode = blend)
    }
}
