package com.maxrave.simpmusic.ui.screen.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo

@Composable
internal fun SearchEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = typo()
    BoxWithConstraints(modifier.padding(horizontal = 24.dp)) {
        val compact = maxHeight < 320.dp
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(0f, if (compact) 0f else -0.65f),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 56.dp else 80.dp)
                        .background(colors.onSurface.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SimpIcons.Search,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(if (compact) 28.dp else 36.dp),
                    )
                }
                Spacer(Modifier.height(if (compact) 12.dp else 20.dp))
                Text(
                    text = title,
                    style = typography.titleMedium.copy(fontSize = 22.sp, lineHeight = 28.sp),
                    color = colors.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = typography.bodyLarge.copy(
                        fontSize = if (compact) 16.sp else 18.sp,
                        lineHeight = if (compact) 22.sp else 26.sp,
                    ),
                    color = colors.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
