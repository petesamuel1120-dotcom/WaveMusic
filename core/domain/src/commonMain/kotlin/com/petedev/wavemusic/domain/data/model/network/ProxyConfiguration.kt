package com.petedev.wavemusic.domain.data.model.network

import com.petedev.wavemusic.domain.manager.DataStoreManager

data class ProxyConfiguration(
    val host: String,
    val port: Int,
    val type: DataStoreManager.ProxyType,
    val username: String? = null,
    val password: String? = null,
)