package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test

/**
 * Amiri Quran through Skia, the renderer iOS uses too: the stacked marks must not clip and the
 * words must join, at every reading style.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
class QuranTypeScreenshotTest {

    @Test
    fun quranStyles() = runDesktopComposeUiTest(width = 400, height = 640) {
        setContent {
            val type = quranType(amiriQuran())
            Column(
                Modifier
                    .width(400.dp)
                    .background(Color(0xFFF3EBDD))
                    .padding(16.dp),
            ) {
                listOf(type.display, type.body, type.title, type.label).forEach { style ->
                    BasicText(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        style = style.copy(color = Color(0xFF1B1B1B), textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        onRoot().captureRoboImage("src/jvmTest/screenshots/quran_styles.png")
    }
}
