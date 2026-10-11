package com.maxrave.kotlinytmusicscraper.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote app config fetched from GitHub raw on each app launch.
 *
 * Flat schema. All fields are nullable so a partial, malformed, or future version of the
 * file never crashes parsing — a missing field just leaves the corresponding credential
 * empty (TIDAL metadata stays disabled until a valid value is fetched).
 */
@Serializable
data class RemoteConfig(
    @SerialName("tidalClientId")
    val tidalClientId: String? = null,
    @SerialName("tidalClientSecret")
    val tidalClientSecret: String? = null,
)
