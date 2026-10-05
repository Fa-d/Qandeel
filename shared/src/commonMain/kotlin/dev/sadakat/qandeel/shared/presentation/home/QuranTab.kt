package dev.sadakat.qandeel.shared.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.Glyph
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.SearchField
import dev.sadakat.qandeel.shared.presentation.components.CenteredMessage
import dev.sadakat.qandeel.shared.presentation.components.ScreenHeader
import dev.sadakat.qandeel.shared.presentation.components.SurahRow
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.load_error
import dev.sadakat.qandeel.shared.resources.load_retry
import dev.sadakat.qandeel.shared.resources.quran_browse_juz
import dev.sadakat.qandeel.shared.resources.quran_browse_offline
import dev.sadakat.qandeel.shared.resources.quran_browse_surahs
import dev.sadakat.qandeel.shared.resources.quran_juz_starts
import dev.sadakat.qandeel.shared.resources.quran_juz_title
import dev.sadakat.qandeel.shared.resources.quran_jump_to
import dev.sadakat.qandeel.shared.resources.quran_no_results
import dev.sadakat.qandeel.shared.resources.quran_no_results_hint
import dev.sadakat.qandeel.shared.resources.quran_offline_empty_text
import dev.sadakat.qandeel.shared.resources.quran_offline_empty_title
import dev.sadakat.qandeel.shared.resources.quran_search_hint
import dev.sadakat.qandeel.shared.resources.quran_subtitle
import dev.sadakat.qandeel.shared.resources.quran_title
import org.jetbrains.compose.resources.stringResource

/** What the Quran tab's controls do. */
class QuranTabActions(
    val onQueryChange: (String) -> Unit,
    val onBrowseChange: (BrowseMode) -> Unit,
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
    val onRetry: () -> Unit,
)

/**
 * The Quran: one search for names, numbers and verse references ("2:255" offers to go straight
 * there), and every surah, the thirty juz, or what is on the phone.
 */
@Composable
fun QuranTab(
    state: HomeUiState,
    actions: QuranTabActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
) {
    Box(modifier.fillMaxSize()) {
        CelestialSky(
            Modifier.fillMaxSize(),
            scroll = { (listState.firstVisibleItemIndex * ROW_ESTIMATE_PX + listState.firstVisibleItemScrollOffset).toFloat() },
            glowCenter = Offset(0.85f, 0.02f),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("quran_list"),
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
        ) {
            item(key = "header") {
                Column(Modifier.padding(horizontal = Celestial.spacing.gutter).padding(top = Celestial.spacing.xl)) {
                    ScreenHeader(stringResource(Res.string.quran_title), subtitle = stringResource(Res.string.quran_subtitle))
                    Spacer(Modifier.height(Celestial.spacing.lg))
                    SearchField(
                        query = state.query,
                        onQueryChange = actions.onQueryChange,
                        placeholder = stringResource(Res.string.quran_search_hint),
                        modifier = Modifier.fillMaxWidth().testTag("quran_search"),
                    )
                    if (!state.isSearching) {
                        Spacer(Modifier.height(Celestial.spacing.md))
                        BrowseTabs(state.browse, actions.onBrowseChange)
                    }
                    Spacer(Modifier.height(Celestial.spacing.sm))
                }
            }
            quranItems(state, actions)
        }
    }
}

private const val ROW_ESTIMATE_PX = 160

@Composable
private fun BrowseTabs(selected: BrowseMode, onSelect: (BrowseMode) -> Unit) {
    val labels = mapOf(
        BrowseMode.SURAH to Res.string.quran_browse_surahs,
        BrowseMode.JUZ to Res.string.quran_browse_juz,
        BrowseMode.OFFLINE to Res.string.quran_browse_offline,
    )
    PillTabs(
        options = BrowseMode.entries,
        selected = selected,
        onSelect = onSelect,
        label = { stringResource(labels.getValue(it)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun LazyListScope.quranItems(state: HomeUiState, actions: QuranTabActions) {
    when {
        state.loadFailed -> item(key = "error") {
            CenteredMessage(
                title = stringResource(Res.string.load_error),
                text = "",
                action = stringResource(Res.string.load_retry),
                onAction = actions.onRetry,
            )
        }

        state.isLoading -> Unit

        state.isSearching || state.browse != BrowseMode.JUZ -> {
            state.jumpTarget?.let { jump ->
                item(key = "jump") { JumpRow(jump, onClick = { actions.onOpenReader(jump.ref.surah, jump.ref.ayah) }) }
            }
            items(state.surahs, key = { "surah_${it.surah.number}" }) { row ->
                SurahRow(
                    row,
                    onClick = { actions.onOpenReader(row.surah.number, 0) },
                    modifier = Modifier.testTag("surah_${row.surah.number}"),
                )
            }
            if (state.surahs.isEmpty() && state.jumpTarget == null) item(key = "empty") { EmptyList(state) }
        }

        else -> items(state.juz, key = { "juz_${it.juz}" }) { row ->
            JuzRow(row, onClick = { actions.onOpenReader(row.start.surah, row.start.ayah) })
        }
    }
}

@Composable
private fun EmptyList(state: HomeUiState) {
    if (state.isSearching) {
        CenteredMessage(
            title = stringResource(Res.string.quran_no_results, state.query.trim()),
            text = stringResource(Res.string.quran_no_results_hint),
        )
    } else {
        CenteredMessage(
            title = stringResource(Res.string.quran_offline_empty_title),
            text = stringResource(Res.string.quran_offline_empty_text),
        )
    }
}

/** A typed verse reference: go straight to it. */
@Composable
private fun JumpRow(jump: AyahJumpUi, onClick: () -> Unit) {
    val colors = Celestial.colors
    GlassSurface(
        Modifier
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.sm)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        radius = Celestial.shapes.tile,
    ) {
        Row(Modifier.padding(Celestial.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                BasicText(
                    stringResource(Res.string.quran_jump_to, jump.ref.surah, jump.ref.ayah),
                    style = Celestial.type.title.copy(color = colors.accent),
                )
                BasicText(jump.surahName, style = Celestial.type.caption.copy(color = colors.inkMuted))
            }
            Glyph(CelestialIcons.Chevron, tint = colors.accent)
        }
    }
}

@Composable
private fun JuzRow(row: JuzRowUi, onClick: () -> Unit) {
    val colors = Celestial.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OctagramBadge(row.juz)
        Spacer(Modifier.width(Celestial.spacing.lg))
        Column {
            BasicText(stringResource(Res.string.quran_juz_title, row.juz), style = Celestial.type.title.copy(color = colors.ink))
            BasicText(
                stringResource(Res.string.quran_juz_starts, row.surahName, row.start.surah, row.start.ayah),
                style = Celestial.type.caption.copy(color = colors.inkMuted),
            )
        }
    }
}
