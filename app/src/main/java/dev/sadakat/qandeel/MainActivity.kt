package dev.sadakat.qandeel

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.shared.app.QandeelApp
import dev.sadakat.qandeel.shared.app.QandeelPlatform
import dev.sadakat.qandeel.shared.designsystem.CelestialPalette
import dev.sadakat.qandeel.shared.presentation.onboarding.isNight
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * The phone's one activity: it hosts the shared app ([QandeelApp]) and does for it what only
 * Android can (sharing, asking for notifications), and paints the window the sky's colour the
 * settings choose, before the first frame and whenever they change.
 */
@AndroidEntryPoint
class MainActivity :
    ComponentActivity(),
    QandeelPlatform {

    // Notifications show playback controls and download progress; the app works without them.
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    @Inject
    lateinit var graph: AndroidQandeelGraph

    override val versionName: String = BuildConfig.VERSION_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = graph.settings
        val prefs = runBlocking { settings.readingPrefs.first() }
        paintWindow(prefs.themeMode.isNight(isSystemNight()))

        // A new install is asked as it finishes onboarding, once it knows what the app is for.
        if (runBlocking { settings.onboardingDone.first() }) askForNotifications()

        setContent {
            val current by settings.readingPrefs.collectAsStateWithLifecycle(null)
            val night = current?.themeMode?.isNight(isSystemInDarkTheme())
            // Until the settings arrive the window keeps what onCreate painted.
            DisposableEffect(night) {
                if (night != null) paintWindow(night)
                onDispose {}
            }
            QandeelApp(graph, platform = this)
        }
    }

    override fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, null))
    }

    /** Notifications show playback controls and download progress; asked once, from Android 13. */
    override fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** The window shows the sky's top until the first frame, and the bars' icons suit it. */
    private fun paintWindow(night: Boolean) {
        val palette = if (night) CelestialPalette.night else CelestialPalette.dawn
        window.setBackgroundDrawable(palette.sky.top.toArgb().toDrawable())
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { night },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { night },
        )
    }

    private fun isSystemNight(): Boolean = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
}

/** For tests: the theme mode's sky colour at the top, as the window paints it. */
internal fun ThemeMode.windowColor(systemNight: Boolean): Int =
    (if (isNight(systemNight)) CelestialPalette.night else CelestialPalette.dawn).sky.top.toArgb()
