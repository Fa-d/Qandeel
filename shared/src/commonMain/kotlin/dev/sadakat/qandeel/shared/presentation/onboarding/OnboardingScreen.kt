package dev.sadakat.qandeel.shared.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.CelestialTheme
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.ChoiceCard
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.GlyphButton
import dev.sadakat.qandeel.shared.designsystem.kit.PageDots
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PrimaryButton
import dev.sadakat.qandeel.shared.designsystem.kit.QuietButton
import dev.sadakat.qandeel.shared.designsystem.kit.SizeSteps
import dev.sadakat.qandeel.shared.designsystem.kit.ToggleRow
import dev.sadakat.qandeel.shared.designsystem.lamp.LampMotion
import dev.sadakat.qandeel.shared.designsystem.lamp.QandeelLamp
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size_step
import dev.sadakat.qandeel.shared.resources.onboarding_back
import dev.sadakat.qandeel.shared.resources.onboarding_begin
import dev.sadakat.qandeel.shared.resources.onboarding_finish
import dev.sadakat.qandeel.shared.resources.onboarding_listening_supporting
import dev.sadakat.qandeel.shared.resources.onboarding_listening_title
import dev.sadakat.qandeel.shared.resources.onboarding_look_supporting
import dev.sadakat.qandeel.shared.resources.onboarding_look_title
import dev.sadakat.qandeel.shared.resources.onboarding_mode_arabic
import dev.sadakat.qandeel.shared.resources.onboarding_mode_arabic_detail
import dev.sadakat.qandeel.shared.resources.onboarding_mode_bangla
import dev.sadakat.qandeel.shared.resources.onboarding_mode_bangla_detail
import dev.sadakat.qandeel.shared.resources.onboarding_mode_english
import dev.sadakat.qandeel.shared.resources.onboarding_mode_english_detail
import dev.sadakat.qandeel.shared.resources.onboarding_next
import dev.sadakat.qandeel.shared.resources.onboarding_offline_later
import dev.sadakat.qandeel.shared.resources.onboarding_offline_supporting
import dev.sadakat.qandeel.shared.resources.onboarding_offline_title
import dev.sadakat.qandeel.shared.resources.onboarding_reading_sample_ayah
import dev.sadakat.qandeel.shared.resources.onboarding_reading_sample_meaning
import dev.sadakat.qandeel.shared.resources.onboarding_reading_supporting
import dev.sadakat.qandeel.shared.resources.onboarding_reading_title
import dev.sadakat.qandeel.shared.resources.onboarding_reduce_motion
import dev.sadakat.qandeel.shared.resources.onboarding_reduce_motion_detail
import dev.sadakat.qandeel.shared.resources.onboarding_sample_bn_1
import dev.sadakat.qandeel.shared.resources.onboarding_sample_bn_2
import dev.sadakat.qandeel.shared.resources.onboarding_sample_bn_3
import dev.sadakat.qandeel.shared.resources.onboarding_sample_bn_4
import dev.sadakat.qandeel.shared.resources.onboarding_sample_en_1
import dev.sadakat.qandeel.shared.resources.onboarding_sample_en_2
import dev.sadakat.qandeel.shared.resources.onboarding_sample_en_3
import dev.sadakat.qandeel.shared.resources.onboarding_sample_en_4
import dev.sadakat.qandeel.shared.resources.onboarding_sample_word_1
import dev.sadakat.qandeel.shared.resources.onboarding_sample_word_2
import dev.sadakat.qandeel.shared.resources.onboarding_sample_word_3
import dev.sadakat.qandeel.shared.resources.onboarding_sample_word_4
import dev.sadakat.qandeel.shared.resources.onboarding_show_translation
import dev.sadakat.qandeel.shared.resources.onboarding_skip
import dev.sadakat.qandeel.shared.resources.onboarding_starter
import dev.sadakat.qandeel.shared.resources.onboarding_starter_detail
import dev.sadakat.qandeel.shared.resources.onboarding_theme_auto
import dev.sadakat.qandeel.shared.resources.onboarding_theme_dawn
import dev.sadakat.qandeel.shared.resources.onboarding_theme_night
import dev.sadakat.qandeel.shared.resources.onboarding_voice_baezeed
import dev.sadakat.qandeel.shared.resources.onboarding_voice_baezeed_detail
import dev.sadakat.qandeel.shared.resources.onboarding_voice_if
import dev.sadakat.qandeel.shared.resources.onboarding_voice_if_detail
import dev.sadakat.qandeel.shared.resources.onboarding_voice_play
import dev.sadakat.qandeel.shared.resources.onboarding_voice_stop
import dev.sadakat.qandeel.shared.resources.onboarding_voice_title
import dev.sadakat.qandeel.shared.resources.onboarding_voice_toha
import dev.sadakat.qandeel.shared.resources.onboarding_voice_toha_detail
import dev.sadakat.qandeel.shared.resources.onboarding_watch
import dev.sadakat.qandeel.shared.resources.onboarding_watch_detail
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_bangla
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_english
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_off
import dev.sadakat.qandeel.shared.resources.onboarding_welcome_ayah
import dev.sadakat.qandeel.shared.resources.onboarding_welcome_meaning
import dev.sadakat.qandeel.shared.resources.onboarding_welcome_reference
import dev.sadakat.qandeel.shared.resources.onboarding_welcome_subtitle
import dev.sadakat.qandeel.shared.resources.onboarding_welcome_title
import dev.sadakat.qandeel.shared.resources.onboarding_word_by_word
import dev.sadakat.qandeel.shared.resources.onboarding_word_by_word_detail
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Connects [OnboardingScreen] to its ViewModel and calls [onDone] once the choices are saved.
 * [onFinishing] runs as the user finishes, for what only the platform can do (on Android, asking to
 * show notifications for playback and downloads).
 */
