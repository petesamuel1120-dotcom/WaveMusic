package com.petedev.wavemusic.data.di.loader

import com.petedev.wavemusic.common.AppIdentity
import com.petedev.wavemusic.data.di.databaseModule
import com.petedev.wavemusic.data.di.listenTogetherModule
import com.petedev.wavemusic.data.di.mediaHandlerModule
import com.petedev.wavemusic.data.di.repositoryModule
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun loadAllModules(appIdentity: AppIdentity) {
    loadKoinModules(
        listOf(
            module { single { appIdentity } },
            databaseModule,
            repositoryModule,
        ),
    )
    loadKoinModules(mediaHandlerModule)
    // NOTE: `createdAtStart` is NOT enough for a module loaded this way — nothing constructs it
    // unless something injects it, and the bridge exists purely to listen, so nobody would.
    // ListenTogetherViewModel injects and starts it; start() is idempotent.
    loadKoinModules(listenTogetherModule)
    loadMediaService()
    loadLoginSyncModule()
}

expect fun loadMediaService()

/** Login sync is one-sided per platform: Desktop hosts, Android sends, iOS takes no part. */
expect fun loadLoginSyncModule()