package dev.sadakat.qandeel.watch

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sadakat.qandeel.core.domain.model.Track
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Without Wear OS services (as on many phones), the watch link answers "no watch" and never throws. */
@RunWith(AndroidJUnit4::class)
class WatchLinkTest {

    private val link = WatchLink(ApplicationProvider.getApplicationContext())

    @Test
    fun `no watch is reachable without Wear OS services`() = runTest {
        assertFalse(link.isWatchReachable())
    }

    @Test
    fun `sending a download without Wear OS services fails as a result`() = runTest {
        val result = link.sendDownload(1, listOf(Track.ARABIC))

        assertTrue(result.isFailure)
    }
}
