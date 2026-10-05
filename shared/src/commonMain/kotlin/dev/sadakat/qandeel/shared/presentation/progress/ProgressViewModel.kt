package dev.sadakat.qandeel.shared.presentation.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.qandeel.core.domain.model.ListeningOrder
import dev.sadakat.qandeel.core.domain.model.ListeningProgress
import dev.sadakat.qandeel.core.domain.model.Surah
import dev.sadakat.qandeel.core.domain.repository.ListeningHistory
import dev.sadakat.qandeel.core.domain.repository.QuranText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One heard surah in the list. */
data class ProgressRowUi(
    val surah: Int,
    /** English name; null while the text source loads, or if it failed ("Surah N" then). */
    val nameEnglish: String?,
    /** Arabic name without "سُورَةُ"; null while unknown. */
    val nameArabicShort: String?,
    /** Completed full rounds of the surah. */
    val rounds: Int,
    val ayahsHeard: Int,
    val ayahCount: Int,
    val ayahsIntoNextRound: Int,
    /** How far the round in progress has come, 0..1. */
    val nextRoundProgress: Float,
    /** Every ayah listen in the surah, summed. */
    val totalListens: Int,
)

/** What the Progress screen shows: the whole Quran's summary and the surahs heard, sorted. */
data class ProgressUiState(
    val isLoading: Boolean = true,
    val ayahsHeard: Int = 0,
    val coverage: Float = 0f,
    /** Times the whole Quran has been heard. */
    val rounds: Int = 0,
    val listenedMs: Long = 0L,
    val order: ListeningOrder = ListeningOrder.RECENT,
    /** The surahs heard at least once, already sorted by [order]. */
    val rows: List<ProgressRowUi> = emptyList(),
) {
    /** Nothing has been heard yet; the screen shows its empty state. */
    val isEmpty: Boolean get() = rows.isEmpty()
}

class ProgressViewModel(
    private val history: ListeningHistory,
    quranText: QuranText,
    private val savedStateHandle: SavedStateHandle,
    /** Epoch milliseconds now; a reset is stamped with it, so synced devices know which is newer. */
    private val now: () -> Long,
) : ViewModel() {

    // Saved by name: a multiplatform SavedStateHandle keeps only basic types.
    private val order = MutableStateFlow(
        savedStateHandle.get<String>(ORDER_KEY)?.let { name -> ListeningOrder.entries.firstOrNull { it.name == name } }
            ?: ListeningOrder.RECENT,
    )

    private data class Surahs(val byNumber: Map<Int, Surah> = emptyMap(), val loaded: Boolean = false)

    private val surahs = flow {
        val byNumber = quranText.surahs().associateBy { it.number }
        emit(Surahs(byNumber = byNumber, loaded = true))
    }.catch {
        // Names are cosmetic; rows fall back to "Surah N".
        emit(Surahs(loaded = true))
    }

    val uiState: StateFlow<ProgressUiState> = combine(surahs, history.counts, order) { surahs, counts, selectedOrder ->
        val quran = ListeningProgress.quran(counts)
        ProgressUiState(
            isLoading = !surahs.loaded,
            ayahsHeard = quran.ayahsHeard,
            coverage = quran.coverage,
            rounds = quran.rounds,
            listenedMs = quran.listenedMs,
            order = selectedOrder,
            rows = ListeningProgress.sorted(quran.surahs, selectedOrder).map { listening ->
                val surah = surahs.byNumber[listening.surah]
                ProgressRowUi(
                    surah = listening.surah,
                    nameEnglish = surah?.nameEnglish,
                    nameArabicShort = surah?.nameArabicShort,
                    rounds = listening.rounds,
                    ayahsHeard = listening.ayahsHeard,
                    ayahCount = listening.ayahCount,
                    ayahsIntoNextRound = listening.ayahsIntoNextRound,
                    nextRoundProgress = listening.nextRoundProgress,
                    totalListens = listening.totalListens,
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setOrder(order: ListeningOrder) {
        savedStateHandle[ORDER_KEY] = order.name
        this.order.value = order
    }

    /** Forgets everything heard, here and on synced devices (the screen asks first). */
    fun reset() {
        viewModelScope.launch { history.reset(now()) }
    }

    private companion object {
        const val ORDER_KEY = "order"
    }
}
