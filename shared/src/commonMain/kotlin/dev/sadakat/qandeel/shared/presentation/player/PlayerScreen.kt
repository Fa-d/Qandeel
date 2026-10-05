package dev.sadakat.qandeel.shared.presentation.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.core.domain.model.QuranMeta
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.player.PlaybackProgress
import dev.sadakat.qandeel.core.domain.player.PlaybackSpeed
import dev.sadakat.qandeel.core.domain.player.RepeatSetting
import dev.sadakat.qandeel.core.domain.player.SleepTimerStatus
import dev.sadakat.qandeel.core.domain.player.WordPointer
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.GlyphButton
import dev.sadakat.qandeel.shared.designsystem.kit.PlayButton
import dev.sadakat.qandeel.shared.designsystem.lamp.LampMotion
import dev.sadakat.qandeel.shared.designsystem.lamp.QandeelLamp
import dev.sadakat.qandeel.shared.presentation.components.RecitedArabicText
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.player_ayah_of
import dev.sadakat.qandeel.shared.resources.player_basmala
import dev.sadakat.qandeel.shared.resources.player_cd_close
import dev.sadakat.qandeel.shared.resources.player_cd_more
import dev.sadakat.qandeel.shared.resources.player_cd_previous
import dev.sadakat.qandeel.shared.resources.player_next_ayah
import dev.sadakat.qandeel.shared.resources.player_pause
import dev.sadakat.qandeel.shared.resources.player_play
import dev.sadakat.qandeel.shared.resources.player_sleep
import dev.sadakat.qandeel.shared.resources.player_speed
import dev.sadakat.qandeel.shared.resources.reader_basmala
import dev.sadakat.qandeel.shared.resources.surah_fallback_name
import org.jetbrains.compose.resources.stringResource

/**
 * The full player, centred on the ayah: its Arabic large with the gold word pointer, the meaning
 * of the word being recited, and its translation; behind it the lantern, faint, swelling a little
 * with every recited word. Below: the whole surah as one bar, the transport, and the recitation,
 * repeat, speed and sleep, each a tap away.
 */
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    pointer: WordPointer,
    progress: () -> PlaybackProgress,
    actions: PlayerActions,
    modifier: Modifier = Modifier,
) {
    val nowPlaying = state.nowPlaying ?: return
    var sheet by rememberSaveable { mutableStateOf<PlayerSheet?>(null) }
    val clock = Celestial.clock
    val pulse = rememberWordPulse(pointer)
    val colors = Celestial.colors

    Box(modifier.fillMaxSize().testTag("player")) {
        CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.5f, 0.4f))
        QandeelLamp(
            motion = { LampMotion(time = clock.seconds(), energy = pulse.value) },
            colors = colors.lamp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 160.dp)
                .size(420.dp)
                .graphicsLayer { alpha = LANTERN_ALPHA },
        )
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Celestial.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphButton(CelestialIcons.Down, stringResource(Res.string.player_cd_close), actions.onClose)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Eyebrow(state.surahName ?: stringResource(Res.string.surah_fallback_name, nowPlaying.surah))
                    BasicText(
                        if (nowPlaying.ayah == 0) {
                            stringResource(Res.string.player_basmala)
                        } else {
                            stringResource(
                                Res.string.player_ayah_of,
                                nowPlaying.ayah,
                                QuranMeta.ayahCount(nowPlaying.surah),
                            )
                        },
                        style = Celestial.type.caption.copy(color = colors.inkMuted),
                    )
                }
                GlyphButton(CelestialIcons.More, stringResource(Res.string.player_cd_more), {
                    sheet = PlayerSheet.MORE
                })
            }
            Ayah(state, pointer, Modifier.weight(1f))
            Column(Modifier.padding(horizontal = Celestial.spacing.gutter)) {
                TimeBar(progress, fallback = nowPlaying.progress, ayahAt = state::ayahAt, onSeek = actions.onSeek)
                Spacer(Modifier.height(Celestial.spacing.sm))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphButton(
                        CelestialIcons.Previous,
                        stringResource(Res.string.player_cd_previous),
                        actions.onPrevious,
                    )
                    PlayButton(
                        playing = nowPlaying.isPlaying,
                        label = stringResource(
                            if (nowPlaying.isPlaying) Res.string.player_pause else Res.string.player_play,
                        ),
                        onClick = actions.onTogglePlayPause,
                        size = 76.dp,
                    )
                    GlyphButton(CelestialIcons.Next, stringResource(Res.string.player_next_ayah), actions.onNext)
                }
                Spacer(Modifier.height(Celestial.spacing.md))
                Options(state, onOpen = { sheet = it })
                Spacer(Modifier.height(Celestial.spacing.lg))
            }
        }
    }

    when (sheet) {
        PlayerSheet.RECITATION -> RecitationSheet(
            nowPlaying.mode,
            state.voice,
            actions.onModeChange,
            actions.onVoiceChange,
            onDismiss = { sheet = null },
        )

        PlayerSheet.REPEAT -> RepeatSheet(
            current = nowPlaying.repeat,
            ayah = nowPlaying.ayah,
            ayahCount = QuranMeta.ayahCount(nowPlaying.surah),
            onConfirm = actions.onRepeatChange,
            onDismiss = { sheet = null },
        )

        PlayerSheet.SPEED -> SpeedSheet(nowPlaying.speed, actions.onSpeedChange, onDismiss = { sheet = null })

        PlayerSheet.SLEEP -> SleepSheet(state.sleepTimer, actions.onSleepTimerChange, onDismiss = { sheet = null })

        PlayerSheet.MORE -> PlayerMoreSheet(
            onOpenReader = { actions.onOpenReader(nowPlaying.surah, nowPlaying.ayah) },
            onStop = actions.onStop,
            onDismiss = { sheet = null },
        )

        null -> Unit
    }
}