@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onFinishing: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(state.done) { if (state.done) currentOnDone() }
    val actions = OnboardingActions(
        onModeChange = viewModel::setMode,
        onVoiceChange = viewModel::setVoice,
        onToggleSample = viewModel::toggleSample,
        onStopSample = viewModel::stopSample,
        onArabicTextSizeChange = viewModel::setArabicTextSize,
        onShowTranslationChange = viewModel::setShowTranslation,
        onWordByWordChange = viewModel::setWordByWord,
        onThemeModeChange = viewModel::setThemeMode,
        onReduceMotionChange = viewModel::setReduceMotion,
        onDownloadStarterChange = viewModel::setDownloadStarter,
        onSendToWatchChange = viewModel::setSendToWatch,
        onFinish = {
            onFinishing()
            viewModel.finish()
        },
        onSkip = viewModel::skip,
    )
    // The theme follows the choices as they are made: the screen is its own preview.
    val choices = state.choices
    CelestialTheme(
        night = choices.themeMode.isNight(isSystemInDarkTheme()),
        reduceMotion = choices.reduceMotion,
        arabicScale = choices.arabicTextSize.scale,
    ) {
        OnboardingScreen(state, actions, modifier)
    }
}

/** Dark sky for night, or the phone's dark mode on Auto; dawn for the rest (sepia included). */
internal fun ThemeMode.isNight(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.DARK -> true
    ThemeMode.LIGHT, ThemeMode.SEPIA -> false
}

internal const val ONBOARDING_PAGES = 5
private const val LISTENING_PAGE = 1

