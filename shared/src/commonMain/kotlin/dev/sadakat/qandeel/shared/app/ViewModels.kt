package dev.sadakat.qandeel.shared.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sadakat.qandeel.shared.presentation.home.HomeViewModel
import dev.sadakat.qandeel.shared.presentation.onboarding.OnboardingViewModel
import dev.sadakat.qandeel.shared.presentation.player.PlayerViewModel
import dev.sadakat.qandeel.shared.presentation.progress.ProgressViewModel
import dev.sadakat.qandeel.shared.presentation.reader.SurahReaderViewModel
import dev.sadakat.qandeel.shared.presentation.you.SettingsViewModel

// Each ViewModel built from the graph's ports, scoped (like any viewModel()) to the destination
// or screen that asks for it.

@Composable
internal fun QandeelGraph.homeViewModel(): HomeViewModel = viewModel {
    HomeViewModel(quranText, settings, downloads, player)
}

@Composable
internal fun QandeelGraph.progressViewModel(): ProgressViewModel =
    viewModel { ProgressViewModel(history, quranText, createSavedStateHandle(), now = ::now) }

@Composable
internal fun QandeelGraph.settingsViewModel(): SettingsViewModel = viewModel { SettingsViewModel(settings, player) }

@Composable
internal fun QandeelGraph.playerViewModel(): PlayerViewModel =
    viewModel { PlayerViewModel(player, quranText, settings, timings, wordMeanings) }

@Composable
internal fun QandeelGraph.readerViewModel(surah: Int, ayah: Int): SurahReaderViewModel = viewModel {
    SurahReaderViewModel(surah, ayah, quranText, settings, downloads, player, watch, history, wordMeanings)
}

@Composable
internal fun QandeelGraph.onboardingViewModel(): OnboardingViewModel =
    viewModel { OnboardingViewModel(settings, player, downloads, watch) }
