package dev.sadakat.qandeel.shared.designsystem.lamp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** The lamp's colours: its glass and metal, the light it gives, and its flame. */
@Immutable
data class LampColors(
    val glass: Color,
    val edge: Color,
    val chain: Color,
    val halo: Color,
    val flameCore: Color,
    val flameBody: Color,
    val flameTip: Color,
)

/**
 * Where the lamp is in its motion: [time] in seconds drives the turn, the sway and the flicker;
 * [energy] (0..1) is how brightly it burns, which a recitation raises on every word; [tilt] (-1..1)
 * leans it towards or away from the viewer, as the page scrolls.
 */
@Immutable
data class LampMotion(val time: Float = 0f, val energy: Float = 0f, val tilt: Float = 0f)

/**
 * Qandeel's lamp: the rub el hizb as a glass prism, hung from a chain, turning slowly with a flame
 * at its heart. Drawn in 3D on a Canvas, every frame from [motion], which it reads only while
 * drawing: the lamp redraws as it moves but never recomposes.
 */
@Composable
fun QandeelLamp(motion: () -> LampMotion, colors: LampColors, modifier: Modifier = Modifier) {
    Spacer(modifier.drawBehind { drawLamp(motion(), colors) })
}

private val glassFaces = LampGeometry.prism(depth = 0.24f)
private const val CAMERA_DISTANCE = 4.2f

/** Light falls from the upper left, in front: the faces turned to it shine. */
private val keyLight = Vec3(-0.45f, -0.6f, -1f).normalized()

internal fun DrawScope.drawLamp(motion: LampMotion, colors: LampColors) {
    val t = motion.time
    val radius = min(size.width * 0.34f, size.height * 0.3f)
    val center = Offset(size.width / 2f, size.height * 0.56f)
    val anchor = Offset(size.width / 2f, 0f)
    val burn = 0.75f + 0.25f * motion.energy

    drawHalo(center, radius, colors.halo, burn)
    // The whole lamp swings a little on its chain, about the point it hangs from.
    rotate(degrees = sin(t * 0.7f) * 2.2f, pivot = anchor) {
        // It turns back and forth rather than round, so the star always faces the viewer.
        val rotation = LampRotation(
            yaw = sin(t * 0.4f) * 0.55f + sin(t * 0.17f) * 0.2f,
            pitch = 0.16f + motion.tilt * 0.22f,
        )
        val top = project(LampGeometry.outline.first(), rotation, center, radius)
        val bottom = project(LampGeometry.outline[LampGeometry.outline.size / 2], rotation, center, radius)
        drawChain(anchor, top.y, radius, colors.chain)
        drawRays(center, radius, t, colors.halo, burn)
        val projected = glassFaces.map { face -> ProjectedFace(face, rotation, center, radius) }
        // Painter's order, far to near; the flame sits between the faces turned away and the rest.
        val (away, toward) = projected.sortedByDescending { it.depth }.partition { it.normal.z > 0f }
        away.forEach { drawFace(it, colors) }
        drawFlame(center, radius, t, motion.energy, colors)
        toward.forEach { drawFace(it, colors) }
        drawFinials(top, bottom, radius, colors)
    }
}

/** A face turned and projected: its screen outline, its depth and its turned normal. */
private class ProjectedFace(face: Face, rotation: LampRotation, center: Offset, radius: Float) {
    val normal = rotation.apply(face.normal)
    val depth: Float
    val path = Path()

