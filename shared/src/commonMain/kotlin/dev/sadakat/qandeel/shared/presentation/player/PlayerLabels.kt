package dev.sadakat.qandeel.shared.presentation.player

import androidx.compose.runtime.Composable
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.player.PlaybackSpeed
import dev.sadakat.qandeel.core.domain.player.RepeatSetting
import dev.sadakat.qandeel.core.domain.player.SleepTimerStatus
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.onboarding_voice_baezeed
import dev.sadakat.qandeel.shared.resources.onboarding_voice_if
import dev.sadakat.qandeel.shared.resources.onboarding_voice_toha
import dev.sadakat.qandeel.shared.resources.player_mode_full_arabic
import dev.sadakat.qandeel.shared.resources.player_mode_full_bangla
import dev.sadakat.qandeel.shared.resources.player_mode_full_english
import dev.sadakat.qandeel.shared.resources.player_repeat_ayah
import dev.sadakat.qandeel.shared.resources.player_repeat_ayah_forever
import dev.sadakat.qandeel.shared.resources.player_repeat_off
import dev.sadakat.qandeel.shared.resources.player_repeat_range
import dev.sadakat.qandeel.shared.resources.player_repeat_range_forever
import dev.sadakat.qandeel.shared.resources.player_sleep_end_of_surah
import dev.sadakat.qandeel.shared.resources.player_sleep_remaining
import org.jetbrains.compose.resources.stringResource

/** Full mode names ("Arabic + English"). */
@Composable
fun modeName(mode: RecitationMode): String = stringResource(
    when (mode) {
        RecitationMode.ARABIC_ONLY -> Res.string.player_mode_full_arabic
        RecitationMode.ARABIC_ENGLISH -> Res.string.player_mode_full_english
        RecitationMode.ARABIC_BANGLA -> Res.string.player_mode_full_bangla
    },
)

/** Who reads the Bangla. */
@Composable
fun banglaVoiceName(voice: BanglaVoice): String = stringResource(
    when (voice) {
        BanglaVoice.ISLAMIC_FOUNDATION -> Res.string.onboarding_voice_if
        BanglaVoice.SAYED_ISMAT_TOHA -> Res.string.onboarding_voice_toha
        BanglaVoice.SHAREEF_BAEZEED_MAHMOOD -> Res.string.onboarding_voice_baezeed
    },
)

/** "Repeat", "Ayah ×3", "Ayah ∞", "3–7 ×2", "3–7 ∞". */
@Composable
fun repeatLabel(repeat: RepeatSetting): String = when (repeat) {
    RepeatSetting.Off -> stringResource(Res.string.player_repeat_off)

    is RepeatSetting.Ayah -> repeat.times?.let { stringResource(Res.string.player_repeat_ayah, it) }
        ?: stringResource(Res.string.player_repeat_ayah_forever)

    is RepeatSetting.Range -> repeat.times?.let {
        stringResource(Res.string.player_repeat_range, repeat.from, repeat.to, it)
    }
        ?: stringResource(Res.string.player_repeat_range_forever, repeat.from, repeat.to)
}

/** "0.75", "1", "1.25", "1.5": the factor without a trailing ".0". */
fun speedFactor(speed: PlaybackSpeed): String = speed.factor.toString().removeSuffix(".0")

/** "12:34" while counting, "End of surah", or null when no timer runs. */
@Composable
fun sleepLabel(status: SleepTimerStatus): String? = when (status) {
    SleepTimerStatus.Off -> null
    SleepTimerStatus.EndOfSurah -> stringResource(Res.string.player_sleep_end_of_surah)
    is SleepTimerStatus.Counting -> remaining(status.remainingMs)
    is SleepTimerStatus.FadingOut -> remaining(status.remainingMs)
}

@Composable
private fun remaining(ms: Long): String {
    val seconds = (ms + MS_PER_SECOND - 1) / MS_PER_SECOND
    return stringResource(
        Res.string.player_sleep_remaining,
        (seconds / SECONDS_PER_MINUTE).toInt(),
        (seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0'),
    )
}

/** A time as a clock reads it: "3:07", or "1:02:45" from an hour on. */
fun clockText(ms: Long): String {
    val seconds = ms.coerceAtLeast(0) / MS_PER_SECOND
    val hours = seconds / SECONDS_PER_HOUR
    val minutes = seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val rest = (seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$rest" else "$minutes:$rest"
}

private const val MS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L
