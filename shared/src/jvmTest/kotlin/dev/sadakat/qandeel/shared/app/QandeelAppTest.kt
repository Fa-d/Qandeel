package dev.sadakat.qandeel.shared.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import dev.sadakat.qandeel.core.domain.model.ReadingPrefs
import dev.sadakat.qandeel.core.domain.model.Track
import dev.sadakat.qandeel.core.domain.player.PlaybackSpeed
import dev.sadakat.qandeel.core.domain.player.SleepOption
import dev.sadakat.qandeel.core.domain.repository.SurahDownloadState
import dev.sadakat.qandeel.core.testing.FakeAudioTimings
import dev.sadakat.qandeel.core.testing.FakeListeningHistory
import dev.sadakat.qandeel.core.testing.FakeQuranPlayer
import dev.sadakat.qandeel.core.testing.FakeQuranSettings
import dev.sadakat.qandeel.core.testing.FakeQuranText
import dev.sadakat.qandeel.core.testing.FakeSurahDownloads
import dev.sadakat.qandeel.core.testing.FakeWatchConnection
import dev.sadakat.qandeel.core.testing.FakeWordMeanings
import dev.sadakat.qandeel.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

/**
 * The whole shared app over fakes, as a user walks it: onboarding, the tabs, a surah in the
 * reader with its sheets, the full player with its options, and Progress.
 */
@OptIn(ExperimentalTestApi::class)
class QandeelAppTest {

    // ViewModels and the navigation's lifecycle run on Dispatchers.Main.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class TestGraph(onboardingDone: Boolean = true) : QandeelGraph {
        override val quranText = FakeQuranText()

