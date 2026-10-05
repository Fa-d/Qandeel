package dev.sadakat.qandeel.shared.presentation.reader

import dev.sadakat.qandeel.core.domain.model.RecitationMode

/** What the reader's controls do; the route binds them to [SurahReaderViewModel] and navigation. */
data class ReaderActions(
    val onBack: () -> Unit = {},
    val onPlaySurah: () -> Unit = {},
    val onPlayAyah: (ayah: Int) -> Unit = {},
    val onPlayFromWord: (ayah: Int, word: Int) -> Unit = { _, _ -> },
    val onShowAyahActions: (ayah: Int) -> Unit = {},
    val onDismissAyahActions: () -> Unit = {},
    val onRepeatAyah: (ayah: Int) -> Unit = {},
    val onShareAyah: (text: String) -> Unit = {},
    val onModeChange: (RecitationMode) -> Unit = {},
    val onDownload: () -> Unit = {},
    val onRemoveDownload: () -> Unit = {},
    val onOpenMore: () -> Unit = {},
    val onConsumeMessage: () -> Unit = {},
    val onOpenQuickSettings: () -> Unit = {},
    val onRetry: () -> Unit = {},
)
