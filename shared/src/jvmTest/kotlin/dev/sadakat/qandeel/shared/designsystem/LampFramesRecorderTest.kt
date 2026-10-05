package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.lamp.LampMotion
import dev.sadakat.qandeel.shared.designsystem.lamp.QandeelLamp
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Not a check: writes the hero's frames (the sky and the lamp in motion) to `build/lamp-frames`,
 * for an animation to review the motion by. Runs only with LAMP_FRAMES set:
 * `LAMP_FRAMES=1 ./gradlew :shared:jvmTest --tests '*LampFramesRecorder*'`.
 */
@OptIn(ExperimentalTestApi::class)
class LampFramesRecorderTest {

    @Test
    fun recordFrames() {
        assumeTrue(System.getenv("LAMP_FRAMES") != null)
        val out = File("build/lamp-frames").apply { mkdirs() }
        val seconds = mutableFloatStateOf(0f)
        runDesktopComposeUiTest(width = 824, height = 840) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(2f)) {
                    CelestialTheme(night = true, clock = { seconds.floatValue }) {
                        Box(Modifier.fillMaxSize()) {
                            CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.5f, 0.45f))
                            QandeelLamp(
                                motion = { LampMotion(time = seconds.floatValue, energy = 0.4f) },
                                colors = Celestial.colors.lamp,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
            repeat(FRAMES) { frame ->
                seconds.floatValue = frame / FPS
                waitForIdle()
                ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(out, "frame_%03d.png".format(frame)))
            }
        }
    }

    private companion object {
        const val FPS = 15f
        const val FRAMES = 150
    }
}
