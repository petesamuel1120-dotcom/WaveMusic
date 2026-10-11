package com.maxrave.simpmusic.ui.navigation.graph

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.BrowseDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.LocalPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PodcastDestination
import com.maxrave.simpmusic.ui.screen.library.LocalPlaylistScreen
import com.maxrave.simpmusic.ui.screen.other.AlbumScreen
import com.maxrave.simpmusic.ui.screen.other.ArtistScreen
import com.maxrave.simpmusic.ui.screen.other.BrowseScreen
import com.maxrave.simpmusic.ui.screen.other.MoreAlbumsScreen
import com.maxrave.simpmusic.ui.screen.other.PlaylistScreen
import com.maxrave.simpmusic.ui.screen.other.PodcastScreen
import com.maxrave.simpmusic.ui.theme.ForceDarkContent
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun NavGraphBuilder.listScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<AlbumDestination> { entry ->
        val data = entry.toRoute<AlbumDestination>()
        AlbumScreen(
            browseId = data.browseId,
            navController = navController,
        )
    }
    composable<ArtistDestination> { entry ->
        val data = entry.toRoute<ArtistDestination>()
        ArtistScreen(
            channelId = data.channelId,
            navController = navController,
        )
    }
    composable<LocalPlaylistDestination> { entry ->
        val data = entry.toRoute<LocalPlaylistDestination>()
        LocalPlaylistScreen(
            id = data.id,
            navController = navController,
        )
    }
    composable<MoreAlbumsDestination> { entry ->
        val data = entry.toRoute<MoreAlbumsDestination>()
        MoreAlbumsScreen(
            innerPadding = innerPadding,
            navController = navController,
            type = data.type,
            id = data.id,
        )
    }
    composable<BrowseDestination> { entry ->
        val data = entry.toRoute<BrowseDestination>()
        BrowseScreen(
            innerPadding = innerPadding,
            navController = navController,
            browseId = data.browseId,
            params = data.params,
            title = data.title,
        )
    }
    composable<PlaylistDestination> { entry ->
        val data = entry.toRoute<PlaylistDestination>()
        val isYouTubeLikedMusic = data.playlistId.removePrefix("VL") == "LM"
        val isLightThemeUserPlaylist = data.isYourYouTubePlaylist && !LocalAppIsDarkTheme.current
        if (isYouTubeLikedMusic || isLightThemeUserPlaylist) {
            // Liked Music uses the regular page surface. User-owned playlists in light mode also
            // need the regular theme so empty covers and suggested-song text stay legible.
            PlaylistScreen(
                playlistId = data.playlistId,
                isYourYouTubePlaylist = data.isYourYouTubePlaylist,
                navController = navController,
            )
        } else {
            ForceDarkContent {
                PlaylistScreen(
                    playlistId = data.playlistId,
                    isYourYouTubePlaylist = data.isYourYouTubePlaylist,
                    navController = navController,
                )
            }
        }
    }
    composable<PodcastDestination> { entry ->
        val data = entry.toRoute<PodcastDestination>()
        PodcastScreen(
            podcastId = data.podcastId,
            navController = navController,
        )
    }
}
