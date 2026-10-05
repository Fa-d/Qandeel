package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import dev.sadakat.qandeel.shared.designsystem.Celestial

/**
 * A pane of glass over the sky: a translucent fill, lit along its top edge and fading to a faint
 * rim below, the way light catches glass held under a lamp. [strong] is for what floats above
 * other glass (the player, the tab bar).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    radius: Dp = Celestial.shapes.card,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = Celestial.colors
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .clip(shape)
            .background(if (strong) colors.glassStrong else colors.glass)
            .border(
                width = Celestial.shapes.hairline,
                brush = Brush.verticalGradient(listOf(colors.glassHighlight, colors.glassEdge)),
                shape = shape,
            ),
        content = content,
    )
}
