package com.petedev.wavemusic.utils

/** Small UI-thread cache bounded by both entry count and retained memory. */
internal class ByteBudgetCache<K, V>(
    private val maxEntries: Int,
    private val maxBytes: Long,
    private val sizeOf: (V) -> Long,
) {
    private data class Entry<V>(val value: V, val bytes: Long)
    private val entries = LinkedHashMap<K, Entry<V>>()
    private var retainedBytes = 0L

    init {
        require(maxEntries > 0 && maxBytes > 0L)
    }

    operator fun get(key: K): V? {
        val entry = entries.remove(key) ?: return null
        entries[key] = entry
        return entry.value
    }

    operator fun set(key: K, value: V) {
        val bytes = sizeOf(value)
        require(bytes >= 0L)
        entries.remove(key)?.let { retainedBytes -= it.bytes }
        // Oversized images remain owned by the visible screen, without retaining another reference.
        if (bytes > maxBytes) return
        entries[key] = Entry(value, bytes)
        retainedBytes += bytes
        while (entries.size > maxEntries || retainedBytes > maxBytes) {
            val oldest = entries.entries.iterator()
            retainedBytes -= oldest.next().value.bytes
            oldest.remove()
        }
    }
}
