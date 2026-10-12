package com.petedev.wavemusic.di

import com.petedev.wavemusic.viewModel.AlbumViewModel
import com.petedev.wavemusic.utils.VersionManager
import com.petedev.wavemusic.viewModel.AnalyticsViewModel
import com.petedev.wavemusic.viewModel.ListenTogetherSettingsViewModel
import com.petedev.wavemusic.viewModel.ListenTogetherViewModel
import com.petedev.wavemusic.viewModel.ArtistViewModel
import com.petedev.wavemusic.viewModel.BrowseViewModel
import com.petedev.wavemusic.viewModel.HomeViewModel
import com.petedev.wavemusic.viewModel.ImportViewModel
import com.petedev.wavemusic.viewModel.LibraryDynamicPlaylistViewModel
import com.petedev.wavemusic.viewModel.LibraryViewModel
import com.petedev.wavemusic.viewModel.LocalPlaylistViewModel
import com.petedev.wavemusic.viewModel.LogInViewModel
import com.petedev.wavemusic.viewModel.LoginSyncHostViewModel
import com.petedev.wavemusic.viewModel.LoginSyncSenderViewModel
import com.petedev.wavemusic.viewModel.MoodViewModel
import com.petedev.wavemusic.viewModel.MoreAlbumsViewModel
import com.petedev.wavemusic.viewModel.NotificationViewModel
import com.petedev.wavemusic.viewModel.NowPlayingBottomSheetViewModel
import com.petedev.wavemusic.viewModel.PlaylistViewModel
import com.petedev.wavemusic.viewModel.PodcastViewModel
import com.petedev.wavemusic.viewModel.RecentlySongsViewModel
import com.petedev.wavemusic.viewModel.SearchViewModel
import com.petedev.wavemusic.viewModel.AutoEqViewModel
import com.petedev.wavemusic.viewModel.SettingsViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.SongSelectionViewModel
import com.petedev.wavemusic.viewModel.WrappedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SongSelectionViewModel(
                get(),
                get(),
            )
        }
        // Only one of these resolves on a given platform: Desktop binds the host repository, Android the
        // sender. Koin resolves dependencies when a ViewModel is created, not when it is declared.
        viewModel { LoginSyncHostViewModel(get()) }
        viewModel { LoginSyncSenderViewModel(get()) }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ImportViewModel(
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AutoEqViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            BrowseViewModel(
                get(),
            )
        }
        viewModel {
            RecentlySongsViewModel(
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            WrappedViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ListenTogetherSettingsViewModel(get())
        }
        viewModel {
            ListenTogetherViewModel(
                repository = get(),
                dataStore = get(),
                bridge = get(),
            )
        }

    }
