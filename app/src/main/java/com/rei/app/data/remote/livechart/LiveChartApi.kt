package com.rei.app.data.remote.livechart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LiveChart.me API — Precise airing schedule with streaming availability
 * Shows WHERE to watch (Crunchyroll, HIDIVE, Netflix, etc.) per region
 * with exact air times and episode countdowns.
 * 
 * LiveChart provides an unofficial JSON API through their pages.
 * We parse the structured data from their seasonal schedule pages.
 * Website: https://www.livechart.me
 */
@Singleton
class LiveChartApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object {
        const val URL = "https://www.livechart.me"
    }

    private suspend fun fetch(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$URL/$path")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Rei/2.0")
            .build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("LiveChart: ${resp.code}")
        body
    }

    /**
     * Get current season's anime with streaming availability
     * Returns parsed JSON from LiveChart's seasonal feed
     */
    suspend fun getCurrentSeason(): List<LiveChartAnime> {
        val body = fetch("api/v1/charts?season=current")
        return try {
            val parsed = json.parseToJsonElement(body)
            when (parsed) {
                is JsonArray -> parsed.map { parseAnime(it.jsonObject) }
                is JsonObject -> parsed["anime"]?.jsonArray?.map { parseAnime(it.jsonObject) } ?: emptyList()
                else -> emptyList()
            }
        } catch (_: Exception) {
            // Fallback: try alternate endpoint
            try {
                val body2 = fetch("api/v1/seasonal")
                val parsed2 = json.parseToJsonElement(body2)
                if (parsed2 is JsonArray) parsed2.map { parseAnime(it.jsonObject) } else emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    /**
     * Get anime airing on a specific day
     */
    suspend fun getSchedule(dayOffset: Int = 0): List<LiveChartAnime> {
        val body = fetch("api/v1/schedule?offset=$dayOffset")
        return try {
            json.parseToJsonElement(body).jsonArray.map { parseAnime(it.jsonObject) }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Get streaming availability for a specific anime
     */
    suspend fun getStreaming(animeId: Int): List<StreamingOption> {
        val body = fetch("api/v1/anime/$animeId/streaming")
        return try {
            json.parseToJsonElement(body).jsonArray.map { parseStreaming(it.jsonObject) }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Get the current seasonal page with all data
     */
    suspend fun getSeasonPage(season: String? = null, year: Int? = null): LiveChartSeason {
        val path = if (season != null && year != null) "$season-$year" else ""
        val body = fetch(if (path.isNotEmpty()) "api/v1/charts?season=$path" else "api/v1/charts?season=current")
        return try {
            val parsed = json.parseToJsonElement(body).jsonObject
            LiveChartSeason(
                season = parsed["season"]?.jsonPrimitive?.contentOrNull ?: "Current",
                year = parsed["year"]?.jsonPrimitive?.intOrNull ?: 2024,
                anime = parsed["anime"]?.jsonArray?.map { parseAnime(it.jsonObject) } ?: emptyList()
            )
        } catch (_: Exception) {
            LiveChartSeason(anime = emptyList())
        }
    }

    private fun parseAnime(o: JsonObject) = LiveChartAnime(
        id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
        anilistId = o["anilist_id"]?.jsonPrimitive?.intOrNull ?: o["anilistId"]?.jsonPrimitive?.intOrNull,
        title = o["title"]?.jsonPrimitive?.contentOrNull ?: o["title_english"]?.jsonPrimitive?.contentOrNull ?: "",
        titleRomaji = o["title_romaji"]?.jsonPrimitive?.contentOrNull,
        titleNative = o["title_native"]?.jsonPrimitive?.contentOrNull,
        imageUrl = o["image_url"]?.jsonPrimitive?.contentOrNull ?: o["coverImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull,
        episodes = o["episodes"]?.jsonPrimitive?.intOrNull,
        episodeCount = o["episode_count"]?.jsonPrimitive?.intOrNull,
        nextEpisode = o["next_episode"]?.jsonPrimitive?.intOrNull,
        nextEpisodeAiringAt = o["next_episode_airing_at"]?.jsonPrimitive?.longOrNull,
        airingAt = o["airing_at"]?.jsonPrimitive?.longOrNull,
        score = o["score"]?.jsonPrimitive?.floatOrNull,
        status = o["status"]?.jsonPrimitive?.contentOrNull,
        format = o["format"]?.jsonPrimitive?.contentOrNull,
        studios = o["studios"]?.jsonArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull } ?: emptyList(),
        streaming = o["streaming"]?.jsonArray?.map { parseStreaming(it.jsonObject) } ?: emptyList(),
        genres = o["genres"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
        siteUrl = o["site_url"]?.jsonPrimitive?.contentOrNull
    )

    private fun parseStreaming(o: JsonObject) = StreamingOption(
        service = o["service"]?.jsonPrimitive?.contentOrNull ?: o["provider"]?.jsonPrimitive?.contentOrNull ?: "",
        url = o["url"]?.jsonPrimitive?.contentOrNull ?: "",
        regions = o["regions"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
        logo = o["logo"]?.jsonPrimitive?.contentOrNull ?: o["image"]?.jsonPrimitive?.contentOrNull
    )
}

// ═══════════════════════════════════════════
// LiveChart Data Models
// ═══════════════════════════════════════════
data class LiveChartSeason(
    val season: String = "Current",
    val year: Int = 2024,
    val anime: List<LiveChartAnime> = emptyList()
)

data class LiveChartAnime(
    val id: Int = 0,
    val anilistId: Int? = null,
    val title: String = "",
    val titleRomaji: String? = null,
    val titleNative: String? = null,
    val imageUrl: String? = null,
    val episodes: Int? = null,
    val episodeCount: Int? = null,
    val nextEpisode: Int? = null,
    val nextEpisodeAiringAt: Long? = null,
    val airingAt: Long? = null,
    val score: Float? = null,
    val status: String? = null,
    val format: String? = null,
    val studios: List<String> = emptyList(),
    val streaming: List<StreamingOption> = emptyList(),
    val genres: List<String> = emptyList(),
    val siteUrl: String? = null
) {
    val hasNextEpisode: Boolean get() = nextEpisode != null
    val hasStreaming: Boolean get() = streaming.isNotEmpty()
    val streamingServices: List<String> get() = streaming.map { it.service }
    val bestTitle: String get() = title.ifBlank { titleRomaji ?: titleNative ?: "Unknown" }
}

data class StreamingOption(
    val service: String = "",
    val url: String = "",
    val regions: List<String> = emptyList(),
    val logo: String? = null
) {
    val isAvailable: Boolean get() = url.isNotBlank()
    val isCrunchyroll: Boolean get() = service.contains("Crunchyroll", ignoreCase = true)
    val isHidive: Boolean get() = service.contains("HIDIVE", ignoreCase = true)
    val isNetflix: Boolean get() = service.contains("Netflix", ignoreCase = true)
    val isAmazon: Boolean get() = service.contains("Amazon", ignoreCase = true) || service.contains("Prime", ignoreCase = true)
    val isDisney: Boolean get() = service.contains("Disney", ignoreCase = true)

    val icon: String get() = when {
        isCrunchyroll -> "▸"
        isHidive -> "●"
        isNetflix -> "✕"
        isAmazon -> "◆"
        isDisney -> "●"
        else -> "▶"
    }

    val displayName: String get() = "$icon $service"
}
