package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.lamp.LampGeometry

/**
 * A surah's number inside the rub el hizb, the lamp's star in miniature. With [progress] (0..1)
 * the star's outline is traced in gold that far round, the way the mini player shows how much of
 * the surah has played.
 */
@Composable
fun OctagramBadge(number: Int, modifier: Modifier = Modifier, size: Dp = 40.dp, progress: Float? = null) {
    val colors = Celestial.colors
    Box(
        modifier
            .size(size)
            .drawBehind { drawOctagram(colors.inkFaint.copy(alpha = 0.6f), colors.accent, progress) },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = number.toString(),
            style = Celestial.type.label.copy(color = colors.ink, textAlign = TextAlign.Center),
        )
    }
}

private fun DrawScope.drawOctagram(track: Color, gold: Color, progress: Float?) {
    val stroke = 1.4.dp.toPx()
    val radius = size.minDimension / 2f - stroke
    val path = octagramPath(center, radius)
    drawPath(path, track, style = Stroke(stroke))
    if (progress != null && progress > 0f) {
        drawPath(
            octagramPath(center, radius, upTo = progress.coerceIn(0f, 1f)),
            gold,
            style = Stroke(stroke * 1.6f, cap = StrokeCap.Round),
        )
    } else if (progress == null) {
        drawPath(path, gold.copy(alpha = 0.55f), style = Stroke(stroke))
    }
}

/** The star's outline around [center], from its top point clockwise, stopped [upTo] of the way. */
internal fun octagramPath(center: Offset, radius: Float, upTo: Float = 1f): Path {
    val points = LampGeometry.outline.map { Offset(center.x + it.x * radius, center.y + it.y * radius) }
    val ring = points + points.first()
    val lengths = ring.zipWithNext { a, b -> (b - a).getDistance() }
    var remaining = lengths.sum() * upTo
    return Path().apply {
        moveTo(ring[0].x, ring[0].y)
        for (i in lengths.indices) {
            if (remaining <= 0f) break
            val step = minOf(1f, remaining / lengths[i])
            val end = ring[i] + (ring[i + 1] - ring[i]) * step
            lineTo(end.x, end.y)
            remaining -= lengths[i]
        }
        if (upTo >= 1f) close()
    }
}
