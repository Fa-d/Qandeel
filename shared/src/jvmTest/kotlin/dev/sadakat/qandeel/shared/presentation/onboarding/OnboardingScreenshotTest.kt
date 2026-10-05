package dev.sadakat.qandeel.shared.presentation.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import dev.sadakat.qandeel.core.domain.model.BanglaVoice
import dev.sadakat.qandeel.core.domain.model.WordByWord
import dev.sadakat.qandeel.shared.designsystem.phoneSnapshot
import org.junit.Test

/** Every onboarding page, at night; the welcome at dawn too. */
class OnboardingScreenshotTest {

    private fun page(name: String, page: Int, night: Boolean = true, state: OnboardingUiState = OnboardingUiState()) =
        phoneSnapshot("onboarding_$name", night = night) {
            OnboardingScreen(
                state,
                OnboardingActions(),
                pagerState = rememberPagerState(initialPage = page) {
                    ONBOARDING_PAGES
                },
            )
        }

    @Test
    fun welcome() = page("welcome_night", 0)

    @Test
    fun welcomeDawn() = page("welcome_dawn", 0, night = false)

    @Test
    fun listeningWithASamplePlaying() = page(
        "listening",
        1,
        state = OnboardingUiState(
            choices = OnboardingChoices(voice = BanglaVoice.SAYED_ISMAT_TOHA),
            previewing = BanglaVoice.SAYED_ISMAT_TOHA,
        ),
    )

    @Test
    fun readingWordByWord() =
        page("reading", 2, state = OnboardingUiState(OnboardingChoices(wordByWord = WordByWord.ENGLISH)))

    @Test
    fun look() = page("look", 3)

    @Test
    fun offlineWithAWatch() = page("offline", 4, state = OnboardingUiState(watchReachable = true))
}
