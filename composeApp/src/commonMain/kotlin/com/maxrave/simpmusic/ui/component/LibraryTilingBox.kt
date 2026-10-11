package com.maxrave.simpmusic.ui.component

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
import com.maxrave.simpmusic.ui.icon.Downloading
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Insights
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.TrendingUp
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.favorite
import simpmusic.composeapp.generated.resources.followed
import simpmusic.composeapp.generated.resources.most_played

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
                icon = SimpIcons.Favorite,
                iconGradient = listOf(Color(0xFFFF5BAE), Color(0xFFC45BEB), Color(0xFF755BFF)),
            )
        val Followed =
            LibraryTilingState(
                title = Res.string.followed,
                icon = SimpIcons.Insights,
                iconGradient = listOf(Color(0xFF765BFF), Color(0xFFB24FE3), Color(0xFFFF5BAE)),
            )
        val MostPlayed =
            LibraryTilingState(
                title = Res.string.most_played,
                icon = SimpIcons.TrendingUp,
                iconGradient = listOf(Color(0xFFC45BEB), Color(0xFF915BFF), Color(0xFF765BFF)),
            )
        val Downloaded =
            LibraryTilingState(
                title = Res.string.downloaded,
                icon = SimpIcons.Downloading,
                iconGradient = listOf(Color(0xFFFF5BAE), Color(0xFFB24FE3), Color(0xFF765BFF)),
            )
    }
}
