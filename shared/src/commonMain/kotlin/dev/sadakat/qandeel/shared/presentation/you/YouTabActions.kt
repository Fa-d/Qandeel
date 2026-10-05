package dev.sadakat.qandeel.shared.presentation.you

import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord

/** What the You tab's controls do: the settings ViewModel's setters, and where it leads. */
data class YouTabActions(
    val onModeChange: (RecitationMode) -> Unit = {},
    val onVoiceChange: (BanglaVoice) -> Unit = {},
    val onArabicTextSizeChange: (ArabicTextSize) -> Unit = {},
    val onShowTranslationChange: (Boolean) -> Unit = {},
    val onFollowAlongChange: (Boolean) -> Unit = {},
    val onWordByWordChange: (WordByWord) -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onReduceMotionChange: (Boolean) -> Unit = {},
    val onOpenProgress: () -> Unit = {},
    val onOpenAbout: () -> Unit = {},
)
