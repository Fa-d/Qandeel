package dev.sadakat.qandeel.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.qandeel.presentation.player.PlayerUiState
import dev.sadakat.qandeel.shared.designsystem.CelestialTheme
import dev.sadakat.qandeel.shared.presentation.home.HomeTab
import dev.sadakat.qandeel.shared.presentation.home.HomeTabActions
import dev.sadakat.qandeel.shared.presentation.home.QuranTab
import dev.sadakat.qandeel.shared.presentation.home.QuranTabActions
import dev.sadakat.qandeel.shared.presentation.main.MainTab
import dev.sadakat.qandeel.shared.presentation.main.MainTabs
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerActions
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerUi
import dev.sadakat.qandeel.shared.presentation.onboarding.isNight
import dev.sadakat.qandeel.shared.presentation.progress.ProgressActions
import dev.sadakat.qandeel.shared.presentation.progress.ProgressScreen
import dev.sadakat.qandeel.shared.presentation.you.YouTab
import dev.sadakat.qandeel.shared.presentation.you.YouTabActions

/** Where the tabs lead outside themselves; the reader and the full player are still the phone's own. */
class MainTabsNavigation(
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
    val onOpenPlayer: () -> Unit,
    val onOpenProgress: () -> Unit,
    val onOpenAbout: () -> Unit,
)

/**
 * The three tabs (`:shared`) on the phone, in the Celestial theme the settings choose, with the
 * mini player fed from the phone's player.
 */
@Composable
fun MainTabsRoute(
    player: PlayerUiState,
    playerActions: MiniPlayerActions,
    navigation: MainTabsNavigation,
    modifier: Modifier = Modifier,
) {
    val homeViewModel = homeViewModel()
    val progressViewModel = progressViewModel()
    val settingsViewModel = settingsViewModel()
    val home by homeViewModel.uiState.collectAsStateWithLifecycle()
    val progress by progressViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val prefs = settings.prefs

    CelestialTheme(
        night = prefs.themeMode.isNight(isSystemInDarkTheme()),
        reduceMotion = prefs.reduceMotion,
        arabicScale = prefs.arabicTextSize.scale,
    ) {
        MainTabs(
            selected = tab,
            onSelect = { tab = it },
            player = player.miniPlayer(),
            playerActions = MiniPlayerActions(playerActions.onTogglePlay, playerActions.onNext, navigation.onOpenPlayer),
            modifier = modifier,
        ) { selected, padding ->
            when (selected) {
                MainTab.HOME -> HomeTab(
                    home = home,
                    progress = progress,
                    playing = player.nowPlaying?.isPlaying == true,
                    actions = HomeTabActions(navigation.onOpenReader, homeViewModel::onContinuePlayPause, navigation.onOpenProgress),
                    contentPadding = padding,
                )
                MainTab.QURAN -> QuranTab(
                    state = home,
                    actions = QuranTabActions(
                        onQueryChange = homeViewModel::onQueryChange,
                        onBrowseChange = homeViewModel::onBrowseChange,
                        onOpenReader = navigation.onOpenReader,
                        onRetry = homeViewModel::retry,
                    ),
                    contentPadding = padding,
                )
                MainTab.YOU -> YouTab(
                    settings = settings,
                    progress = progress,
                    actions = YouTabActions(
                        onModeChange = settingsViewModel::setMode,
                        onVoiceChange = settingsViewModel::setBanglaVoice,
                        onArabicTextSizeChange = settingsViewModel::setArabicTextSize,
                        onShowTranslationChange = settingsViewModel::setShowTranslation,
                        onFollowAlongChange = settingsViewModel::setFollowAlong,
                        onWordByWordChange = settingsViewModel::setWordByWord,
                        onThemeModeChange = settingsViewModel::setThemeMode,
                        onReduceMotionChange = settingsViewModel::setReduceMotion,
                        onOpenProgress = navigation.onOpenProgress,
                        onOpenAbout = navigation.onOpenAbout,
                    ),
                    contentPadding = padding,
                )
            }
        }
    }
}

/** Your listening (`:shared`), in the Celestial theme. */
@Composable
fun ProgressRoute(onBack: () -> Unit, onOpenReader: (surah: Int, ayah: Int) -> Unit, modifier: Modifier = Modifier) {
    val progressViewModel = progressViewModel()
    val settingsViewModel = settingsViewModel()
    val progress by progressViewModel.uiState.collectAsStateWithLifecycle()
    val prefs = settingsViewModel.uiState.collectAsStateWithLifecycle().value.prefs
    CelestialTheme(night = prefs.themeMode.isNight(isSystemInDarkTheme()), reduceMotion = prefs.reduceMotion) {
        ProgressScreen(
            state = progress,
            actions = ProgressActions(onBack, progressViewModel::setOrder, progressViewModel::reset, onOpenReader),
            modifier = modifier,
        )
    }
}

private fun PlayerUiState.miniPlayer(): MiniPlayerUi? = nowPlaying?.let { playing ->
    MiniPlayerUi(
        surah = playing.surah,
        surahName = surahName ?: "${playing.surah}",
        ayah = playing.ayah,
        modeLabel = playing.mode.label,
        progress = playing.progress,
        isPlaying = playing.isPlaying,
    )
}
