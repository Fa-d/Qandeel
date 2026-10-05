package dev.sadakat.qandeel.presentation

import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.Track
import dev.sadakat.qandeel.core.domain.player.NowPlaying
import dev.sadakat.qandeel.presentation.player.PlayerUiState
import dev.sadakat.qandeel.shared.presentation.main.MiniPlayerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniPlayerMappingTest {

    private val playing =
        NowPlaying(18, 11, Track.ARABIC, RecitationMode.ARABIC_ENGLISH, isPlaying = false, isBuffering = false)

    @Test
    fun `nothing queued, no mini player`() {
        assertNull(PlayerUiState().miniPlayer())
    }

    @Test
    fun `what plays, by name, mode and how far through the surah`() {
        assertEquals(
            MiniPlayerUi(18, "Al-Kahf", ayah = 11, modeLabel = "Arabic + English", progress = 0.1f, isPlaying = false),
            PlayerUiState(nowPlaying = playing, surahName = "Al-Kahf").miniPlayer(),
        )
    }

    @Test
    fun `before the surah's name is known, its number stands in`() {
        assertEquals("18", PlayerUiState(nowPlaying = playing).miniPlayer()?.surahName)
    }
}
