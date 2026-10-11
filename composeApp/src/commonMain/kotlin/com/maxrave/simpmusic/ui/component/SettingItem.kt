package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo

@Composable
@Suppress("UNUSED_PARAMETER")
fun SettingItem(
    title: String = "Title",
    subtitle: String = "Subtitle",
    smallSubtitle: Boolean = false,
    isEnable: Boolean = true,
    onClick: (() -> Unit)? = null,
    switch: Pair<Boolean, ((Boolean) -> Unit)>? = null,
    otherView: @Composable (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
) {
    // isEnable only greys the row out; it deliberately writes nothing.
    //
    // There used to be an onDisable callback fired from a LaunchedEffect(isEnable) here, to clear a
    // login-gated child flag when its gate went false (#2157, #2064). It was wrong twice over. The
    // gate is a StateFlow filled asynchronously from DataStore, so the FIRST composition always sees
    // its "not loaded yet" value and read it as "signed out" — sync_follow_to_youtube was erased on
    // every visit to Settings. And it only ever ran if the user opened Settings and scrolled to the
    // row at all, so it could not do the job it existed for. Clearing a child flag now belongs to the
    // logout that invalidates it (SettingsViewModel: setSpotifyLogIn, logOutDiscord, logOutLastfm,
    // setUsedAccount, logOutAllYouTube, setAIApiKey), which is a real event and cannot misfire.
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().alpha(if (isEnable) 1f else 0.5f)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .then(
                        when {
                            switch != null -> Modifier.toggleable(
                                value = switch.first,
                                enabled = isEnable,
                                role = Role.Switch,
                                onValueChange = switch.second,
                            )
                            onClick != null -> Modifier.dialogMotionClickable(enabled = isEnable, onClick = onClick)
                            else -> Modifier
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leadingIcon != null) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                        .background(colors.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(leadingIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = typo().labelMedium,
                    color = colors.onSurface,
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    // Descriptions wrap freely, including at larger system font sizes.
                    Text(text = subtitle, style = typo().bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            if (switch != null) {
                Switch(
                    modifier = Modifier.wrapContentWidth(),
                    checked = switch.first,
                    onCheckedChange = null,
                    enabled = isEnable,
                )
            } else if (onClick != null) {
                Icon(
                    SimpIcons.ArrowForwardIos,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        otherView?.let {
            Box(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.04f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                it()
            }
        }
    }
}
