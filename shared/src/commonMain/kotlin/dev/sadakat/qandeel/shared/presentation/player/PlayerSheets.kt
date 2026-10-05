package dev.sadakat.qandeel.shared.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.player.PlaybackSpeed
import dev.sadakat.qandeel.core.domain.player.RepeatSetting
import dev.sadakat.qandeel.core.domain.player.SleepOption
import dev.sadakat.qandeel.core.domain.player.SleepTimerStatus
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialSheet
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PrimaryButton
import dev.sadakat.qandeel.shared.designsystem.kit.QuietButton
import dev.sadakat.qandeel.shared.designsystem.kit.SheetAction
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.onboarding_voice_title
import dev.sadakat.qandeel.shared.resources.player_cancel
import dev.sadakat.qandeel.shared.resources.player_cd_mode
import dev.sadakat.qandeel.shared.resources.player_open_in_reader
import dev.sadakat.qandeel.shared.resources.player_repeat_apply
import dev.sadakat.qandeel.shared.resources.player_repeat_range_label
import dev.sadakat.qandeel.shared.resources.player_repeat_target_ayah
import dev.sadakat.qandeel.shared.resources.player_repeat_target_off
import dev.sadakat.qandeel.shared.resources.player_repeat_target_range
import dev.sadakat.qandeel.shared.resources.player_repeat_times
import dev.sadakat.qandeel.shared.resources.player_repeat_times_forever
import dev.sadakat.qandeel.shared.resources.player_repeat_times_n
import dev.sadakat.qandeel.shared.resources.player_repeat_title
import dev.sadakat.qandeel.shared.resources.player_sleep
import dev.sadakat.qandeel.shared.resources.player_sleep_cancel
import dev.sadakat.qandeel.shared.resources.player_sleep_end_of_surah
import dev.sadakat.qandeel.shared.resources.player_sleep_minutes
import dev.sadakat.qandeel.shared.resources.player_speed
import dev.sadakat.qandeel.shared.resources.player_stop
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

/** The recitation: the mode, and for Arabic + Bangla, whose voice reads it. */
@Composable
fun RecitationSheet(
    mode: RecitationMode,
    voice: BanglaVoice,
    onModeChange: (RecitationMode) -> Unit,
    onVoiceChange: (BanglaVoice) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        Eyebrow(stringResource(Res.string.player_cd_mode))
        Spacer(Modifier.height(Celestial.spacing.sm))
        RecitationMode.entries.forEach { option ->
            SheetAction(modeName(option), onClick = { onModeChange(option) }, selected = option == mode)
        }
        if (mode == RecitationMode.ARABIC_BANGLA) {
            Spacer(Modifier.height(Celestial.spacing.md))
            Eyebrow(stringResource(Res.string.onboarding_voice_title))
            Spacer(Modifier.height(Celestial.spacing.sm))
            BanglaVoice.entries.forEach { option ->
                SheetAction(banglaVoiceName(option), onClick = { onVoiceChange(option) }, selected = option == voice)
            }
        }
    }
}

@Composable
fun SpeedSheet(
    speed: PlaybackSpeed,
    onSpeedChange: (PlaybackSpeed) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        PlaybackSpeed.entries.forEach { option ->
            SheetAction(
                stringResource(Res.string.player_speed, speedFactor(option)),
                onClick = {
                    onSpeedChange(option)
                    onDismiss()
                },
                selected = option == speed,
            )
        }
    }
}

@Composable
fun SleepSheet(
    status: SleepTimerStatus,
    onChange: (SleepOption?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val choose = { option: SleepOption? ->
        onChange(option)
        onDismiss()
    }
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        Eyebrow(stringResource(Res.string.player_sleep))
        Spacer(Modifier.height(Celestial.spacing.sm))
        SLEEP_MINUTES.forEach { minutes ->
            SheetAction(stringResource(Res.string.player_sleep_minutes, minutes), onClick = {
                choose(SleepOption.Minutes(minutes))
            })
        }
        SheetAction(
            stringResource(Res.string.player_sleep_end_of_surah),
            onClick = { choose(SleepOption.EndOfSurah) },
            selected = status == SleepTimerStatus.EndOfSurah,
        )
        if (status !=
            SleepTimerStatus.Off
        ) {
            SheetAction(stringResource(Res.string.player_sleep_cancel), onClick = { choose(null) })
        }
    }
}

@Composable
fun PlayerMoreSheet(
    onOpenReader: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        SheetAction(stringResource(Res.string.player_open_in_reader), onClick = {
            onDismiss()
            onOpenReader()
        })
        SheetAction(stringResource(Res.string.player_stop), onClick = {
            onDismiss()
            onStop()
        })
    }
}

