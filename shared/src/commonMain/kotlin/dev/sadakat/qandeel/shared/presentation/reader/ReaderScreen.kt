package dev.sadakat.qandeel.shared.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.core.domain.model.Ayah
import dev.sadakat.qandeel.core.domain.model.QuranMeta
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.Revelation
import dev.sadakat.qandeel.core.domain.player.WordPointer
import dev.sadakat.qandeel.core.domain.repository.SurahDownloadState
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.Glyph
import dev.sadakat.qandeel.shared.designsystem.kit.GlyphButton
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PlayButton
import dev.sadakat.qandeel.shared.presentation.components.CenteredMessage
import dev.sadakat.qandeel.shared.presentation.components.RecitedArabicText
import dev.sadakat.qandeel.shared.presentation.components.WordByWordText
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.ayah_cd_number
import dev.sadakat.qandeel.shared.resources.ayah_heard_short
import dev.sadakat.qandeel.shared.resources.cd_more_options
import dev.sadakat.qandeel.shared.resources.downloading_percent
import dev.sadakat.qandeel.shared.resources.jump_to_reciting_ayah
import dev.sadakat.qandeel.shared.resources.load_error
import dev.sadakat.qandeel.shared.resources.load_retry
import dev.sadakat.qandeel.shared.resources.mode_arabic
import dev.sadakat.qandeel.shared.resources.mode_bangla
import dev.sadakat.qandeel.shared.resources.mode_english
import dev.sadakat.qandeel.shared.resources.play_surah
import dev.sadakat.qandeel.shared.resources.quran_meccan
import dev.sadakat.qandeel.shared.resources.quran_medinan
import dev.sadakat.qandeel.shared.resources.quran_surah_subtitle
import dev.sadakat.qandeel.shared.resources.reader_back
import dev.sadakat.qandeel.shared.resources.reader_basmala
import dev.sadakat.qandeel.shared.resources.reader_heard_ayahs
import dev.sadakat.qandeel.shared.resources.reader_heard_rounds_next
import dev.sadakat.qandeel.shared.resources.reader_offline_download
import dev.sadakat.qandeel.shared.resources.reader_offline_downloaded
import dev.sadakat.qandeel.shared.resources.reader_offline_failed
import dev.sadakat.qandeel.shared.resources.reader_offline_progress
import dev.sadakat.qandeel.shared.resources.reader_quick_settings
import dev.sadakat.qandeel.shared.resources.reciting_ayah_state
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** The header is the list's first item; ayah N is item N. */
private const val HEADER_COUNT = 1

/**
 * A surah to read and listen to. Reading, it is a calm column of ayahs on the sky, each with its
 * meaning; tap one to play from it, press and hold for its actions. While the surah plays it turns
 * to lyrics: the reciting ayah glides onto a line in the upper third, lit, with the gold word
 * pointer, and the ayahs around it fade back. Scrolling away pauses that, and a chip brings it back.
 *
 * [bottomPadding] keeps the last ayah clear of what floats at the bottom (the mini player).
 */
