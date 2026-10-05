package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage

/** A phone-sized window: 412 × 892 dp at 2× (Pixel-class width). */
private const val PHONE_WIDTH_DP = 412
private const val PHONE_HEIGHT_DP = 892
private const val PHONE_DENSITY = 2f

/** The moment of the sky's motion goldens are drawn at: stars mid-twinkle, the lamp turned a little. */
internal const val GOLDEN_SECONDS = 3.2f

/**
 * Renders [content] in a Celestial phone at [seconds] into the golden `name.png`. The sky's clock
 * is held still, so the frame is the same on every run.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal fun phoneSnapshot(
    name: String,
    night: Boolean,
    seconds: Float = GOLDEN_SECONDS,
    content: @Composable () -> Unit,
) = runDesktopComposeUiTest(
    width = (PHONE_WIDTH_DP * PHONE_DENSITY).toInt(),
    height = (PHONE_HEIGHT_DP * PHONE_DENSITY).toInt(),
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(PHONE_DENSITY)) {
            CelestialTheme(night = night, clock = { seconds }, content = content)
        }
    }
    onRoot().captureRoboImage("src/jvmTest/screenshots/$name.png")
}