    init {
        var depthSum = 0f
        face.corners.forEachIndexed { i, corner ->
            val p = rotation.apply(corner)
            depthSum += p.z
            val scale = perspective(p.z, CAMERA_DISTANCE) * radius
            val x = center.x + p.x * scale
            val y = center.y + p.y * scale
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        depth = depthSum / face.corners.size
    }
}

private fun DrawScope.drawFace(face: ProjectedFace, colors: LampColors) {
    val lit = max(0f, face.normal.dot(keyLight))
    // Glass seen edge-on looks denser (a rim); seen face-on it is clearer.
    val rim = 1f - abs(face.normal.z)
    val alpha = (0.1f + 0.3f * lit + 0.22f * rim).coerceAtMost(0.75f)
    drawPath(face.path, colors.glass.copy(alpha = colors.glass.alpha * alpha))
    drawPath(
        face.path,
        colors.edge.copy(alpha = colors.edge.alpha * (0.35f + 0.55f * lit)),
        style = Stroke(width = 1.2f * density, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawHalo(center: Offset, radius: Float, halo: Color, burn: Float) {
    val reach = radius * (1.9f + 0.3f * burn)
    drawCircle(
        Brush.radialGradient(
            0f to halo.copy(alpha = halo.alpha * 0.45f * burn),
            0.3f to halo.copy(alpha = halo.alpha * 0.12f * burn),
            1f to Color.Transparent,
            center = center,
            radius = reach,
        ),
        radius = reach,
        center = center,
    )
}

/** Faint shafts of light from the heart through the star's eight points, turning with it. */
private fun DrawScope.drawRays(center: Offset, radius: Float, t: Float, halo: Color, burn: Float) {
    val length = radius * 2.6f
    val spin = t * 0.32f
    repeat(8) { k ->
        val angle = spin + k * PI.toFloat() / 4f
        val shimmer = 0.6f + 0.4f * sin(t * 1.3f + k * 1.7f)
        rotate(degrees = angle * 180f / PI.toFloat(), pivot = center) {
            val ray = Path().apply {
                moveTo(center.x, center.y - radius * 0.05f)
                lineTo(center.x + length, center.y - radius * 0.11f)
                lineTo(center.x + length, center.y + radius * 0.11f)
                lineTo(center.x, center.y + radius * 0.05f)
                close()
            }
            drawPath(
                ray,
                Brush.horizontalGradient(
                    0f to halo.copy(alpha = halo.alpha * 0.16f * burn * shimmer),
                    1f to Color.Transparent,
                    startX = center.x,
                    endX = center.x + length,
                ),
            )
        }
    }
}

private fun DrawScope.drawChain(anchor: Offset, bottom: Float, radius: Float, chain: Color) {
    val link = radius * 0.12f
    var y = anchor.y
    var i = 0
    while (y < bottom) {
        val upright = i % 2 == 0
        val size = if (upright) Size(link * 0.55f, link) else Size(link * 0.22f, link)
        drawRoundRect(
            color = chain,
            topLeft = Offset(anchor.x - size.width / 2f, y),
            size = size,
            cornerRadius = CornerRadius(size.width / 2f),
            style = Stroke(width = 1.4f * density),
        )
        y += link * 0.8f
        i++
    }
}

/** A model-space point on screen. */
private fun project(point: Vec3, rotation: LampRotation, center: Offset, radius: Float): Offset {
    val p = rotation.apply(point)
    val scale = perspective(p.z, CAMERA_DISTANCE) * radius
    return Offset(center.x + p.x * scale, center.y + p.y * scale)
}

/** The cap the chain holds at the [top] point, and the drop under the [bottom] one. */
private fun DrawScope.drawFinials(top: Offset, bottom: Offset, radius: Float, colors: LampColors) {
    drawCircle(colors.edge, radius = radius * 0.06f, center = top)
    val tip = bottom
    val drop = Path().apply {
        moveTo(tip.x, tip.y)
        cubicTo(
            tip.x + radius * 0.07f,
            tip.y + radius * 0.08f,
            tip.x + radius * 0.03f,
            tip.y + radius * 0.2f,
            tip.x,
            tip.y + radius * 0.24f,
        )
        cubicTo(
            tip.x - radius * 0.03f,
            tip.y + radius * 0.2f,
            tip.x - radius * 0.07f,
            tip.y + radius * 0.08f,
            tip.x,
            tip.y,
        )
        close()
    }
    drawPath(drop, colors.edge)
}

/** A teardrop flame that flickers, leans with the swing, and stands taller as it burns brighter. */
private fun DrawScope.drawFlame(center: Offset, radius: Float, t: Float, energy: Float, colors: LampColors) {
    val flicker = 1f + 0.07f * sin(t * 9.1f) + 0.05f * sin(t * 13.7f + 1.3f) + 0.18f * energy
    val height = radius * 0.62f * flicker
    val width = radius * 0.2f * (1f + 0.08f * energy)
    val lean = sin(t * 2.3f) * width * 0.35f
    val base = Offset(center.x, center.y + height * 0.32f)

    drawCircle(
        Brush.radialGradient(
            0f to colors.flameBody.copy(alpha = 0.55f + 0.3f * energy),
            1f to Color.Transparent,
            center = center,
            radius = radius * 0.7f,
        ),
        radius = radius * 0.7f,
        center = center,
    )
    drawPath(teardrop(base, width, height, lean), flameBrush(base, height, colors))
    drawPath(teardrop(base, width * 0.45f, height * 0.5f, lean * 0.5f), colors.flameCore)
}

private fun teardrop(base: Offset, width: Float, height: Float, lean: Float) = Path().apply {
    val tip = Offset(base.x + lean, base.y - height)
    moveTo(tip.x, tip.y)
    cubicTo(
        base.x + width * 0.25f,
        base.y - height * 0.55f,
        base.x + width,
        base.y - height * 0.2f,
        base.x + width * 0.8f,
        base.y - width * 0.1f,
    )
    cubicTo(
        base.x + width * 0.55f,
        base.y + width * 0.75f,
        base.x - width * 0.55f,
        base.y + width * 0.75f,
        base.x - width * 0.8f,
        base.y - width * 0.1f,
    )
    cubicTo(base.x - width, base.y - height * 0.2f, base.x - width * 0.25f, base.y - height * 0.55f, tip.x, tip.y)
    close()
}

private fun flameBrush(base: Offset, height: Float, colors: LampColors) = Brush.verticalGradient(
    0f to colors.flameTip.copy(alpha = 0f),
    0.35f to colors.flameTip,
    0.7f to colors.flameBody,
    1f to colors.flameCore,
    startY = base.y - height,
    endY = base.y + height * 0.12f,
)
