package dev.sadakat.qandeel.shared.presentation.reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Follow-along: the reader's list keeps the reciting ayah in the upper third of the screen, like
 * lyrics, until the user scrolls somewhere else; the jump chip, or tapping an ayah, resumes it.
 */
@Stable
class FollowAlongState {

    /** True while the list should keep the reciting ayah in view on its own. */
    var following: Boolean by mutableStateOf(true)
        private set

    fun pause() {
        following = false
    }

    fun resume() {
        following = true
    }

    /** Attach to the list; any user drag means the reader wants to read somewhere else. */
    val userDragObserver = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput) pause()
            return Offset.Zero
        }
    }
}

/** List index of the reciting [ayah]; the basmala (ayah 0) is the header itself. */
internal fun recitingAyahIndex(headerCount: Int, ayah: Int): Int = if (ayah < 1) 0 else headerCount + ayah - 1

/** Where the reciting ayah's top settles: this far down the list's height, the lyrics line. */
internal const val LYRICS_LINE = 0.26f

/** Scrolls [listState] so the reciting [ayah]'s item sits on the lyrics line. */
internal suspend fun LazyListState.scrollToReciting(headerCount: Int, ayah: Int) {
    val viewport = layoutInfo.viewportSize.height
    animateScrollToItem(recitingAyahIndex(headerCount, ayah), scrollOffset = -(viewport * LYRICS_LINE).toInt())
}

/**
 * The follow-along state of one reader list: while following (and the setting allows it), each
 * new reciting ayah glides onto the lyrics line; a user drag cancels that scroll and pauses following.
 */
@Composable
fun rememberFollowAlongState(
    listState: LazyListState,
    headerCount: Int,
    playingAyah: Int?,
    enabled: Boolean,
): FollowAlongState {
    val state = remember { FollowAlongState() }
    val currentEnabled by rememberUpdatedState(enabled)
    LaunchedEffect(playingAyah, headerCount) {
        val ayah = playingAyah ?: return@LaunchedEffect
        if (!currentEnabled || !state.following) return@LaunchedEffect
        val scroll = launch { listState.scrollToReciting(headerCount, ayah) }
        // A drag mid-flight must win over the auto-scroll, not fight it.
        launch {
            snapshotFlow { state.following }.first { !it }
            scroll.cancel()
        }
    }
    return state
}
