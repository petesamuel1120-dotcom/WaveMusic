package com.petedev.wavemusic.domain.repository

import com.petedev.wavemusic.domain.data.model.update.UpdateData
import com.petedev.wavemusic.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {
    fun checkForGithubReleaseUpdate(): Flow<Resource<UpdateData>>
    fun checkForFdroidUpdate(): Flow<Resource<UpdateData>>

    fun getFdroidSigningKeys(): Flow<Resource<List<String>>>
}