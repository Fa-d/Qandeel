package dev.sadakat.qandeel.shared.presentation.you

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.ChoiceCard
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.Glyph
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.ProgressLine
import dev.sadakat.qandeel.shared.designsystem.kit.SizeSteps
import dev.sadakat.qandeel.shared.designsystem.kit.ToggleRow
import dev.sadakat.qandeel.shared.presentation.components.ScreenHeader
import dev.sadakat.qandeel.shared.presentation.components.listenTime
import dev.sadakat.qandeel.shared.presentation.progress.ProgressUiState
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size_step
import dev.sadakat.qandeel.shared.resources.onboarding_reduce_motion
import dev.sadakat.qandeel.shared.resources.onboarding_reduce_motion_detail
import dev.sadakat.qandeel.shared.resources.onboarding_show_translation
import dev.sadakat.qandeel.shared.resources.onboarding_theme_auto
import dev.sadakat.qandeel.shared.resources.onboarding_theme_dawn
import dev.sadakat.qandeel.shared.resources.onboarding_theme_night
import dev.sadakat.qandeel.shared.resources.onboarding_voice_baezeed
import dev.sadakat.qandeel.shared.resources.onboarding_voice_baezeed_detail
import dev.sadakat.qandeel.shared.resources.onboarding_voice_if
import dev.sadakat.qandeel.shared.resources.onboarding_voice_if_detail
import dev.sadakat.qandeel.shared.resources.onboarding_voice_title
import dev.sadakat.qandeel.shared.resources.onboarding_voice_toha
import dev.sadakat.qandeel.shared.resources.onboarding_voice_toha_detail
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_bangla
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_english
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_off
import dev.sadakat.qandeel.shared.resources.onboarding_word_by_word
import dev.sadakat.qandeel.shared.resources.progress_coverage_line
import dev.sadakat.qandeel.shared.resources.progress_of_ayahs_heard
import dev.sadakat.qandeel.shared.resources.you_about
import dev.sadakat.qandeel.shared.resources.you_about_detail
import dev.sadakat.qandeel.shared.resources.you_follow_along
import dev.sadakat.qandeel.shared.resources.you_follow_along_detail
import dev.sadakat.qandeel.shared.resources.you_mode
import dev.sadakat.qandeel.shared.resources.you_mode_arabic
import dev.sadakat.qandeel.shared.resources.you_mode_bangla
import dev.sadakat.qandeel.shared.resources.you_mode_english
import dev.sadakat.qandeel.shared.resources.you_progress_open
import dev.sadakat.qandeel.shared.resources.you_settings_appearance
import dev.sadakat.qandeel.shared.resources.you_settings_listening
import dev.sadakat.qandeel.shared.resources.you_settings_reading
import dev.sadakat.qandeel.shared.resources.you_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** What the You tab's controls do: the settings ViewModel's setters, and where it leads. */
data class YouTabActions(
    val onModeChange: (RecitationMode) -> Unit = {},
    val onVoiceChange: (BanglaVoice) -> Unit = {},
    val onArabicTextSizeChange: (ArabicTextSize) -> Unit = {},
    val onShowTranslationChange: (Boolean) -> Unit = {},
    val onFollowAlongChange: (Boolean) -> Unit = {},
    val onWordByWordChange: (WordByWord) -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onReduceMotionChange: (Boolean) -> Unit = {},
    val onOpenProgress: () -> Unit = {},
    val onOpenAbout: () -> Unit = {},
)

/**
 * You: how far you've come (a card that opens Progress), then every setting, grouped by what it
 * changes (listening, reading, appearance), and the sources and credits.
 */
@Composable
fun YouTab(
    settings: SettingsUiState,
    progress: ProgressUiState,
    actions: YouTabActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize()) {
        CelestialSky(Modifier.fillMaxSize(), scroll = { scroll.value.toFloat() }, glowCenter = Offset(0.15f, 0.02f))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(contentPadding)
                .padding(horizontal = Celestial.spacing.gutter)
                .testTag("you"),
        ) {
            Spacer(Modifier.height(Celestial.spacing.xl))
            ScreenHeader(stringResource(Res.string.you_title))
            Spacer(Modifier.height(Celestial.spacing.lg))
            ProgressCard(progress, onClick = actions.onOpenProgress)
            ListeningSection(settings, actions)
            ReadingSection(settings, actions)
            AppearanceSection(settings, actions)
            Section(Res.string.you_about) {
                LinkRow(stringResource(Res.string.you_about), stringResource(Res.string.you_about_detail), actions.onOpenAbout)
            }
            Spacer(Modifier.height(Celestial.spacing.xl))
        }
    }
}

@Composable
private fun ProgressCard(progress: ProgressUiState, onClick: () -> Unit) {
    val colors = Celestial.colors
    GlassSurface(Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("you_progress")) {
        Column(Modifier.padding(Celestial.spacing.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(stringResource(Res.string.you_progress_open))
                Spacer(Modifier.weight(1f))
                Glyph(CelestialIcons.Chevron, tint = colors.inkMuted, size = Celestial.spacing.lg)
            }
            Spacer(Modifier.height(Celestial.spacing.sm))
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(progress.ayahsHeard.toString(), style = Celestial.type.display.copy(color = colors.ink))
                Spacer(Modifier.width(Celestial.spacing.sm))
                BasicText(
                    stringResource(Res.string.progress_of_ayahs_heard, TOTAL_AYAHS),
                    style = Celestial.type.body.copy(color = colors.inkMuted),
                    modifier = Modifier.padding(bottom = Celestial.spacing.xs),
                )
            }
            Spacer(Modifier.height(Celestial.spacing.md))
            ProgressLine(progress.coverage, Modifier.fillMaxWidth().height(Celestial.spacing.xs))
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(
                stringResource(Res.string.progress_coverage_line, (progress.coverage * 100).roundToInt(), listenTime(progress.listenedMs)),
                style = Celestial.type.caption.copy(color = colors.inkMuted),
            )
        }
    }
}

