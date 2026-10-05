package dev.sadakat.qandeel.shared.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.Toast
import dev.sadakat.qandeel.shared.presentation.home.HomeTab
import dev.sadakat.qandeel.shared.presentation.home.HomeTabActions
import dev.sadakat.qandeel.shared.presentation.home.QuranTab
import dev.sadakat.qandeel.shared.presentation.home.QuranTabActions
import dev.sadakat.qandeel.shared.presentation.main.MainTab
import dev.sadakat.qandeel.shared.presentation.main.MainTabs
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerActions
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerBar
import dev.sadakat.qandeel.shared.presentation.player.PlayerActions
import dev.sadakat.qandeel.shared.presentation.player.PlayerScreen
import dev.sadakat.qandeel.shared.presentation.player.PlayerViewModel
import dev.sadakat.qandeel.shared.presentation.progress.ProgressActions
import dev.sadakat.qandeel.shared.presentation.progress.ProgressScreen
import dev.sadakat.qandeel.shared.presentation.reader.AyahActionsSheet
import dev.sadakat.qandeel.shared.presentation.reader.ReaderActions
import dev.sadakat.qandeel.shared.presentation.reader.ReaderMessage
import dev.sadakat.qandeel.shared.presentation.reader.ReaderMoreSheet
import dev.sadakat.qandeel.shared.presentation.reader.ReaderQuickSettingsSheet
import dev.sadakat.qandeel.shared.presentation.reader.ReaderScreen
import dev.sadakat.qandeel.shared.presentation.reader.RemoveDownloadSheet
import dev.sadakat.qandeel.shared.presentation.you.SettingsViewModel
import dev.sadakat.qandeel.shared.presentation.you.YouTab
import dev.sadakat.qandeel.shared.presentation.you.YouTabActions
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.ayah_copied
import dev.sadakat.qandeel.shared.resources.no_watch_found
import dev.sadakat.qandeel.shared.resources.sent_to_watch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Where the screens lead; [AppNavigation] binds it to the nav controller. */
internal class Navigation(
    val openReader: (surah: Int, ayah: Int) -> Unit,
    val openPlayer: () -> Unit,
    val openProgress: () -> Unit,
    val openAbout: () -> Unit,
    val back: () -> Unit,
)

internal fun PlayerViewModel.miniPlayerActions(onExpand: () -> Unit) =
    MiniPlayerActions(onTogglePlay = ::togglePlayPause, onNext = ::nextAyah, onExpand = onExpand)

internal fun SettingsViewModel.youActions(
    onOpenProgress: () -> Unit = {
    },
    onOpenAbout: () -> Unit = {},
) = YouTabActions(
    onModeChange = ::setMode,
    onVoiceChange = ::setBanglaVoice,
    onArabicTextSizeChange = ::setArabicTextSize,
    onShowTranslationChange = ::setShowTranslation,
    onFollowAlongChange = ::setFollowAlong,
    onWordByWordChange = ::setWordByWord,
    onThemeModeChange = ::setThemeMode,
    onReduceMotionChange = ::setReduceMotion,
    onOpenProgress = onOpenProgress,
    onOpenAbout = onOpenAbout,
)

/** The three tabs, with the mini player floating over them. */
@Composable
internal fun TabsDestination(graph: QandeelGraph, playerViewModel: PlayerViewModel, navigation: Navigation) {
    val homeViewModel = graph.homeViewModel()
    val progressViewModel = graph.progressViewModel()
    val settingsViewModel = graph.settingsViewModel()
    val home by homeViewModel.uiState.collectAsStateWithLifecycle()
    val progress by progressViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    MainTabs(
        selected = tab,
        onSelect = { tab = it },
        player = player.miniPlayer(),
        playerActions = playerViewModel.miniPlayerActions(navigation.openPlayer),
    ) { selected, padding ->
        when (selected) {
            MainTab.HOME -> HomeTab(
                home = home,
                progress = progress,
                playing = player.nowPlaying?.isPlaying == true,
                actions = HomeTabActions(
                    navigation.openReader,
                    homeViewModel::onContinuePlayPause,
                    navigation.openProgress,
                ),
                contentPadding = padding,
            )

            MainTab.QURAN -> QuranTab(
                state = home,
                actions = QuranTabActions(
                    onQueryChange = homeViewModel::onQueryChange,
                    onBrowseChange = homeViewModel::onBrowseChange,
                    onOpenReader = navigation.openReader,
                    onRetry = homeViewModel::retry,
                ),
                contentPadding = padding,
            )

            MainTab.YOU -> YouTab(
                settings = settings,
                progress = progress,
                actions = settingsViewModel.youActions(navigation.openProgress, navigation.openAbout),
                contentPadding = padding,
            )
        }
    }
}

