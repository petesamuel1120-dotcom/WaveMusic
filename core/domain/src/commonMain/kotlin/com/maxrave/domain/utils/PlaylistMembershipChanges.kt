package com.maxrave.domain.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Confirmed writes invalidate only the affected playlist's in-process presentation cache. */
object PlaylistMembershipChanges {
    private val revisions = MutableStateFlow<Map<String, Long>>(emptyMap())
    val changes = revisions.asStateFlow()

    fun revision(playlistId: String): Long = revisions.value[playlistId.removePrefix("VL")] ?: 0L

    fun record(playlistId: String) {
        val id = playlistId.removePrefix("VL")
        revisions.update { current -> current + (id to ((current[id] ?: 0L) + 1L)) }
    }
}
