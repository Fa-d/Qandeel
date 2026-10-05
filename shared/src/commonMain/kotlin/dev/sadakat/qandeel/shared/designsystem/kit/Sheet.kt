package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.sadakat.qandeel.shared.designsystem.Celestial

/**
 * A sheet of glass rising from the bottom over everything, for a few choices or actions. Tapping
 * outside or going back dismisses it. Built on a dialog, so it is modal the same way on every
 * platform.
 */
@Composable
fun CelestialSheet(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            GlassSurface(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Celestial.spacing.sm)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = Celestial.spacing.sm)
                    // Taps on the sheet stay on it.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                strong = true,
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Celestial.spacing.gutter)
                        .padding(bottom = Celestial.spacing.lg),
                ) {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = Celestial.spacing.md),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(width = 36.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(Celestial.colors.inkFaint.copy(alpha = 0.5f)),
                        )
                    }
                    content()
                    Spacer(Modifier.height(Celestial.spacing.sm))
                }
            }
        }
    }
}

/** One action in a sheet: an optional icon, its label, and what tapping it does. */
@Composable
fun SheetAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val colors = Celestial.colors
    androidx.compose.foundation.layout.Row(
        modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(Celestial.shapes.control))
            .clickable(onClick = onClick)
            .padding(horizontal = Celestial.spacing.md, vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.text.BasicText(
            label,
            style = Celestial.type.title.copy(color = if (selected) colors.accent else colors.ink),
            modifier = Modifier.weight(1f),
        )
        if (selected) Glyph(CelestialIcons.Check, tint = colors.accent, size = 20.dp)
    }
}
