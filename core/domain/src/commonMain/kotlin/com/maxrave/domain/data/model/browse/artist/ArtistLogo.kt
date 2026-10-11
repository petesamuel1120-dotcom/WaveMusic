package com.maxrave.domain.data.model.browse.artist

/**
 * Artist title rendered as a color logo image, used in place of the plain-text artist name in the
 * header. [bgColorHex] is the logo's dominant color (hex string, no `#`) used to tint the action
 * buttons; null when unavailable.
 */
data class ArtistLogo(
    val logoUrl: String,
    val bgColorHex: String?,
    val width: Int,
    val height: Int,
)
