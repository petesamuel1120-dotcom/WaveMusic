package com.petedev.wavemusic.data.di

import com.petedev.wavemusic.common.Config.SERVICE_SCOPE
import com.petedev.wavemusic.data.io.fileDir
import com.petedev.wavemusic.data.repository.AccountRepositoryImpl
import com.petedev.wavemusic.data.repository.AlbumRepositoryImpl
import com.petedev.wavemusic.data.repository.AnalyticsRepositoryImpl
import com.petedev.wavemusic.data.repository.ArtistRepositoryImpl
import com.petedev.wavemusic.data.repository.AutoEqRepositoryImpl
import com.petedev.wavemusic.data.lyrics.LyricsRomanizerRepositoryImpl
import com.petedev.wavemusic.data.repository.CommonRepositoryImpl
import com.petedev.wavemusic.data.repository.HomeRepositoryImpl
import com.petedev.wavemusic.data.repository.ImportRepositoryImpl
import com.petedev.wavemusic.data.repository.LocalPlaylistRepositoryImpl
import com.petedev.wavemusic.data.repository.LyricsCanvasRepositoryImpl
import com.petedev.wavemusic.data.repository.PlaylistRepositoryImpl
import com.petedev.wavemusic.data.repository.PodcastRepositoryImpl
import com.petedev.wavemusic.data.repository.SearchRepositoryImpl
import com.petedev.wavemusic.data.repository.SongRepositoryImpl
import com.petedev.wavemusic.data.repository.StreamRepositoryImpl
import com.petedev.wavemusic.data.repository.UpdateRepositoryImpl
import com.petedev.wavemusic.domain.repository.AccountRepository
import com.petedev.wavemusic.domain.repository.AlbumRepository
import com.petedev.wavemusic.domain.repository.AnalyticsRepository
import com.petedev.wavemusic.domain.repository.ArtistRepository
import com.petedev.wavemusic.domain.repository.AutoEqRepository
import com.petedev.wavemusic.domain.repository.LyricsRomanizerRepository
import com.petedev.wavemusic.domain.repository.CommonRepository
import com.petedev.wavemusic.domain.repository.HomeRepository
import com.petedev.wavemusic.domain.repository.ImportRepository
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.repository.LyricsCanvasRepository
import com.petedev.wavemusic.domain.repository.PlaylistRepository
import com.petedev.wavemusic.domain.repository.PodcastRepository
import com.petedev.wavemusic.domain.repository.SearchRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.repository.StreamRepository
import com.petedev.wavemusic.domain.repository.UpdateRepository
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
