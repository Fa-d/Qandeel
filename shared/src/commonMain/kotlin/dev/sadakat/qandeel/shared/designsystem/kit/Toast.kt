package dev.sadakat.qandeel.shared.designsystem.kit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import dev.sadakat.qandeel.shared.designsystem.Celestial
import kotlinx.coroutines.delay

/**
 * A short message on a pill of glass, with an optional action; it goes by itself after a few
 * seconds (or when [onDismiss] is called), and screen readers announce it.
 */
@Composable
fun Toast(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(message) {
        if (message != null) {
            delay(if (action != null) LONG_MS else SHORT_MS)
            dismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        GlassSurface(
            Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
            radius = Celestial.shapes.control,
            strong = true,
        ) {
            Row(
                Modifier.padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    message.orEmpty(),
                    style = Celestial.type.body.copy(color = Celestial.colors.ink),
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (action != null) {
                    Spacer(Modifier.width(Celestial.spacing.lg))
                    BasicText(
                        action,
                        style = Celestial.type.label.copy(color = Celestial.colors.accent),
                        modifier = Modifier.clickable {
                            onAction()
                            dismiss()
                        },
                    )
                }
            }
        }
    }
}

private const val SHORT_MS = 3_000L
private const val LONG_MS = 6_000L