private const val TOTAL_AYAHS = "6,236"

@Composable
private fun Section(title: StringResource, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(Celestial.spacing.xl))
    Eyebrow(stringResource(title))
    Spacer(Modifier.height(Celestial.spacing.sm))
    GlassSurface(Modifier.fillMaxWidth(), radius = Celestial.shapes.tile) {
        Column(Modifier.padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.sm), content = content)
    }
}

private val voiceNames = listOf(
    Triple(BanglaVoice.ISLAMIC_FOUNDATION, Res.string.onboarding_voice_if, Res.string.onboarding_voice_if_detail),
    Triple(BanglaVoice.SAYED_ISMAT_TOHA, Res.string.onboarding_voice_toha, Res.string.onboarding_voice_toha_detail),
    Triple(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD, Res.string.onboarding_voice_baezeed, Res.string.onboarding_voice_baezeed_detail),
)

@Composable
private fun ListeningSection(settings: SettingsUiState, actions: YouTabActions) {
    Section(Res.string.you_settings_listening) {
        Label(Res.string.you_mode)
        val modes = mapOf(
            RecitationMode.ARABIC_ONLY to Res.string.you_mode_arabic,
            RecitationMode.ARABIC_ENGLISH to Res.string.you_mode_english,
            RecitationMode.ARABIC_BANGLA to Res.string.you_mode_bangla,
        )
        PillTabs(
            options = RecitationMode.entries,
            selected = settings.mode,
            onSelect = actions.onModeChange,
            label = { stringResource(modes.getValue(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedVisibility(settings.mode == RecitationMode.ARABIC_BANGLA) {
            Column {
                Label(Res.string.onboarding_voice_title)
                Column(verticalArrangement = Arrangement.spacedBy(Celestial.spacing.sm)) {
                    voiceNames.forEach { (voice, name, detail) ->
                        ChoiceCard(
                            title = stringResource(name),
                            detail = stringResource(detail),
                            selected = settings.voice == voice,
                            onSelect = { actions.onVoiceChange(voice) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(Celestial.spacing.md))
    }
}

@Composable
private fun ReadingSection(settings: SettingsUiState, actions: YouTabActions) {
    val prefs = settings.prefs
    Section(Res.string.you_settings_reading) {
        Label(Res.string.onboarding_arabic_size)
        val sizes = ArabicTextSize.entries
        SizeSteps(
            descriptions = sizes.indices.map { stringResource(Res.string.onboarding_arabic_size_step, it + 1, sizes.size) },
            selected = sizes.indexOf(prefs.arabicTextSize),
            onSelect = { actions.onArabicTextSizeChange(sizes[it]) },
        )
        ToggleRow(
            title = stringResource(Res.string.onboarding_show_translation),
            checked = prefs.showTranslation,
            onCheckedChange = actions.onShowTranslationChange,
        )
        ToggleRow(
            title = stringResource(Res.string.you_follow_along),
            detail = stringResource(Res.string.you_follow_along_detail),
            checked = prefs.followAlong,
            onCheckedChange = actions.onFollowAlongChange,
        )
        Label(Res.string.onboarding_word_by_word)
        val wordByWord = mapOf(
            WordByWord.OFF to Res.string.onboarding_wbw_off,
            WordByWord.ENGLISH to Res.string.onboarding_wbw_english,
            WordByWord.BANGLA to Res.string.onboarding_wbw_bangla,
        )
        PillTabs(
            options = WordByWord.entries,
            selected = prefs.wordByWord,
            onSelect = actions.onWordByWordChange,
            label = { stringResource(wordByWord.getValue(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Celestial.spacing.md))
    }
}

@Composable
private fun AppearanceSection(settings: SettingsUiState, actions: YouTabActions) {
    val prefs = settings.prefs
    Section(Res.string.you_settings_appearance) {
        Spacer(Modifier.height(Celestial.spacing.sm))
        val themes = mapOf(
            ThemeMode.SYSTEM to Res.string.onboarding_theme_auto,
            ThemeMode.LIGHT to Res.string.onboarding_theme_dawn,
            ThemeMode.DARK to Res.string.onboarding_theme_night,
        )
        PillTabs(
            options = themes.keys.toList(),
            selected = if (prefs.themeMode == ThemeMode.SEPIA) ThemeMode.LIGHT else prefs.themeMode,
            onSelect = actions.onThemeModeChange,
            label = { stringResource(themes.getValue(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        ToggleRow(
            title = stringResource(Res.string.onboarding_reduce_motion),
            detail = stringResource(Res.string.onboarding_reduce_motion_detail),
            checked = prefs.reduceMotion,
            onCheckedChange = actions.onReduceMotionChange,
        )
    }
}

@Composable
private fun Label(text: StringResource) {
    BasicText(
        stringResource(text),
        style = Celestial.type.title.copy(color = Celestial.colors.ink),
        modifier = Modifier.padding(top = Celestial.spacing.md, bottom = Celestial.spacing.sm),
    )
}

@Composable
private fun LinkRow(title: String, detail: String, onClick: () -> Unit) {
    val colors = Celestial.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(title, style = Celestial.type.title.copy(color = colors.ink))
            BasicText(detail, style = Celestial.type.caption.copy(color = colors.inkMuted))
        }
        Glyph(CelestialIcons.Chevron, tint = colors.inkMuted, size = Celestial.spacing.lg)
    }
}