@Composable
fun ReaderScreen(
    state: SurahReaderUiState,
    pointer: WordPointer,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    listState: LazyListState = rememberLazyListState(),
) {
    val playing = state.playingAyah
    val follow = rememberFollowAlongState(listState, HEADER_COUNT, playing, enabled = state.followAlong)
    val safe = WindowInsets.safeDrawing.asPaddingValues()

    // Open at the ayah asked for.
    LaunchedEffect(state.ayahs.isNotEmpty()) {
        if (state.ayahs.isNotEmpty() && state.initialAyah > 0) {
            listState.scrollToItem(recitingAyahIndex(HEADER_COUNT, state.initialAyah))
        }
    }

    Box(modifier.fillMaxSize().testTag("reader")) {
        CelestialSky(
            Modifier.fillMaxSize(),
            scroll = { (listState.firstVisibleItemIndex * 600 + listState.firstVisibleItemScrollOffset).toFloat() },
            glowCenter = Offset(0.5f, 0.05f),
        )
        when {
            state.loadFailed -> CenteredMessage(
                title = stringResource(Res.string.load_error),
                text = "",
                action = stringResource(Res.string.load_retry),
                onAction = actions.onRetry,
                modifier = Modifier.align(Alignment.Center),
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(follow.userDragObserver)
                    .testTag("ayah_list"),
                contentPadding = PaddingValues(
                    top = safe.calculateTopPadding() + TOP_BAR_HEIGHT,
                    bottom = safe.calculateBottomPadding() + bottomPadding + Celestial.spacing.xxl,
                ),
            ) {
                item(key = "header") { ReaderHeader(state, actions) }
                itemsIndexed(state.ayahs, key = { _, ayah -> ayah.number }) { index, ayah ->
                    AyahItem(
                        ayah = ayah,
                        state = state,
                        pointer = if (ayah.number == playing) pointer else WordPointer.Off,
                        heard = state.heard.getOrNull(index) ?: 0,
                        actions = actions,
                        onPlay = {
                            follow.resume()
                            actions.onPlayAyah(ayah.number)
                        },
                    )
                }
            }
        }
        TopBar(state, listState, actions)
        JumpToReciting(
            follow = follow,
            listState = listState,
            playingAyah = playing,
            enabled = state.followAlong,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = safe.calculateBottomPadding() + bottomPadding + Celestial.spacing.md),
        )
    }
}

private val TOP_BAR_HEIGHT = 56.dp
private val TOP_BAR_FADE = 24.dp
private const val TOP_BAR_SOLID = 0.6f

