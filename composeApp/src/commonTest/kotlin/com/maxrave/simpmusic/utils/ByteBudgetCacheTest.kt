package com.maxrave.simpmusic.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ByteBudgetCacheTest {
    private fun cache(entries: Int = 6, bytes: Long = 10L) =
        ByteBudgetCache<String, String>(entries, bytes) { it.length.toLong() }

    @Test fun evictsByMemoryBeforeEntryLimit() {
        val cache = cache()
        cache["a"] = "123456"
        cache["b"] = "abcdef"
        assertNull(cache["a"])
        assertEquals("abcdef", cache["b"])
    }

    @Test fun recentlyViewedArtworkSurvivesEviction() {
        val cache = cache(entries = 2)
        cache["a"] = "a"
        cache["b"] = "b"
        assertEquals("a", cache["a"])
        cache["c"] = "c"
        assertNull(cache["b"])
        assertEquals("a", cache["a"])
    }

    @Test fun oversizedArtworkDoesNotFlushOtherArtwork() {
        val cache = cache()
        cache["a"] = "small"
        cache["large"] = "12345678901"
        assertNull(cache["large"])
        assertEquals("small", cache["a"])
    }

    @Test fun replacingArtworkReclaimsThePreviousSize() {
        val cache = cache()
        cache["a"] = "123456789"
        cache["a"] = "12"
        cache["b"] = "12345678"
        assertEquals("12", cache["a"])
        assertEquals("12345678", cache["b"])
    }

    @Test fun entryLimitStillBoundsTinyImages() {
        val cache = cache(entries = 2)
        cache["a"] = "a"
        cache["b"] = "b"
        cache["c"] = "c"
        assertNull(cache["a"])
        assertEquals("b", cache["b"])
        assertEquals("c", cache["c"])
    }
}
