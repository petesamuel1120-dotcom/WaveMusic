package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petedev.wavemusic.ui.icon.Downloading
import com.petedev.wavemusic.ui.icon.Favorite
import com.petedev.wavemusic.ui.icon.Insights
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.TrendingUp
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.petedev.wavemusic.ui.screen.library.LibraryDynamicPlaylistType
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.downloaded
import com.petedev.wavemusic.resources.favorite
import com.petedev.wavemusic.resources.followed
import com.petedev.wavemusic.resources.most_played

@Composable
fun LibraryTilingBox(navController: NavController) {
    fun open(item: LibraryTilingState) {
        val type =
            when (item) {
                LibraryTilingState.Favorite -> LibraryDynamicPlaylistType.Favorite
                LibraryTilingState.Followed -> LibraryDynamicPlaylistType.Followed
                LibraryTilingState.MostPlayed -> LibraryDynamicPlaylistType.MostPlayed
                LibraryTilingState.Downloaded -> LibraryDynamicPlaylistType.Downloaded
                else -> return
            }
        navController.navigate(LibraryDynamicPlaylistDestination(type = type.toStringParams()))
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppContentHorizontalInset, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(LibraryTilingState.Favorite, LibraryTilingState.Followed).forEach { item ->
                Box(Modifier.weight(1f)) {
                    LibraryTilingItem(item) { open(item) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                LibraryTilingItem(LibraryTilingState.MostPlayed) { open(LibraryTilingState.MostPlayed) }
            }
            Box(Modifier.weight(1f)) {
                LibraryTilingItem(LibraryTilingState.Downloaded) { open(LibraryTilingState.Downloaded) }
            }
        }
    }
}

@Composable
fun LibraryTilingItem(
    state: LibraryTilingState,
    onClick: () -> Unit = {},
) {
    val title = stringResource(state.title)
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.linearGradient(state.iconGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = state.icon,
                contentDescription = null,
                modifier = Modifier.size(23.dp),
                tint = Color.White,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(start = 7.dp),
            style = typo().titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

data class LibraryTilingState(
    val title: StringResource,
    val icon: ImageVector,
    val iconGradient: List<Color>,
) {
    companion object {
        val Favorite =
            LibraryTilingState(
                title = Res.string.favorite,
                icon = WaveIcons.Favorite,
                iconGradient = listOf(Color(0xFFFF5BAE), Color(0xFFC45BEB), Color(0xFF755BFF)),
            )
        val Followed =
            LibraryTilingState(
                title = Res.string.followed,
                icon = WaveIcons.Insights,
                iconGradient = listOf(Color(0xFF765BFF), Color(0xFFB24FE3), Color(0xFFFF5BAE)),
            )
        val MostPlayed =
            LibraryTilingState(
                title = Res.string.most_played,
                icon = WaveIcons.TrendingUp,
                iconGradient = listOf(Color(0xFFC45BEB), Color(0xFF915BFF), Color(0xFF765BFF)),
            )
        val Downloaded =
            LibraryTilingState(
                title = Res.string.downloaded,
                icon = WaveIcons.Downloading,
                iconGradient = listOf(Color(0xFFFF5BAE), Color(0xFFB24FE3), Color(0xFF765BFF)),
            )
    }
}
