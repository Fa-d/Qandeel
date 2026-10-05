package dev.sadakat.qandeel.core.domain.repository

import dev.sadakat.qandeel.core.domain.model.Track

/**
 * The phone's link to the Qandeel watch app (Wear OS, through Google Play services on Android; an
 * iPhone has none, so there it is never reachable).
 */
interface WatchConnection {

    /** True if a watch with the Qandeel app installed is currently reachable. */
    suspend fun isWatchReachable(): Boolean

    /**
     * Asks every reachable watch to download [surah] for [tracks].
     * Returns the number of watches the request reached; fails if none did.
     */
    suspend fun sendDownload(surah: Int, tracks: List<Track>): Result<Int>
}
