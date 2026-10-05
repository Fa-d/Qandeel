package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.shared.designsystem.Celestial

/** An icon in [tint] (the ink by default), decorative unless [description] names it. */
@Composable
fun Glyph(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = Celestial.colors.ink,
    size: Dp = 24.dp,
    description: String? = null,
) {
    Image(
        imageVector = icon,
        contentDescription = description,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size),
    )
}

/** A round icon button with a full touch target; the glyph's [label] is what a screen reader says. */
@Composable
fun GlyphButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Celestial.colors.ink,
) {
    Box(
        modifier
            .size(Celestial.shapes.touchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Glyph(icon, tint = tint)
    }
}

/** The gold play/pause disc: the one solid, lit control on a screen. */
@Composable
fun PlayButton(playing: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 52.dp) {
    val colors = Celestial.colors
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(colors.accentBright, colors.accent)))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Glyph(if (playing) CelestialIcons.Pause else CelestialIcons.Play, tint = colors.onAccent, size = size * 0.46f)
    }
}

/** Small capitals over a section. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Celestial.colors.accent) {
    BasicText(text.uppercase(), modifier, style = Celestial.type.eyebrow.copy(color = color))
}

/** A thin gold line filled [fraction] of the way, on a faint track. */
@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Box(
        modifier.drawBehind {
            val radius = CornerRadius(size.height / 2f)
            drawRoundRect(colors.inkFaint.copy(alpha = 0.25f), cornerRadius = radius)
            drawRoundRect(
                Brush.horizontalGradient(listOf(colors.accent, colors.accentBright)),
                size = size.copy(width = size.width * fraction.coerceIn(0f, 1f)),
                cornerRadius = radius,
            )
        },
    )
}
