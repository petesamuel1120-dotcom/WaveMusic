package com.petedev.wavemusic.data.di

import DatabaseDao
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.petedev.wavemusic.data.dataStore.DataStoreManagerImpl
import com.petedev.wavemusic.data.dataStore.createDataStoreInstance
import com.petedev.wavemusic.data.db.Converters
import com.petedev.wavemusic.data.db.MusicDatabase
import com.petedev.wavemusic.data.db.datasource.AnalyticsDatasource
import com.petedev.wavemusic.data.db.datasource.LocalDataSource
import com.petedev.wavemusic.data.db.getDatabaseBuilder
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.kotlinytmusicscraper.YouTube
import com.petedev.wavemusic.spotify.Spotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import com.petedev.wavemusic.aiservice.AiClient
import org.simpmusic.lyrics.SimpMusicLyricsClient
import kotlin.time.ExperimentalTime
import com.petedev.wavemusic.autoeq.AutoEq

@OptIn(ExperimentalTime::class)
val databaseModule =
    module {
        single(createdAtStart = true) {
            Converters()
        }
        // Database
        single(createdAtStart = true) {
            getDatabaseBuilder(
                get<Converters>()
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
        // DatabaseDao
        single(createdAtStart = true) {
            get<MusicDatabase>().getDatabaseDao()
        }
        // LocalDataSource
        single(createdAtStart = true) {
            LocalDataSource(get<DatabaseDao>(), get<MusicDatabase>())
        }
        // AnalyticsDatasource
        single(createdAtStart = true) {
            AnalyticsDatasource(get<DatabaseDao>())
        }
        // Datastore
        single(createdAtStart = true) {
            createDataStoreInstance()
        }
        // DatastoreManager
        single<DataStoreManager>(createdAtStart = true) {
            DataStoreManagerImpl(get<DataStore<Preferences>>())
        }

        // Move YouTube from Singleton to Koin DI
        single(createdAtStart = true) {
            YouTube()
        }

        single {
            Spotify()
        }

        single {
            AiClient()
        }

        single {
            SimpMusicLyricsClient()
        }

        // Not created at start, unlike the rest: nothing needs it until someone opens the AutoEq
        // picker, and it holds an HTTP client the vast majority of sessions never use.
        single {
            AutoEq()
        }
    }
