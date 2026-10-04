package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class QuranTypeTest {

    private val type = quranType(FontFamily.Serif)

    @Test
    fun `scaling by one returns the same styles`() {
        assertSame(type, type.scaled(1f))
    }

    @Test
    fun `scaling grows the reading styles and their line heights together`() {
        val scaled = type.scaled(1.5f)

        assertEquals(39.sp, scaled.body.fontSize)
        assertEquals(75.sp, scaled.body.lineHeight)
        assertEquals(51.sp, scaled.display.fontSize)
        assertEquals(36.sp, scaled.title.fontSize)
    }

    @Test
    fun `scaling leaves the list label fixed`() {
        assertEquals(type.label, type.scaled(1.5f).label)
    }
}
