package dev.sadakat.qandeel.shared.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.core.domain.player.NowPlaying
import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads
import dev.sadakat.qandeel.core.domain.repository.WatchConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * What the user has chosen so far. Nothing is saved until they finish (or skip, which keeps the
 * defaults), except the Bangla voice they preview, which the player needs to play it.
 */
data class OnboardingChoices(
    val mode: RecitationMode = RecitationMode.ARABIC_BANGLA,
    val voice: BanglaVoice = BanglaVoice.DEFAULT,
    val arabicTextSize: ArabicTextSize = ArabicTextSize.MEDIUM,
    val showTranslation: Boolean = true,
    val wordByWord: WordByWord = WordByWord.OFF,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val downloadStarter: Boolean = true,
    val sendToWatch: Boolean = true,
)

data class OnboardingUiState(
    val choices: OnboardingChoices = OnboardingChoices(),
    /** The voice whose sample is playing, if one is. */
    val previewing: BanglaVoice? = null,
    /** A watch with Qandeel is in reach, so the starter set can go to it too. */
    val watchReachable: Boolean = false,
    /** Set once the choices are saved: the app moves on to Home. */
    val done: Boolean = false,
)

/**
 * Onboarding: listening, reading, look and motion, then offline and the watch. Finishing saves
 * every choice, starts the starter set's downloads (on the watch too, if asked) and marks
 * onboarding done; skipping marks it done with the defaults.
 */
@Suppress("TooManyFunctions") // One setter per choice, each a one-liner on the choices.
class OnboardingViewModel(
    private val settings: QuranSettings,
    private val player: QuranPlayer,
    private val downloads: SurahDownloads,
    private val watch: WatchConnection,
) : ViewModel() {

    private val choices = MutableStateFlow(OnboardingChoices())
    private val watchReachable = MutableStateFlow(false)
    private val previewVoice = MutableStateFlow<BanglaVoice?>(null)
    private val done = MutableStateFlow(false)

    val uiState: StateFlow<OnboardingUiState> = combine(
        choices,
        watchReachable,
        previewVoice,
        player.nowPlaying,
        done,
    ) { choices, reachable, preview, nowPlaying, done ->
        OnboardingUiState(
            choices = choices,
            previewing = preview.takeIf { nowPlaying.isSample() },
            watchReachable = reachable,
            done = done,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OnboardingUiState())

    init {
        viewModelScope.launch {
            // Pinned, so the voice a sample writes doesn't read as an existing user's settings.
            settings.setOnboardingDone(false)
            choices.update { it.copy(mode = settings.mode.first(), voice = settings.banglaVoice.first()) }
        }
        viewModelScope.launch {
            watchReachable.value = runCatching { watch.isWatchReachable() }.getOrDefault(false)
        }
    }

    fun setMode(mode: RecitationMode) = choices.update { it.copy(mode = mode) }

    fun setArabicTextSize(size: ArabicTextSize) = choices.update { it.copy(arabicTextSize = size) }

    fun setShowTranslation(show: Boolean) = choices.update { it.copy(showTranslation = show) }

    fun setWordByWord(wordByWord: WordByWord) = choices.update { it.copy(wordByWord = wordByWord) }

    fun setThemeMode(mode: ThemeMode) = choices.update { it.copy(themeMode = mode) }

    fun setReduceMotion(reduce: Boolean) = choices.update { it.copy(reduceMotion = reduce) }

    fun setDownloadStarter(download: Boolean) = choices.update { it.copy(downloadStarter = download) }

    fun setSendToWatch(send: Boolean) = choices.update { it.copy(sendToWatch = send) }

    /** The voice whose sample is playing now, if one is. */
    private fun previewing(): BanglaVoice? = previewVoice.value.takeIf { player.nowPlaying.value.isSample() }

    /** Chooses [voice]; playing its sample is [toggleSample]. A sample already playing switches to it. */
    fun setVoice(voice: BanglaVoice) {
        choices.update { it.copy(voice = voice) }
        val playing = previewing()
        if (playing != null && playing != voice) toggleSample(voice)
    }

    /** Plays the opening of Al-Fatiha in [voice] (choosing it), or stops it if it is the one playing. */
    fun toggleSample(voice: BanglaVoice) {
        if (previewing() == voice) {
            stopSample()
            return
        }
        choices.update { it.copy(voice = voice) }
        previewVoice.value = voice
        viewModelScope.launch {
            settings.setBanglaVoice(voice)
            player.play(SAMPLE_SURAH, fromAyah = 1, mode = RecitationMode.ARABIC_BANGLA)
        }
    }

    /** Stops a sample that is playing; nothing else. */
    fun stopSample() {
        if (previewing() != null) player.stop()
        previewVoice.value = null
    }

    /** Saves every choice, starts the starter downloads, and moves on. */
    fun finish() {
        stopSample()
        val chosen = choices.value
        viewModelScope.launch {
            settings.setMode(chosen.mode)
            settings.setBanglaVoice(chosen.voice)
            settings.updateReadingPrefs {
                it.copy(
                    arabicTextSize = chosen.arabicTextSize,
                    showTranslation = chosen.showTranslation,
                    wordByWord = chosen.wordByWord,
                    themeMode = chosen.themeMode,
                    reduceMotion = chosen.reduceMotion,
                )
            }
            if (chosen.downloadStarter) {
                val tracks = chosen.mode.tracks(chosen.voice)
                STARTER_SURAHS.forEach { downloads.download(it, tracks) }
                if (chosen.sendToWatch && watchReachable.value) {
                    STARTER_SURAHS.forEach { watch.sendDownload(it, tracks) }
                }
            }
            settings.setOnboardingDone(true)
            done.value = true
        }
    }

    /** Leaves onboarding with the defaults. */
    fun skip() {
        stopSample()
        viewModelScope.launch {
            settings.setOnboardingDone(true)
            done.value = true
        }
    }

    /** A sample is playing while the player is on Al-Fatiha in Arabic + Bangla, playing or loading. */
    private fun NowPlaying?.isSample(): Boolean =
        this != null && surah == SAMPLE_SURAH && mode == RecitationMode.ARABIC_BANGLA && (isPlaying || isBuffering)

    companion object {
        /** Al-Fatiha and the last ten surahs: the ones recited most, in every prayer. */
        val STARTER_SURAHS: List<Int> = listOf(1) + (105..114)

        /** The surah a voice's sample plays from. */
        const val SAMPLE_SURAH = 1

        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
