package com.maxrave.kotlinytmusicscraper.models.youtube.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Keep this shared model outside the watch-page hierarchy: D8 appends synthetic
// suffixes to JVM class names, which exceeded Windows' 255-character filename limit.
@Serializable
data class ContinuationItemRenderer(
    @SerialName("trigger")
    val trigger: String? = null,
    @SerialName("continuationEndpoint")
    val continuationEndpoint: ContinuationEndpoint? = null,
) {
    @Serializable
    data class ContinuationEndpoint(
        @SerialName("clickTrackingParams")
        val clickTrackingParams: String? = null,
        @SerialName("continuationCommand")
        val continuationCommand: ContinuationCommand? = null,
        @SerialName("commandExecutorCommand")
        val commandExecutorCommand: CommandExecutorCommand? = null,
    ) {
        @Serializable
        data class CommandExecutorCommand(
            val commands: List<Command> = emptyList(),
        ) {
            @Serializable
            data class Command(
                val continuationCommand: ContinuationCommand? = null,
            )
        }

        @Serializable
        data class ContinuationCommand(
            @SerialName("token")
            val token: String? = null,
            @SerialName("request")
            val request: String? = null,
        )
    }
}
