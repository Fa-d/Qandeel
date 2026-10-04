package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.shared.designsystem.Celestial

/** One search field for everything: names, numbers and verse references. */
@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    GlassSurface(modifier.height(52.dp), radius = Celestial.shapes.control) {
        Row(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = Celestial.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Glyph(CelestialIcons.Search, tint = colors.inkMuted, size = 20.dp)
            Spacer(Modifier.width(Celestial.spacing.md))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) BasicText(placeholder, style = Celestial.type.body.copy(color = colors.inkFaint))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = Celestial.type.body.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** A row of choices in a glass capsule; the chosen one is lit gold. */
@Composable
fun <T> PillTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = Celestial.colors
    GlassSurface(modifier.height(44.dp), radius = 22.dp) {
        Row(
            Modifier
                .fillMaxHeight()
                .padding(4.dp)
                .selectableGroup(),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) colors.accent else colors.accent.copy(alpha = 0f))
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(option) }),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        label(option),
                        style = Celestial.type.label.copy(color = if (isSelected) colors.onAccent else colors.inkMuted),
                    )
                }
            }
        }
    }
}
