package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.shared.designsystem.Celestial

/** One tab: its [icon] and [label]. */
class TabItem(val icon: ImageVector, val label: String)

/**
 * The tab bar, floating over the sky as a capsule of glass. The selected tab glows gold behind
 * its icon and shows its label; the others are quieter.
 */
@Composable
fun FloatingTabBar(tabs: List<TabItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(modifier.height(Celestial.shapes.barHeight), radius = Celestial.shapes.barHeight / 2, strong = true) {
        Row(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = Celestial.spacing.sm)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(tab, selected = index == selected, onClick = { onSelect(index) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Tab(tab: TabItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    val tint by animateColorAsState(if (selected) colors.accent else colors.inkMuted)
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(Celestial.shapes.control))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(Celestial.shapes.control))
                .background(if (selected) colors.accentSoft else colors.accentSoft.copy(alpha = 0f))
                .padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.xs),
        ) {
            Glyph(tab.icon, tint = tint, size = 22.dp)
        }
        Spacer(Modifier.height(2.dp))
        BasicText(tab.label, style = Celestial.type.label.copy(color = tint))
    }
}

/**
 * The mini player: what plays and how far through the surah (traced round its star), with
 * play/pause and next. Tapping the rest opens the full player.
 */
@Composable
fun FloatingPlayerBar(
    surahNumber: Int,
    title: String,
    subtitle: String,
    progress: Float,
    playing: Boolean,
    labels: PlayerBarLabels,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Celestial.colors
    GlassSurface(modifier.height(Celestial.shapes.barHeight), radius = Celestial.shapes.barHeight / 2, strong = true) {
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable(onClickLabel = labels.expand, onClick = onExpand)
                .padding(start = Celestial.spacing.sm, end = Celestial.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OctagramBadge(surahNumber, size = 46.dp, progress = progress)
            Spacer(Modifier.width(Celestial.spacing.md))
            Column(Modifier.weight(1f)) {
                BasicText(
                    title,
                    style = Celestial.type.title.copy(color = colors.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BasicText(
                    subtitle,
                    style = Celestial.type.caption.copy(color = colors.inkMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlyphButton(
                if (playing) CelestialIcons.Pause else CelestialIcons.Play,
                label = if (playing) labels.pause else labels.play,
                onClick = onTogglePlay,
            )
            GlyphButton(CelestialIcons.Next, label = labels.next, onClick = onNext)
        }
    }
}

/** What the mini player's controls are called, in the user's language. */
class PlayerBarLabels(val play: String, val pause: String, val next: String, val expand: String)
