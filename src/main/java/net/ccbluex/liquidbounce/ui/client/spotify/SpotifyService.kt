/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
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
        LOGGER.info(
            "[Spotify][HTTP] POST ${TOKEN_URL} (clientId=${mask(credentials.clientId)}, refreshToken=${mask(credentials.refreshToken)}, flow=${credentials.flow})"
        )

        val refreshToken = credentials.refreshToken
            ?: throw IOException("Spotify refresh token was null")
        val clientId = credentials.clientId
            ?: throw IOException("Spotify client ID was null")
        val encodedRefresh = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name())
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
        val payloadBuilder = StringBuilder("grant_type=refresh_token&refresh_token=$encodedRefresh&client_id=$encodedClientId")

        val requestBuilder = Request.Builder()
            .url(TOKEN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")

        if (credentials.flow == SpotifyAuthFlow.CONFIDENTIAL_CLIENT) {
            val clientSecret = credentials.clientSecret
                ?: throw IOException("Spotify client secret was null for confidential flow")
            val basicAuth = Base64.getEncoder()
                .encodeToString("${clientId}:${clientSecret}".toByteArray(StandardCharsets.UTF_8))
            requestBuilder.header("Authorization", "Basic $basicAuth")
        }

        val request = requestBuilder
            .post(payloadBuilder.toString().toRequestBody(FORM_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Token response status=${response.code} message=${response.message}")

            val body = response.body?.string()?.orEmpty()
            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                LOGGER.warn("[Spotify][HTTP] Token refresh failed body=$message")
                throw IOException("Spotify token refresh failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                throw IOException("Spotify token response was empty")
            }

            val json = parseJson(body)
            val token = json.get("access_token")?.asString
                ?: throw IOException("Spotify token response did not contain an access token")
            val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY

            logTokenResponse(json, token)

            val refreshToken = json.get("refresh_token")?.asString
            SpotifyAccessToken(
                value = token,
                expiresAtMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn - 5),
                refreshToken = refreshToken,
            )
        }
    }

    suspend fun exchangeAuthorizationCode(
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String,
        codeVerifier: String?,
    ): SpotifyAccessToken = withContext(Dispatchers.IO) {
        LOGGER.info(
            "[Spotify][HTTP] POST ${TOKEN_URL} (clientId=${mask(clientId)}, grant_type=authorization_code)"
        )

        val encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8.name())
        val encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name())
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
        val payloadBuilder = StringBuilder(
            "grant_type=authorization_code&code=$encodedCode&redirect_uri=$encodedRedirect&client_id=$encodedClientId",
        )
        if (!codeVerifier.isNullOrBlank()) {
            payloadBuilder.append("&code_verifier=")
                .append(URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8.name()))
        }

        val requestBuilder = Request.Builder()
            .url(TOKEN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")

        if (!clientSecret.isNullOrBlank()) {
            val basicAuth = Base64.getEncoder()
                .encodeToString("$clientId:$clientSecret".toByteArray(StandardCharsets.UTF_8))
            requestBuilder.header("Authorization", "Basic $basicAuth")
        }

        val request = requestBuilder
            .post(payloadBuilder.toString().toRequestBody(FORM_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Authorization response status=${response.code} message=${response.message}")

            val body = response.body?.string()?.orEmpty()
            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                LOGGER.warn("[Spotify][HTTP] Authorization exchange failed body=$message")
                throw IOException("Spotify authorization failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                throw IOException("Spotify authorization response was empty")
            }

            val json = parseJson(body)
            val token = json.get("access_token")?.asString
                ?: throw IOException("Spotify authorization response missing access token")
            val refreshToken = json.get("refresh_token")?.asString
                ?: throw IOException("Spotify authorization response missing refresh token")
            val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY

            logTokenResponse(json, token)

            SpotifyAccessToken(
                value = token,
                expiresAtMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn - 5),
                refreshToken = refreshToken,
            )
        }
    }

    suspend fun fetchCurrentlyPlaying(accessToken: String): SpotifyState? = withContext(Dispatchers.IO) {
        LOGGER.info("[Spotify][HTTP] GET ${NOW_PLAYING_URL} (token=${mask(accessToken)})")

        val request = Request.Builder()
            .url(NOW_PLAYING_URL)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Playback response status=${response.code} message=${response.message}")
            if (response.code == 204) {
                LOGGER.info("[Spotify] Spotify API returned 204 - no active playback")
                return@use null
            }

            val body = response.body?.string()?.orEmpty()
            LOGGER.info("[Spotify][HTTP] Playback response body=${body.ifBlank { "<empty>" }}")

            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                throw IOException("Spotify now playing request failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                LOGGER.info("[Spotify] Playback response was empty")
                return@use null
            }
            val state = parseState(body)
            logPlaybackState(state)
            state
        }
    }

    suspend fun fetchUserPlaylists(accessToken: String, limit: Int = 50, offset: Int = 0): List<SpotifyPlaylistSummary> =
        withContext(Dispatchers.IO) {
            val resolvedLimit = limit.coerceIn(1, 50)
            val resolvedOffset = offset.coerceAtLeast(0)
            val url = "$PLAYLISTS_URL?limit=$resolvedLimit&offset=$resolvedOffset"
            LOGGER.info("[Spotify][HTTP] GET $PLAYLISTS_URL (limit=$resolvedLimit, offset=$resolvedOffset)")
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = body.ifBlank { "<empty>" }
                    throw IOException("Spotify playlist request failed with HTTP ${response.code}: $message")
                }
                if (body.isBlank()) {
                    return@use emptyList()
                }
                val json = parseJson(body)
                val items = json.get("items")?.takeIf { it.isJsonArray }?.asJsonArray ?: return@use emptyList()
                items.mapNotNull { element ->
                    val playlistObj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                    parsePlaylistSummary(playlistObj)
                }
            }
        }

    suspend fun fetchPlaylistTracks(
        accessToken: String,
        playlistId: String,
        limit: Int = 100,
        offset: Int = 0,
    ): SpotifyTrackPage {
        val encodedId = URLEncoder.encode(playlistId, StandardCharsets.UTF_8.name())
        val url = "$PLAYLIST_URL/$encodedId/tracks"
        return fetchTrackPage(url, accessToken, limit.coerceIn(1, 100), offset)
    }

    suspend fun fetchSavedTracks(accessToken: String, limit: Int = 50, offset: Int = 0): SpotifyTrackPage {
        return fetchTrackPage(SAVED_TRACKS_URL, accessToken, limit.coerceIn(1, 50), offset)
    }

    suspend fun startPlayback(
        accessToken: String,
        contextUri: String? = null,
        trackUri: String? = null,
        offsetUri: String? = null,
        positionMs: Int = 0,
    ) {
        val payload = JsonObject()
        if (!contextUri.isNullOrBlank()) {
            payload.addProperty("context_uri", contextUri)
        }
        if (!offsetUri.isNullOrBlank()) {
            val offset = JsonObject().apply { addProperty("uri", offsetUri) }
            payload.add("offset", offset)
        } else if (!trackUri.isNullOrBlank() && contextUri.isNullOrBlank()) {
            val uris = JsonArray().apply { add(trackUri) }
            payload.add("uris", uris)
        }
        if (positionMs > 0) {
            payload.addProperty("position_ms", positionMs)
        }

        val body = payload.toString().takeIf { payload.entrySet().isNotEmpty() } ?: "{}"
        val request = Request.Builder()
            .url("$PLAYER_URL/play")
            .header("Authorization", "Bearer $accessToken")
            .put(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()
        LOGGER.info("[Spotify][HTTP] PUT $PLAYER_URL/play (context=${!contextUri.isNullOrBlank()}, track=${!trackUri.isNullOrBlank()}, offset=${!offsetUri.isNullOrBlank()})")
        executeControlRequest(request)
    }

    suspend fun pausePlayback(accessToken: String) {
        LOGGER.info("[Spotify][HTTP] PUT $PLAYER_URL/pause")
        val request = Request.Builder()
            .url("$PLAYER_URL/pause")
            .header("Authorization", "Bearer $accessToken")
            .put("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        executeControlRequest(request)
    }

    suspend fun skipToNext(accessToken: String) {
        LOGGER.info("[Spotify][HTTP] POST $PLAYER_URL/next")
        val request = Request.Builder()
            .url("$PLAYER_URL/next")
            .header("Authorization", "Bearer $accessToken")
            .post("".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        executeControlRequest(request)
    }

    suspend fun skipToPrevious(accessToken: String) {
        LOGGER.info("[Spotify][HTTP] POST $PLAYER_URL/previous")
        val request = Request.Builder()
            .url("$PLAYER_URL/previous")
            .header("Authorization", "Bearer $accessToken")
            .post("".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        executeControlRequest(request)
    }

    private suspend fun fetchTrackPage(
        url: String,
        accessToken: String,
        limit: Int,
        offset: Int,
    ): SpotifyTrackPage = withContext(Dispatchers.IO) {
        val resolvedOffset = offset.coerceAtLeast(0)
        val requestUrl = "$url?limit=$limit&offset=$resolvedOffset"
        LOGGER.info("[Spotify][HTTP] GET $url (limit=$limit, offset=$resolvedOffset)")
        val request = Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                throw IOException("Spotify track request failed with HTTP ${response.code}: $message")
            }
            if (body.isBlank()) {
                return@use SpotifyTrackPage(emptyList(), 0)
            }
            val json = parseJson(body)
            val items = json.get("items")?.takeIf { it.isJsonArray }?.asJsonArray
                ?: return@use SpotifyTrackPage(emptyList(), json.get("total")?.asInt ?: 0)
            val tracks = items.mapNotNull { element ->
                val wrapper = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val trackObj = wrapper.get("track")?.takeIf { it.isJsonObject }?.asJsonObject ?: wrapper
                parseTrack(trackObj)
            }
            val total = json.get("total")?.asInt ?: tracks.size
            SpotifyTrackPage(tracks, total)
        }
    }

    private fun parsePlaylistSummary(obj: JsonObject): SpotifyPlaylistSummary? {
        val id = obj.get("id")?.asString ?: return null
        val name = obj.get("name")?.asString ?: "Untitled"
        val description = obj.get("description")?.asString
        val owner = obj.get("owner")?.takeIf { it.isJsonObject }?.asJsonObject?.get("display_name")?.asString
        val trackCount = obj.get("tracks")?.takeIf { it.isJsonObject }?.asJsonObject?.get("total")?.asInt
            ?: obj.get("total")?.asInt
            ?: 0
        val imageUrl = obj.get("images")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull { it.isJsonObject }
            ?.asJsonObject
            ?.get("url")
            ?.asString
        val uri = obj.get("uri")?.asString
        return SpotifyPlaylistSummary(id, name, description, owner, trackCount, imageUrl, uri)
    }

    private suspend fun executeControlRequest(request: Request) = withContext(Dispatchers.IO) {
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful && response.code != 204) {
                val message = body.ifBlank { "<empty>" }
                throw IOException("Spotify control request failed with HTTP ${response.code}: $message")
            }
        }
    }

    private fun parseState(body: String): SpotifyState? {
        val json = parseJson(body)
        val isPlaying = json.get("is_playing")?.asBoolean ?: false
        val progress = json.get("progress_ms")?.asInt ?: 0

        val track = parseTrack(item)
        return SpotifyState(
            track,
            isPlaying,
            progress,
        )
    }

    private fun parseJson(body: String) = JsonParser().parse(body).asJsonObject

    private fun logTokenResponse(json: JsonObject, token: String) {
        val sanitized = JsonObject()
        for ((key, value) in json.entrySet()) {
            when (key) {
                "access_token" -> sanitized.addProperty(key, mask(token))
                "refresh_token" -> sanitized.addProperty(key, mask(value.asString))
                else -> sanitized.add(key, value)
            }
        }
        LOGGER.info("[Spotify][HTTP] Token response body=$sanitized")
        val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY
        LOGGER.info("[Spotify] Access token refreshed (expires in ${expiresIn}s)")
    }

    private fun logPlaybackState(state: SpotifyState?) {
        if (state == null) {
            LOGGER.info("[Spotify] No playback data returned by the API")
            return
        }

        val track = state.track
        if (track == null) {
            LOGGER.info("[Spotify] Playback status ${if (state.isPlaying) "is playing" else "paused"} but no track metadata provided")
            return
        }

        val elapsedSeconds = state.progressMs / 1000
        val durationSeconds = track.durationMs.coerceAtLeast(1) / 1000
        LOGGER.info(
            "[Spotify] ${if (state.isPlaying) "Playing" else "Paused"}: ${track.title} - ${track.artists} (${elapsedSeconds}s/${durationSeconds}s)"
        )
    }

    private fun parseTrack(item: JsonObject?): SpotifyTrack? {
        if (item == null) {
            return null
        }

        val id = item.get("id")?.asString ?: return null
        val title = item.get("name")?.asString ?: "Unknown"
        val artists = item.get("artists")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.mapNotNull { artist -> artist.asJsonObject.get("name")?.asString }
            ?.joinToString(", ") ?: "Unknown"
        val albumObj = item.get("album")?.takeIf { it.isJsonObject }?.asJsonObject
        val albumName = albumObj?.get("name")?.asString ?: ""
        val coverUrl = albumObj
            ?.get("images")
            ?.takeIf { it.isJsonArray }
            ?.asJsonArray
            ?.firstOrNull { it.isJsonObject }
            ?.asJsonObject
            ?.get("url")
            ?.asString
        val duration = item.get("duration_ms")?.asInt ?: 0

        return SpotifyTrack(
            id = id,
            title = title,
            artists = artists,
            album = albumName,
            coverUrl = coverUrl,
            durationMs = duration,
        )
    }

    private companion object {
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"
        const val NOW_PLAYING_URL = "https://api.spotify.com/v1/me/player/currently-playing"
        const val PLAYLISTS_URL = "https://api.spotify.com/v1/me/playlists"
        const val PLAYLIST_URL = "https://api.spotify.com/v1/playlists"
        const val SAVED_TRACKS_URL = "https://api.spotify.com/v1/me/tracks"
        const val PLAYER_URL = "https://api.spotify.com/v1/me/player"
        const val DEFAULT_TOKEN_EXPIRY = 3600L
        val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        fun mask(value: String?): String = when {
            value == null -> "<null>"
            value.isEmpty() -> "<empty>"
            value.length <= 4 -> "***"
            else -> value.take(4) + "***"
        }
    }
}