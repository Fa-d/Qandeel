package dev.sadakat.qandeel

import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.AudioTimings
import dev.sadakat.qandeel.core.domain.repository.ListeningHistory
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.QuranText
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads
import dev.sadakat.qandeel.core.domain.repository.WatchConnection
import dev.sadakat.qandeel.core.domain.repository.WordMeanings
import dev.sadakat.qandeel.shared.app.QandeelGraph
import javax.inject.Inject
import javax.inject.Singleton

/** The shared UI's ports on the phone: what Hilt provides, handed over as one graph. */
@Singleton
class AndroidQandeelGraph @Inject constructor(
    override val quranText: QuranText,
    override val settings: QuranSettings,
    override val downloads: SurahDownloads,
    override val player: QuranPlayer,
    override val history: ListeningHistory,
    override val timings: AudioTimings,
    override val wordMeanings: WordMeanings,
    override val watch: WatchConnection,
) : QandeelGraph {
    override fun now(): Long = System.currentTimeMillis()
}
