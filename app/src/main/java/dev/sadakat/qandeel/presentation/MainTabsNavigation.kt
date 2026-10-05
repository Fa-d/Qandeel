package dev.sadakat.qandeel.presentation

import dev.sadakat.qandeel.shared.presentation.main.MainTab
import dev.sadakat.qandeel.shared.presentation.main.MainTabs

/** Where the tabs lead outside themselves; the reader and the full player are still the phone's own. */
class MainTabsNavigation(
    val onOpenReader: (surah: Int, ayah: Int) -> Unit,
    val onOpenPlayer: () -> Unit,
    val onOpenProgress: () -> Unit,
    val onOpenAbout: () -> Unit,
)
