package com.rei.app.data.remote.animeschedule

import com.rei.app.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// ═══════════════════════════════════════════
// AnimeSchedule.net API — Free, no key needed
// Airing schedule with episode times per day
// ═══════════════════════════════════════════

@Serializable
data class ScheduleResponse(
    val results: List<ScheduleItem> = emptyList()
)

@Serializable
data class ScheduleItem(
    val anilistId: Int = 0,
    val malId: Int = 0,
    val title: String = "",
    val imageUrl: String = "",
    val episode: Int = 0,
    val airingAt: Long = 0,
    val timeUntilAiring: Int = 0
)

class AnimeScheduleApi(
    private val client: OkHttpClient,
    private val json: Json
) {
    private val baseUrl = "https://animeschedule.net/api/v3"

    /** Get current season schedule grouped by day */
    suspend fun getCurrentSchedule(): List<ScheduleItem> {
        return try {
            val request = okhttp3.Request.Builder()
                .url("$baseUrl/timetables")
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            json.decodeFromString<ScheduleResponse>(body).results
        } catch (_: Exception) { emptyList() }
    }

    /** Get schedule for a specific day (0=Monday..6=Sunday) */
    suspend fun getDaySchedule(day: Int): List<ScheduleItem> {
        return try {
            val dayParam = when (day) {
                0 -> "monday"; 1 -> "tuesday"; 2 -> "wednesday"; 3 -> "thursday"
                4 -> "friday"; 5 -> "saturday"; 6 -> "sunday"; else -> "monday"
            }
            val request = okhttp3.Request.Builder()
                .url("$baseUrl/timetables/$dayParam")
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            json.decodeFromString<ScheduleResponse>(body).results
        } catch (_: Exception) { emptyList() }
    }

    /** Convert ScheduleItem to domain Anime */
    fun ScheduleItem.toAnime() = Anime(
        id = anilistId,
        title = AnimeTitle(romaji = title, english = title, native = title),
        coverImage = CoverImage(large = imageUrl, medium = imageUrl),
        bannerImage = null,
        meanScore = null,
        episodes = null,
        status = MediaStatus.RELEASING,
        format = MediaFormat.TV,
        genres = emptyList(),
        tags = emptyList(),
        studios = emptyList(),
        nextAiringEpisode = AiringSchedule(airingAt = airingAt, timeUntilAiring = timeUntilAiring, episode = episode),
        seasonYear = null,
        duration = null,
        description = null,
        popularity = null,
        trending = null,
        favourites = null,
        mediaListEntry = null,
        isFavourite = false,
        recommendations = emptyList(),
        trailer = null,
        siteUrl = null
    )
}
