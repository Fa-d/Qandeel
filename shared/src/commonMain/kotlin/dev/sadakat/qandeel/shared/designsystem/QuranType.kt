package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.amiri_quran
import org.jetbrains.compose.resources.Font

/**
 * The Quran's Arabic, in Amiri Quran, the same on Android and iOS. Line heights are generous (about
 * 1.8×) because stacked marks above and below the letters need the room, and
 * [LineHeightStyle.Trim.None] keeps the first and last lines from clipping them.
 *
 * The styles set an RTL text direction (so numbers and punctuation order correctly), which makes
 * `TextAlign.End` the *left* edge: align Arabic with `TextAlign.Right` or `Center`.
 */
@Immutable
data class QuranType(
    /** The ayah in the full-screen player. */
    val display: TextStyle,
    /** Ayahs in the reader. */
    val body: TextStyle,
    /** The basmala and the continue card. */
    val title: TextStyle,
    /** Surah names in lists. */
    val label: TextStyle,
) {
    /** The reading styles multiplied by [factor]; 1 returns this. */
    fun scaled(factor: Float): QuranType = if (factor == 1f) {
        this
    } else {
        copy(display = display.scaled(factor), body = body.scaled(factor), title = title.scaled(factor))
    }
}

/** Amiri Quran, loaded from the shared resources (a composable, as resource fonts are on iOS). */
@Composable
fun amiriQuran(): FontFamily = FontFamily(Font(Res.font.amiri_quran))

/** The Quran styles in [family] (normally [amiriQuran]). */
fun quranType(family: FontFamily): QuranType = QuranType(
    display = arabic(family, size = 34, lineHeight = 64),
    body = arabic(family, size = 26, lineHeight = 50),
    title = arabic(family, size = 24, lineHeight = 44),
    label = arabic(family, size = 18, lineHeight = 32),
)

private fun arabic(family: FontFamily, size: Int, lineHeight: Int) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    textDirection = TextDirection.Rtl,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

private fun TextStyle.scaled(factor: Float) = copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)