        // Reduce motion stills the sky and the lamp, so the UI goes idle between steps.
        override val settings = FakeQuranSettings(
            readingPrefs = ReadingPrefs(reduceMotion = true),
            onboardingDone = onboardingDone,
        )
        override val downloads = FakeSurahDownloads()
        override val player = FakeQuranPlayer()
        override val history = FakeListeningHistory()
        override val timings = FakeAudioTimings()
        override val wordMeanings = FakeWordMeanings()
        override val watch = FakeWatchConnection()
        override fun now(): Long = NOW
    }

    private class TestPlatform : QandeelPlatform {
        override val versionName = "1.0"
        val shared = mutableListOf<String>()
        override fun shareText(text: String) {
            shared += text
        }
    }

    private fun app(graph: TestGraph, platform: TestPlatform = TestPlatform(), test: ComposeUiTest.() -> Unit) =
        // A walk draws a few hundred frames of the sky on the CPU: more than runTest's default minute on CI.
        runDesktopComposeUiTest(width = WIDTH, height = HEIGHT, testTimeout = WALK_TIMEOUT) {
            // The sky's clock runs for as long as motion is on, so the UI is never idle on its own:
            // the test moves time forward itself after every step.
            mainClock.autoAdvance = false
            setContent {
                // A phone's layout in dp at a low density: every frame draws the sky on the CPU here.
                CompositionLocalProvider(LocalDensity provides Density(DENSITY)) {
                    QandeelApp(graph, platform)
                }
            }
            settle()
            test()
        }

    private fun ComposeUiTest.settle() = mainClock.advanceTimeBy(SETTLE_MS)

    /** Clicks the node and lets what it starts (navigation, a sheet, a toast) finish. */
    private fun ComposeUiTest.tap(node: SemanticsNodeInteraction) {
        node.performClick()
        settle()
    }

    private fun ComposeUiTest.longPress(tag: String) {
        onNodeWithTag(tag).performTouchInput { longClick() }
        settle()
    }

    private fun ComposeUiTest.openSurahOne() {
        tap(onNodeWithTag("tab_quran"))
        tap(onNodeWithTag("surah_1"))
        onNodeWithTag("reader").assertExists()
    }

    @Test
    fun `a new install starts in onboarding, and skipping it lands on the tabs`() {
        val graph = TestGraph(onboardingDone = false)
        app(graph) {
            onNodeWithTag("onboarding").assertExists()
            tap(onNodeWithTag("onboarding_skip"))
            onNodeWithTag("tab_bar").assertExists()
        }
        assertTrue(graph.settings.onboardingDone.value)
    }

    @Test
    fun `a surah opens from the Quran tab, and back returns to the tabs`() = app(TestGraph()) {
        openSurahOne()
        tap(onNodeWithContentDescription("Back"))
        onNodeWithTag("tab_bar").assertExists()
    }

    @Test
    fun `the reader downloads the surah and sends it to the watch`() {
        val graph = TestGraph()
        app(graph) {
            openSurahOne()
            tap(onNodeWithTag("reader_offline"))
            tap(onNodeWithContentDescription("More options"))
            tap(onNodeWithText("Send to watch"))
        }
        assertEquals(1, graph.downloads.downloadRequests.size)
        assertEquals(1, graph.watch.sentSurahs.single().first)
    }

    @Test
    fun `a downloaded surah asks before it is removed`() {
        val graph = TestGraph()
        Track.entries.forEach { graph.downloads.setState(1, it, SurahDownloadState.Downloaded) }
        app(graph) {
            openSurahOne()
            tap(onNodeWithTag("reader_offline"))
            tap(onNodeWithText("Remove"))
        }
        assertEquals(1, graph.downloads.removeRequests.single().first)
    }

    @Test
    fun `a long-pressed ayah offers copy, share and play from here`() {
        val graph = TestGraph()
        val platform = TestPlatform()
        app(graph, platform) {
            openSurahOne()
            longPress("ayah_1")
            tap(onNodeWithText("Copy"))
            onNodeWithText("Ayah copied").assertExists()
            longPress("ayah_1")
            tap(onNodeWithText("Share"))
            longPress("ayah_1")
            tap(onNodeWithText("Play from here"))
        }
        assertEquals(1, platform.shared.size)
        assertEquals(1, graph.player.playCalls.single().fromAyah)
    }

    @Test
    fun `the reader's quick settings sheet opens from Aa`() = app(TestGraph()) {
        openSurahOne()
        tap(onNodeWithContentDescription("Text and translation"))
        onNodeWithText("Word by word").assertExists()
    }

    @Test
    fun `the mini player opens the full player, whose options change the recitation`() {
        val graph = TestGraph()
        app(graph) {
            openSurahOne()
            tap(onNodeWithTag("ayah_1"))
            tap(onNodeWithTag("mini_player"))
            onNodeWithTag("player").assertExists()

            tap(onNodeWithTag("player_speed"))
            tap(onNodeWithText("1.5×"))
            tap(onNodeWithTag("player_sleep"))
            tap(onNodeWithText("End of surah"))
            tap(onNodeWithTag("player_mode"))
            onNodeWithText("Arabic + English").assertExists()
        }
        assertEquals(PlaybackSpeed.X1_5, graph.player.speed)
        assertEquals(SleepOption.EndOfSurah, graph.player.sleepOption)
    }

    @Test
    fun `stopping from the player's menu closes it`() {
        val graph = TestGraph()
        app(graph) {
            openSurahOne()
            tap(onNodeWithTag("ayah_1"))
            tap(onNodeWithTag("mini_player"))
            // The reader stays composed under the player, with its own "More options".
            tap(onNode(hasContentDescription("More options") and hasAnyAncestor(hasTestTag("player"))))
            tap(onNodeWithText("Stop playback"))
            onNodeWithTag("player").assertDoesNotExist()
        }
        assertEquals(null, graph.player.nowPlaying.value)
    }

    @Test
    fun `You opens Progress`() = app(TestGraph()) {
        tap(onNodeWithTag("tab_you"))
        tap(onNodeWithTag("you_progress"))
        onNodeWithTag("progress_list").assertExists()
        tap(onNodeWithContentDescription("Back"))
        onNodeWithTag("you").assertExists()
    }

    private companion object {
        const val DENSITY = 0.5f
        const val WIDTH = (412 * DENSITY).toInt()
        const val HEIGHT = (892 * DENSITY).toInt()
        const val NOW = 1_700_000_000_000L
        const val SETTLE_MS = 600L
        val WALK_TIMEOUT = 5.minutes
    }
}
