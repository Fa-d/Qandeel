package dev.sadakat.qandeel.shared.presentation.onboarding

import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.ReadingPrefs
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.Track
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.core.testing.FakeQuranPlayer
import dev.sadakat.qandeel.core.testing.FakeQuranSettings
import dev.sadakat.qandeel.core.testing.FakeSurahDownloads
import dev.sadakat.qandeel.core.testing.FakeWatchConnection
import dev.sadakat.qandeel.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeQuranSettings(onboardingDone = false)
    private val player = FakeQuranPlayer()
    private val downloads = FakeSurahDownloads()
    private val watch = FakeWatchConnection().apply { reachable = false }

    private fun viewModel() = OnboardingViewModel(settings, player, downloads, watch)

    /** Keeps [vm]'s state collected, updating as soon as anything changes. */
    private fun TestScope.observe(vm: OnboardingViewModel) =
        vm.uiState.launchIn(backgroundScope + UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `it starts from the stored mode and voice, and pins onboarding as not done`() = runTest {
        settings.mode.value = RecitationMode.ARABIC_ENGLISH
        settings.banglaVoice.value = BanglaVoice.SAYED_ISMAT_TOHA
        settings.onboardingDone.value = true

        val vm = viewModel()
        observe(vm)

        assertEquals(RecitationMode.ARABIC_ENGLISH, vm.uiState.value.choices.mode)
        assertEquals(BanglaVoice.SAYED_ISMAT_TOHA, vm.uiState.value.choices.voice)
        assertFalse(settings.onboardingDone.value)
    }

    @Test
    fun `finishing saves every choice and marks onboarding done`() = runTest {
        val vm = viewModel()
        observe(vm)
        vm.setMode(RecitationMode.ARABIC_ENGLISH)
        vm.setArabicTextSize(ArabicTextSize.LARGE)
        vm.setShowTranslation(false)
        vm.setWordByWord(WordByWord.BANGLA)
        vm.setThemeMode(ThemeMode.DARK)
        vm.setReduceMotion(true)

        vm.finish()

        assertEquals(RecitationMode.ARABIC_ENGLISH, settings.mode.value)
        assertEquals(
            ReadingPrefs(
                arabicTextSize = ArabicTextSize.LARGE,
                showTranslation = false,
                wordByWord = WordByWord.BANGLA,
                themeMode = ThemeMode.DARK,
                reduceMotion = true,
            ),
            settings.readingPrefs.value,
        )
        assertTrue(settings.onboardingDone.value)
        assertTrue(vm.uiState.value.done)
    }

    @Test
    fun `finishing downloads the starter set for the chosen mode and voice`() = runTest {
        val vm = viewModel()
        vm.setVoice(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD)

        vm.finish()

        val tracks = listOf(Track.ARABIC_SUDAIS, Track.BANGLA_BAEZEED)
        assertEquals(OnboardingViewModel.STARTER_SURAHS.map { it to tracks }, downloads.downloadRequests)
        assertEquals(listOf(1, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114), OnboardingViewModel.STARTER_SURAHS)
    }

    @Test
    fun `without the starter set nothing is downloaded`() = runTest {
        val vm = viewModel()
        vm.setDownloadStarter(false)

        vm.finish()

        assertTrue(downloads.downloadRequests.isEmpty())
    }

    @Test
    fun `a reachable watch gets the starter set too, unless declined`() = runTest {
        watch.reachable = true
        val vm = viewModel()
        observe(vm)
        assertTrue(vm.uiState.value.watchReachable)

        vm.finish()

        assertEquals(OnboardingViewModel.STARTER_SURAHS, watch.sentSurahs.map { it.first })
    }

    @Test
    fun `a declined watch gets nothing`() = runTest {
        watch.reachable = true
        val vm = viewModel()
        vm.setSendToWatch(false)

        vm.finish()

        assertTrue(watch.sentSurahs.isEmpty())
    }

    @Test
    fun `an unreachable watch is never offered or sent to`() = runTest {
        val vm = viewModel()
        observe(vm)

        vm.finish()

        assertFalse(vm.uiState.value.watchReachable)
        assertTrue(watch.sentSurahs.isEmpty())
    }

    @Test
    fun `skipping keeps the defaults and marks onboarding done`() = runTest {
        val vm = viewModel()
        observe(vm)
        vm.setThemeMode(ThemeMode.DARK)

        vm.skip()

        assertEquals(ReadingPrefs(), settings.readingPrefs.value)
        assertTrue(downloads.downloadRequests.isEmpty())
        assertTrue(settings.onboardingDone.value)
        assertTrue(vm.uiState.value.done)
    }

    @Test
    fun `a sample plays Al-Fatiha in that voice, and a second tap stops it`() = runTest {
        val vm = viewModel()
        observe(vm)

        vm.toggleSample(BanglaVoice.SAYED_ISMAT_TOHA)

        assertEquals(FakeQuranPlayer.PlayCall(1, 1, RecitationMode.ARABIC_BANGLA), player.playCalls.single())
        assertEquals(BanglaVoice.SAYED_ISMAT_TOHA, settings.banglaVoice.value)
        assertEquals(BanglaVoice.SAYED_ISMAT_TOHA, vm.uiState.value.previewing)
        assertEquals(BanglaVoice.SAYED_ISMAT_TOHA, vm.uiState.value.choices.voice)

        vm.toggleSample(BanglaVoice.SAYED_ISMAT_TOHA)

        assertNull(player.nowPlaying.value)
        assertNull(vm.uiState.value.previewing)
    }

    @Test
    fun `choosing another voice while a sample plays plays that voice instead`() = runTest {
        val vm = viewModel()
        observe(vm)
        vm.toggleSample(BanglaVoice.SAYED_ISMAT_TOHA)

        vm.setVoice(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD)

        assertEquals(2, player.playCalls.size)
        assertEquals(BanglaVoice.SHAREEF_BAEZEED_MAHMOOD, vm.uiState.value.previewing)
    }

    @Test
    fun `finishing stops a sample that is playing`() = runTest {
        val vm = viewModel()
        observe(vm)
        vm.toggleSample(BanglaVoice.ISLAMIC_FOUNDATION)

        vm.finish()

        assertNull(player.nowPlaying.value)
    }

    @Test
    fun `stopping when no sample plays leaves the player alone`() = runTest {
        player.play(36, 1, RecitationMode.ARABIC_ONLY)
        val vm = viewModel()

        vm.stopSample()

        assertEquals(36, player.nowPlaying.value?.surah)
    }

    @Test
    fun `night follows the phone on auto, and dawn covers light and sepia`() {
        assertTrue(ThemeMode.SYSTEM.isNight(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isNight(systemDark = false))
        assertTrue(ThemeMode.DARK.isNight(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isNight(systemDark = true))
        assertFalse(ThemeMode.SEPIA.isNight(systemDark = true))
    }
}
