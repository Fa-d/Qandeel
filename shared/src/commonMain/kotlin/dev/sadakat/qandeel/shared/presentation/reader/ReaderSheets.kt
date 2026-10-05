package dev.sadakat.qandeel.shared.presentation.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.core.domain.player.WordPointer
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialSheet
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PrimaryButton
import dev.sadakat.qandeel.shared.designsystem.kit.QuietButton
import dev.sadakat.qandeel.shared.designsystem.kit.SheetAction
import dev.sadakat.qandeel.shared.designsystem.kit.SizeSteps
import dev.sadakat.qandeel.shared.designsystem.kit.ToggleRow
import dev.sadakat.qandeel.shared.presentation.components.WordByWordText
import dev.sadakat.qandeel.shared.presentation.you.SettingsUiState
import dev.sadakat.qandeel.shared.presentation.you.YouTabActions
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.ayah_action_copy
import dev.sadakat.qandeel.shared.resources.ayah_action_play
import dev.sadakat.qandeel.shared.resources.ayah_action_repeat
import dev.sadakat.qandeel.shared.resources.ayah_action_share
import dev.sadakat.qandeel.shared.resources.ayah_actions_title
import dev.sadakat.qandeel.shared.resources.ayah_actions_words
import dev.sadakat.qandeel.shared.resources.ayah_share_reference
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size
import dev.sadakat.qandeel.shared.resources.onboarding_arabic_size_step
import dev.sadakat.qandeel.shared.resources.onboarding_show_translation
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_bangla
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_english
import dev.sadakat.qandeel.shared.resources.onboarding_wbw_off
import dev.sadakat.qandeel.shared.resources.onboarding_word_by_word
import dev.sadakat.qandeel.shared.resources.player_cancel
import dev.sadakat.qandeel.shared.resources.reader_quick_settings
import dev.sadakat.qandeel.shared.resources.reader_remove
import dev.sadakat.qandeel.shared.resources.remove_download_text
import dev.sadakat.qandeel.shared.resources.remove_download_title
import dev.sadakat.qandeel.shared.resources.send_to_watch
import dev.sadakat.qandeel.shared.resources.you_follow_along
import dev.sadakat.qandeel.shared.resources.you_follow_along_detail
import org.jetbrains.compose.resources.stringResource

/**
 * A long-pressed ayah: its Arabic and meaning, what to do with it (play from it, repeat it, copy
 * or share it), and its words with their meanings.
 */
@Composable
fun AyahActionsSheet(
    surahName: String,
    surahNumber: Int,
    ayah: AyahActionsUi,
    actions: ReaderActions,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Celestial.colors
    val clipboard = LocalClipboardManager.current
    val reference = stringResource(Res.string.ayah_share_reference, surahName, surahNumber, ayah.ayah)
    val shareText = ayahShareText(ayah.arabic, ayah.translation, reference)
    CelestialSheet(onDismiss = actions.onDismissAyahActions, modifier = modifier) {
        Eyebrow(stringResource(Res.string.ayah_actions_title, surahName, surahNumber, ayah.ayah))
        Spacer(Modifier.height(Celestial.spacing.md))
        BasicText(
            ayah.arabic,
            style = Celestial.quran.body.copy(color = colors.arabic, textAlign = TextAlign.Right),
            modifier = Modifier.fillMaxWidth(),
        )
        ayah.translation?.let {
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(it, style = Celestial.type.body.copy(color = colors.inkMuted))
        }
        Spacer(Modifier.height(Celestial.spacing.md))
        SheetAction(stringResource(Res.string.ayah_action_play), onClick = {
            actions.onDismissAyahActions()
            actions.onPlayAyah(ayah.ayah)
        })
        SheetAction(stringResource(Res.string.ayah_action_repeat), onClick = {
            actions.onDismissAyahActions()
            actions.onRepeatAyah(ayah.ayah)
        })
        SheetAction(stringResource(Res.string.ayah_action_copy), onClick = {
            clipboard.setText(AnnotatedString(shareText))
            actions.onDismissAyahActions()
            onCopy()
        })
        SheetAction(stringResource(Res.string.ayah_action_share), onClick = {
            actions.onDismissAyahActions()
            actions.onShareAyah(shareText)
        })
        if (ayah.words.any { it.meaning != null }) {
            Spacer(Modifier.height(Celestial.spacing.md))
            Eyebrow(stringResource(Res.string.ayah_actions_words))
            Spacer(Modifier.height(Celestial.spacing.sm))
            WordByWordText(
                text = ayah.words.joinToString(" ") { it.arabic },
                meanings = ayah.words.map { it.meaning.orEmpty() },
                pointer = WordPointer.Off,
                style = Celestial.quran.label,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The reader's quick text settings: what changes how the page reads, without leaving it. */
@Composable
fun ReaderQuickSettingsSheet(
    settings: SettingsUiState,
    actions: YouTabActions,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = settings.prefs
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        Eyebrow(stringResource(Res.string.reader_quick_settings))
        Spacer(Modifier.height(Celestial.spacing.md))
        BasicText(
            stringResource(Res.string.onboarding_arabic_size),
            style = Celestial.type.title.copy(color = Celestial.colors.ink),
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        val sizes = ArabicTextSize.entries
        SizeSteps(
            descriptions = sizes.indices.map {
                stringResource(Res.string.onboarding_arabic_size_step, it + 1, sizes.size)
            },
            selected = sizes.indexOf(prefs.arabicTextSize),
            onSelect = { actions.onArabicTextSizeChange(sizes[it]) },
        )
        ToggleRow(
            stringResource(Res.string.onboarding_show_translation),
            prefs.showTranslation,
            actions.onShowTranslationChange,
        )
        ToggleRow(
            title = stringResource(Res.string.you_follow_along),
            detail = stringResource(Res.string.you_follow_along_detail),
            checked = prefs.followAlong,
            onCheckedChange = actions.onFollowAlongChange,
        )
        BasicText(
            stringResource(Res.string.onboarding_word_by_word),
            style = Celestial.type.title.copy(color = Celestial.colors.ink),
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        val labels = mapOf(
            WordByWord.OFF to Res.string.onboarding_wbw_off,
            WordByWord.ENGLISH to Res.string.onboarding_wbw_english,
            WordByWord.BANGLA to Res.string.onboarding_wbw_bangla,
        )
        PillTabs(
            options = WordByWord.entries,
            selected = prefs.wordByWord,
            onSelect = actions.onWordByWordChange,
            label = { stringResource(labels.getValue(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The reader's other actions: sending the surah to the watch. */
@Composable
fun ReaderMoreSheet(onSendToWatch: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        SheetAction(stringResource(Res.string.send_to_watch), onClick = {
            onDismiss()
            onSendToWatch()
        })
    }
}

/** Removing a download asks first: it is a lot to fetch again. */
@Composable
fun RemoveDownloadSheet(surahName: String, onRemove: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        BasicText(
            stringResource(Res.string.remove_download_title),
            style = Celestial.type.title.copy(color = Celestial.colors.ink),
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        BasicText(
            stringResource(Res.string.remove_download_text, surahName),
            style = Celestial.type.body.copy(color = Celestial.colors.inkMuted),
        )
        Spacer(Modifier.height(Celestial.spacing.lg))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuietButton(stringResource(Res.string.player_cancel), onClick = onDismiss)
            Spacer(Modifier.width(Celestial.spacing.sm))
            PrimaryButton(stringResource(Res.string.reader_remove), onClick = {
                onDismiss()
                onRemove()
            })
        }
    }
}
