/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Handles the Spotify Web API HTTP calls.
 */
class SpotifyService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .build(),
) {

    suspend fun refreshAccessToken(credentials: SpotifyCredentials): SpotifyAccessToken = withContext(Dispatchers.IO) {
        val encodedRefresh = URLEncoder.encode(credentials.refreshToken, StandardCharsets.UTF_8.name())
        val payload = "grant_type=refresh_token&refresh_token=$encodedRefresh"
        val basicAuth = Base64.getEncoder()
            .encodeToString("${credentials.clientId}:${credentials.clientSecret}".toByteArray(StandardCharsets.UTF_8))

        val request = Request.Builder()
            .url(TOKEN_URL)
            .header("Authorization", "Basic $basicAuth")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(payload.toRequestBody(FORM_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Spotify token refresh failed with HTTP ${'$'}{response.code}")
            }

            val body = response.body?.string()?.takeIf { it.isNotBlank() }
                ?: throw IOException("Spotify token response was empty")

            val json = JsonParser.parseString(body).asJsonObject
            val token = json.get("access_token")?.asString
                ?: throw IOException("Spotify token response did not contain an access token")
            val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY

            SpotifyAccessToken(
                token,
                System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn - 5)
            )
        }
    }

    suspend fun fetchCurrentlyPlaying(accessToken: String): SpotifyState? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(NOW_PLAYING_URL)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.code == 204) {
                return@use null
            }

            if (!response.isSuccessful) {
                throw IOException("Spotify now playing request failed with HTTP ${'$'}{response.code}")
            }

            val body = response.body?.string()?.takeIf { it.isNotBlank() } ?: return@use null
            parseState(body)
        }
    }

    private fun parseState(body: String): SpotifyState? {
        val json = JsonParser.parseString(body).asJsonObject
        val isPlaying = json.get("is_playing")?.asBoolean ?: false
        val progress = json.get("progress_ms")?.asInt ?: 0

        val item = json.get("item")?.takeIf { it.isJsonObject }?.asJsonObject ?: return SpotifyState(null, isPlaying, progress)
        val id = item.get("id")?.asString ?: ""
        val title = item.get("name")?.asString ?: "Unknown"

        val artists = item.get("artists")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.mapNotNull { it.asJsonObject.get("name")?.asString }
            ?.joinToString(", ") ?: "Unknown"

        val albumObj = item.get("album")?.takeIf { it.isJsonObject }?.asJsonObject
        val albumName = albumObj?.get("name")?.asString ?: ""
        val coverUrl = albumObj?.get("images")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull { it.isJsonObject }
            ?.asJsonObject
            ?.get("url")
            ?.asString

        return SpotifyState(
            SpotifyTrack(
                id = id,
                title = title,
                artists = artists,
                album = albumName,
                coverUrl = coverUrl,
            ),
            isPlaying,
            progress,
        )
    }

    private companion object {
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"
        const val NOW_PLAYING_URL = "https://api.spotify.com/v1/me/player/currently-playing"
        const val DEFAULT_TOKEN_EXPIRY = 3600L
        val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()
    }
}
