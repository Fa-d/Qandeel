package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.qandeel.shared.designsystem.Celestial

/** The one gold call to action on a screen. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Box(
        modifier
            .heightIn(min = 52.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(colors.accentBright, colors.accent)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Celestial.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = Celestial.type.title.copy(color = colors.onAccent, textAlign = TextAlign.Center))
    }
}

/** A quiet text action beside the primary one (skip, back). */
@Composable
fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = Celestial.shapes.touchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Celestial.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = Celestial.type.label.copy(color = Celestial.colors.inkMuted))
    }
}

/**
 * One choice of several, as a pane of glass: lit gold at the edge, with its star filled, when
 * [selected]. [trailing] holds a secondary control (a sample's play button).
 */
@Composable
fun ChoiceCard(
    title: String,
    detail: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = Celestial.colors
    val edge by animateColorAsState(if (selected) colors.accent else colors.accent.copy(alpha = 0f))
    val shape = RoundedCornerShape(Celestial.shapes.tile)
    GlassSurface(modifier.border(1.5.dp, edge, shape), radius = Celestial.shapes.tile) {
        Row(
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
                .padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StarMark(selected)
            Spacer(Modifier.width(Celestial.spacing.md))
            Column(Modifier.weight(1f)) {
                BasicText(title, style = Celestial.type.title.copy(color = colors.ink))
                BasicText(detail, style = Celestial.type.caption.copy(color = colors.inkMuted))
            }
            trailing()
        }
    }
}

/** The radio mark: the rub el hizb, outlined, or filled gold when chosen. */
@Composable
private fun StarMark(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Box(
        modifier
            .size(22.dp)
            .drawBehind {
                val radius = size.minDimension / 2f - 1.dp.toPx()
                val star = octagramPath(center, radius)
                if (selected) {
                    drawPath(star, colors.accent)
                } else {
                    drawPath(star, colors.inkFaint, style = Stroke(1.4.dp.toPx()))
                }
            },
    )
}

/** A setting that is on or off: its label and detail, and a gold switch. The whole row toggles. */
@Composable
fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    val colors = Celestial.colors
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(title, style = Celestial.type.title.copy(color = colors.ink))
            if (detail != null) BasicText(detail, style = Celestial.type.caption.copy(color = colors.inkMuted))
        }
        Spacer(Modifier.width(Celestial.spacing.lg))
        Switch(checked)
    }
}

@Composable
private fun Switch(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    val track by animateColorAsState(if (checked) colors.accent else colors.inkFaint.copy(alpha = 0.35f))
    val knob by animateDpAsState(if (checked) 22.dp else 2.dp)
    Box(
        modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(CircleShape)
            .background(track),
    ) {
        Box(
            Modifier
                .offset(x = knob, y = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) colors.onAccent else colors.ink),
        )
    }
}

/**
 * A choice among a few sizes, as a row of growing letters: the chosen one sits on gold.
 * [descriptions] say each step for screen readers ("Arabic size 2 of 5").
 */
@Composable
fun SizeSteps(descriptions: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        descriptions.forEachIndexed { step, description ->
            val chosen = step == selected
            Box(
                Modifier
                    .size(Celestial.shapes.touchTarget)
                    .clip(CircleShape)
                    .background(if (chosen) colors.accent else colors.glass)
                    .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(step) })
                    .semantics {
                        contentDescription = description
                        stateDescription = if (chosen) "selected" else ""
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "ع",
                    style = Celestial.quran.label.copy(
                        color = if (chosen) colors.onAccent else colors.ink,
                        fontSize = (14 + step * 4).sp,
                        lineHeight = (20 + step * 6).sp,
                    ),
                )
            }
        }
    }
}

/** Where the user is in a sequence: a small star per page, the current one gold and wider. */
@Composable
fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { page ->
            val width by animateDpAsState(if (page == current) 22.dp else 6.dp)
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (page == current) colors.accent else colors.inkFaint.copy(alpha = 0.5f)),
            )
        }
    }
}
