package dev.sadakat.qandeel.shared.app

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.sadakat.qandeel.core.domain.player.PlaybackError
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.CelestialTheme
import dev.sadakat.qandeel.shared.designsystem.kit.Toast
import dev.sadakat.qandeel.shared.presentation.about.AboutScreen
import dev.sadakat.qandeel.shared.presentation.onboarding.OnboardingRoute
import dev.sadakat.qandeel.shared.presentation.onboarding.isNight
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.playback_error_failed
import dev.sadakat.qandeel.shared.resources.playback_error_network
import dev.sadakat.qandeel.shared.resources.playback_retry
import org.jetbrains.compose.resources.stringResource

/**
 * Qandeel, the same on every platform: onboarding on a new install, then the tabs, the reader,
 * the full player, Progress and About, in the Celestial theme the settings choose. Until the
 * settings are read nothing is drawn, so the first frame already has the right sky.
 */
@Composable
fun QandeelApp(graph: QandeelGraph, platform: QandeelPlatform, modifier: Modifier = Modifier) {
    val prefs by graph.settings.readingPrefs.collectAsStateWithLifecycle(null)
    val onboarded by graph.settings.onboardingDone.collectAsStateWithLifecycle(null)
    val loadedPrefs = prefs ?: return
    when (onboarded) {
        null -> Unit

        false -> OnboardingRoute(
            viewModel = graph.onboardingViewModel(),
            onDone = {},
            onFinishing = platform::askForNotifications,
            modifier = modifier,
        )

        true -> CelestialTheme(
            night = loadedPrefs.themeMode.isNight(isSystemInDarkTheme()),
            reduceMotion = loadedPrefs.reduceMotion,
            arabicScale = loadedPrefs.arabicTextSize.scale,
        ) {
            AppNavigation(graph, platform, modifier)
        }
    }
}

@Composable
private fun AppNavigation(graph: QandeelGraph, platform: QandeelPlatform, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // One player ViewModel for the whole app: the mini player, the full player and errors share it.
    val playerViewModel = graph.playerViewModel()
    val navigation = Navigation(
        openReader = { surah, ayah -> navController.navigate(ReaderRoute(surah, ayah)) { launchSingleTop = true } },
        openPlayer = { navController.navigate(PlayerRoute) { launchSingleTop = true } },
        openProgress = { navController.navigate(ProgressRoute) { launchSingleTop = true } },
        openAbout = { navController.navigate(AboutRoute) { launchSingleTop = true } },
        back = { navController.popBackStack() },
    )
    Box(modifier.fillMaxSize().testTag("app")) {
        NavHost(navController, startDestination = TabsRoute) {
            composable<TabsRoute> { TabsDestination(graph, playerViewModel, navigation) }
            composable<ReaderRoute> { entry ->
                ReaderDestination(graph, platform, entry.toRoute(), playerViewModel, navigation)
            }
            composable<PlayerRoute>(
                enterTransition = { slideInVertically { it } },
                exitTransition = { slideOutVertically { it } },
                popEnterTransition = { slideInVertically { it } },
                popExitTransition = { slideOutVertically { it } },
            ) { PlayerDestination(playerViewModel, navigation) }
            composable<ProgressRoute> { ProgressDestination(graph, navigation) }
            composable<AboutRoute> { AboutScreen(platform.versionName, onBack = navigation.back) }
        }
        PlaybackErrors(playerViewModel)
    }
}

/** A failed recitation, once, with a way to try again. */
@Composable
private fun PlaybackErrors(playerViewModel: dev.sadakat.qandeel.shared.presentation.player.PlayerViewModel) {
    val state by playerViewModel.uiState.collectAsStateWithLifecycle()
    val message = when (state.error) {
        PlaybackError.NETWORK -> stringResource(Res.string.playback_error_network)
        PlaybackError.FAILED -> stringResource(Res.string.playback_error_failed)
        null -> null
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Toast(
            message = message,
            onDismiss = playerViewModel::consumeError,
            action = stringResource(Res.string.playback_retry),
            onAction = playerViewModel::retry,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(Celestial.spacing.lg),
        )
    }
}
