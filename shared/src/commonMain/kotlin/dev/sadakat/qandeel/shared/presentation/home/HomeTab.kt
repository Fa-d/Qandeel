package dev.sadakat.qandeel.shared.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.designsystem.kit.PlayButton
import dev.sadakat.qandeel.shared.designsystem.kit.ProgressLine
import dev.sadakat.qandeel.shared.designsystem.lamp.LampMotion
import dev.sadakat.qandeel.shared.designsystem.lamp.QandeelLamp
import dev.sadakat.qandeel.shared.presentation.components.listenTime
import dev.sadakat.qandeel.shared.presentation.progress.ProgressRowUi
import dev.sadakat.qandeel.shared.presentation.progress.ProgressUiState
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.home_begin_text
import dev.sadakat.qandeel.shared.resources.home_begin_title
import dev.sadakat.qandeel.shared.resources.home_card_progress
import dev.sadakat.qandeel.shared.resources.home_continue_listening
import dev.sadakat.qandeel.shared.resources.home_now_playing
import dev.sadakat.qandeel.shared.resources.home_progress_heard
import dev.sadakat.qandeel.shared.resources.home_progress_line
import dev.sadakat.qandeel.shared.resources.home_recently_heard
import dev.sadakat.qandeel.shared.resources.home_wordmark
import dev.sadakat.qandeel.shared.resources.home_wordmark_arabic
import dev.sadakat.qandeel.shared.resources.player_pause
import dev.sadakat.qandeel.shared.resources.player_play
import dev.sadakat.qandeel.shared.resources.progress_full_rounds
import dev.sadakat.qandeel.shared.resources.progress_into_round
import dev.sadakat.qandeel.shared.resources.surah_fallback_name
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Home: the lantern hanging in the sky over the wordmark; then the one thing most often wanted,
 * to continue listening; then the surahs heard lately, and a glance at the whole Quran's progress.
 * As the page scrolls the lantern drifts up slower than the page and leans back, and the stars
 * move apart: the depth is in the motion.
 */
@Composable
fun HomeTab(
    home: HomeUiState,
    progress: ProgressUiState,
    playing: Boolean,
    actions: HomeTabActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
) {
    val clock = Celestial.clock
    val scroll = { listState.scrollPixels() }
    Box(modifier.fillMaxSize()) {
        CelestialSky(Modifier.fillMaxSize(), scroll = scroll, glowCenter = Offset(0.5f, 0.2f))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_list"),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        ) {
            item(key = "hero") {
                Box(Modifier.fillMaxWidth().height(HERO_HEIGHT.dp)) {
                    QandeelLamp(
                        motion = {
                            LampMotion(
                                time = clock.seconds(),
                                energy = if (playing) PLAYING_ENERGY else IDLE_ENERGY,
                                tilt = (scroll() / TILT_DISTANCE_PX).coerceIn(0f, 1f),
                            )
                        },
                        colors = Celestial.colors.lamp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 28.dp)
                            // Parallax: the lantern rises at half the page's pace.
                            .graphicsLayer { translationY = scroll().coerceAtMost(HERO_HEIGHT * density) * 0.5f },
                    )
                    Wordmark(Modifier.padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.md))
                }
            }
            item(key = "continue") {
                val card = home.continueListening
                Box(Modifier.padding(horizontal = Celestial.spacing.gutter)) {
                    if (card != null) {
                        ContinueCard(card, actions)
                    } else {
                        BeginCard(onOpen = { actions.onOpenReader(1, 0) })
                    }
                }
            }
            if (progress.rows.isNotEmpty()) {
                item(key = "recent_title") {
                    Eyebrow(
                        stringResource(Res.string.home_recently_heard),
                        Modifier.padding(
                            start = Celestial.spacing.gutter,
                            top = Celestial.spacing.xl,
                            bottom = Celestial.spacing.md,
                        ),
                    )
                }
                item(key = "recent") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Celestial.spacing.gutter),
                        horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.md),
                    ) {
                        items(progress.rows.take(RECENT_COUNT), key = { it.surah }) { row ->
                            RecentTile(row, onClick = { actions.onOpenReader(row.surah, 0) })
                        }
                    }
                }
                item(key = "glance") {
                    ProgressGlance(
                        progress,
                        onClick = actions.onOpenProgress,
                        modifier = Modifier.padding(
                            horizontal = Celestial.spacing.gutter,
                            vertical = Celestial.spacing.lg,
                        ),
                    )
                }
            }
        }
    }
}

private const val HERO_HEIGHT = 300
private const val TILT_DISTANCE_PX = 900f
private const val RECENT_COUNT = 8

/** Past the hero, items are counted as this tall: only the depth effects read it. */
private const val ITEM_ESTIMATE_PX = 1_000
private const val IDLE_ENERGY = 0.2f
private const val PLAYING_ENERGY = 0.55f

/** How far the list has scrolled, in pixels: exact within the first item, estimated after it. */
private fun LazyListState.scrollPixels(): Float = if (firstVisibleItemIndex ==
    0
) {
    firstVisibleItemScrollOffset.toFloat()
} else {
    (firstVisibleItemIndex * ITEM_ESTIMATE_PX + firstVisibleItemScrollOffset).toFloat()
}