@Composable
private fun TopBar(state: SurahReaderUiState, listState: LazyListState, actions: ReaderActions) {
    val colors = Celestial.colors
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val titleAlpha by animateFloatAsState(if (scrolled) 1f else 0f)
    Box(
        Modifier
            .fillMaxWidth()
            // Solid behind the bar, then a fade below it, so the ayahs dissolve as they scroll under.
            .background(
                Brush.verticalGradient(
                    0f to colors.sky.top,
                    TOP_BAR_SOLID to colors.sky.top,
                    1f to colors.sky.top.copy(alpha = 0f),
                ),
            )
            .padding(WindowInsets.safeDrawing.asPaddingValues().let { PaddingValues(top = it.calculateTopPadding()) })
            .height(TOP_BAR_HEIGHT + TOP_BAR_FADE),
    ) {
        Row(
            Modifier.fillMaxWidth().height(TOP_BAR_HEIGHT).padding(horizontal = Celestial.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphButton(CelestialIcons.Back, stringResource(Res.string.reader_back), actions.onBack)
            BasicText(
                state.surah?.nameEnglish.orEmpty(),
                style = Celestial.type.title.copy(color = colors.ink),
                modifier = Modifier.weight(1f).graphicsLayer { alpha = titleAlpha },
            )
            GlyphButton(
                CelestialIcons.TextSize,
                stringResource(Res.string.reader_quick_settings),
                actions.onOpenQuickSettings,
            )
            GlyphButton(CelestialIcons.More, stringResource(Res.string.cd_more_options), actions.onOpenMore)
        }
    }
}

@Composable
private fun ReaderHeader(state: SurahReaderUiState, actions: ReaderActions) {
    val colors = Celestial.colors
    val surah = state.surah ?: return
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Celestial.spacing.gutter)
            .padding(top = Celestial.spacing.lg, bottom = Celestial.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            surah.nameArabicShort,
            style = Celestial.quran.display.copy(color = colors.arabic, textAlign = TextAlign.Center),
        )
        BasicText(
            surah.nameEnglish,
            style = Celestial.type.display.copy(color = colors.ink, textAlign = TextAlign.Center),
        )
        val revelation = stringResource(
            if (surah.revelation ==
                Revelation.MECCAN
            ) {
                Res.string.quran_meccan
            } else {
                Res.string.quran_medinan
            },
        )
        BasicText(
            stringResource(Res.string.quran_surah_subtitle, surah.meaningEnglish, revelation, surah.ayahCount),
            style = Celestial.type.caption.copy(color = colors.inkMuted, textAlign = TextAlign.Center),
        )
        state.listening?.let { listening ->
            Spacer(Modifier.height(Celestial.spacing.xs))
            BasicText(
                if (listening.rounds > 0) {
                    stringResource(
                        Res.string.reader_heard_rounds_next,
                        listening.rounds,
                        (listening.nextRoundProgress * 100).roundToInt(),
                    )
                } else {
                    stringResource(Res.string.reader_heard_ayahs, listening.ayahsHeard, listening.ayahCount)
                },
                style = Celestial.type.caption.copy(color = colors.accent, textAlign = TextAlign.Center),
            )
        }
        Spacer(Modifier.height(Celestial.spacing.lg))
        // The play button stays centred under the title; the offline button takes the space to its left.
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.md),
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                OfflineButton(state.downloadState, actions)
            }
            PlayButton(
                playing = state.playingAyah != null,
                label = stringResource(Res.string.play_surah),
                onClick = actions.onPlaySurah,
                size = 60.dp,
            )
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(Celestial.spacing.lg))
        val modes = mapOf(
            RecitationMode.ARABIC_ONLY to Res.string.mode_arabic,
            RecitationMode.ARABIC_ENGLISH to Res.string.mode_english,
            RecitationMode.ARABIC_BANGLA to Res.string.mode_bangla,
        )
        PillTabs(
            options = RecitationMode.entries,
            selected = state.mode,
            onSelect = actions.onModeChange,
            label = { stringResource(modes.getValue(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (QuranMeta.hasBasmalaPrefix(surah.number)) {
            Spacer(Modifier.height(Celestial.spacing.xl))
            BasicText(
                stringResource(Res.string.reader_basmala),
                style = Celestial.quran.title.copy(color = colors.arabic, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Whether the surah is on the phone, as a small glass button that downloads, retries or removes it. */
@Composable
private fun OfflineButton(state: SurahDownloadState, actions: ReaderActions) {
    val colors = Celestial.colors
    val (label, onClick) = when (state) {
        SurahDownloadState.NotDownloaded -> stringResource(Res.string.reader_offline_download) to actions.onDownload

        is SurahDownloadState.Downloading ->
            stringResource(Res.string.reader_offline_progress, (state.progress * 100).roundToInt()) to {}

        SurahDownloadState.Downloaded -> stringResource(Res.string.reader_offline_downloaded) to
            actions.onRemoveDownload

        is SurahDownloadState.Failed -> stringResource(Res.string.reader_offline_failed) to actions.onDownload
    }
    // The label is short to fit beside the play button; while downloading, it is read out in full.
    val spoken = (state as? SurahDownloadState.Downloading)?.let {
        stringResource(Res.string.downloading_percent, (it.progress * 100).roundToInt())
    }
    GlassSurface(
        Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { if (spoken != null) contentDescription = spoken }
            .testTag("reader_offline"),
        radius = 22.dp,
    ) {
        Row(
            Modifier.padding(horizontal = Celestial.spacing.md, vertical = Celestial.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Glyph(
                if (state == SurahDownloadState.Downloaded) CelestialIcons.Check else CelestialIcons.Download,
                tint = if (state == SurahDownloadState.Downloaded) colors.accent else colors.ink,
                size = 18.dp,
            )
            Spacer(Modifier.width(Celestial.spacing.xs))
            BasicText(label, style = Celestial.type.caption.copy(color = colors.ink), maxLines = 1)
        }
    }
}

/**
 * One ayah. While its surah plays, the reciting ayah is lit and pointed and the others step back
 * (faded, a little smaller), which is what makes the list read as lyrics.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AyahItem(
    ayah: Ayah,
    state: SurahReaderUiState,
    pointer: WordPointer,
    heard: Int,
    actions: ReaderActions,
    onPlay: () -> Unit,
) {
    val colors = Celestial.colors
    val surahPlaying = state.playingAyah != null
    val reciting = ayah.number == state.playingAyah
    val alpha by animateFloatAsState(if (!surahPlaying || reciting) 1f else FADED_ALPHA)
    val scale by animateFloatAsState(if (surahPlaying && !reciting) FADED_SCALE else 1f)
    val glow by animateFloatAsState(if (reciting) 1f else 0f)
    val number = stringResource(Res.string.ayah_cd_number, ayah.number)
    val recitingState = stringResource(Res.string.reciting_ayah_state)
    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = Celestial.spacing.md, vertical = Celestial.spacing.xs)
            .clip(RoundedCornerShape(Celestial.shapes.card))
            .background(colors.accentSoft.copy(alpha = colors.accentSoft.alpha * glow))
            .combinedClickable(onClick = onPlay, onLongClick = { actions.onShowAyahActions(ayah.number) })
            .semantics {
                contentDescription = number
                if (reciting) stateDescription = recitingState
            }
            .padding(horizontal = Celestial.spacing.md, vertical = Celestial.spacing.lg)
            .testTag("ayah_${ayah.number}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OctagramBadge(ayah.number, size = 34.dp)
            if (heard > 0) {
                Spacer(Modifier.width(Celestial.spacing.sm))
                BasicText(
                    stringResource(Res.string.ayah_heard_short, heard),
                    style = Celestial.type.caption.copy(color = colors.inkFaint),
                )
            }
        }
        Spacer(Modifier.height(Celestial.spacing.sm))
        AyahArabic(ayah, state, pointer, reciting, actions)
        val translation = ayahTranslation(ayah, state)
        if (translation != null) {
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(
                translation,
                style = Celestial.type.body.copy(color = if (reciting) colors.ink else colors.inkMuted),
            )
        }
    }
}

/** The ayah's Arabic: word by word under its meanings when they are on, else the recited text. */
@Composable
private fun AyahArabic(
    ayah: Ayah,
    state: SurahReaderUiState,
    pointer: WordPointer,
    reciting: Boolean,
    actions: ReaderActions,
) {
    val meanings = state.wordMeanings[ayah.number]
    if (!meanings.isNullOrEmpty()) {
        WordByWordText(
            text = ayah.arabic,
            meanings = meanings,
            pointer = pointer,
            style = Celestial.quran.label,
            modifier = Modifier.fillMaxWidth(),
            onWordClick = { word -> actions.onPlayFromWord(ayah.number, word) },
            onLongPress = { actions.onShowAyahActions(ayah.number) },
        )
    } else {
        val style = if (reciting) {
            Celestial.quran.title.copy(
                fontSize = Celestial.quran.body.fontSize,
            )
        } else {
            Celestial.quran.body
        }
        RecitedArabicText(ayah.arabic, pointer, style, Modifier.fillMaxWidth())
    }
}

/** The translation the reader shows under an ayah, if any: the mode's, or English for Arabic only. */
private fun ayahTranslation(ayah: Ayah, state: SurahReaderUiState): String? {
    if (!state.showTranslation) return null
    return state.mode.translation?.let { ayah.translation(it) }
        ?: ayah.english.takeIf { state.mode == RecitationMode.ARABIC_ONLY }
}

private const val FADED_ALPHA = 0.36f
private const val FADED_SCALE = 0.97f

/** The way back to the reciting ayah, shown while it is off screen and the list isn't following it. */
@Composable
private fun JumpToReciting(
    follow: FollowAlongState,
    listState: LazyListState,
    playingAyah: Int?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val offScreen by remember(playingAyah, enabled) {
        derivedStateOf {
            val ayah = playingAyah ?: return@derivedStateOf false
            if (enabled && follow.following) return@derivedStateOf false
            val index = recitingAyahIndex(HEADER_COUNT, ayah)
            val visible = listState.layoutInfo.visibleItemsInfo.map { it.index }
            index !in visible
        }
    }
    AnimatedVisibility(
        visible = offScreen,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        GlassSurface(
            Modifier
                .clip(CircleShape)
                .clickable {
                    val ayah = playingAyah ?: return@clickable
                    scope.launch {
                        follow.resume()
                        listState.scrollToReciting(HEADER_COUNT, ayah)
                    }
                }
                .testTag("jump_to_reciting"),
            radius = 22.dp,
            strong = true,
        ) {
            BasicText(
                stringResource(Res.string.jump_to_reciting_ayah),
                style = Celestial.type.label.copy(color = Celestial.colors.accent),
                modifier = Modifier.padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.md),
            )
        }
    }
}
