package com.petedev.wavemusic.kotlinytmusicscraper.models

import com.petedev.wavemusic.kotlinytmusicscraper.models.subscriptionButton.SubscribeButtonRenderer
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer,
)