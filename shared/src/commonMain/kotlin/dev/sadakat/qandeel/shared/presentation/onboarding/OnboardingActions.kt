package dev.sadakat.qandeel.shared.presentation.onboarding

import dev.sadakat.qandeel.core.domain.model.ArabicTextSize
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.ThemeMode
import dev.sadakat.qandeel.core.domain.model.WordByWord

/** What onboarding's controls do; the route binds them to [OnboardingViewModel]. */
data class OnboardingActions(
    val onModeChange: (RecitationMode) -> Unit = {},
    val onVoiceChange: (BanglaVoice) -> Unit = {},
    val onToggleSample: (BanglaVoice) -> Unit = {},
    val onStopSample: () -> Unit = {},
    val onArabicTextSizeChange: (ArabicTextSize) -> Unit = {},
    val onShowTranslationChange: (Boolean) -> Unit = {},
    val onWordByWordChange: (WordByWord) -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onReduceMotionChange: (Boolean) -> Unit = {},
    val onDownloadStarterChange: (Boolean) -> Unit = {},
    val onSendToWatchChange: (Boolean) -> Unit = {},
    val onFinish: () -> Unit = {},
    val onSkip: () -> Unit = {},
)
