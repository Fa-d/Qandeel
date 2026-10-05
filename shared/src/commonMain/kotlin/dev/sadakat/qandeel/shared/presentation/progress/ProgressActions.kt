package dev.sadakat.qandeel.shared.presentation.progress

import dev.sadakat.qandeel.core.domain.model.ListeningOrder
import dev.sadakat.qandeel.shared.resources.Res

/** What the Progress screen's controls do. */
class ProgressActions(
    val onBack: () -> Unit,
    val onOrderChange: (ListeningOrder) -> Unit,
    val onReset: () -> Unit,
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
)
