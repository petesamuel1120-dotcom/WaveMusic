package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.resources.vote_count
import com.petedev.wavemusic.resources.vote_id

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.ThumbDown
import com.petedev.wavemusic.ui.icon.ThumbUp
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.VoteData
import com.petedev.wavemusic.viewModel.VoteState
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.cancel
import com.petedev.wavemusic.resources.downvote
import com.petedev.wavemusic.resources.rate_lyrics
import com.petedev.wavemusic.resources.rate_translated_lyrics
import com.petedev.wavemusic.resources.upvote
import com.petedev.wavemusic.resources.vote_for_lyrics

@Composable
fun VoteLyricsDialog(
    canVoteLyrics: Boolean,
    canVoteTranslatedLyrics: Boolean,
    lyricsVoteState: VoteData?,
    translatedLyricsVoteState: VoteData?,
    onVoteLyrics: (upvote: Boolean) -> Unit,
    onVoteTranslatedLyrics: (upvote: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AmoledAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.cancel),
                    style = typo().bodySmall,
                )
            }
        },
        title = {
            Text(
                stringResource(Res.string.vote_for_lyrics),
                style = typo().labelSmall,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Vote for original lyrics
                if (canVoteLyrics && lyricsVoteState != null) {
                    VoteRow(
                        label = stringResource(Res.string.rate_lyrics),
                        voteState = lyricsVoteState,
                        onUpvote = { onVoteLyrics(true) },
                        onDownvote = { onVoteLyrics(false) },
                    )
                }

                // Vote for translated lyrics
                if (canVoteTranslatedLyrics && translatedLyricsVoteState != null) {
                    VoteRow(
                        label = stringResource(Res.string.rate_translated_lyrics),
                        voteState = translatedLyricsVoteState,
                        onUpvote = { onVoteTranslatedLyrics(true) },
                        onDownvote = { onVoteTranslatedLyrics(false) },
                    )
                }
            }
        },
    )
}

@Composable
private fun VoteRow(
    label: String,
    voteState: VoteData,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = label,
                style = typo().bodySmall,
            )
            Text(
                text = stringResource(Res.string.vote_id, voteState.id.toString()),
                style = typo().bodySmall,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.vote_count, voteState.vote.toString()),
                style = typo().bodySmall,
            )
            when (voteState.state) {
                is VoteState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }

                is VoteState.Success -> {
                    Icon(
                        imageVector = if (voteState.state.upvote) WaveIcons.ThumbUp else WaveIcons.ThumbDown,
                        contentDescription = null,
                        tint = Color.Cyan,
                        modifier = Modifier.size(24.dp),
                    )
                }

                is VoteState.Error -> {
                    Text(
                        text = voteState.state.message,
                        style = typo().bodySmall,
                        color = Color.Red,
                    )
                }

                is VoteState.Idle -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = onUpvote,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = WaveIcons.ThumbUp,
                                contentDescription = stringResource(Res.string.upvote),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = onDownvote,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = WaveIcons.ThumbDown,
                                contentDescription = stringResource(Res.string.downvote),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
