package dev.sadakat.qandeel.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.ListeningHistory
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.QuranText
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads
import dev.sadakat.qandeel.shared.presentation.home.HomeViewModel
import dev.sadakat.qandeel.shared.presentation.progress.ProgressViewModel
import dev.sadakat.qandeel.shared.presentation.you.SettingsViewModel

/**
 * The domain ports the multiplatform ViewModels (`:shared`) are built from. They can't use Hilt
 * themselves, so the phone app hands them what Hilt provides.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedPorts {
    fun quranText(): QuranText

    fun settings(): QuranSettings

    fun downloads(): SurahDownloads

    fun player(): QuranPlayer

    fun history(): ListeningHistory
}

@Composable
private fun sharedPorts(): SharedPorts {
    val context = LocalContext.current
    return remember(context) { context.sharedPorts() }
}

private fun Context.sharedPorts(): SharedPorts = EntryPointAccessors.fromApplication(applicationContext, SharedPorts::class.java)

/** Home and the Quran tab's ViewModel, scoped like a Hilt one to the current destination. */
@Composable
fun homeViewModel(): HomeViewModel {
    val ports = sharedPorts()
    return viewModel { HomeViewModel(ports.quranText(), ports.settings(), ports.downloads(), ports.player()) }
}

/** The listening progress behind Home's glance, You's card and the Progress screen. */
@Composable
fun progressViewModel(): ProgressViewModel {
    val ports = sharedPorts()
    return viewModel {
        ProgressViewModel(ports.history(), ports.quranText(), createSavedStateHandle(), now = System::currentTimeMillis)
    }
}

/** Every setting, under You. */
@Composable
fun settingsViewModel(): SettingsViewModel {
    val ports = sharedPorts()
    return viewModel { SettingsViewModel(ports.settings(), ports.player()) }
}
