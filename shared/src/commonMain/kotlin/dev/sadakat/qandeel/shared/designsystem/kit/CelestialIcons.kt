package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Qandeel's own line icons, on a 24-unit grid with a 1.8 stroke (filled where noted). Drawn in
 * black; tint them where they are shown.
 */
object CelestialIcons {
    /** Home: a mihrab's arch. */
    val Home: ImageVector by lazy {
        stroked("Home") {
            moveTo(5f, 20.5f)
            verticalLineTo(11f)
            curveTo(5f, 7f, 8f, 4.2f, 12f, 3f)
            curveTo(16f, 4.2f, 19f, 7f, 19f, 11f)
            verticalLineTo(20.5f)
            close()
            moveTo(9.5f, 20.5f)
            verticalLineTo(15f)
            curveTo(9.5f, 13.4f, 10.6f, 12.3f, 12f, 12.3f)
            curveTo(13.4f, 12.3f, 14.5f, 13.4f, 14.5f, 15f)
            verticalLineTo(20.5f)
        }
    }

    /** The Quran: an open book. */
    val Quran: ImageVector by lazy {
        stroked("Quran") {
            moveTo(3f, 5.5f)
            curveTo(6f, 4.6f, 9f, 4.9f, 12f, 6.6f)
            curveTo(15f, 4.9f, 18f, 4.6f, 21f, 5.5f)
            verticalLineTo(19f)
            curveTo(18f, 18.1f, 15f, 18.4f, 12f, 20f)
            curveTo(9f, 18.4f, 6f, 18.1f, 3f, 19f)
            close()
            moveTo(12f, 6.6f)
            verticalLineTo(20f)
        }
    }

    /** You: a person. */
    val You: ImageVector by lazy {
        stroked("You") {
            moveTo(12f, 4.5f)
            curveTo(13.9f, 4.5f, 15.5f, 6.1f, 15.5f, 8f)
            curveTo(15.5f, 9.9f, 13.9f, 11.5f, 12f, 11.5f)
            curveTo(10.1f, 11.5f, 8.5f, 9.9f, 8.5f, 8f)
            curveTo(8.5f, 6.1f, 10.1f, 4.5f, 12f, 4.5f)
            close()
            moveTo(5f, 20f)
            curveTo(5.8f, 16.2f, 8.6f, 14f, 12f, 14f)
            curveTo(15.4f, 14f, 18.2f, 16.2f, 19f, 20f)
        }
    }

    val Search: ImageVector by lazy {
        stroked("Search") {
            moveTo(11f, 4.5f)
            curveTo(14.6f, 4.5f, 17.5f, 7.4f, 17.5f, 11f)
            curveTo(17.5f, 14.6f, 14.6f, 17.5f, 11f, 17.5f)
            curveTo(7.4f, 17.5f, 4.5f, 14.6f, 4.5f, 11f)
            curveTo(4.5f, 7.4f, 7.4f, 4.5f, 11f, 4.5f)
            close()
            moveTo(15.8f, 15.8f)
            lineTo(20f, 20f)
        }
    }

    /** Filled. */
    val Play: ImageVector by lazy {
        filled("Play") {
            moveTo(8f, 5.2f)
            curveTo(8f, 4.4f, 8.9f, 3.9f, 9.6f, 4.3f)
            lineTo(19.2f, 10.3f)
            curveTo(19.9f, 10.7f, 19.9f, 11.7f, 19.2f, 12.1f)
            lineTo(9.6f, 18.1f)
            curveTo(8.9f, 18.5f, 8f, 18f, 8f, 17.2f)
            close()
        }
    }

    /** Filled. */
    val Pause: ImageVector by lazy {
        filled("Pause") {
            roundedBar(6.5f)
            roundedBar(13.5f)
        }
    }

    /** Filled: the next ayah. */
    val Next: ImageVector by lazy {
        filled("Next") {
            moveTo(5f, 6.2f)
            curveTo(5f, 5.4f, 5.9f, 4.9f, 6.6f, 5.4f)
            lineTo(14.4f, 10.9f)
            curveTo(15f, 11.3f, 15f, 12.2f, 14.4f, 12.6f)
            lineTo(6.6f, 18.1f)
            curveTo(5.9f, 18.6f, 5f, 18.1f, 5f, 17.3f)
            close()
            roundedBar(16f, width = 2.5f)
        }
    }

    private fun PathBuilder.roundedBar(left: Float, width: Float = 4f) {
        val r = width / 2f
        moveTo(left + r, 5f)
        curveTo(left + width, 5f, left + width, 5f, left + width, 5f + r)
        verticalLineTo(19f - r)
        curveTo(left + width, 19f, left + width, 19f, left + r, 19f)
        curveTo(left, 19f, left, 19f, left, 19f - r)
        verticalLineTo(5f + r)
        curveTo(left, 5f, left, 5f, left + r, 5f)
        close()
    }

    private fun stroked(name: String, block: PathBuilder.() -> Unit) = icon(name) {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }

    private fun filled(name: String, block: PathBuilder.() -> Unit) = icon(name) {
        path(fill = SolidColor(Color.Black), pathBuilder = block)
    }

    private fun icon(name: String, block: ImageVector.Builder.() -> ImageVector.Builder) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).block().build()
}
