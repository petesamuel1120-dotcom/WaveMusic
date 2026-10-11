package com.maxrave.data.di

import com.maxrave.common.Config.SERVICE_SCOPE
import com.maxrave.data.io.fileDir
import com.maxrave.data.repository.AccountRepositoryImpl
import com.maxrave.data.repository.AlbumRepositoryImpl
import com.maxrave.data.repository.AnalyticsRepositoryImpl
import com.maxrave.data.repository.ArtistRepositoryImpl
import com.maxrave.data.repository.AutoEqRepositoryImpl
import com.maxrave.data.lyrics.LyricsRomanizerRepositoryImpl
import com.maxrave.data.repository.CommonRepositoryImpl
import com.maxrave.data.repository.HomeRepositoryImpl
import com.maxrave.data.repository.ImportRepositoryImpl
import com.maxrave.data.repository.LocalPlaylistRepositoryImpl
import com.maxrave.data.repository.LyricsCanvasRepositoryImpl
import com.maxrave.data.repository.PlaylistRepositoryImpl
import com.maxrave.data.repository.PodcastRepositoryImpl
import com.maxrave.data.repository.SearchRepositoryImpl
import com.maxrave.data.repository.SongRepositoryImpl
import com.maxrave.data.repository.StreamRepositoryImpl
import com.maxrave.data.repository.UpdateRepositoryImpl
import com.maxrave.domain.repository.AccountRepository
import com.maxrave.domain.repository.AlbumRepository
import com.maxrave.domain.repository.AnalyticsRepository
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.repository.AutoEqRepository
import com.maxrave.domain.repository.LyricsRomanizerRepository
import com.maxrave.domain.repository.CommonRepository
import com.maxrave.domain.repository.HomeRepository
import com.maxrave.domain.repository.ImportRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.LyricsCanvasRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.PodcastRepository
import com.maxrave.domain.repository.SearchRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.repository.StreamRepository
import com.maxrave.domain.repository.UpdateRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule =
    module {
        single<AccountRepository> {
            AccountRepositoryImpl(get(), get())
        }

        single<AlbumRepository> {
            AlbumRepositoryImpl(get(), get())
        }

        single<ArtistRepository> {
            ArtistRepositoryImpl(get(), get(), get())
        }

        single<CommonRepository>(createdAtStart = true) {
            CommonRepositoryImpl(get(named(SERVICE_SCOPE)), get(), get(), get(), get(), get()).apply {
                this.init("${fileDir()}/ytdlp-cookie.txt", get())
            }
        }

        // Lazy for the same reason its client is: the picker is the only thing that wants it.
        single<AutoEqRepository> {
            AutoEqRepositoryImpl(get(), get())
        }

        // Lazy: constructing it costs a few File.length() calls, but the kuromoji dictionary
        // behind it is loaded on first Japanese line and never before — so this must NOT be
        // createdAtStart, or every launch pays for a feature most listeners leave off. The path
        // is where Android keeps the downloaded ipadic pack (the APK no longer bundles it);
        // Desktop and iOS ignore it.
        single<LyricsRomanizerRepository> {
            LyricsRomanizerRepositoryImpl("${fileDir()}/kuromoji-ipadic")
        }

        single<HomeRepository> {
            HomeRepositoryImpl(get(), get())
        }

        single<ImportRepository> {
            ImportRepositoryImpl(get())
        }

        single<LocalPlaylistRepository> {
            LocalPlaylistRepositoryImpl(get(), get())
        }

        single<LyricsCanvasRepository> {
            LyricsCanvasRepositoryImpl(get(), get(), get(), get(), get(), get())
        }

        single<PlaylistRepository> {
            PlaylistRepositoryImpl(get(), get(), get())
        }

        single<PodcastRepository> {
            PodcastRepositoryImpl(get(), get())
        }

        single<SearchRepository> {
            SearchRepositoryImpl(get(), get())
        }

        single<SongRepository> {
            SongRepositoryImpl(get(), get(), get())
        }

        single<StreamRepository> {
            StreamRepositoryImpl(get(), get())
        }

        single<UpdateRepository> {
            UpdateRepositoryImpl(get())
        }

        single<AnalyticsRepository> {
            AnalyticsRepositoryImpl(get())
        }
    }
