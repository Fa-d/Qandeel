package dev.sadakat.qandeel.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.sadakat.qandeel.R
import dev.sadakat.qandeel.core.designsystem.QandeelTheme
import dev.sadakat.qandeel.core.ui.kit.QandeelAppShell
import dev.sadakat.qandeel.presentation.about.AboutRoute
import dev.sadakat.qandeel.presentation.appearance.AppearanceRoute
import dev.sadakat.qandeel.presentation.navigation.AboutDestination
import dev.sadakat.qandeel.presentation.navigation.AppearanceDestination
import dev.sadakat.qandeel.presentation.navigation.HomeDestination
import dev.sadakat.qandeel.presentation.navigation.ProgressDestination
import dev.sadakat.qandeel.presentation.navigation.ReaderDestination
import dev.sadakat.qandeel.presentation.player.MiniPlayer
import dev.sadakat.qandeel.presentation.player.NowPlayingActions
import dev.sadakat.qandeel.presentation.player.NowPlayingSheet
import dev.sadakat.qandeel.presentation.player.PlayerViewModel
import dev.sadakat.qandeel.presentation.player.messageRes
import dev.sadakat.qandeel.presentation.reader.SurahReaderRoute
import dev.sadakat.qandeel.presentation.settings.ReadingSettingsSheet
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerActions

/**
 * Root of the phone UI: the three tabs (`:shared`, with their own floating mini player), the reader
 * and progress, the mini player pinned under the reader whenever something is queued, the full
 * player sliding up from either, and the reading settings sheet.
 */
@Composable
fun QuranApp(modifier: Modifier = Modifier, playerViewModel: PlayerViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val pointer by playerViewModel.pointer.collectAsStateWithLifecycle()
    // Read only inside lambdas, where it's drawn: the root doesn't recompose with every tick.
    val progress = playerViewModel.progress.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var showReadingSettings by rememberSaveable { mutableStateOf(false) }
    val openAppearance = { navController.navigate(AppearanceDestination) { launchSingleTop = true } }
    val openReader: (Int, Int) -> Unit = { surah, ayah ->
        navController.navigate(ReaderDestination(surah, ayah)) { launchSingleTop = true }
    }
    val openProgress = { navController.navigate(ProgressDestination) { launchSingleTop = true } }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onCelestialScreen = backStackEntry?.destination?.let {
        it.hasRoute<HomeDestination>() || it.hasRoute<ProgressDestination>()
    } ?: true

    // Playback errors (no network for a streaming surah, ...) surface once, then are consumed.
    val playbackError = playerState.error
    val playbackErrorMessage = playbackError?.let { stringResource(it.messageRes()) }
    val retryLabel = stringResource(R.string.playback_retry)
    LaunchedEffect(playbackError) {
        playbackError?.let {
            val result = snackbarHostState.showSnackbar(
                message = playbackErrorMessage.orEmpty(),
                actionLabel = retryLabel,
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) playerViewModel.retry()
            playerViewModel.consumeError()
        }
    }

    QandeelAppShell(
        // Test tags double as resource ids, so the baseline profile journey can find the screens.
        modifier = modifier.semantics { testTagsAsResourceId = true },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // The tabs and Progress float their own mini player; the older screens keep this one.
            AnimatedVisibility(
                visible = playerState.nowPlaying != null && !onCelestialScreen,
                enter = expandVertically(QandeelTheme.motion.enter()),
                exit = shrinkVertically(QandeelTheme.motion.exit()),
            ) {
                MiniPlayer(
                    state = playerState,
                    progress = { progress.value },
                    onExpand = { showNowPlaying = true },
                    onTogglePlayPause = playerViewModel::togglePlayPause,
                    onNext = playerViewModel::nextAyah,
                )
            }
        },
    ) { contentPadding ->
        // Screens draw behind the mini player and keep their content clear of it with this padding.
        NavHost(navController = navController, startDestination = HomeDestination) {
            composable<HomeDestination> {
                MainTabsRoute(
                    player = playerState,
                    playerActions = MiniPlayerActions(
                        onTogglePlay = playerViewModel::togglePlayPause,
                        onNext = playerViewModel::nextAyah,
                        onExpand = { showNowPlaying = true },
                    ),
                    navigation = MainTabsNavigation(
                        onOpenReader = openReader,
                        onOpenPlayer = { showNowPlaying = true },
                        onOpenProgress = openProgress,
                        onOpenAbout = { navController.navigate(AboutDestination) { launchSingleTop = true } },
                    ),
                )
            }
            composable<ReaderDestination> {
                SurahReaderRoute(
                    onBack = { navController.popBackStack() },
                    onOpenReadingSettings = { showReadingSettings = true },
                    contentPadding = contentPadding,
                )
            }
            composable<AppearanceDestination> {
                AppearanceRoute(onBack = { navController.popBackStack() }, contentPadding = contentPadding)
            }
            composable<AboutDestination> {
                AboutRoute(onBack = { navController.popBackStack() }, contentPadding = contentPadding)
            }
            composable<ProgressDestination> {
                ProgressRoute(onBack = { navController.popBackStack() }, onOpenReader = openReader)
            }
        }
    }

    if (showNowPlaying && playerState.nowPlaying != null) {
        NowPlayingSheet(
            state = playerState,
            actions = NowPlayingActions(
                onTogglePlayPause = playerViewModel::togglePlayPause,
                onPrevious = playerViewModel::previousAyah,
                onNext = playerViewModel::nextAyah,
                onSeek = playerViewModel::seekTo,
                onModeChange = playerViewModel::setMode,
                onVoiceChange = playerViewModel::setVoice,
                onRepeatChange = playerViewModel::setRepeat,
                onSpeedChange = playerViewModel::setSpeed,
                onSleepTimerChange = playerViewModel::setSleepTimer,
                onOpenReader = { surah, ayah ->
                    showNowPlaying = false
                    openReader(surah, ayah)
                },
                onStop = {
                    showNowPlaying = false
                    playerViewModel.stop()
                },
            ),
            onDismiss = { showNowPlaying = false },
            pointer = pointer,
            progress = { progress.value },
        )
    }
    if (showReadingSettings) {
        ReadingSettingsSheet(
            onDismiss = { showReadingSettings = false },
            onOpenAppearance = {
                showReadingSettings = false
                openAppearance()
            },
            onOpenAbout = {
                showReadingSettings = false
                navController.navigate(AboutDestination) { launchSingleTop = true }
            },
        )
    }
}
