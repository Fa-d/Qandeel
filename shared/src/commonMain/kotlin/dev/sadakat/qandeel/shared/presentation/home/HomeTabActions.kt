package dev.sadakat.qandeel.shared.presentation.home

/** What Home's controls do. */
class HomeTabActions(
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
    val onContinuePlayPause: () -> Unit,
    val onOpenProgress: () -> Unit,
)
