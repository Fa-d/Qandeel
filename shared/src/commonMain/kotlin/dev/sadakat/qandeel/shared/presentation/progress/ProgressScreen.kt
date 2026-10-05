package dev.sadakat.qandeel.shared.presentation.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import dev.sadakat.qandeel.core.domain.model.ListeningOrder
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.GlyphButton
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PrimaryButton
import dev.sadakat.qandeel.shared.designsystem.kit.ProgressLine
import dev.sadakat.qandeel.shared.designsystem.kit.QuietButton
import dev.sadakat.qandeel.shared.presentation.components.CenteredMessage
import dev.sadakat.qandeel.shared.presentation.components.ScreenHeader
import dev.sadakat.qandeel.shared.presentation.components.listenTime
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.progress_ayahs_heard
import dev.sadakat.qandeel.shared.resources.progress_back
import dev.sadakat.qandeel.shared.resources.progress_coverage_line
import dev.sadakat.qandeel.shared.resources.progress_empty_text
import dev.sadakat.qandeel.shared.resources.progress_empty_title
import dev.sadakat.qandeel.shared.resources.progress_full_rounds
import dev.sadakat.qandeel.shared.resources.progress_into_round
import dev.sadakat.qandeel.shared.resources.progress_listens
import dev.sadakat.qandeel.shared.resources.progress_of_ayahs_heard
import dev.sadakat.qandeel.shared.resources.progress_order_by_number
import dev.sadakat.qandeel.shared.resources.progress_order_most_heard
import dev.sadakat.qandeel.shared.resources.progress_order_recent
import dev.sadakat.qandeel.shared.resources.progress_reset
import dev.sadakat.qandeel.shared.resources.progress_reset_cancel
import dev.sadakat.qandeel.shared.resources.progress_reset_confirm
import dev.sadakat.qandeel.shared.resources.progress_reset_text
import dev.sadakat.qandeel.shared.resources.progress_reset_title
import dev.sadakat.qandeel.shared.resources.progress_title
import dev.sadakat.qandeel.shared.resources.progress_whole_quran_rounds
import dev.sadakat.qandeel.shared.resources.surah_fallback_name
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Your listening: how much of the Quran you've heard and for how long, then every surah heard,
 * recent first (or most heard, or in order), each with its full rounds and how far into the next.
 */
