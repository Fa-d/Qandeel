package dev.sadakat.qandeel.shared.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.FloatingPlayerBar
import dev.sadakat.qandeel.shared.designsystem.kit.FloatingTabBar
import dev.sadakat.qandeel.shared.designsystem.kit.PlayerBarLabels
import dev.sadakat.qandeel.shared.designsystem.kit.TabItem
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.player_bar_basmala
import dev.sadakat.qandeel.shared.resources.player_bar_subtitle
import dev.sadakat.qandeel.shared.resources.player_next_ayah
import dev.sadakat.qandeel.shared.resources.player_open
import dev.sadakat.qandeel.shared.resources.player_pause
import dev.sadakat.qandeel.shared.resources.player_play
import dev.sadakat.qandeel.shared.resources.tab_home
import dev.sadakat.qandeel.shared.resources.tab_quran
import dev.sadakat.qandeel.shared.resources.tab_you
import org.jetbrains.compose.resources.stringResource

/** The three tabs. */
enum class MainTab { HOME, QURAN, YOU }

/** What the mini player shows: the surah, where in it, the mode, and whether it plays. */
@Immutable
data class MiniPlayerUi(
    val surah: Int,
    val surahName: String,
    /** 0 while the basmala before verse 1 plays. */
    val ayah: Int,
    val modeLabel: String,
    /** How far through the surah, 0..1. */
    val progress: Float,
    val isPlaying: Boolean,
)

/** What the mini player's controls do. */
class MiniPlayerActions(
    val onTogglePlay: () -> Unit,
    val onNext: () -> Unit,
    val onExpand: () -> Unit,
)

/**
 * The app's frame: the [selected] tab's content, and over it, floating at the bottom, the mini
 * player (while something is queued) and the tab bar. Each tab draws its own sky and keeps its
 * content clear of the floating chrome with the padding it is given.
 */
@Composable
fun MainTabs(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    player: MiniPlayerUi?,
    playerActions: MiniPlayerActions,
    modifier: Modifier = Modifier,
    content: @Composable (tab: MainTab, contentPadding: PaddingValues) -> Unit,
) {
    val tabs = listOf(
        TabItem(CelestialIcons.Home, stringResource(Res.string.tab_home), tag = "tab_home"),
        TabItem(CelestialIcons.Quran, stringResource(Res.string.tab_quran), tag = "tab_quran"),
        TabItem(CelestialIcons.You, stringResource(Res.string.tab_you), tag = "tab_you"),
    )
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val bottom = chromeHeight(hasPlayer = player != null) + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(
        start = safe.calculateStartPadding(layoutDirection),
        top = safe.calculateTopPadding(),
        end = safe.calculateEndPadding(layoutDirection),
        bottom = bottom,
    )
    Box(modifier.fillMaxSize()) {
        Crossfade(selected, label = "tab") { tab -> content(tab, contentPadding) }
        // Content fades into the sky under the floating chrome instead of running into it.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottom + Celestial.spacing.xl)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Celestial.colors.sky.bottom))),
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Celestial.spacing.lg)
                .padding(bottom = Celestial.spacing.md),
        ) {
            AnimatedVisibility(
                visible = player != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                if (player != null) MiniPlayer(player, playerActions)
            }
            Spacer(Modifier.height(Celestial.spacing.sm))
            FloatingTabBar(
                tabs = tabs,
                selected = selected.ordinal,
                onSelect = { onSelect(MainTab.entries[it]) },
                modifier = Modifier.testTag("tab_bar"),
            )
        }
    }
}

@Composable
private fun MiniPlayer(player: MiniPlayerUi, actions: MiniPlayerActions) {
    FloatingPlayerBar(
        surahNumber = player.surah,
        title = player.surahName,
        subtitle = if (player.ayah == 0) {
            stringResource(Res.string.player_bar_basmala, player.modeLabel)
        } else {
            stringResource(Res.string.player_bar_subtitle, player.ayah, player.modeLabel)
        },
        progress = player.progress,
        playing = player.isPlaying,
        labels = PlayerBarLabels(
            play = stringResource(Res.string.player_play),
            pause = stringResource(Res.string.player_pause),
            next = stringResource(Res.string.player_next_ayah),
            expand = stringResource(Res.string.player_open),
        ),
        onTogglePlay = actions.onTogglePlay,
        onNext = actions.onNext,
        onExpand = actions.onExpand,
        modifier = Modifier.testTag("mini_player"),
    )
}

/** The floating chrome's height above the navigation bar, so lists can scroll clear of it. */
@Composable
private fun chromeHeight(hasPlayer: Boolean): Dp {
    val bar = Celestial.shapes.barHeight
    val gaps = Celestial.spacing.sm + Celestial.spacing.md + Celestial.spacing.lg
    return bar + gaps + if (hasPlayer) bar + Celestial.spacing.sm else 0.dp
}
