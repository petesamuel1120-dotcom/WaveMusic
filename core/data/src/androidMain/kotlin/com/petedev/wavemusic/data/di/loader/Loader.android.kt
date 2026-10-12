package com.petedev.wavemusic.data.di.loader

import com.petedev.wavemusic.media3.di.loadMediaService
import com.petedev.wavemusic.data.loginsync.LoginSyncSenderRepositoryImpl
import com.petedev.wavemusic.data.loginsync.LoginSyncStore
import com.petedev.wavemusic.domain.repository.LoginSyncSenderRepository
import org.koin.core.context.loadKoinModules
import com.petedev.wavemusic.loginsync.LoginSyncClient
import org.koin.dsl.module

actual fun loadMediaService() {
    loadMediaService()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncSenderRepository> { LoginSyncSenderRepositoryImpl(get(), LoginSyncClient()) }
        },
    )
}
