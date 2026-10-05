package dev.sadakat.qandeel.shared.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.PrimaryButton
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.progress_time_hours_minutes
import dev.sadakat.qandeel.shared.resources.progress_time_minutes
import dev.sadakat.qandeel.shared.resources.progress_time_under_minute
import org.jetbrains.compose.resources.stringResource

/** A screen's title in the serif, with an optional line under it. */
@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        BasicText(
            title,
            style = Celestial.type.headline.copy(color = Celestial.colors.ink),
            modifier = Modifier.semantics { heading() },
        )
        if (subtitle != null) {
            BasicText(subtitle, style = Celestial.type.caption.copy(color = Celestial.colors.inkMuted))
        }
    }
}

/** A quiet message in place of content (nothing found, nothing yet, a failure), with an optional action. */
@Composable
fun CenteredMessage(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(title, style = Celestial.type.title.copy(color = Celestial.colors.ink, textAlign = TextAlign.Center))
        Spacer(Modifier.height(Celestial.spacing.xs))
        BasicText(
            text,
            style = Celestial.type.body.copy(color = Celestial.colors.inkMuted, textAlign = TextAlign.Center),
        )
        if (action != null) {
            Spacer(Modifier.height(Celestial.spacing.lg))
            PrimaryButton(action, onClick = onAction)
        }
    }
}

/** How long [ms] of listening is, in hours and minutes. */
@Composable
fun listenTime(ms: Long): String {
    val minutes = ms / MS_PER_MINUTE
    return when {
        minutes < 1 -> stringResource(Res.string.progress_time_under_minute)

        minutes < MINUTES_PER_HOUR -> stringResource(Res.string.progress_time_minutes, minutes.toInt())

        else -> stringResource(
            Res.string.progress_time_hours_minutes,
            (minutes / MINUTES_PER_HOUR).toInt(),
            (minutes % MINUTES_PER_HOUR).toInt(),
        )
    }
}

private const val MS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
