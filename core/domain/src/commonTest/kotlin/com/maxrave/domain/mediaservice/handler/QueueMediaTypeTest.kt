package com.maxrave.domain.mediaservice.handler

import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.searchResult.songs.Thumbnail
import com.maxrave.domain.extension.isVideo
import com.maxrave.domain.extension.toGenericMediaItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueueMediaTypeTest {
    private val song = track("song", "MUSIC_VIDEO_TYPE_ATV")
    private val video = track("video", "MUSIC_VIDEO_TYPE_OMV")
    private val anotherSong = song.copy(videoId = "another-song")
    private val anotherVideo = video.copy(videoId = "another-video")

    @Test
    fun automaticQueuesFollowTheirSeedAndKeepItsIndex() {
        val mixed = listOf(song, video, anotherSong, anotherVideo)
        val videos = radio(mixed, video).homogeneous()
        assertEquals(listOf(video, anotherVideo), videos.listTracks)
        assertEquals(0, videos.listTracks.indexOf(videos.firstPlayedTrack))
        assertEquals(QueueMediaType.VIDEO, videos.mediaType)
        assertEquals(listOf(song, anotherSong), radio(mixed, song).homogeneous().listTracks)
    }

    @Test
    fun favoritesAndOtherLibraryListsKeepBothKindsWhenInternallyTaggedRadio() {
        val mixed = listOf(song, video, anotherSong, anotherVideo)
        val favorites = radio(mixed, video).copy(playlistId = null, mediaType = null)
        // Construct a fresh data object as the favorites Play, Play All and Shuffle paths do.
        val queue = QueueData.Data(
            listTracks = mixed,
            firstPlayedTrack = video,
            playlistType = PlaylistType.RADIO,
        ).homogeneous()
        assertNull(queue.mediaType)
        assertEquals(favorites.listTracks, queue.listTracks)
        assertEquals(1, queue.listTracks.indexOf(queue.firstPlayedTrack))
        assertEquals(mixed.reversed(), queue.copy(listTracks = mixed.reversed()).homogeneous().listTracks)
    }

    @Test
    fun youtubeLikedMusicAndUserPlaylistsKeepMixedContentsAndPages() {
        for (type in listOf(PlaylistType.PLAYLIST, PlaylistType.LOCAL_PLAYLIST, PlaylistType.ALBUM)) {
            val data = QueueData.Data(
                listTracks = listOf(song, video),
                firstPlayedTrack = video,
                playlistId = "LM",
                playlistType = type,
            ).homogeneous()
            assertNull(data.mediaType)
            assertEquals(listOf(song, video), data.listTracks)
            val appended = QueueData(data = data).addTrackList(listOf(anotherVideo, anotherSong))
            assertEquals(listOf(song, video, anotherVideo, anotherSong), appended.data.listTracks)
        }
    }

    @Test
    fun remotePlaylistShuffleAndCuratedMixesKeepAllItems() {
        for (id in listOf("LM", "PLplaylist", "RDCLAKcurated", "VLRDCLAKcurated")) {
            val queue = QueueData.Data(
                listTracks = listOf(video, song),
                firstPlayedTrack = video,
                playlistId = id,
                playlistType = PlaylistType.RADIO,
            ).homogeneous()
            assertNull(queue.mediaType)
            assertEquals(listOf(video, song), queue.listTracks)
        }
    }

    @Test
    fun manualAddAndPlayNextAllowOppositeKindWithoutChangingAutoplay() {
        val queue = QueueData(data = radio(listOf(song), song))
            .addTrackList(listOf(video), isAddToQueue = true)
            .addToIndex(anotherVideo, 1)
            .addTrackList(listOf(video.copy(videoId = "automatic-video"), anotherSong))
        assertEquals(listOf(song, anotherVideo, video, anotherSong), queue.data.listTracks)
        assertEquals(QueueMediaType.SONG, queue.data.mediaType)
    }

    @Test
    fun radioTypeSurvivesTemporarilyEmptyCatalogAndHistoryRemoval() {
        val data = radio(listOf(video, anotherVideo), video)
        assertEquals(QueueMediaType.VIDEO, data.copy(listTracks = emptyList()).mediaType)
        val trimmed = QueueData(data = data).removeFirstTrackForPlaylistAndAlbum()
        assertNull(trimmed.data.firstPlayedTrack)
        assertEquals(listOf(anotherVideo), trimmed.data.listTracks)
        assertEquals(QueueMediaType.VIDEO, trimmed.data.mediaType)
        assertEquals(listOf(anotherVideo), trimmed.data.matchingTracks(listOf(song, anotherVideo)))
    }

    @Test
    fun matchingCounterpartsAreUsedWithoutOppositeKindFallback() {
        data class Item(val id: String, val type: QueueMediaType, val counterpart: Item? = null)
        val audio = Item("audio", QueueMediaType.SONG)
        val clip = Item("clip", QueueMediaType.VIDEO, audio)
        val audioWithClip = audio.copy(counterpart = clip)
        val audioOnly = audio.copy(id = "audio-only")
        val videoOnly = Item("video-only", QueueMediaType.VIDEO)
        val items = listOf(audioWithClip, videoOnly, audioOnly)
        assertEquals(
            listOf(clip, videoOnly),
            selectQueueMedia(items, QueueMediaType.VIDEO, { it.type }, { it.counterpart }),
        )
        assertEquals(
            listOf(audio, audioOnly),
            selectQueueMedia(listOf(clip, audioOnly), QueueMediaType.SONG, { it.type }, { it.counterpart }),
        )
        assertTrue(selectQueueMedia(listOf(videoOnly), QueueMediaType.SONG, { it.type }, { it.counterpart }).isEmpty())
        assertTrue(selectQueueMedia(listOf(audioOnly), QueueMediaType.VIDEO, { it.type }, { it.counterpart }).isEmpty())
    }

    @Test
    fun metadataWinsOverCoverShapeAndPlayerUsesSameClassification() {
        assertEquals(QueueMediaType.VIDEO, QueueMediaType.fromTrack(video)) // square cover
        val rectangularSong = song.copy(thumbnails = listOf(Thumbnail(90, "cover", 160)))
        assertEquals(QueueMediaType.SONG, QueueMediaType.fromTrack(rectangularSong))
        val forcedVideo = song.copy(resultType = "Video")
        assertEquals(QueueMediaType.VIDEO, QueueMediaType.fromTrack(forcedVideo))
        assertTrue(forcedVideo.toGenericMediaItem().isVideo())
        assertEquals(false, rectangularSong.toGenericMediaItem().isVideo())
    }

    @Test
    fun missingThumbnailsDoNotCrashAndFallbackRecognizesVideoArtwork() {
        val unknown = song.copy(videoType = null)
        assertEquals(QueueMediaType.SONG, QueueMediaType.fromTrack(unknown))
        assertEquals(QueueMediaType.VIDEO, QueueMediaType.fromTrack(unknown.copy(thumbnails = emptyList())))
        assertEquals(QueueMediaType.VIDEO, QueueMediaType.fromTrack(unknown.copy(thumbnails = null)))
        assertEquals(
            QueueMediaType.VIDEO,
            QueueMediaType.fromTrack(unknown.copy(thumbnails = listOf(Thumbnail(100, "hq720.jpg", 100)))),
        )
        assertTrue(unknown.copy(thumbnails = emptyList()).toGenericMediaItem().isVideo())
    }

    private fun radio(tracks: List<Track>, seed: Track) = QueueData.Data(
        listTracks = tracks,
        firstPlayedTrack = seed,
        playlistId = "RDAMVM${seed.videoId}",
        playlistType = PlaylistType.RADIO,
    )

    private fun track(id: String, type: String) = Track(
        album = null,
        artists = emptyList(),
        duration = "3:00",
        durationSeconds = 180,
        isAvailable = true,
        isExplicit = false,
        likeStatus = null,
        thumbnails = listOf(Thumbnail(100, "cover", 100)),
        title = id,
        videoId = id,
        videoType = type,
        category = null,
        feedbackTokens = null,
        resultType = null,
    )
}
