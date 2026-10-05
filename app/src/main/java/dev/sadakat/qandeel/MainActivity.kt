package dev.sadakat.qandeel

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.qandeel.core.designsystem.skin.QandeelSkins
import dev.sadakat.qandeel.core.designsystem.skin.QandeelStyle
import dev.sadakat.qandeel.core.designsystem.skin.QandeelTone
import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads
import dev.sadakat.qandeel.core.domain.repository.WatchConnection
import dev.sadakat.qandeel.presentation.AppViewModel
import dev.sadakat.qandeel.presentation.QuranApp
import dev.sadakat.qandeel.presentation.appearance.tone
import dev.sadakat.qandeel.shared.presentation.onboarding.OnboardingRoute
import dev.sadakat.qandeel.shared.presentation.onboarding.OnboardingViewModel
import dev.sadakat.qandeel.ui.theme.QandeelAppTheme
import dev.sadakat.qandeel.ui.theme.toQandeelStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Notifications show playback controls and download progress; the app works without them.
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    @Inject
    lateinit var settings: QuranSettings

    // What onboarding needs: it plays voice samples, starts the starter downloads and reaches the watch.
    @Inject
    lateinit var player: QuranPlayer

    @Inject
    lateinit var downloads: SurahDownloads

    @Inject
    lateinit var watch: WatchConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = runBlocking { settings.readingPrefs.first() }
        applyTone(prefs.themeMode.tone(systemDark = isSystemNight()), prefs.uiStyle.toQandeelStyle())

        // A new install is asked as it finishes onboarding, once it knows what the app is for.
        if (runBlocking { settings.onboardingDone.first() }) askForNotifications()

        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val app by appViewModel.uiState.collectAsStateWithLifecycle()
            val tone = app.themeMode.tone(systemDark = isSystemInDarkTheme())
            val style = app.uiStyle.toQandeelStyle()
            // The state-in placeholder (SYSTEM/MUSHAF) would repaint the window the settings
            // just painted in onCreate, so only apply once the real settings have arrived.
            DisposableEffect(app.isReady, tone, style) {
                if (app.isReady) applyTone(tone, style)
                onDispose {}
            }
            // Until the settings are read the window's page color shows, so there's no theme flash.
            if (app.isReady && !app.onboardingDone) {
                // Done is saved by the ViewModel; the settings then bring the app in.
                OnboardingRoute(
                    viewModel = viewModel { OnboardingViewModel(settings, player, downloads, watch) },
                    onDone = {},
                    onFinishing = ::askForNotifications,
                )
            } else if (app.isReady) {
                QandeelAppTheme(
                    dynamicColor = app.dynamicColor,
                    arabicScale = app.arabicTextSize.scale,
                    style = style,
                    tone = tone,
                ) {
                    QuranApp()
                }
            }
        }
    }

    /** Notifications show playback controls and download progress; asked once, from Android 13. */
    private fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** The window's page color and the system bars for one tone of one style. */
    private fun applyTone(tone: QandeelTone, style: QandeelStyle) {
        val darkTheme = tone == QandeelTone.DARK
        window.setBackgroundDrawable(
            QandeelSkins.of(style, tone).colors.background.toArgb().toDrawable(),
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
        )
    }

    private fun isSystemNight(): Boolean = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
}
