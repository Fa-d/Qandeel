package dev.sadakat.qandeel.shared.presentation.main

import dev.sadakat.qandeel.core.domain.model.AyahRef
import dev.sadakat.qandeel.core.domain.model.ListeningOrder
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.Revelation
import dev.sadakat.qandeel.core.domain.model.Surah
import dev.sadakat.qandeel.core.domain.repository.SurahDownloadState
import dev.sadakat.qandeel.shared.designsystem.phoneSnapshot
import dev.sadakat.qandeel.shared.presentation.home.AyahJumpUi
import dev.sadakat.qandeel.shared.presentation.home.BrowseMode
import dev.sadakat.qandeel.shared.presentation.home.ContinueListeningUi
import dev.sadakat.qandeel.shared.presentation.home.HomeTab
import dev.sadakat.qandeel.shared.presentation.home.HomeTabActions
import dev.sadakat.qandeel.shared.presentation.home.HomeUiState
import dev.sadakat.qandeel.shared.presentation.home.QuranTab
import dev.sadakat.qandeel.shared.presentation.home.QuranTabActions
import dev.sadakat.qandeel.shared.presentation.home.SurahRowUi
import dev.sadakat.qandeel.shared.presentation.progress.ProgressActions
import dev.sadakat.qandeel.shared.presentation.progress.ProgressRowUi
import dev.sadakat.qandeel.shared.presentation.progress.ProgressScreen
import dev.sadakat.qandeel.shared.presentation.progress.ProgressUiState
import dev.sadakat.qandeel.shared.presentation.you.SettingsUiState
import dev.sadakat.qandeel.shared.presentation.you.YouTab
import dev.sadakat.qandeel.shared.presentation.you.YouTabActions
import org.junit.Test

/** The three tabs in their frame, with the mini player, and Progress. */
class MainTabsScreenshotTest {

    private val surahs = listOf(
        Surah(1, "سُورَةُ ٱلْفَاتِحَةِ", "Al-Faatiha", "The Opening", 7, Revelation.MECCAN),
        Surah(2, "سُورَةُ ٱلْبَقَرَةِ", "Al-Baqara", "The Cow", 286, Revelation.MEDINAN),
        Surah(3, "سُورَةُ آلِ عِمۡرَانَ", "Aal-i-Imraan", "The Family of Imraan", 200, Revelation.MEDINAN),
        Surah(18, "سُورَةُ ٱلْكَهْفِ", "Al-Kahf", "The Cave", 110, Revelation.MECCAN),
        Surah(36, "سُورَةُ يسٓ", "Yaseen", "Yaseen", 83, Revelation.MECCAN),
        Surah(55, "سُورَةُ ٱلرَّحْمَٰنِ", "Ar-Rahmaan", "The Beneficent", 78, Revelation.MEDINAN),
        Surah(67, "سُورَةُ ٱلْمُلْكِ", "Al-Mulk", "The Sovereignty", 30, Revelation.MECCAN),
    )

    private val rows = surahs.map { surah ->
        SurahRowUi(
            surah,
            download = when (surah.number) {
                1 -> SurahDownloadState.Downloaded
                36 -> SurahDownloadState.Downloading(completedFiles = 40, totalFiles = 100)
                else -> SurahDownloadState.NotDownloaded
            },
            isPlaying = surah.number == 18,
        )
    }

    private val home = HomeUiState(
        isLoading = false,
        continueListening = ContinueListeningUi(
            18,
            "Al-Kahf",
            "ٱلْكَهْفِ",
            ayah = 10,
            ayahCount = 110,
            isCurrent = true,
            isPlaying = true,
        ),
        surahs = rows,
    )

    private val progress = ProgressUiState(
        isLoading = false,
        ayahsHeard = 742,
        coverage = 742 / 6236f,
        listenedMs = 52_320_000,
        order = ListeningOrder.RECENT,
        rows = listOf(
            ProgressRowUi(18, "Al-Kahf", "ٱلْكَهْفِ", 1, 110, 110, 10, 10 / 110f, 120),
            ProgressRowUi(36, "Yaseen", "يسٓ", 3, 83, 83, 0, 0f, 260),
            ProgressRowUi(67, "Al-Mulk", "ٱلْمُلْكِ", 0, 12, 30, 12, 0.4f, 12),
            ProgressRowUi(55, "Ar-Rahmaan", "ٱلرَّحْمَٰنِ", 0, 48, 78, 48, 48 / 78f, 48),
            ProgressRowUi(1, "Al-Faatiha", "ٱلْفَاتِحَةِ", 41, 7, 7, 0, 0f, 290),
        ),
    )

    private val player =
        MiniPlayerUi(
            18,
            "Al-Kahf",
            ayah = 10,
            modeLabel = RecitationMode.ARABIC_BANGLA.label,
            progress =
            10 / 110f,
            isPlaying = true,
        )
    private val playerActions = MiniPlayerActions({}, {}, {})

    private fun tabs(name: String, tab: MainTab, night: Boolean = true) = phoneSnapshot("tabs_$name", night) {
        MainTabs(tab, onSelect = {}, player = player, playerActions = playerActions) { selected, padding ->
            when (selected) {
                MainTab.HOME -> HomeTab(
                    home,
                    progress,
                    playing = true,
                    actions = HomeTabActions({ _, _ ->
                    }, {}, {}),
                    contentPadding = padding,
                )

                MainTab.QURAN -> QuranTab(
                    home,
                    QuranTabActions({}, {}, { _, _ -> }, {}),
                    contentPadding = padding,
                )

                MainTab.YOU -> YouTab(SettingsUiState(), progress, YouTabActions(), contentPadding = padding)
            }
        }
    }

    @Test
    fun home() = tabs("home_night", MainTab.HOME)

    @Test
    fun homeDawn() = tabs("home_dawn", MainTab.HOME, night = false)

    @Test
    fun quran() = tabs("quran_night", MainTab.QURAN)

    @Test
    fun you() = tabs("you_night", MainTab.YOU)

    @Test
    fun aFreshHomeOffersAlFatiha() = phoneSnapshot("tabs_home_fresh", night = true) {
        MainTabs(MainTab.HOME, onSelect = {}, player = null, playerActions = playerActions) { _, padding ->
            HomeTab(
                HomeUiState(isLoading = false),
                ProgressUiState(isLoading = false),
                false,
                HomeTabActions({ _, _ ->
                }, {}, {}),
                contentPadding = padding,
            )
        }
    }

    @Test
    fun aVerseReference() = phoneSnapshot("tabs_quran_jump", night = true) {
        QuranTab(
            home.copy(
                query = "18:10",
                jumpTarget = AyahJumpUi(AyahRef(18, 10), "Al-Kahf"),
                surahs = rows.filter {
                    it.surah.number ==
                        18
                },
            ),
            QuranTabActions({}, {}, { _, _ -> }, {}),
        )
    }

    @Test
    fun offlineEmpty() = phoneSnapshot("tabs_quran_offline_empty", night = false) {
        QuranTab(home.copy(browse = BrowseMode.OFFLINE, surahs = emptyList()), QuranTabActions({}, {}, { _, _ -> }, {}))
    }

    @Test
    fun progressScreen() = phoneSnapshot("progress_night", night = true) {
        ProgressScreen(progress, ProgressActions({}, {}, {}, { _, _ -> }))
    }
}
