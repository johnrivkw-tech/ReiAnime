package com.rei.app.data.remote.kitsu

import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.AnimeTitle
import com.rei.app.domain.model.CoverImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kitsu API — anime/manga library with trending, categories, and library entries.
 * Base: https://kitsu.io/api/edge
 */
@Singleton
class KitsuApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object { const val URL = "https://kitsu.io/api/edge" }

    private suspend fun get(path: String): JsonArray = withContext(Dispatchers.IO) {
        val req = Request.Builder().url("$URL/$path")
            .addHeader("Accept", "application/vnd.api+json")
            .addHeader("Content-Type", "application/vnd.api+json")
            .build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string() ?: return@withContext JsonArray(emptyList())
        json.parseToJsonElement(body).jsonObject["data"]?.jsonArray ?: JsonArray(emptyList())
    }

    suspend fun getTrending(limit: Int = 20): List<Anime> {
        val data = get("trending/anime?limit=$limit")
        return data.map { parseKitsuAnime(it.jsonObject) }
    }

    suspend fun getCategories(): List<String> {
        val data = get("categories?limit=40&page%5Boffset%5D=0")
        return data.mapNotNull { it.jsonObject["attributes"]?.jsonObject?.get("title")?.jsonPrimitive?.contentOrNull }
    }

    suspend fun searchAnime(query: String, limit: Int = 20): List<Anime> {
        val data = get("anime?filter[text]=$query&limit=$limit")
        return data.map { parseKitsuAnime(it.jsonObject) }
    }

    private fun parseKitsuAnime(o: JsonObject): Anime {
        val attr = o["attributes"]?.jsonObject ?: JsonObject(emptyMap())
        val titles = attr["titles"]?.jsonObject
        val poster = attr["posterImage"]?.jsonObject
        val cover = attr["coverImage"]?.jsonObject
        return Anime(
            id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
            title = AnimeTitle(
                english = titles?.get("en")?.jsonPrimitive?.contentOrNull,
                romaji = titles?.get("en_jp")?.jsonPrimitive?.contentOrNull,
                native = titles?.get("ja_jp")?.jsonPrimitive?.contentOrNull,
                userPreferred = attr["canonicalTitle"]?.jsonPrimitive?.contentOrNull
            ),
            description = attr["synopsis"]?.jsonPrimitive?.contentOrNull,
            episodes = attr["episodeCount"]?.jsonPrimitive?.intOrNull,
            averageScore = attr["averageRating"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()?.let { (it / 10).toInt() },
            popularity = attr["popularityRank"]?.jsonPrimitive?.intOrNull,
            coverImage = CoverImage(
                large = poster?.get("large")?.jsonPrimitive?.contentOrNull,
                medium = poster?.get("medium")?.jsonPrimitive?.contentOrNull
            ),
            bannerImage = cover?.get("large")?.jsonPrimitive?.contentOrNull,
            startDate = attr["startDate"]?.jsonPrimitive?.contentOrNull?.let {
                val p = it.split("-"); com.rei.app.domain.model.FuzzyDate(p.getOrNull(0)?.toIntOrNull(), p.getOrNull(1)?.toIntOrNull(), p.getOrNull(2)?.toIntOrNull())
            },
            genres = emptyList(),
            status = attr["status"]?.jsonPrimitive?.contentOrNull?.let { s ->
                when (s) { "current" -> com.rei.app.domain.model.MediaStatus.RELEASING; "finished" -> com.rei.app.domain.model.MediaStatus.FINISHED; else -> null }
            }
        )
    }
}
