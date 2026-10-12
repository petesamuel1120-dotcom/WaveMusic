package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.extension.shimmer
import com.petedev.wavemusic.ui.theme.LocalAppColors

private val LyricsShimmerLineWidths = listOf(0.88f, 0.64f, 0.78f, 0.53f, 0.83f)

@Composable
fun HomeItemShimmer(shimmerProgress: State<Float>) {
    Column {
        Box(
            Modifier
                .width(150.dp)
                .height(36.dp)
                .padding(vertical = 8.dp)
                .background(
                    color = LocalAppColors.current.shimmerBackground,
                ).clip(RoundedCornerShape(10))
                .shimmer(shimmerProgress),
        )
        LazyRow(userScrollEnabled = false) {
            items(10) {
                PlaylistShimmer(shimmerProgress)
            }
        }
    }
}

@Composable
fun PlaylistShimmer(shimmerProgress: State<Float>) {
    Column(
        Modifier
            .height(270.dp)
            .padding(10.dp),
    ) {
        Box(
            Modifier
                .size(160.dp)
                .clip(
                    RoundedCornerShape(10),
                ).background(
                    color = LocalAppColors.current.shimmerBackground,
                ).shimmer(shimmerProgress),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Box(
            Modifier
                .width(130.dp)
                .height(18.dp)
                .clip(
                    RoundedCornerShape(10),
                ).background(
                    color = LocalAppColors.current.shimmerBackground,
                ).shimmer(shimmerProgress),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Box(
            Modifier
                .width(130.dp)
                .height(18.dp)
                .clip(
                    RoundedCornerShape(10),
                ).background(
                    color = LocalAppColors.current.shimmerBackground,
                ).shimmer(shimmerProgress),
        )
    }
}

@Composable
fun QuickPicksShimmerItem(shimmerProgress: State<Float>) {
    Row(
        Modifier
            .height(70.dp)
            .padding(10.dp),
    ) {
        Box(
            Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10))
                .background(LocalAppColors.current.shimmerBackground)
                .shimmer(shimmerProgress),
        )
        Column(
            Modifier
                .padding(start = 10.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .align(Alignment.CenterVertically),
        ) {
            Box(
                Modifier
                    .width(300.dp)
                    .height(21.dp)
                    .clip(RoundedCornerShape(10))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer(shimmerProgress),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                Modifier
                    .width(260.dp)
                    .height(21.dp)
                    .clip(RoundedCornerShape(10))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer(shimmerProgress),
            )
        }
    }
}

@Composable
fun QuickPicksShimmer(shimmerProgress: State<Float>) {
    Column {
        Box(
            Modifier
                .width(150.dp)
                .height(36.dp)
                .padding(vertical = 8.dp)
                .background(
                    color = LocalAppColors.current.shimmerBackground,
                ).clip(RoundedCornerShape(10))
                .shimmer(shimmerProgress),
        )
        repeat(4) {
            QuickPicksShimmerItem(shimmerProgress)
        }
    }
}

@Composable
fun HomeShimmer() {
    val shimmerProgress =
        rememberInfiniteTransition(label = "HomePlaceholderShimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(1000)),
            label = "HomePlaceholderShimmer",
        )
    LazyColumn(
        modifier = Modifier.padding(horizontal = 15.dp),
        userScrollEnabled = false,
    ) {
        item(key = "home-quick-picks-skeleton") {
            QuickPicksShimmer(shimmerProgress)
        }
        items(
            count = 4,
            key = { index -> "home-shelf-skeleton-$index" },
        ) {
            HomeItemShimmer(shimmerProgress)
        }
    }
}

@Composable
fun ShimmerSearchItem() {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail shimmer
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LocalAppColors.current.shimmerBackground)
                .shimmer()
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text content shimmer
        Column {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer()
            )
        }
    }
}

/** A quiet loading placeholder shaped like lyric lines instead of a generic progress spinner. */
@Composable
fun LyricsTextLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LyricsShimmerLineWidths.forEachIndexed { index, widthFraction ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(if (index == 2) 25.dp else 21.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LocalAppColors.current.shimmerBackground)
                    .shimmer(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerSearchItemPreview() {
    ShimmerSearchItem()
}
