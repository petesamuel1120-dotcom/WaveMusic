package com.petedev.wavemusic.viewModel

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal data class HomeFeedContext(val cookie: String, val language: String, val region: String)

/** Category snapshots live with Home's ViewModel, and expire from their initial fetch time. */
internal class HomeFeedSession<T>(
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val maxAge: Duration = 5.minutes,
) {
    data class Feed<T>(val items: List<T>, val continuation: String?)
    data class Request(val params: String?, val revision: Long)
    private data class Entry<T>(val feed: Feed<T>, val fetchedAt: TimeMark)

    private val feeds = mutableMapOf<String?, Entry<T>>()
    private var context: HomeFeedContext? = null
    private var revision = 0L
    private var current: Request? = null

    fun beginRequest(params: String?): Request = Request(params, ++revision).also { current = it }

    fun isCurrent(request: Request): Boolean = current == request

    fun read(request: Request, context: HomeFeedContext, forceRefresh: Boolean = false): Feed<T>? {
        if (!isCurrent(request)) return null
        if (this.context != context) {
            feeds.clear()
            this.context = context
        }
        if (forceRefresh) {
            feeds.remove(request.params)
            return null
        }
        val entry = feeds[request.params] ?: return null
        if (entry.fetchedAt.elapsedNow() >= maxAge) {
            feeds.remove(request.params)
            return null
        }
        return entry.feed
    }

    fun store(request: Request, feed: Feed<T>): Boolean {
        if (!isCurrent(request)) return false
        feeds[request.params] = Entry(feed, timeSource.markNow())
        return true
    }

    fun updatePagination(request: Request, feed: Feed<T>): Boolean {
        if (!isCurrent(request)) return false
        val entry = feeds[request.params] ?: return false
        // Loading another page must not make an old recommendation feed fresh again.
        feeds[request.params] = entry.copy(feed = feed)
        return true
    }
}
