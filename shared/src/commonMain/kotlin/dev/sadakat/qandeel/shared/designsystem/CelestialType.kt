package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.fraunces_medium
import dev.sadakat.qandeel.shared.resources.manrope_bold
import dev.sadakat.qandeel.shared.resources.manrope_regular
import dev.sadakat.qandeel.shared.resources.manrope_semibold
import org.jetbrains.compose.resources.Font

/**
 * Celestial's Latin type: Fraunces, a soft old-style serif, for names and titles, so a surah's name
 * reads like a heading in a book; Manrope, a clear humanist sans, for everything that is UI. Both
 * are bundled, so the type is the same on every phone.
 */
@Immutable
data class CelestialType(
    /** The biggest names: the surah on the continue card and the full player. */
    val display: TextStyle,
    /** Screen titles. */
    val headline: TextStyle,
    /** Names in rows and cards. */
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    /** Small capitals over a section ("CONTINUE LISTENING"); uppercase the text itself. */
    val eyebrow: TextStyle,
    val caption: TextStyle,
)

/** The bundled families, loaded from the shared resources. */
@Composable
internal fun celestialType(): CelestialType {
    val serif = FontFamily(Font(Res.font.fraunces_medium, FontWeight.Medium))
    val sans = FontFamily(
        Font(Res.font.manrope_regular, FontWeight.Normal),
        Font(Res.font.manrope_semibold, FontWeight.SemiBold),
        Font(Res.font.manrope_bold, FontWeight.Bold),
    )
    return celestialType(serif, sans)
}

/** The scale in [serif] and [sans] (tests pass stand-ins). */
internal fun celestialType(serif: FontFamily, sans: FontFamily) = CelestialType(
    display = TextStyle(
        fontFamily = serif,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.01).em,
    ),
    headline = TextStyle(
        fontFamily = serif,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em,
    ),
    title = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    label = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
    eyebrow = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.14.em,
    ),
    caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
)
