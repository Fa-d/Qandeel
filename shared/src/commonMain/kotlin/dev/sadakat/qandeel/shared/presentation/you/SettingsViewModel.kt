package dev.sadakat.qandeel.shared.presentation.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.ReadingPrefs
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val mode: RecitationMode = RecitationMode.ARABIC_BANGLA,
    val voice: BanglaVoice = BanglaVoice.DEFAULT,
    val prefs: ReadingPrefs = ReadingPrefs(),
)

/**
 * Every setting, in one place under You: listening (the mode and the Bangla voice), reading (size,
 * meaning, following along, word by word) and appearance (night or dawn, motion). Each applies at
 * once; a change to what plays re-queues the recitation at the same ayah, playing or paused as it was.
 */
@Suppress("TooManyFunctions") // One setter per setting, each a one-liner on the settings.
class SettingsViewModel(
    private val settings: QuranSettings,
    private val player: QuranPlayer,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(settings.mode, settings.banglaVoice, settings.readingPrefs, ::SettingsUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    fun setMode(mode: RecitationMode) {
        viewModelScope.launch {
            settings.setMode(mode)
            player.nowPlaying.value?.let { current ->
                if (current.mode != mode) player.play(current.surah, current.ayah, mode, playWhenReady = current.isPlaying)
            }
        }
    }

    fun setBanglaVoice(voice: BanglaVoice) {
        viewModelScope.launch {
            settings.setBanglaVoice(voice)
            player.nowPlaying.value?.let { current ->
                if (current.mode == RecitationMode.ARABIC_BANGLA && current.voice != voice) {
                    player.play(current.surah, current.ayah, current.mode, playWhenReady = current.isPlaying)
                }
            }
        }
    }

    fun setArabicTextSize(size: ArabicTextSize) = update { it.copy(arabicTextSize = size) }

    fun setShowTranslation(show: Boolean) = update { it.copy(showTranslation = show) }

    fun setFollowAlong(follow: Boolean) = update { it.copy(followAlong = follow) }

    fun setWordByWord(language: WordByWord) = update { it.copy(wordByWord = language) }

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun setReduceMotion(reduce: Boolean) = update { it.copy(reduceMotion = reduce) }

    private fun update(transform: (ReadingPrefs) -> ReadingPrefs) {
        viewModelScope.launch { settings.updateReadingPrefs(transform) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
