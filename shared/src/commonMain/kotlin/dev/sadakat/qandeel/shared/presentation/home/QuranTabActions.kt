package dev.sadakat.qandeel.shared.presentation.home

/** What the Quran tab's controls do. */
class QuranTabActions(
    val onQueryChange: (String) -> Unit,
    val onBrowseChange: (BrowseMode) -> Unit,
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
    val onRetry: () -> Unit,
)