@Composable
fun ProgressScreen(state: ProgressUiState, actions: ProgressActions, modifier: Modifier = Modifier) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Box(modifier.fillMaxSize()) {
        CelestialSky(
            Modifier.fillMaxSize(),
            scroll = { (listState.firstVisibleItemIndex * 200 + listState.firstVisibleItemScrollOffset).toFloat() },
            glowCenter = Offset(0.5f, 0f),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().testTag("progress_list"),
            contentPadding = PaddingValues(
                top = safe.calculateTopPadding(),
                bottom =
                safe.calculateBottomPadding() + Celestial.spacing.xl,
            ),
        ) {
            item(key = "header") {
                Column(Modifier.padding(horizontal = Celestial.spacing.gutter)) {
                    GlyphButton(
                        CelestialIcons.Back,
                        label = stringResource(Res.string.progress_back),
                        onClick = actions.onBack,
                    )
                    ScreenHeader(stringResource(Res.string.progress_title))
                    Spacer(Modifier.height(Celestial.spacing.lg))
                    if (!state.isLoading) Summary(state)
                }
            }
            if (!state.isLoading && state.isEmpty) {
                item(key = "empty") {
                    CenteredMessage(
                        stringResource(Res.string.progress_empty_title),
                        stringResource(Res.string.progress_empty_text),
                    )
                }
            } else if (!state.isLoading) {
                item(key = "order") {
                    val labels = mapOf(
                        ListeningOrder.RECENT to Res.string.progress_order_recent,
                        ListeningOrder.MOST_HEARD to Res.string.progress_order_most_heard,
                        ListeningOrder.BY_NUMBER to Res.string.progress_order_by_number,
                    )
                    PillTabs(
                        options = ListeningOrder.entries,
                        selected = state.order,
                        onSelect = actions.onOrderChange,
                        label = { stringResource(labels.getValue(it)) },
                        modifier = Modifier.fillMaxWidth().padding(Celestial.spacing.gutter),
                    )
                }
                items(state.rows, key = { it.surah }) { row ->
                    ProgressRow(row, onClick = { actions.onOpenReader(row.surah, 0) })
                }
                item(key = "reset") {
                    Box(Modifier.fillMaxWidth().padding(Celestial.spacing.xl), contentAlignment = Alignment.Center) {
                        QuietButton(stringResource(Res.string.progress_reset), onClick = { confirmReset = true })
                    }
                }
            }
        }
    }
    if (confirmReset) {
        ResetDialog(
            onConfirm = {
                confirmReset = false
                actions.onReset()
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun Summary(state: ProgressUiState) {
    val colors = Celestial.colors
    GlassSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Celestial.spacing.gutter)) {
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(state.ayahsHeard.toString(), style = Celestial.type.display.copy(color = colors.ink))
                Spacer(Modifier.width(Celestial.spacing.sm))
                BasicText(
                    stringResource(Res.string.progress_of_ayahs_heard, "6,236"),
                    style = Celestial.type.body.copy(color = colors.inkMuted),
                    modifier = Modifier.padding(bottom = Celestial.spacing.xs),
                )
            }
            Spacer(Modifier.height(Celestial.spacing.md))
            ProgressLine(state.coverage, Modifier.fillMaxWidth().height(Celestial.spacing.xs))
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(
                stringResource(
                    Res.string.progress_coverage_line,
                    (state.coverage * 100).roundToInt(),
                    listenTime(state.listenedMs),
                ),
                style = Celestial.type.caption.copy(color = colors.inkMuted),
            )
            if (state.rounds > 0) {
                BasicText(
                    pluralStringResource(Res.plurals.progress_whole_quran_rounds, state.rounds, state.rounds),
                    style = Celestial.type.caption.copy(color = colors.accent),
                )
            }
        }
    }
}

@Composable
private fun ProgressRow(row: ProgressRowUi, onClick: () -> Unit) {
    val colors = Celestial.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OctagramBadge(row.surah, progress = row.nextRoundProgress)
        Spacer(Modifier.width(Celestial.spacing.lg))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    row.nameEnglish ?: stringResource(Res.string.surah_fallback_name, row.surah),
                    style = Celestial.type.title.copy(color = colors.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                row.nameArabicShort?.let { BasicText(it, style = Celestial.quran.label.copy(color = colors.arabic)) }
            }
            BasicText(
                listOf(
                    pluralStringResource(Res.plurals.progress_full_rounds, row.rounds, row.rounds),
                    pluralStringResource(Res.plurals.progress_listens, row.totalListens, row.totalListens),
                    stringResource(
                        Res.string.progress_into_round,
                        (row.nextRoundProgress * 100).roundToInt(),
                        row.rounds + 1,
                    ),
                ).joinToString(" · "),
                style = Celestial.type.caption.copy(color = colors.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Celestial.spacing.xs))
            BasicText(
                stringResource(Res.string.progress_ayahs_heard, row.ayahsHeard, row.ayahCount),
                style = Celestial.type.caption.copy(color = colors.inkFaint),
            )
        }
    }
}

@Composable
private fun ResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(strong = true) {
            Column(Modifier.padding(Celestial.spacing.xl)) {
                BasicText(
                    stringResource(Res.string.progress_reset_title),
                    style = Celestial.type.title.copy(color = Celestial.colors.ink),
                )
                Spacer(Modifier.height(Celestial.spacing.sm))
                BasicText(
                    stringResource(Res.string.progress_reset_text),
                    style = Celestial.type.body.copy(color = Celestial.colors.inkMuted),
                )
                Spacer(Modifier.height(Celestial.spacing.lg))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuietButton(stringResource(Res.string.progress_reset_cancel), onClick = onDismiss)
                    Spacer(Modifier.width(Celestial.spacing.sm))
                    PrimaryButton(stringResource(Res.string.progress_reset_confirm), onClick = onConfirm)
                }
            }
        }
    }
}
