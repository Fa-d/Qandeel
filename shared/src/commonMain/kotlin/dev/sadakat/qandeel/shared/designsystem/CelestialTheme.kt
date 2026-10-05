// The theme's tokens and clock are the one place CompositionLocals belong, as in QandeelTheme.
@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.TextStyle

/** Everything Celestial components read: colours, type, the Quran's type, rhythm and motion. */
@Immutable
data class CelestialTokens(
    val colors: CelestialColors,
    val type: CelestialType,
    val quran: QuranType,
    val spacing: CelestialSpacing = CelestialSpacing(),
    val shapes: CelestialShapes = CelestialShapes(),
    /** Still the sky and the lamp: no drifting, turning or twinkling (a setting, or the system's). */
    val reduceMotion: Boolean = false,
)

/**
 * Seconds since the sky started moving, read only while drawing so that motion redraws without
 * recomposing. Frozen under reduce motion; tests set it to draw any moment of the animation.
 */
fun interface CelestialClock {
    fun seconds(): Float
}

private val LocalCelestialTokens = staticCompositionLocalOf<CelestialTokens> {
    error("No CelestialTheme: wrap the UI in CelestialTheme { }")
}

private val LocalCelestialClock = staticCompositionLocalOf { CelestialClock { 0f } }

/** Reads the current theme: `Celestial.colors.accent`, `Celestial.type.title`, … */
object Celestial {
    val colors: CelestialColors
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.colors

    val type: CelestialType
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.type

    val quran: QuranType
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.quran

    val spacing: CelestialSpacing
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.spacing

    val shapes: CelestialShapes
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.shapes

    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalCelestialTokens.current.reduceMotion

    val clock: CelestialClock
        @Composable @ReadOnlyComposable
        get() = LocalCelestialClock.current
}

/**
 * The Celestial theme: [night] (dark) or dawn (light), with the Arabic at [arabicScale]. The sky's
 * clock runs on the frame clock unless [reduceMotion] stills it; pass [clock] to drive it yourself.
 */
@Composable
fun CelestialTheme(
    night: Boolean,
    reduceMotion: Boolean = false,
    arabicScale: Float = 1f,
    clock: CelestialClock? = null,
    content: @Composable () -> Unit,
) {
    val type = celestialType()
    val amiri = amiriQuran()
    val tokens = remember(night, reduceMotion, arabicScale, type, amiri) {
        CelestialTokens(
            colors = if (night) CelestialPalette.night else CelestialPalette.dawn,
            type = type,
            quran = quranType(amiri).scaled(arabicScale),
            reduceMotion = reduceMotion,
        )
    }
    val frameClock = clock ?: rememberFrameClock(running = !reduceMotion)
    CompositionLocalProvider(
        LocalCelestialTokens provides tokens,
        LocalCelestialClock provides frameClock,
        content = content,
    )
}

@Composable
private fun rememberFrameClock(running: Boolean): CelestialClock {
    val seconds = remember { mutableFloatStateOf(0f) }
    if (running) {
        LaunchedEffect(Unit) {
            val offset = seconds.floatValue
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { now -> seconds.floatValue = offset + (now - start) / NANOS_PER_SECOND }
            }
        }
    }
    return remember(seconds) { clockOf(seconds) }
}

private fun clockOf(state: State<Float>) = CelestialClock { state.value }

private const val NANOS_PER_SECOND = 1_000_000_000f

/** A state change's animation: the default spring, or an instant change with Reduce motion. */
@Composable
@ReadOnlyComposable
fun <T> motionSpec(): AnimationSpec<T> = if (Celestial.reduceMotion) snap() else spring()

/** [style] in [color], for the many places that only change a style's colour. */
internal fun TextStyle.inColor(color: androidx.compose.ui.graphics.Color) = copy(color = color)
