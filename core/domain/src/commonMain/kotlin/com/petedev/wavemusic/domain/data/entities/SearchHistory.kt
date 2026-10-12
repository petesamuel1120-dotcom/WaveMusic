package com.petedev.wavemusic.domain.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = false)
    val query: String,
)