package com.rei.app.data.remote.jikan

import com.rei.app.domain.model.MediaListStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MAL API v2 write operations for anime lists management.
 * Requires OAuth token (MALAuth).
 *
 * MAL API docs: https://myanimelist.net/apiconfig/references/api/v2
 */
@Singleton
class MALListApi @Inject constructor(
    private val client: OkHttpClient,
    private val malAuth: MALAuth
) {
    companion object {
        const val BASE_URL = "https://api.myanimelist.net/v2"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** Map our status to MAL status string */
    private fun MediaListStatus.toMalStatus(): String = when (this) {
        MediaListStatus.CURRENT -> "watching"
        MediaListStatus.COMPLETED -> "completed"
        MediaListStatus.PAUSED -> "on_hold"
        MediaListStatus.DROPPED -> "dropped"
        MediaListStatus.PLANNING -> "plan_to_watch"
        MediaListStatus.REPEATING -> "watching" // MAL has no rewatching status, use watching
    }

    /** Map MAL status string back to our status */
    fun fromMalStatus(status: String): MediaListStatus = when (status) {
        "watching" -> MediaListStatus.CURRENT
        "completed" -> MediaListStatus.COMPLETED
        "on_hold" -> MediaListStatus.PAUSED
        "dropped" -> MediaListStatus.DROPPED
        "plan_to_watch" -> MediaListStatus.PLANNING
        else -> MediaListStatus.CURRENT
    }

    /**
     * Update anime entry on user's MAL list.
     * PUT /v2/anime/{anime_id}/my_list_status
     */
    suspend fun updateAnimeList(
        animeId: Int,
        status: MediaListStatus,
        score: Int? = null,       // 0-10 on MAL
        progress: Int? = null,    // episodes watched
        isRewatching: Boolean? = null,
        notes: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val token = malAuth.getToken() ?: return@withContext false

        val fields = mutableListOf("status=${status.toMalStatus()}")
        score?.let { fields.add("score=$it") }
        progress?.let { fields.add("num_watched_episodes=$it") }
        isRewatching?.let { fields.add("is_rewatching=$it") }
        notes?.let { fields.add("comments=${java.net.URLEncoder.encode(it, "UTF-8")}") }

        val formBody = fields.joinToString("&")
            .toRequestBody("application/x-www-form-urlencoded".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/anime/$animeId/my_list_status")
            .put(formBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) { false }
    }

    /**
     * Delete anime from user's MAL list.
     * DELETE /v2/anime/{anime_id}/my_list_status
     */
    suspend fun deleteAnimeList(animeId: Int): Boolean = withContext(Dispatchers.IO) {
        val token = malAuth.getToken() ?: return@withContext false

        val request = Request.Builder()
            .url("$BASE_URL/anime/$animeId/my_list_status")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) { false }
    }

    /**
     * Get user's anime list.
     * GET /v2/users/@me/animelist?fields=status,score,num_watched_episodes,is_rewatching
     */
    suspend fun getUserAnimeList(
        status: String? = null,
        sort: String = "anime_title",
        limit: Int = 100,
        offset: Int = 0
    ): List<MALListEntry> = withContext(Dispatchers.IO) {
        val token = malAuth.getToken() ?: return@withContext emptyList()

        var url = "$BASE_URL/users/@me/animelist?limit=$limit&offset=$offset&sort=$sort&fields=status,score,num_watched_episodes,is_rewatching,comments"
        status?.let { url += "&status=$it" }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            if (!response.isSuccessful) return@withContext emptyList()

            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonArray ?: return@withContext emptyList()

            data.map { item ->
                val obj = item.jsonObject
                val node = obj["node"]?.jsonObject ?: return@map MALListEntry()
                val listStatus = obj["list_status"]?.jsonObject

                MALListEntry(
                    animeId = node["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    title = node["title"]?.jsonPrimitive?.contentOrNull ?: "",
                    mainPicture = node["main_picture"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull,
                    status = listStatus?.get("status")?.jsonPrimitive?.contentOrNull ?: "watching",
                    score = listStatus?.get("score")?.jsonPrimitive?.intOrNull ?: 0,
                    episodesWatched = listStatus?.get("num_watched_episodes")?.jsonPrimitive?.intOrNull ?: 0,
                    isRewatching = listStatus?.get("is_rewatching")?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false,
                    comments = listStatus?.get("comments")?.jsonPrimitive?.contentOrNull ?: "",
                    totalEpisodes = node["num_episodes"]?.jsonPrimitive?.intOrNull
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Get authenticated user info.
     * GET /v2/users/@me
     */
    suspend fun getAuthenticatedUser(): MALUser? = withContext(Dispatchers.IO) {
        val token = malAuth.getToken() ?: return@withContext null

        val request = Request.Builder()
            .url("$BASE_URL/users/@me?fields=anime_statistics")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            if (!response.isSuccessful) return@withContext null

            val root = json.parseToJsonElement(body).jsonObject
            val stats = root["anime_statistics"]?.jsonObject

            MALUser(
                id = root["id"]?.jsonPrimitive?.intOrNull ?: 0,
                name = root["name"]?.jsonPrimitive?.contentOrNull ?: "",
                avatar = root["picture"]?.jsonPrimitive?.contentOrNull,
                animeWatched = stats?.get("num_items_watched")?.jsonPrimitive?.intOrNull ?: 0,
                episodesWatched = stats?.get("num_episodes_watched")?.jsonPrimitive?.intOrNull ?: 0,
                daysWatched = stats?.get("num_days_watched")?.jsonPrimitive?.intOrNull?.toFloat() ?: 0f,
                meanScore = stats?.get("mean_score")?.jsonPrimitive?.contentOrNull?.toFloatOrNull() ?: 0f
            )
        } catch (_: Exception) { null }
    }
}

/** MAL list entry from user's anime list */
data class MALListEntry(
    val animeId: Int = 0,
    val title: String = "",
    val mainPicture: String? = null,
    val status: String = "watching",
    val score: Int = 0,
    val episodesWatched: Int = 0,
    val isRewatching: Boolean = false,
    val comments: String = "",
    val totalEpisodes: Int? = null
)

/** MAL authenticated user info */
data class MALUser(
    val id: Int = 0,
    val name: String = "",
    val avatar: String? = null,
    val animeWatched: Int = 0,
    val episodesWatched: Int = 0,
    val daysWatched: Float = 0f,
    val meanScore: Float = 0f
)
