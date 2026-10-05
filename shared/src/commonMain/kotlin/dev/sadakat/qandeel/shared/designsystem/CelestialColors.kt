package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import dev.sadakat.qandeel.shared.designsystem.lamp.LampColors

/**
 * The sky behind everything: a gradient from [top] to [bottom], [nebula] clouds, the lamp's [glow]
 * low in the frame, and [star] dust. Alphas are part of the tokens: they set how strongly each
 * layer shows.
 */
@Immutable
data class SkyColors(val top: Color, val bottom: Color, val nebula: Color, val glow: Color, val star: Color)

/**
 * Celestial's colours. Night (dark) is indigo to deep green lit by mushaf gold; dawn (light) is the
 * same sky at first light: pale gold over warm paper, with gold dust for stars.
 *
 * Content sits on [glass] cards edged with [glassEdge]; [ink] and [inkMuted] are text on them and
 * on the sky; [accent] is gold, for what is active, and [onAccent] is text on it.
 */
@Immutable
data class CelestialColors(
    val isNight: Boolean,
    val sky: SkyColors,
    val lamp: LampColors,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val accent: Color,
    /** The lit end of gold gradients (the play button, progress). */
    val accentBright: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val glass: Color,
    val glassStrong: Color,
    val glassEdge: Color,
    val glassHighlight: Color,
    /** What floats over content (the tab bar, the mini player): glass dense enough to read over text. */
    val chrome: Color,
    val scrim: Color,
    val arabic: Color,
)

/** The colour sets. */
object CelestialPalette {

    private val Gold = Color(0xFFE3BC6A)
    private val GoldBright = Color(0xFFF2D08A)
    private val GoldDeep = Color(0xFF8C5E12)
    private val Paper = Color(0xFFF4ECDD)
    private val Ink = Color(0xFF1D1A16)

    val night = CelestialColors(
        isNight = true,
        sky = SkyColors(
            top = Color(0xFF0A1036),
            bottom = Color(0xFF07251F),
            nebula = Color(0x405B6BE0),
            glow = Color(0x40E8B860),
            star = Color(0xFFFFF4DC),
        ),
        lamp = LampColors(
            string = GoldBright,
            stringFar = Color(0xFFB98E4A),
            core = Color(0xFFFFF8E7),
            glow = Color(0xFFF0B85A),
            spark = Color(0xFFFFE2A8),
            additive = true,
        ),
        ink = Paper,
        inkMuted = Color(0xFFBDB6A8),
        inkFaint = Color(0xFF8A8577),
        accent = Gold,
        accentBright = GoldBright,
        accentSoft = Color(0x33E3BC6A),
        onAccent = Color(0xFF1A1405),
        glass = Color(0x14FFFFFF),
        glassStrong = Color(0x29FFFFFF),
        glassEdge = Color(0x24FFFFFF),
        glassHighlight = Color(0x4DF2D08A),
        chrome = Color(0xF0121A33),
        scrim = Color(0xB3050816),
        arabic = Color(0xFFF7EEDB),
    )

    val dawn = CelestialColors(
        isNight = false,
        sky = SkyColors(
            top = Color(0xFFF6E2BD),
            bottom = Color(0xFFF6F0E6),
            nebula = Color(0x40E9B98A),
            glow = Color(0x80F3C27A),
            star = Color(0x99B8862F),
        ),
        lamp = LampColors(
            string = Color(0xFFC48A2C),
            stringFar = Color(0xFFDDB97E),
            core = Color(0xFFFFD27A),
            glow = Color(0xFFF2B65C),
            spark = Color(0xFFE39A2E),
            additive = false,
        ),
        ink = Ink,
        inkMuted = Color(0xFF5B5347),
        inkFaint = Color(0xFF8A8172),
        accent = GoldDeep,
        accentBright = Color(0xFFB8862F),
        accentSoft = Color(0x268C5E12),
        onAccent = Color(0xFFFFF8EC),
        glass = Color(0x8CFFFFFF),
        glassStrong = Color(0xCCFFFFFF),
        glassEdge = Color(0x40B8862F),
        glassHighlight = Color(0x99FFFFFF),
        chrome = Color(0xF5FFFAF1),
        scrim = Color(0x80F6F0E6),
        arabic = Color(0xFF1B1712),
    )
}
