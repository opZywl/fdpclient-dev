/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

/**
 * Reads default values for the Spotify module from system properties or environment variables.
 * They can be configured via Gradle properties which are then passed as JVM arguments.
 */
object SpotifyDefaults {
    private fun read(propertyKey: String, envKey: String, fallback: String = ""): String {
        return System.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
            ?: System.getenv(envKey)?.takeIf { it.isNotBlank() }
            ?: fallback
    }

    val clientId: String = read("spotify.clientId", "SPOTIFY_CLIENT_ID")
    val clientSecret: String = read("spotify.clientSecret", "SPOTIFY_CLIENT_SECRET")
    val refreshToken: String = read("spotify.refreshToken", "SPOTIFY_REFRESH_TOKEN")
    val pollIntervalSeconds: Int = read("spotify.pollIntervalSeconds", "SPOTIFY_POLL_INTERVAL", "5").toIntOrNull() ?: 5
    val httpTimeoutMillis: Long = read("spotify.httpTimeoutMs", "SPOTIFY_HTTP_TIMEOUT_MS", "12000").toLongOrNull() ?: 12_000L
}
