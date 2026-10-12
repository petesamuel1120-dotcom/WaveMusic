package com.petedev.wavemusic.data.di.loader

import com.petedev.wavemusic.media_jvm.di.loadDesktopPlayerModule
import com.petedev.wavemusic.data.loginsync.LoginSyncHostRepositoryImpl
import com.petedev.wavemusic.data.loginsync.LoginSyncStore
import com.petedev.wavemusic.domain.repository.LoginSyncHostRepository
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

actual fun loadMediaService() {
    loadDesktopPlayerModule()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncHostRepository> { LoginSyncHostRepositoryImpl(get()) }
        },
    )
}
