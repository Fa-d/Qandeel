package dev.sadakat.qandeel.shared.presentation.player

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.core.domain.player.PlaybackProgress
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.player_cd_time_bar
import dev.sadakat.qandeel.shared.resources.player_seek_ayah
import dev.sadakat.qandeel.shared.resources.player_time_remaining
import dev.sadakat.qandeel.shared.resources.player_time_state
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

/**
 * The whole surah as one line of gold: time gone on the left, time left on the right. Drag or tap
 * anywhere to move there; while dragging it names the ayah under the thumb ([ayahAt]). Until the
 * surah's length is known it shows [fallback] (how far through by ayah) and can't be dragged.
 */
@Composable
fun TimeBar(
    progress: () -> PlaybackProgress,
    fallback: Float,
    ayahAt: (Long) -> Int?,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Celestial.colors
    var dragged by remember { mutableStateOf<Float?>(null) }
    val current = progress()
    val duration = current.surahDurationMs
    val known = duration > 0
    val fraction = dragged ?: if (known) current.surahPositionMs.toFloat() / duration else fallback
    val shownMs = (fraction * duration).roundToLong()
    val description = stringResource(Res.string.player_cd_time_bar)
    val state = stringResource(Res.string.player_time_state, clockText(shownMs), clockText(duration))
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .testTag("player_time_bar")
                .semantics {
                    contentDescription = description
                    stateDescription = state
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f)
                    if (known) {
                        setProgress { value ->
                            onSeek((value * duration).roundToLong())
                            true
                        }
                    }
                }
                .then(
                    if (!known) {
                        Modifier
                    } else {
                        Modifier
                            .pointerInput(duration) {
                                detectTapGestures { onSeek((it.x / size.width * duration).roundToLong()) }
                            }
                            .pointerInput(duration) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragged = (it.x / size.width).coerceIn(0f, 1f) },
                                    onDragEnd = {
                                        dragged?.let { onSeek((it * duration).roundToLong()) }
                                        dragged = null
                                    },
                                    onDragCancel = { dragged = null },
                                ) { change, _ -> dragged = (change.position.x / size.width).coerceIn(0f, 1f) }
                            }
                    },
                )
                .drawBehind {
                    val track = 4.dp.toPx()
                    val y = size.height / 2f - track / 2f
                    val radius = CornerRadius(track / 2f)
                    drawRoundRect(colors.inkFaint.copy(alpha = 0.3f), Offset(0f, y), Size(size.width, track), radius)
                    val filled = size.width * fraction.coerceIn(0f, 1f)
                    drawRoundRect(
                        Brush.horizontalGradient(listOf(colors.accent, colors.accentBright)),
                        Offset(0f, y),
                        Size(filled, track),
                        radius,
                    )
                    drawCircle(
                        colors.accentBright,
                        radius = if (dragged !=
                            null
                        ) {
                            9.dp.toPx()
                        } else {
                            7.dp.toPx()
                        },
                        center = Offset(filled, size.height / 2f),
                    )
                },
        )
        Box(Modifier.fillMaxWidth()) {
            val caption = Celestial.type.caption.copy(color = colors.inkMuted)
            if (known) {
                BasicText(clockText(shownMs), style = caption, modifier = Modifier.align(Alignment.CenterStart))
                BasicText(
                    stringResource(Res.string.player_time_remaining, clockText(duration - shownMs)),
                    style = caption,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
            dragged?.let { ayahAt((it * duration).roundToLong()) }?.takeIf { it > 0 }?.let { ayah ->
                BasicText(
                    stringResource(Res.string.player_seek_ayah, ayah),
                    style = Celestial.type.label.copy(color = colors.accent),
                    modifier = Modifier.align(Alignment.Center).testTag("player_seek_ayah"),
                )
            }
        }
    }
}