/** A surah to read, with its sheets, its messages and the mini player. */
@Composable
internal fun ReaderDestination(
    graph: QandeelGraph,
    platform: QandeelPlatform,
    route: ReaderRoute,
    playerViewModel: PlayerViewModel,
    navigation: Navigation,
) {
    val viewModel = graph.readerViewModel(route.surah, route.ayah)
    val settingsViewModel = graph.settingsViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pointer by viewModel.pointer.collectAsStateWithLifecycle()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    var quickSettings by rememberSaveable { mutableStateOf(false) }
    var more by rememberSaveable { mutableStateOf(false) }
    var removing by rememberSaveable { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    val copied = stringResource(Res.string.ayah_copied)
    val message = when (val m = state.message) {
        is ReaderMessage.SentToWatch -> pluralStringResource(Res.plurals.sent_to_watch, m.watches, m.watches)
        ReaderMessage.NoWatch -> stringResource(Res.string.no_watch_found)
        null -> null
    }
    LaunchedEffect(message) {
        if (message != null) {
            toast = message
            viewModel.consumeMessage()
        }
    }
    val mini = player.miniPlayer()
    Box(Modifier.fillMaxSize()) {
        ReaderScreen(
            state = state,
            pointer = pointer,
            actions = ReaderActions(
                onBack = navigation.back,
                onPlaySurah = viewModel::playSurah,
                onPlayAyah = viewModel::playAyah,
                onPlayFromWord = viewModel::playFromWord,
                onShowAyahActions = viewModel::showAyahActions,
                onDismissAyahActions = viewModel::dismissAyahActions,
                onRepeatAyah = viewModel::repeatAyah,
                onShareAyah = platform::shareText,
                onModeChange = viewModel::setMode,
                onDownload = viewModel::download,
                onRemoveDownload = { removing = true },
                onOpenMore = { more = true },
                onOpenQuickSettings = { quickSettings = true },
                onRetry = viewModel::retry,
            ),
            bottomPadding = if (mini !=
                null
            ) {
                Celestial.shapes.barHeight + Celestial.spacing.md
            } else {
                Celestial.spacing.md
            },
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Celestial.spacing.lg)
                .padding(bottom = Celestial.spacing.md),
        ) {
            AnimatedVisibility(
                mini != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                if (mini != null) MiniPlayerBar(mini, playerViewModel.miniPlayerActions(navigation.openPlayer))
            }
            Toast(toast, onDismiss = { toast = null }, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
    val surah = state.surah
    state.ayahActions?.let { ayah ->
        if (surah != null) {
            AyahActionsSheet(
                surah.nameEnglish,
                surah.number,
                ayah,
                ReaderActions(
                    onDismissAyahActions = viewModel::dismissAyahActions,
                    onPlayAyah = viewModel::playAyah,
                    onRepeatAyah = viewModel::repeatAyah,
                    onShareAyah = platform::shareText,
                ),
                onCopy = { toast = copied },
            )
        }
    }
    if (quickSettings) {
        ReaderQuickSettingsSheet(
            settings = settings,
            actions = settingsViewModel.youActions(),
            onDismiss = { quickSettings = false },
        )
    }
    if (more) ReaderMoreSheet(onSendToWatch = viewModel::sendToWatch, onDismiss = { more = false })
    if (removing && surah != null) {
        RemoveDownloadSheet(
            surahName = surah.nameEnglish,
            onRemove = viewModel::remove,
            onDismiss = { removing = false },
        )
    }
}

/** The full player. */
@Composable
internal fun PlayerDestination(playerViewModel: PlayerViewModel, navigation: Navigation) {
    val state by playerViewModel.uiState.collectAsStateWithLifecycle()
    val pointer by playerViewModel.pointer.collectAsStateWithLifecycle()
    val progress = playerViewModel.progress.collectAsStateWithLifecycle()
    // Nothing queued (stopped from here, or from the notification): nothing to show.
    LaunchedEffect(state.nowPlaying == null) { if (state.nowPlaying == null) navigation.back() }
    PlayerScreen(
        state = state,
        pointer = pointer,
        progress = { progress.value },
        actions = PlayerActions(
            onClose = navigation.back,
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
                navigation.back()
                navigation.openReader(surah, ayah)
            },
            onStop = playerViewModel::stop,
        ),
    )
}

@Composable
internal fun ProgressDestination(graph: QandeelGraph, navigation: Navigation) {
    val viewModel = graph.progressViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressScreen(
        state,
        ProgressActions(navigation.back, viewModel::setOrder, viewModel::reset, navigation.openReader),
    )
}
