package com.petedev.wavemusic.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.math.ceil

internal fun initialArtistThumbnailCount(viewportHeightPx: Int, reservedHeightPx: Float, rowHeightPx: Float): Int =
    if (rowHeightPx <= 0f) 0 else ceil((viewportHeightPx - reservedHeightPx).coerceAtLeast(0f) / rowHeightPx).toInt()

/** Library owns these requests, so changing its category does not cancel or duplicate them. */
internal class LibraryArtistThumbnailLoader(
    private val scope: CoroutineScope,
    private val thumbnailCache: MutableMap<String, String>,
    private val resolveThumbnail: suspend (String) -> String?,
    private val preloadImage: suspend (String) -> Boolean,
) {
    private val requests = mutableMapOf<String, Job>()
    private val preloadedUrls = mutableSetOf<String>()
    private val permits = Semaphore(4)

    fun preload(channelIds: List<String>) {
        channelIds.distinct().filter(String::isNotBlank).forEach { channelId ->
            if (requests.containsKey(channelId)) return@forEach
            val cached = thumbnailCache[channelId]?.takeIf(String::isNotBlank)
            if (cached != null && cached in preloadedUrls) return@forEach
            val job = scope.launch(start = CoroutineStart.LAZY) {
                try {
                    permits.withPermit {
                        val thumbnail = thumbnailCache[channelId]?.takeIf(String::isNotBlank)
                            ?: resolveThumbnail(channelId)?.takeIf(String::isNotBlank)
                            ?: return@withPermit
                        thumbnailCache[channelId] = thumbnail
                        if (thumbnail !in preloadedUrls && preloadImage(thumbnail)) {
                            preloadedUrls.add(thumbnail)
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // An offline or unavailable artist can retry on a later Library visit/category.
                } finally {
                    requests.remove(channelId)
                }
            }
            requests[channelId] = job
            job.start()
        }
    }
}
