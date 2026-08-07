package com.rei.app.data.remote.mangadex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MangaDex v5 API — free, no API key required.
 * Supports manga search, detail, chapter listing, and cover art.
 */
@Singleton
class MangaDexApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object { const val URL = "https://api.mangadex.org" }

    private suspend fun get(path: String): JsonObject = withContext(Dispatchers.IO) {
        val resp = client.newCall(
            Request.Builder().url("$URL/$path")
                .addHeader("Accept", "application/json")
                .build()
        ).execute()
        val body = resp.body?.string() ?: return@withContext JsonObject(emptyMap())
        json.parseToJsonElement(body).jsonObject
    }

    suspend fun searchManga(query: String, limit: Int = 10): List<MangaDexEntry> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val result = get("manga?title=$encoded&limit=$limit&includes[]=cover_art&contentRating[]=safe")
        return result["data"]?.jsonArray?.mapNotNull { parseManga(it.jsonObject) } ?: emptyList()
    }

    suspend fun getMangaDetail(id: String): MangaDexEntry? {
        val result = get("manga/$id?includes[]=cover_art&includes[]=author&includes[]=artist")
        return result["data"]?.jsonObject?.let { parseManga(it) }
    }

    suspend fun getMangaChapters(id: String, limit: Int = 20): List<MangaDexChapter> {
        val result = get("manga/$id/feed?limit=$limit&translatedLanguage[]=en&order[chapter]=asc")
        return result["data"]?.jsonArray?.mapNotNull { parseChapter(it.jsonObject) } ?: emptyList()
    }

    suspend fun getTrendingManga(limit: Int = 15): List<MangaDexEntry> {
        val result = get("manga?limit=$limit&order[followedCount]=desc&includes[]=cover_art&contentRating[]=safe")
        return result["data"]?.jsonArray?.mapNotNull { parseManga(it.jsonObject) } ?: emptyList()
    }

    suspend fun getRecentlyUpdated(limit: Int = 15): List<MangaDexEntry> {
        val result = get("manga?limit=$limit&order[updatedAt]=desc&includes[]=cover_art&contentRating[]=safe")
        return result["data"]?.jsonArray?.mapNotNull { parseManga(it.jsonObject) } ?: emptyList()
    }

    /** Build cover art URL from MangaDex manga ID and cover file */
    fun coverUrl(mangaId: String, fileName: String): String =
        "https://uploads.mangadex.org/covers/$mangaId/$fileName.512.jpg"

    private fun parseManga(o: JsonObject): MangaDexEntry? {
        val id = o["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val attrs = o["attributes"]?.jsonObject ?: return null
        val titleObj = attrs["title"]?.jsonObject
        val title = titleObj?.get("en")?.jsonPrimitive?.contentOrNull
            ?: titleObj?.entries?.firstOrNull()?.value?.jsonPrimitive?.contentOrNull
            ?: return null
        val desc = attrs["description"]?.jsonObject?.get("en")?.jsonPrimitive?.contentOrNull
        val status = attrs["status"]?.jsonPrimitive?.contentOrNull ?: ""
        val year = attrs["year"]?.jsonPrimitive?.intOrNull
        val tags = attrs["tags"]?.jsonArray?.mapNotNull { it.jsonObject["attributes"]?.jsonObject?.get("name")?.jsonObject?.get("en")?.jsonPrimitive?.contentOrNull } ?: emptyList()
        val rating = attrs["contentRating"]?.jsonPrimitive?.contentOrNull ?: "safe"

        // Extract cover art filename from relationships
        var coverFileName: String? = null
        o["relationships"]?.jsonArray?.forEach { rel ->
            if (rel.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "cover_art") {
                coverFileName = rel.jsonObject["attributes"]?.jsonObject?.get("fileName")?.jsonPrimitive?.contentOrNull
            }
        }

        val authors = o["relationships"]?.jsonArray?.filter {
            it.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "author"
        }?.mapNotNull { it.jsonObject["attributes"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull } ?: emptyList()

        return MangaDexEntry(
            id = id,
            title = title,
            description = desc?.take(300),
            coverFileName = coverFileName,
            status = status,
            year = year,
            tags = tags,
            contentRating = rating,
            authors = authors,
            coverUrl = if (coverFileName != null) coverUrl(id, coverFileName!!) else null
        )
    }

    private fun parseChapter(o: JsonObject): MangaDexChapter? {
        val id = o["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val attrs = o["attributes"]?.jsonObject ?: return null
        return MangaDexChapter(
            id = id,
            chapter = attrs["chapter"]?.jsonPrimitive?.contentOrNull ?: "",
            title = attrs["title"]?.jsonPrimitive?.contentOrNull ?: "",
            pages = attrs["pages"]?.jsonPrimitive?.intOrNull ?: 0,
            translatedLanguage = attrs["translatedLanguage"]?.jsonPrimitive?.contentOrNull ?: "",
            publishAt = attrs["publishAt"]?.jsonPrimitive?.contentOrNull ?: ""
        )
    }
}

data class MangaDexEntry(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverFileName: String? = null,
    val status: String = "",
    val year: Int? = null,
    val tags: List<String> = emptyList(),
    val contentRating: String = "safe",
    val authors: List<String> = emptyList(),
    val coverUrl: String? = null
)

data class MangaDexChapter(
    val id: String,
    val chapter: String,
    val title: String,
    val pages: Int,
    val translatedLanguage: String,
    val publishAt: String
)
