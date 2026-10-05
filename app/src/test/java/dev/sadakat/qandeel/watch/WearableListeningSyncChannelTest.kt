package dev.sadakat.qandeel.watch

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Without Wear OS services, there is nothing to catch up on and a reset is quietly not sent. */
@RunWith(AndroidJUnit4::class)
class WearableListeningSyncChannelTest {

    private val channel = WearableListeningSyncChannel(ApplicationProvider.getApplicationContext())

    @Test
    fun `no snapshots without Wear OS services`() = runTest {
        assertEquals(emptyList<Pair<String?, ByteArray>>(), channel.listeningSnapshots())
    }

    @Test
    fun `publishing a reset without Wear OS services does not throw`() = runTest {
        channel.publishReset(resetAt = 1_000)
    }
}
