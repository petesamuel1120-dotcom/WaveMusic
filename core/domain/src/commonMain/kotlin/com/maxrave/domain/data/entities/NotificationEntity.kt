package com.maxrave.domain.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maxrave.domain.extension.now
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val channelId: String,
    val thumbnail: String? = null,
    val name: String,
    val single: List<Map<String, String>> = listOf(),
    val album: List<Map<String, String>> = listOf(),
    val time: LocalDateTime = now(),
    // Discriminates an artist-release notification from an RSS blog-post one.
    // @ColumnInfo(defaultValue) is REQUIRED: Room AutoMigration adds this NOT NULL column and
    // needs a SQL default to backfill existing rows (the Kotlin default alone is not enough).
    // Every pre-existing row therefore becomes an artist notification.
    @ColumnInfo(defaultValue = "artist")
    val type: String = TYPE_ARTIST,
    // Blog-only fields (null on artist rows). `link` doubles as the dedup key.
    val link: String? = null,
    val description: String? = null,
) {
    companion object {
        const val TYPE_ARTIST = "artist"
        const val TYPE_BLOG = "blog"
    }
}