package com.rei.app.data.remote.simkl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simkl API — free tier for streaming link lookup and calendar.
 * Client ID is optional for search; required for user lists.
 * Provides cross-platform streaming availability data.
 *
 * Get free client ID at: https://simkl.com/settings/api/
 */
@Singleton
class SimklApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object {
        const val URL = "https://api.simkl.com"
        // Free client ID — users can replace with their own
        const val CLIENT_ID = ""
    }

    private suspend fun get(path: String): JsonObject = withContext(Dispatchers.IO) {
        val sep = if (path.contains("?")) "&" else "?"
        val url = "$URL/$path${sep}client_id=$CLIENT_ID"
        val resp = client.newCall(
            Request.Builder().url(url)
                .addHeader("Accept", "application/json")
                .build()
        ).execute()
        val body = resp.body?.string() ?: return@withContext JsonObject(emptyMap())
        json.parseToJsonElement(body).jsonObject
    }

    /** Search anime by title — returns Simkl IDs with basic info */
    suspend fun search(query: String): List<SimklSearchResult> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val result = get("search/anime?q=$encoded&limit=10")
        return result.jsonArray?.mapNotNull { parseSearchResult(it.jsonObject) } ?: emptyList()
    }

    /** Get streaming sources for an anime by Simkl ID */
    suspend fun getStreamingSources(simklId: Int): List<StreamingSource> {
        val result = get("anime/$simklId/summary")
        val items = result["items"]?.jsonArray ?: result["data"]?.jsonArray ?: return emptyList()
        return items.firstOrNull()?.jsonObject?.let { parseStreaming(it) } ?: emptyList()
    }

    /** Get anime calendar — what's airing today and upcoming */
    suspend fun getCalendar(date: String? = null): List<SimklCalendarEntry> {
        val path = if (date != null) "calendars/anime?date=$date" else "calendars/anime"
        val result = get(path)
        return result.jsonArray?.mapNotNull { parseCalendarEntry(it.jsonObject) } ?: emptyList()
    }

    /** Get anime detail by Simkl ID */
    suspend fun getDetail(simklId: Int): SimklAnimeDetail? {
        val result = get("anime/$simklId/summary")
        val obj = result["items"]?.jsonArray?.firstOrNull()?.jsonObject ?: result["data"]?.jsonObject
        return obj?.let { parseDetail(it) }
    }

    private fun parseSearchResult(o: JsonObject): SimklSearchResult? {
        return SimklSearchResult(
            simklId = o["simkl_id"]?.jsonPrimitive?.intOrNull ?: return null,
            title = o["title"]?.jsonPrimitive?.contentOrNull ?: return null,
            year = o["year"]?.jsonPrimitive?.intOrNull,
            poster = o["poster"]?.jsonPrimitive?.contentOrNull,
            status = o["status"]?.jsonPrimitive?.contentOrNull ?: "",
            animeType = o["anime_type"]?.jsonPrimitive?.contentOrNull ?: ""
        )
    }

    private fun parseStreaming(o: JsonObject): List<StreamingSource> {
        val sources = mutableListOf<StreamingSource>()
        o["streaming"]?.jsonArray?.forEach { item ->
            val obj = item.jsonObject
            sources.add(StreamingSource(
                name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@forEach,
                url = obj["url"]?.jsonPrimitive?.contentOrNull ?: "",
                type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "sub",
                allEpisodes = obj["allEpisodes"]?.jsonPrimitive?.booleanOrNull ?: false
            ))
        }
        return sources
    }

    private fun parseCalendarEntry(o: JsonObject): SimklCalendarEntry? {
        val anime = o["anime"]?.jsonObject ?: o["show"]?.jsonObject ?: return null
        return SimklCalendarEntry(
            simklId = anime["ids"]?.jsonObject?.get("simkl")?.jsonPrimitive?.intOrNull ?: return null,
            title = anime["title"]?.jsonPrimitive?.contentOrNull ?: return null,
            poster = anime["poster"]?.jsonPrimitive?.contentOrNull,
            episode = o["episode"]?.jsonPrimitive?.intOrNull ?: 1,
            date = o["date"]?.jsonPrimitive?.contentOrNull ?: ""
        )
    }

    private fun parseDetail(o: JsonObject): SimklAnimeDetail? {
        return SimklAnimeDetail(
            simklId = o["ids"]?.jsonObject?.get("simkl")?.jsonPrimitive?.intOrNull ?: return null,
            title = o["title"]?.jsonPrimitive?.contentOrNull ?: return null,
            overview = o["overview"]?.jsonPrimitive?.contentOrNull,
            poster = o["poster"]?.jsonPrimitive?.contentOrNull,
            rating = o["ratings"]?.jsonObject?.get("simkl")?.jsonPrimitive?.intOrNull,
            status = o["status"]?.jsonPrimitive?.contentOrNull ?: "",
            animeType = o["anime_type"]?.jsonPrimitive?.contentOrNull ?: "",
            episodes = o["total_episodes"]?.jsonPrimitive?.intOrNull,
            year = o["year"]?.jsonPrimitive?.intOrNull,
            genres = o["genres"]?.jsonArray?.mapNotNull { it.jsonPrimitive?.contentOrNull } ?: emptyList()
        )
    }
}

data class SimklSearchResult(
    val simklId: Int,
    val title: String,
    val year: Int? = null,
    val poster: String? = null,
    val status: String = "",
    val animeType: String = ""
)

data class StreamingSource(
    val name: String,
    val url: String,
    val type: String = "sub",  // sub, dub
    val allEpisodes: Boolean = false
)

data class SimklCalendarEntry(
    val simklId: Int,
    val title: String,
    val poster: String? = null,
    val episode: Int = 1,
    val date: String = ""
)

data class SimklAnimeDetail(
    val simklId: Int,
    val title: String,
    val overview: String? = null,
    val poster: String? = null,
    val rating: Int? = null,
    val status: String = "",
    val animeType: String = "",
    val episodes: Int? = null,
    val year: Int? = null,
    val genres: List<String> = emptyList()
)