/**
 * Onboarding over the living sky: the lantern above (large on the welcome, smaller after, burning
 * brighter page by page and with a voice's sample), the pages, and the way forward below.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { ONBOARDING_PAGES },
) {
    val scope = rememberCoroutineScope()
    val clock = Celestial.clock
    val page = pagerState.currentPage
    // Leaving the listening page stops a sample that is playing.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { if (it != LISTENING_PAGE) actions.onStopSample() }
    }
    val lampHeight by animateDpAsState(if (page == 0) 300.dp else 150.dp)
    val energy by animateFloatAsState(0.15f + page * 0.12f + if (state.previewing != null) 0.45f else 0f)

    Box(
        modifier
            .fillMaxSize()
            .testTag("onboarding"),
    ) {
        CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.5f, if (page == 0) 0.24f else 0.12f))
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            TopBar(page, onSkip = actions.onSkip)
            QandeelLamp(
                motion = { LampMotion(time = clock.seconds(), energy = energy) },
                colors = Celestial.colors.lamp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lampHeight),
            )
            HorizontalPager(pagerState, Modifier.weight(1f), verticalAlignment = Alignment.Top) { index ->
                Page {
                    when (index) {
                        0 -> WelcomePage()
                        LISTENING_PAGE -> ListeningPage(state, actions)
                        2 -> ReadingPage(state.choices, actions)
                        3 -> LookPage(state.choices, actions)
                        else -> OfflinePage(state, actions)
                    }
                }
            }
            BottomBar(
                page = page,
                onBack = { scope.launch { pagerState.animateScrollToPage(page - 1) } },
                onNext = {
                    if (page == ONBOARDING_PAGES - 1) {
                        actions.onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(page + 1) }
                    }
                },
            )
        }
    }
}

@Composable
private fun TopBar(page: Int, onSkip: () -> Unit) {
    // Always as tall as its button, so the lantern doesn't jump when the button goes.
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Celestial.shapes.touchTarget)
            .padding(horizontal = Celestial.spacing.sm),
        horizontalArrangement = Arrangement.End,
    ) {
        // Skipping is for those who know what they want; on the last page, finishing is as quick.
        if (page < ONBOARDING_PAGES - 1) QuietButton(stringResource(Res.string.onboarding_skip), onClick = onSkip)
    }
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Celestial.spacing.gutter),
        content = content,
    )
}

@Composable
private fun BottomBar(page: Int, onBack: () -> Unit, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageDots(ONBOARDING_PAGES, page)
        Spacer(Modifier.height(Celestial.spacing.lg))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (page > 0) {
                QuietButton(stringResource(Res.string.onboarding_back), onClick = onBack)
                Spacer(Modifier.weight(1f))
            }
            val label = when (page) {
                0 -> Res.string.onboarding_begin
                ONBOARDING_PAGES - 1 -> Res.string.onboarding_finish
                else -> Res.string.onboarding_next
            }
            PrimaryButton(
                stringResource(label),
                onClick = onNext,
                modifier = if (page == 0) Modifier.fillMaxWidth() else Modifier,
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    val colors = Celestial.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            stringResource(Res.string.onboarding_welcome_ayah),
            style = Celestial.quran.title.copy(color = colors.arabic, textAlign = TextAlign.Center),
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        BasicText(
            stringResource(Res.string.onboarding_welcome_meaning),
            style = Celestial.type.body.copy(color = colors.inkMuted, textAlign = TextAlign.Center),
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        Eyebrow(stringResource(Res.string.onboarding_welcome_reference))
        Spacer(Modifier.height(Celestial.spacing.xl))
        BasicText(
            stringResource(Res.string.onboarding_welcome_title),
            style = Celestial.type.display.copy(color = colors.ink, textAlign = TextAlign.Center),
            modifier = Modifier.semantics { heading() },
        )
        BasicText(
            stringResource(Res.string.onboarding_welcome_subtitle),
            style = Celestial.type.body.copy(color = colors.inkMuted, textAlign = TextAlign.Center),
        )
    }
}

@Composable
private fun ColumnScope.PageTitle(title: StringResource, supporting: StringResource) {
    BasicText(
        stringResource(title),
        style = Celestial.type.headline.copy(color = Celestial.colors.ink),
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(Celestial.spacing.xs))
    BasicText(stringResource(supporting), style = Celestial.type.body.copy(color = Celestial.colors.inkMuted))
    Spacer(Modifier.height(Celestial.spacing.lg))
}

private class Option<T>(val value: T, val title: StringResource, val detail: StringResource)

private val modes = listOf(
    Option(RecitationMode.ARABIC_ONLY, Res.string.onboarding_mode_arabic, Res.string.onboarding_mode_arabic_detail),
    Option(
        RecitationMode.ARABIC_ENGLISH,
        Res.string.onboarding_mode_english,
        Res.string.onboarding_mode_english_detail,
    ),
    Option(RecitationMode.ARABIC_BANGLA, Res.string.onboarding_mode_bangla, Res.string.onboarding_mode_bangla_detail),
)

private val voices = listOf(
    Option(BanglaVoice.ISLAMIC_FOUNDATION, Res.string.onboarding_voice_if, Res.string.onboarding_voice_if_detail),
    Option(BanglaVoice.SAYED_ISMAT_TOHA, Res.string.onboarding_voice_toha, Res.string.onboarding_voice_toha_detail),
    Option(
        BanglaVoice.SHAREEF_BAEZEED_MAHMOOD,
        Res.string.onboarding_voice_baezeed,
        Res.string.onboarding_voice_baezeed_detail,
    ),
)

@Composable
private fun ColumnScope.ListeningPage(state: OnboardingUiState, actions: OnboardingActions) {
    PageTitle(Res.string.onboarding_listening_title, Res.string.onboarding_listening_supporting)
    Column(verticalArrangement = Arrangement.spacedBy(Celestial.spacing.sm)) {
        modes.forEach { mode ->
            ChoiceCard(
                title = stringResource(mode.title),
                detail = stringResource(mode.detail),
                selected = state.choices.mode == mode.value,
                onSelect = { actions.onModeChange(mode.value) },
            )
        }
    }
    AnimatedVisibility(state.choices.mode == RecitationMode.ARABIC_BANGLA) {
        Column {
            Spacer(Modifier.height(Celestial.spacing.xl))
            Eyebrow(stringResource(Res.string.onboarding_voice_title))
            Spacer(Modifier.height(Celestial.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(Celestial.spacing.sm)) {
                voices.forEach { voice -> VoiceCard(voice, state, actions) }
            }
        }
    }
}

@Composable
private fun VoiceCard(voice: Option<BanglaVoice>, state: OnboardingUiState, actions: OnboardingActions) {
    val name = stringResource(voice.title)
    val playing = state.previewing == voice.value
    ChoiceCard(
        title = name,
        detail = stringResource(voice.detail),
        selected = state.choices.voice == voice.value,
        onSelect = { actions.onVoiceChange(voice.value) },
    ) {
        GlyphButton(
            icon = if (playing) CelestialIcons.Pause else CelestialIcons.Play,
            label = if (playing) {
                stringResource(Res.string.onboarding_voice_stop)
            } else {
                stringResource(Res.string.onboarding_voice_play, name)
            },
            onClick = { actions.onToggleSample(voice.value) },
            tint = Celestial.colors.accent,
        )
    }
}

@Composable
private fun ColumnScope.ReadingPage(choices: OnboardingChoices, actions: OnboardingActions) {
    val colors = Celestial.colors
    PageTitle(Res.string.onboarding_reading_title, Res.string.onboarding_reading_supporting)
    GlassSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Celestial.spacing.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
            if (choices.wordByWord == WordByWord.OFF) {
                BasicText(
                    stringResource(Res.string.onboarding_reading_sample_ayah),
                    style = Celestial.quran.body.copy(color = colors.arabic, textAlign = TextAlign.Center),
                )
            } else {
                WordByWordSample(choices.wordByWord)
            }
            if (choices.showTranslation) {
                Spacer(Modifier.height(Celestial.spacing.sm))
                BasicText(
                    stringResource(Res.string.onboarding_reading_sample_meaning),
                    style = Celestial.type.body.copy(color = colors.inkMuted, textAlign = TextAlign.Center),
                )
            }
        }
    }
    Spacer(Modifier.height(Celestial.spacing.lg))
    Eyebrow(stringResource(Res.string.onboarding_arabic_size))
    Spacer(Modifier.height(Celestial.spacing.sm))
    val sizes = ArabicTextSize.entries
    SizeSteps(
        descriptions = sizes.indices.map { stringResource(Res.string.onboarding_arabic_size_step, it + 1, sizes.size) },
        selected = sizes.indexOf(choices.arabicTextSize),
        onSelect = { actions.onArabicTextSizeChange(sizes[it]) },
    )
    Spacer(Modifier.height(Celestial.spacing.sm))
    ToggleRow(
        title = stringResource(Res.string.onboarding_show_translation),
        checked = choices.showTranslation,
        onCheckedChange = actions.onShowTranslationChange,
    )
    BasicText(stringResource(Res.string.onboarding_word_by_word), style = Celestial.type.title.copy(color = colors.ink))
    BasicText(
        stringResource(Res.string.onboarding_word_by_word_detail),
        style = Celestial.type.caption.copy(color = colors.inkMuted),
    )
    Spacer(Modifier.height(Celestial.spacing.sm))
    val wbwLabels = mapOf(
        WordByWord.OFF to Res.string.onboarding_wbw_off,
        WordByWord.ENGLISH to Res.string.onboarding_wbw_english,
        WordByWord.BANGLA to Res.string.onboarding_wbw_bangla,
    )
    PillTabs(
        options = WordByWord.entries,
        selected = choices.wordByWord,
        onSelect = actions.onWordByWordChange,
        label = { stringResource(wbwLabels.getValue(it)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private val sampleWords = listOf(
    Res.string.onboarding_sample_word_1,
    Res.string.onboarding_sample_word_2,
    Res.string.onboarding_sample_word_3,
    Res.string.onboarding_sample_word_4,
)
private val sampleEnglish = listOf(
    Res.string.onboarding_sample_en_1,
    Res.string.onboarding_sample_en_2,
    Res.string.onboarding_sample_en_3,
    Res.string.onboarding_sample_en_4,
)
private val sampleBangla = listOf(
    Res.string.onboarding_sample_bn_1,
    Res.string.onboarding_sample_bn_2,
    Res.string.onboarding_sample_bn_3,
    Res.string.onboarding_sample_bn_4,
)

/** Al-Fatiha 1:2 word by word, right to left, each word's meaning under it. */
@Composable
private fun WordByWordSample(language: WordByWord) {
    val colors = Celestial.colors
    val meanings = if (language == WordByWord.BANGLA) sampleBangla else sampleEnglish
    Row(horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.md, Alignment.CenterHorizontally)) {
        // Arabic reads right to left: the first word goes on the right.
        sampleWords.indices.reversed().forEach { i ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    stringResource(sampleWords[i]),
                    style = Celestial.quran.label.copy(color = colors.arabic, textAlign = TextAlign.Center),
                )
                BasicText(
                    stringResource(meanings[i]),
                    style = Celestial.type.caption.copy(color = colors.accent, textAlign = TextAlign.Center),
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.LookPage(choices: OnboardingChoices, actions: OnboardingActions) {
    PageTitle(Res.string.onboarding_look_title, Res.string.onboarding_look_supporting)
    val themes = mapOf(
        ThemeMode.SYSTEM to Res.string.onboarding_theme_auto,
        ThemeMode.LIGHT to Res.string.onboarding_theme_dawn,
        ThemeMode.DARK to Res.string.onboarding_theme_night,
    )
    PillTabs(
        options = themes.keys.toList(),
        selected = if (choices.themeMode == ThemeMode.SEPIA) ThemeMode.LIGHT else choices.themeMode,
        onSelect = actions.onThemeModeChange,
        label = { stringResource(themes.getValue(it)) },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Celestial.spacing.md))
    ToggleRow(
        title = stringResource(Res.string.onboarding_reduce_motion),
        detail = stringResource(Res.string.onboarding_reduce_motion_detail),
        checked = choices.reduceMotion,
        onCheckedChange = actions.onReduceMotionChange,
    )
}

@Composable
private fun ColumnScope.OfflinePage(state: OnboardingUiState, actions: OnboardingActions) {
    val colors = Celestial.colors
    PageTitle(Res.string.onboarding_offline_title, Res.string.onboarding_offline_supporting)
    GlassSurface(Modifier.fillMaxWidth(), radius = Celestial.shapes.tile) {
        Column(Modifier.padding(horizontal = Celestial.spacing.lg)) {
            ToggleRow(
                title = stringResource(Res.string.onboarding_starter),
                detail = stringResource(Res.string.onboarding_starter_detail),
                checked = state.choices.downloadStarter,
                onCheckedChange = actions.onDownloadStarterChange,
            )
            if (state.watchReachable && state.choices.downloadStarter) {
                ToggleRow(
                    title = stringResource(Res.string.onboarding_watch),
                    detail = stringResource(Res.string.onboarding_watch_detail),
                    checked = state.choices.sendToWatch,
                    onCheckedChange = actions.onSendToWatchChange,
                )
            }
        }
    }
    Spacer(Modifier.height(Celestial.spacing.md))
    BasicText(
        stringResource(Res.string.onboarding_offline_later),
        style = Celestial.type.caption.copy(color = colors.inkFaint),
    )
}