private const val LANTERN_ALPHA = 0.28f

/** The ayah, centred: Arabic with the pointer, the recited word's meaning, the translation. */
@Composable
private fun Ayah(state: PlayerUiState, pointer: WordPointer, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val arabic = state.ayahArabic ?: stringResource(Res.string.reader_basmala)
        RecitedArabicText(
            text = arabic,
            pointer = pointer,
            style = Celestial.quran.display,
            textAlign = TextAlign.Center,
            keepCurrentLineInView = true,
            modifier = Modifier.fillMaxWidth().testTag("player_ayah"),
        )
        val word = (pointer as? WordPointer.Reciting)?.word
        val meaning = word?.let { state.ayahMeanings.getOrNull(it) }
        if (meaning != null) {
            Spacer(Modifier.height(Celestial.spacing.md))
            GlassSurface(radius = Celestial.shapes.control) {
                BasicText(
                    meaning,
                    style = Celestial.type.title.copy(color = colors.accent),
                    modifier = Modifier.padding(horizontal = Celestial.spacing.lg, vertical = Celestial.spacing.sm),
                )
            }
        }
        state.ayahTranslation?.let {
            Spacer(Modifier.height(Celestial.spacing.lg))
            BasicText(it, style = Celestial.type.body.copy(color = colors.inkMuted, textAlign = TextAlign.Center))
        }
    }
}

/** The recitation, repeat, speed and sleep, as small glass pills that open their sheets. */
@Composable
private fun Options(state: PlayerUiState, onOpen: (PlayerSheet) -> Unit) {
    val nowPlaying = state.nowPlaying ?: return
    val recitation = if (nowPlaying.mode == RecitationMode.ARABIC_BANGLA) {
        banglaVoiceName(state.voice)
    } else {
        modeName(nowPlaying.mode)
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.sm, Alignment.CenterHorizontally),
    ) {
        OptionPill(recitation, active = false, tag = "player_mode") { onOpen(PlayerSheet.RECITATION) }
        OptionPill(
            repeatLabel(nowPlaying.repeat),
            active = nowPlaying.repeat != RepeatSetting.Off,
            tag = "player_repeat",
        ) {
            onOpen(PlayerSheet.REPEAT)
        }
        OptionPill(
            stringResource(Res.string.player_speed, speedFactor(nowPlaying.speed)),
            active = nowPlaying.speed != PlaybackSpeed.X1,
            tag = "player_speed",
        ) { onOpen(PlayerSheet.SPEED) }
        OptionPill(
            sleepLabel(state.sleepTimer) ?: stringResource(Res.string.player_sleep),
            active = state.sleepTimer != SleepTimerStatus.Off,
            tag = "player_sleep",
        ) { onOpen(PlayerSheet.SLEEP) }
    }
}

@Composable
private fun OptionPill(label: String, active: Boolean, tag: String, onClick: () -> Unit) {
    val colors = Celestial.colors
    GlassSurface(Modifier.clip(CircleShape).clickable(onClick = onClick).testTag(tag), radius = 20.dp) {
        BasicText(
            label,
            style = Celestial.type.label.copy(color = if (active) colors.accent else colors.ink),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = Celestial.spacing.md, vertical = Celestial.spacing.sm),
        )
    }
}

/**
 * The lantern's energy: a swell on each newly recited word that eases back down, so the light
 * breathes with the recitation. Idle when nothing is being recited.
 */
@Composable
private fun rememberWordPulse(pointer: WordPointer): Animatable<Float, *> {
    val pulse = remember { Animatable(PULSE_REST) }
    val word = (pointer as? WordPointer.Reciting)?.word
    LaunchedEffect(word) {
        if (word != null) {
            pulse.snapTo(1f)
            pulse.animateTo(PULSE_REST, tween(PULSE_MS))
        }
    }
    return pulse
}

private const val PULSE_REST = 0.3f
private const val PULSE_MS = 700

/** Which of the player's option sheets is open. */
internal enum class PlayerSheet { RECITATION, REPEAT, SPEED, SLEEP, MORE }
