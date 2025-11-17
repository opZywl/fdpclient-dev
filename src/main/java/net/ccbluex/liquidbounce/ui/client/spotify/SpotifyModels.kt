/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

/**
 * Represents a simplified Spotify track.
 */
data class SpotifyTrack(
    val id: String,
    val title: String,
    val artists: String,
    val album: String,
    val coverUrl: String?,
    val durationMs: Int,
)

/**
 * Represents the state of the current Spotify playback session.
 */
data class SpotifyState(
    val track: SpotifyTrack?,
    val isPlaying: Boolean,
    val progressMs: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * OAuth credentials that are required for the Spotify Web API.
 */
data class SpotifyCredentials(
    val clientId: String,
    val clientSecret: String,
    val refreshToken: String,
) {
    fun isValid(): Boolean = clientId.isNotBlank() && clientSecret.isNotBlank() && refreshToken.isNotBlank()
}

/**
 * Cached access token and expiry information.
 */
data class SpotifyAccessToken(
    val value: String,
    val expiresAtMillis: Long,
)

/**
 * Connection state used by the HUD/GUI to provide feedback to the user.
 */
enum class SpotifyConnectionState(val displayName: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    ERROR("Error"),
}