@Composable
private fun Wordmark(modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BasicText(
            stringResource(Res.string.home_wordmark),
            style = Celestial.type.headline.copy(color = colors.ink, fontSize = 24.sp),
        )
        Spacer(Modifier.weight(1f))
        BasicText(
            stringResource(Res.string.home_wordmark_arabic),
            style = Celestial.quran.label.copy(color = colors.accent),
        )
    }
}

@Composable
private fun ContinueCard(card: ContinueListeningUi, actions: HomeTabActions) {
    val colors = Celestial.colors
    GlassSurface(
        Modifier
            .fillMaxWidth()
            .clickable { actions.onOpenReader(card.surah, card.ayah) }
            .testTag("continue_card"),
    ) {
        Column(Modifier.padding(Celestial.spacing.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(
                    stringResource(
                        if (card.isCurrent) Res.string.home_now_playing else Res.string.home_continue_listening,
                    ),
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    stringResource(Res.string.home_card_progress, card.ayah, card.ayahCount),
                    style = Celestial.type.caption.copy(color = colors.inkMuted),
                )
            }
            Spacer(Modifier.height(Celestial.spacing.md))
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(
                    card.surahName,
                    style = Celestial.type.display.copy(color = colors.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                BasicText(card.surahNameArabic, style = Celestial.quran.title.copy(color = colors.arabic))
            }
            Spacer(Modifier.height(Celestial.spacing.lg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressLine(card.progress, Modifier.weight(1f).height(4.dp))
                Spacer(Modifier.width(Celestial.spacing.lg))
                PlayButton(
                    playing = card.isPlaying,
                    label = stringResource(if (card.isPlaying) Res.string.player_pause else Res.string.player_play),
                    onClick = actions.onContinuePlayPause,
                )
            }
        }
    }
}

/** Before anything has played: where to begin. */
@Composable
private fun BeginCard(onOpen: () -> Unit) {
    val colors = Celestial.colors
    GlassSurface(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(Celestial.spacing.gutter), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                BasicText(
                    stringResource(Res.string.home_begin_title),
                    style = Celestial.type.headline.copy(color = colors.ink),
                )
                BasicText(
                    stringResource(Res.string.home_begin_text),
                    style = Celestial.type.body.copy(color = colors.inkMuted),
                )
            }
            Spacer(Modifier.width(Celestial.spacing.md))
            PlayButton(playing = false, label = stringResource(Res.string.home_begin_title), onClick = onOpen)
        }
    }
}

@Composable
private fun RecentTile(row: ProgressRowUi, onClick: () -> Unit) {
    val colors = Celestial.colors
    GlassSurface(Modifier.width(148.dp).clickable(onClick = onClick), radius = Celestial.shapes.tile) {
        Column(Modifier.padding(Celestial.spacing.lg)) {
            OctagramBadge(row.surah, size = 40.dp, progress = row.nextRoundProgress.takeIf { row.rounds == 0 })
            Spacer(Modifier.height(Celestial.spacing.md))
            BasicText(
                row.nameEnglish ?: stringResource(Res.string.surah_fallback_name, row.surah),
                style = Celestial.type.title.copy(color = colors.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                if (row.rounds > 0) {
                    pluralStringResource(Res.plurals.progress_full_rounds, row.rounds, row.rounds)
                } else {
                    stringResource(Res.string.progress_into_round, (row.nextRoundProgress * 100).roundToInt(), 1)
                },
                style = Celestial.type.caption.copy(color = colors.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProgressGlance(progress: ProgressUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    GlassSurface(modifier.fillMaxWidth().clickable(onClick = onClick), radius = Celestial.shapes.tile) {
        Row(Modifier.padding(Celestial.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .drawBehind {
                        val stroke = 4.dp.toPx()
                        val arc = Size(size.width - stroke, size.height - stroke)
                        val topLeft = Offset(stroke / 2, stroke / 2)
                        drawArc(
                            colors.inkFaint.copy(alpha = 0.25f),
                            0f,
                            360f,
                            false,
                            topLeft,
                            arc,
                            style = Stroke(stroke),
                        )
                        drawArc(
                            colors.accent,
                            -90f,
                            360f * progress.coverage,
                            false,
                            topLeft,
                            arc,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "${(progress.coverage * 100).roundToInt()}%",
                    style = Celestial.type.label.copy(color = colors.ink, textAlign = TextAlign.Center),
                )
            }
            Spacer(Modifier.width(Celestial.spacing.lg))
            Column(Modifier.weight(1f)) {
                BasicText(
                    stringResource(Res.string.home_progress_heard),
                    style = Celestial.type.title.copy(color = colors.ink),
                )
                BasicText(
                    stringResource(Res.string.home_progress_line, listenTime(progress.listenedMs), progress.rows.size),
                    style = Celestial.type.caption.copy(color = colors.inkMuted),
                )
            }
        }
    }
}