private enum class RepeatTarget { OFF, AYAH, RANGE }

/**
 * What repeats: off, each ayah, or a range of this surah, and how many times. Opening it while
 * nothing repeats suggests "each ayah": that's what memorizing usually needs.
 */
@Composable
fun RepeatSheet(
    current: RepeatSetting,
    ayah: Int,
    ayahCount: Int,
    onConfirm: (RepeatSetting) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var target by rememberSaveable {
        mutableStateOf(if (current is RepeatSetting.Range) RepeatTarget.RANGE else RepeatTarget.AYAH)
    }
    var from by rememberSaveable { mutableIntStateOf((current as? RepeatSetting.Range)?.from ?: max(ayah, 1)) }
    var to by rememberSaveable {
        mutableIntStateOf((current as? RepeatSetting.Range)?.to ?: min(max(ayah, 1) + DEFAULT_RANGE_EXTRA, ayahCount))
    }
    var times by rememberSaveable { mutableStateOf(initialTimes(current)) }
    CelestialSheet(onDismiss = onDismiss, modifier = modifier) {
        Eyebrow(stringResource(Res.string.player_repeat_title))
        Spacer(Modifier.height(Celestial.spacing.md))
        val targets = mapOf(
            RepeatTarget.OFF to Res.string.player_repeat_target_off,
            RepeatTarget.AYAH to Res.string.player_repeat_target_ayah,
            RepeatTarget.RANGE to Res.string.player_repeat_target_range,
        )
        PillTabs(RepeatTarget.entries, target, {
            target = it
        }, label = { stringResource(targets.getValue(it)) }, modifier = Modifier.fillMaxWidth())
        if (target == RepeatTarget.RANGE) {
            Spacer(Modifier.height(Celestial.spacing.lg))
            BasicText(
                stringResource(Res.string.player_repeat_range_label, from, to),
                style = Celestial.type.title.copy(color = Celestial.colors.ink),
            )
            Spacer(Modifier.height(Celestial.spacing.sm))
            Stepper(stringResource(Res.string.player_repeat_target_range), from, 1..to) { from = it }
            Stepper(stringResource(Res.string.player_repeat_target_range), to, from..ayahCount) { to = it }
        }
        if (target != RepeatTarget.OFF) {
            Spacer(Modifier.height(Celestial.spacing.lg))
            BasicText(
                stringResource(Res.string.player_repeat_times),
                style = Celestial.type.title.copy(color = Celestial.colors.ink),
            )
            Spacer(Modifier.height(Celestial.spacing.sm))
            PillTabs(
                TIMES,
                times,
                { times = it },
                label = { count ->
                    count?.let { stringResource(Res.string.player_repeat_times_n, it) }
                        ?: stringResource(Res.string.player_repeat_times_forever)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(Celestial.spacing.lg))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuietButton(stringResource(Res.string.player_cancel), onClick = onDismiss)
            Spacer(Modifier.width(Celestial.spacing.sm))
            PrimaryButton(stringResource(Res.string.player_repeat_apply), onClick = {
                onConfirm(
                    when (target) {
                        RepeatTarget.OFF -> RepeatSetting.Off
                        RepeatTarget.AYAH -> RepeatSetting.Ayah(times)
                        RepeatTarget.RANGE -> RepeatSetting.Range(from, to, times)
                    },
                )
                onDismiss()
            })
        }
    }
}

/** − value +, within [range]. */
@Composable
private fun Stepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        QuietButton("−", onClick = { onChange((value - 1).coerceIn(range)) })
        BasicText(
            value.toString(),
            style = Celestial.type.title.copy(color = Celestial.colors.ink),
            modifier = Modifier.weight(1f),
        )
        QuietButton("+", onClick = { onChange((value + 1).coerceIn(range)) })
        Spacer(Modifier.width(Celestial.spacing.xs))
        BasicText(label, style = Celestial.type.caption.copy(color = Celestial.colors.inkFaint))
    }
}

private fun initialTimes(current: RepeatSetting): Int? = when (current) {
    is RepeatSetting.Ayah -> current.times
    is RepeatSetting.Range -> current.times
    RepeatSetting.Off -> DEFAULT_TIMES
}

private val SLEEP_MINUTES = listOf(15, 30, 45, 60)

/** Repeat counts offered; null is "forever". */
private val TIMES = listOf(2, 3, 5, null)
private const val DEFAULT_TIMES = 3

/** A new range starts at the current ayah and spans this many more (a typical memorizing chunk). */
private const val DEFAULT_RANGE_EXTRA = 4
