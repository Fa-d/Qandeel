package dev.sadakat.qandeel.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** The phone app's package; the generator and benchmarks drive the installed app. */
internal const val PACKAGE = "dev.sadakat.qandeel"

private const val TIMEOUT_MS = 10_000L
private const val SHORT_TIMEOUT_MS = 2_000L

/** Cold start to the first frame of home, past onboarding on a fresh install. */
internal fun MacrobenchmarkScope.startApp() {
    pressHome()
    startActivityAndWait()
    device.wait(Until.findObject(By.res("onboarding_skip")), SHORT_TIMEOUT_MS)?.click()
    device.wait(Until.hasObject(By.res("home_list")), TIMEOUT_MS)
}

/**
 * What people do most: scroll home, then the surahs in the Quran tab, open one and read, and open
 * the full player when something is queued. The screens' test tags are exposed as resource ids.
 */
internal fun MacrobenchmarkScope.commonJourney() {
    device.wait(Until.findObject(By.res("home_list")), TIMEOUT_MS)?.let { home ->
        home.setGestureMargin(device.displayWidth / GESTURE_MARGIN_DIVISOR)
        home.fling(Direction.DOWN)
        device.waitForIdle()
    }
    device.wait(Until.findObject(By.res("tab_quran")), TIMEOUT_MS)?.click()
    val list = device.wait(Until.findObject(By.res("quran_list")), TIMEOUT_MS) ?: return
    // Keep flings off the gesture-navigation edges.
    list.setGestureMargin(device.displayWidth / GESTURE_MARGIN_DIVISOR)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()

    device.wait(Until.findObject(By.res("surah_1")), TIMEOUT_MS)?.click()
    device.wait(Until.findObject(By.res("ayah_list")), TIMEOUT_MS)?.let { ayahs ->
        ayahs.setGestureMargin(device.displayWidth / GESTURE_MARGIN_DIVISOR)
        ayahs.fling(Direction.DOWN)
        device.waitForIdle()
    }
    device.pressBack()
    device.wait(Until.hasObject(By.res("quran_list")), TIMEOUT_MS)

    device.findObject(By.res("mini_player"))?.let { miniPlayer ->
        miniPlayer.click()
        device.wait(Until.hasObject(By.res("player_ayah")), TIMEOUT_MS)
        device.pressBack()
    }
}

private const val GESTURE_MARGIN_DIVISOR = 5
