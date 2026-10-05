package dev.sadakat.qandeel.shared.app

import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.AudioTimings
import dev.sadakat.qandeel.core.domain.repository.ListeningHistory
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.QuranText
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads
import dev.sadakat.qandeel.core.domain.repository.WatchConnection
import dev.sadakat.qandeel.core.domain.repository.WordMeanings

/**
 * The domain ports the shared UI's ViewModels are built from, one implementation per platform:
 * on Android Hilt provides them, on iOS a Kotlin object does. No DI library crosses platforms.
 */
interface QandeelGraph {
    val quranText: QuranText
    val settings: QuranSettings
    val downloads: SurahDownloads
    val player: QuranPlayer
    val history: ListeningHistory
    val timings: AudioTimings
    val wordMeanings: WordMeanings
    val watch: WatchConnection

    /** Epoch milliseconds now. */
    fun now(): Long
}

/** What only the platform can do for the shared UI. */
interface QandeelPlatform {
    /** The app's version, for About. */
    val versionName: String

    /** Opens the platform's share sheet with [text]. */
    fun shareText(text: String)

    /** Asks to show notifications (playback controls, download progress), where that needs asking. */
    fun askForNotifications() {}
}
