package dev.sadakat.qandeel.shared.app

import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerUi
import dev.sadakat.qandeel.shared.presentation.player.PlayerUiState

/** The mini player for what plays, or null when nothing is queued. */
internal fun PlayerUiState.miniPlayer(): MiniPlayerUi? = nowPlaying?.let { playing ->
    MiniPlayerUi(
        surah = playing.surah,
        surahName = surahName ?: "${playing.surah}",
        ayah = playing.ayah,
        modeLabel = playing.mode.label,
        progress = playing.progress,
        isPlaying = playing.isPlaying,
    )
}
