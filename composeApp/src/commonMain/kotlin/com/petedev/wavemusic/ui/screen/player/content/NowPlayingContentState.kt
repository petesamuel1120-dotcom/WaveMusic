package com.petedev.wavemusic.ui.screen.player.content

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.streams.TimeLine
import com.petedev.wavemusic.domain.data.player.GenericCastState
import com.petedev.wavemusic.domain.mediaservice.handler.ControlState
import com.petedev.wavemusic.extension.GradientOffset
import com.petedev.wavemusic.viewModel.LyricsProvider
import com.petedev.wavemusic.viewModel.NowPlayingScreenData
import com.petedev.wavemusic.viewModel.UIEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * Whether the lyrics currently on screen can be rated.
 *
 * SimpMusic Lyrics is the only provider with a vote endpoint, and the vote is cast against the
 * Wave Music record itself — so the provider tag alone is NOT the condition: `simpMusicLyrics`
 * must actually be there. Either half qualifying is enough, because the dialog rates whichever
 * of the two came from Wave Music.
 *
 * Lives on the contract the three styles share. The Apple Music style shipped its floating vote
 * button ungated — offering a rating on YouTube, LRCLIB and Spotify lyrics alike — precisely
 * because this rule existed only as an expression copy-pasted inside the other two styles, where
 * a new style had no reason to go looking for it.
 */
internal fun NowPlayingScreenData.LyricsData?.canVote(): Boolean {
    val data = this ?: return false
    val votableLyrics =
        data.lyricsProvider == LyricsProvider.SIMPMUSIC && data.lyrics.simpMusicLyrics != null
    val votableTranslation =
        data.translatedLyrics?.second == LyricsProvider.SIMPMUSIC &&
            data.translatedLyrics?.first?.simpMusicLyrics != null
    return votableLyrics || votableTranslation
}

// Backdrop behind the player. A dark surface rather than pure black: #000000 reads as a hole
// next to the artwork-tinted gradient and cards, which is why Spotify sits its player on a
// near-black surface instead. Used for the gradient's end colour, the fade-to target and the
// area below the gradient so all three match exactly and leave no seam.
internal val PlayerBackdropColor = Color(0xFF121212)

private val RICH_SYNC_TIMESTAMP_REGEX = Regex("""<\d{2}:\d{2}\.\d{2,3}>""")
private val WHITESPACE_REGEX = Regex("""\s+""")

// Timing can split syllables inside a word. Remove only the markers and preserve source spaces.
// Shared by every Now Playing content style (Spotify + M3 Expressive).
internal fun String.stripRichSyncTimestamps(): String =
    replace(RICH_SYNC_TIMESTAMP_REGEX, "")
        .replace(WHITESPACE_REGEX, " ")
        .trim()

/**
 * Everything a Now Playing content layer reads. The shell ([com.petedev.wavemusic.ui.screen.player.NowPlayingScreenContent])
 * owns the ViewModel collection, palette animation, sheets/dialogs and gesture state machines;
 * a content composable only renders from this snapshot.
 */
@Stable
class NowPlayingContentState(
    val screenData: NowPlayingScreenData,
    val controllerState: ControlState,
    val timelineState: TimeLine,
    val timelineFlow: StateFlow<TimeLine>,
    val castState: GenericCastState,
    val shouldShowVideo: Boolean,
    val artworkQueue: List<Track>,
    val currentOrderIndex: Int,
    val currentTrackId: String? = null,
    val artworkPagerState: PagerState,
    val startColor: Animatable<Color, AnimationVector4D>,
    val endColor: Animatable<Color, AnimationVector4D>,
    val spotShadowColor: Color,
    val gradientOffset: GradientOffset,
    val sliderTrackColor: Color,
    val sliderValue: Float,
    val currentLyricLineIndex: Int,
    val showControlLayout: Boolean,
    val controlLayoutAlpha: Float,
    val showHideMiddleLayout: Boolean,
    val shouldShowToolbar: Boolean,
    val isInPipMode: Boolean,
    val mainScrollState: ScrollState,
    val isExpanded: Boolean,
    val dismissIcon: ImageVector,
    /**
     * Width / height of the video now playing, 16:9 until the player knows it. Every style sizes
     * its video frame from this one value, so a frame and the spacer that measures it cannot drift.
     */
    val videoAspectRatio: Float = 16f / 9,
    val translationLoading: Boolean = false,
    val translationError: String? = null,
    /** Hide translation controls when the source lyrics are already mostly in the target language. */
    val canTranslateLyrics: Boolean = false,
    val showLyricsTranslationButton: Boolean = true,
    /** Saved explicit choice for the current song; translated data alone does not enable it. */
    val translationEnabled: Boolean = false,
    val artistLinks: List<Pair<String, String>> = emptyList(),
    /** True while the user is scrubbing; loading UI can avoid fighting the thumb. */
    val isSliding: Boolean = false,
    /** Podcast episodes keep their playback/queue controls but cannot open lyrics. */
    val lyricsEnabled: Boolean = true,
)

/** Media type comes from the active player item; artwork metadata can arrive later. */
internal fun NowPlayingContentState.isCurrentVideoItem(): Boolean =
    shouldShowVideo && !currentTrackId.isNullOrBlank()

/**
 * Everything a Now Playing content layer can do. All callbacks land in the shell, which owns
 * the ViewModel, the navController and the sheet/dialog visibility flags.
 */
@Stable
class NowPlayingContentActions(
    val onUIEvent: (UIEvent) -> Unit,
    val onSeekToQueueIndex: (Int) -> Unit,
    val onArtworkBitmap: (ImageBitmap) -> Unit,
    val onSliderChange: (Float) -> Unit,
    val onSliderChangeFinished: () -> Unit,
    val onToggleControls: () -> Unit,
    val onNavigateToArtist: () -> Unit,
    val onNavigateToArtistId: (String) -> Unit = {},
    val onShowMoreSheet: () -> Unit,
    val onShowQueue: () -> Unit,
    val onShowInfo: () -> Unit,
    val onShowAddToPlaylist: () -> Unit,
    val onToggleLyricsControls: () -> Unit,
    val onTranslateLyrics: () -> Unit,
    val onTranslationEnabledChanged: (Boolean) -> Unit,
    val onShowVoteDialog: () -> Unit,
    val onEnterFullscreenVideo: () -> Unit,
    val onDismiss: () -> Unit,
    val onToolbarVisibilityChange: (Boolean) -> Unit,
    /** Reorders the queue. `from`/`to` are absolute indices into [NowPlayingContentState.artworkQueue]. */
    val onMoveQueueItem: (from: Int, to: Int) -> Unit,
    /** Removes one queue entry. `index` is an absolute index into [NowPlayingContentState.artworkQueue]. */
    val onRemoveQueueItem: (index: Int) -> Unit,
)
