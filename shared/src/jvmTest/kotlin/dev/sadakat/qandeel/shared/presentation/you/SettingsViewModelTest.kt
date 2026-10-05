package dev.sadakat.qandeel.shared.presentation.you

import app.cash.turbine.test
import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.ReadingPrefs
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.core.domain.player.NowPlaying
import dev.sadakat.qandeel.core.domain.model.Track
import dev.sadakat.qandeel.core.testing.FakeQuranPlayer
import dev.sadakat.qandeel.core.testing.FakeQuranSettings
import dev.sadakat.qandeel.core.testing.MainDispatcherRule
import dev.sadakat.qandeel.core.testing.awaitWhere
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeQuranSettings()
    private val player = FakeQuranPlayer()
    private fun viewModel() = SettingsViewModel(settings, player)

    private fun playing(mode: RecitationMode, voice: BanglaVoice = BanglaVoice.DEFAULT, isPlaying: Boolean = true) {
        player.nowPlaying.value = NowPlaying(18, 10, Track.ARABIC, mode, isPlaying = isPlaying, isBuffering = false, voice = voice)
    }

    @Test
    fun `the state mirrors the stored settings`() = runTest {
        settings.mode.value = RecitationMode.ARABIC_ENGLISH
        settings.banglaVoice.value = BanglaVoice.SAYED_ISMAT_TOHA
        settings.readingPrefs.value = ReadingPrefs(wordByWord = WordByWord.BANGLA)

        viewModel().uiState.test {
            val state = awaitWhere { it.mode == RecitationMode.ARABIC_ENGLISH }
            assertEquals(BanglaVoice.SAYED_ISMAT_TOHA, state.voice)
            assertEquals(WordByWord.BANGLA, state.prefs.wordByWord)
        }
    }

    @Test
    fun `every reading and appearance setting is stored`() = runTest {
        val vm = viewModel()

        vm.setArabicTextSize(ArabicTextSize.XXLARGE)
        vm.setShowTranslation(false)
        vm.setFollowAlong(false)
        vm.setWordByWord(WordByWord.ENGLISH)
        vm.setThemeMode(ThemeMode.DARK)
        vm.setReduceMotion(true)

        assertEquals(
            ReadingPrefs(
                arabicTextSize = ArabicTextSize.XXLARGE,
                showTranslation = false,
                followAlong = false,
                wordByWord = WordByWord.ENGLISH,
                themeMode = ThemeMode.DARK,
                reduceMotion = true,
            ),
            settings.readingPrefs.value,
        )
    }

    @Test
    fun `a new mode is stored, and what plays continues in it at the same ayah`() = runTest {
        playing(RecitationMode.ARABIC_BANGLA, isPlaying = false)

        viewModel().setMode(RecitationMode.ARABIC_ONLY)

        assertEquals(RecitationMode.ARABIC_ONLY, settings.mode.value)
        assertEquals(FakeQuranPlayer.PlayCall(18, 10, RecitationMode.ARABIC_ONLY, playWhenReady = false), player.playCalls.single())
    }

    @Test
    fun `the same mode, or nothing queued, re-queues nothing`() = runTest {
        val vm = viewModel()
        vm.setMode(RecitationMode.ARABIC_ENGLISH)
        playing(RecitationMode.ARABIC_ENGLISH)
        vm.setMode(RecitationMode.ARABIC_ENGLISH)

        assertTrue(player.playCalls.isEmpty())
    }

    @Test
    fun `a new voice re-queues Arabic and Bangla at the same ayah`() = runTest {
        playing(RecitationMode.ARABIC_BANGLA)

        viewModel().setBanglaVoice(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD)

        assertEquals(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD, settings.banglaVoice.value)
        assertEquals(FakeQuranPlayer.PlayCall(18, 10, RecitationMode.ARABIC_BANGLA), player.playCalls.single())
    }

    @Test
    fun `a new voice leaves another mode's playback alone`() = runTest {
        playing(RecitationMode.ARABIC_ENGLISH)

        viewModel().setBanglaVoice(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD)

        assertTrue(player.playCalls.isEmpty())
    }
}
