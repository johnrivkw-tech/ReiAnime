package com.rei.app.data.remote.jikan

import com.rei.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JikanApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object { const val URL = "https://api.jikan.moe/v4" }

    private suspend fun get(path: String): JsonObject = withContext(Dispatchers.IO) {
        val resp = client.newCall(Request.Builder().url("$URL/$path").addHeader("Accept","application/json").build()).execute()
        val body = resp.body?.string() ?: return@withContext JsonObject(emptyMap())
        json.parseToJsonElement(body).jsonObject
    }

    suspend fun getTopAnime(page: Int = 1, filter: String = "bypopularity"): List<Anime> =
        get("top/anime?page=$page&filter=$filter&limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    suspend fun getSeasonNow(): List<Anime> =
        get("seasons/now?limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    suspend fun getUpcoming(page: Int = 1): List<Anime> =
        get("seasons/upcoming?page=$page&limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    suspend fun searchAnime(q: String, page: Int = 1): List<Anime> =
        get("anime?q=$q&page=$page&limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    suspend fun getRandomAnime(): Anime? =
        get("random/anime")["data"]?.jsonObject?.let { parseMal(it) }

    suspend fun getAnimeReviews(id: Int): List<MALReview> =
        get("anime/$id/reviews?limit=10")["data"]?.jsonArray?.map { parseRev(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeRecommendations(id: Int): List<Anime> =
        get("anime/$id/recommendations?limit=6")["data"]?.jsonArray?.mapNotNull { it.jsonObject["entry"]?.jsonObject?.let { e -> parseMal(e) } } ?: emptyList()

    suspend fun getRecentNews(page: Int = 1): List<AnimeNews> =
        get("anime?page=$page&order_by=score&sort=desc&limit=12")["data"]?.jsonArray?.mapNotNull { parseNews(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeFull(id: Int): Anime? =
        get("anime/$id/full")["data"]?.jsonObject?.let { parseMal(it) }

    // ── Extended Endpoints ──

    suspend fun getAnimeCharacters(id: Int): List<AnimeCharacter> =
        get("anime/$id/characters")["data"]?.jsonArray?.mapNotNull { parseCharacter(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeStaff(id: Int): List<AnimeStaff> =
        get("anime/$id/staff")["data"]?.jsonArray?.mapNotNull { parseStaff(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeStatistics(id: Int): AnimeStatistics? =
        get("anime/$id/statistics")["data"]?.jsonObject?.let { parseStats(it) }

    suspend fun getAnimeRelations(id: Int): List<AnimeRelation> =
        get("anime/$id/relations")["data"]?.jsonArray?.mapNotNull { parseRelation(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeExternal(id: Int): List<ExternalLink> =
        get("anime/$id/external")["data"]?.jsonArray?.mapNotNull { parseExternal(it.jsonObject) } ?: emptyList()

    suspend fun getAnimeStreaming(id: Int): List<ExternalLink> =
        get("anime/$id/streaming")["data"]?.jsonArray?.mapNotNull { parseExternal(it.jsonObject) } ?: emptyList()

    suspend fun getProducerAnime(producerId: Int, page: Int = 1): List<Anime> =
        get("producers/$producerId?page=$page&limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    suspend fun getGenreAnime(genreId: Int, page: Int = 1): List<Anime> =
        get("genres/anime?genre=$genreId&page=$page&limit=25")["data"]?.jsonArray?.map { parseMal(it.jsonObject) } ?: emptyList()

    private fun parseCharacter(o: JsonObject): AnimeCharacter? {
        val char = o["character"]?.jsonObject ?: return null
        val img = char["images"]?.jsonObject?.get("jpg")?.jsonObject?.get("image_url")?.jsonPrimitive?.contentOrNull
        return AnimeCharacter(
            malId = char["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
            name = char["name"]?.jsonPrimitive?.contentOrNull ?: return null,
            imageUrl = img,
            role = o["role"]?.jsonPrimitive?.contentOrNull ?: ""
        )
    }

    private fun parseStaff(o: JsonObject): AnimeStaff? {
        val person = o["person"]?.jsonObject ?: return null
        val img = person["images"]?.jsonObject?.get("jpg")?.jsonObject?.get("image_url")?.jsonPrimitive?.contentOrNull
        return AnimeStaff(
            malId = person["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
            name = person["name"]?.jsonPrimitive?.contentOrNull ?: return null,
            imageUrl = img,
            positions = o["positions"]?.jsonArray?.mapNotNull { it.jsonPrimitive?.contentOrNull } ?: emptyList()
        )
    }

    private fun parseStats(o: JsonObject): AnimeStatistics {
        val statuses = o["status"]?.jsonObject
        return AnimeStatistics(
            watching = statuses?.get("watching")?.jsonPrimitive?.intOrNull ?: 0,
            completed = statuses?.get("completed")?.jsonPrimitive?.intOrNull ?: 0,
            onHold = statuses?.get("on_hold")?.jsonPrimitive?.intOrNull ?: 0,
            dropped = statuses?.get("dropped")?.jsonPrimitive?.intOrNull ?: 0,
            planToWatch = statuses?.get("plan_to_watch")?.jsonPrimitive?.intOrNull ?: 0,
            total = statuses?.get("total")?.jsonPrimitive?.intOrNull ?: 0,
            scoreDistribution = o["scores"]?.jsonArray?.associate {
                val score = it.jsonObject["score"]?.jsonPrimitive?.intOrNull ?: 0
                val votes = it.jsonObject["votes"]?.jsonPrimitive?.intOrNull ?: 0
                score to votes
            } ?: emptyMap()
        )
    }

    private fun parseRelation(o: JsonObject): AnimeRelation? {
        val entry = o["entry"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null
        return AnimeRelation(
            malId = entry["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
            title = entry["name"]?.jsonPrimitive?.contentOrNull ?: return null,
            type = entry["type"]?.jsonPrimitive?.contentOrNull ?: "",
            relation = o["relation"]?.jsonPrimitive?.contentOrNull ?: ""
        )
    }

    private fun parseExternal(o: JsonObject): ExternalLink? {
        return ExternalLink(
            name = o["name"]?.jsonPrimitive?.contentOrNull ?: return null,
            url = o["url"]?.jsonPrimitive?.contentOrNull ?: return null
        )
    }

    private fun parseNews(o: JsonObject): AnimeNews? {
        val title = o["title"]?.jsonPrimitive?.contentOrNull ?: return null
        val img = o["images"]?.jsonObject?.get("jpg")?.jsonObject?.get("image_url")?.jsonPrimitive?.contentOrNull
        val score = o["score"]?.jsonPrimitive?.floatOrNull
        return AnimeNews(
            malId = o["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
            title = title,
            imageUrl = img,
            score = score,
            synopsis = o["synopsis"]?.jsonPrimitive?.contentOrNull?.take(200),
            genres = o["genres"]?.jsonArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull } ?: emptyList(),
            status = o["status"]?.jsonPrimitive?.contentOrNull ?: "",
            episodes = o["episodes"]?.jsonPrimitive?.intOrNull
        )
    }

    private fun parseMal(o: JsonObject): Anime {
        val img = o["images"]?.jsonObject?.get("jpg")?.jsonObject
        val aired = o["aired"]?.jsonObject?.get("prop")?.jsonObject?.get("from")?.jsonObject
        val score = o["score"]?.jsonPrimitive?.floatOrNull
        return Anime(
            id = o["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
            idMal = o["mal_id"]?.jsonPrimitive?.intOrNull,
            title = AnimeTitle(
                english = o["title_english"]?.jsonPrimitive?.contentOrNull,
                romaji = o["title"]?.jsonPrimitive?.contentOrNull,
                native = o["title_japanese"]?.jsonPrimitive?.contentOrNull,
                userPreferred = o["title"]?.jsonPrimitive?.contentOrNull
            ),
            description = o["synopsis"]?.jsonPrimitive?.contentOrNull,
            episodes = o["episodes"]?.jsonPrimitive?.intOrNull,
            meanScore = score?.let { (it * 10).toInt() },
            popularity = o["popularity"]?.jsonPrimitive?.intOrNull,
            favourites = o["favorites"]?.jsonPrimitive?.intOrNull,
            coverImage = CoverImage(
                extraLarge = img?.get("image_url")?.jsonPrimitive?.contentOrNull,
                large = img?.get("large_image_url")?.jsonPrimitive?.contentOrNull,
                medium = img?.get("small_image_url")?.jsonPrimitive?.contentOrNull
            ),
            genres = o["genres"]?.jsonArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull } ?: emptyList(),
            startDate = aired?.let { FuzzyDate(it["year"]?.jsonPrimitive?.intOrNull, it["month"]?.jsonPrimitive?.intOrNull, it["day"]?.jsonPrimitive?.intOrNull) },
            status = o["status"]?.jsonPrimitive?.contentOrNull?.let { s ->
                when(s) { "Currently Airing" -> MediaStatus.RELEASING; "Finished Airing" -> MediaStatus.FINISHED; "Not yet aired" -> MediaStatus.NOT_YET_RELEASED; else -> null }
            },
            duration = o["duration"]?.jsonPrimitive?.contentOrNull?.let { Regex("\\d+").find(it)?.value?.toIntOrNull() },
            siteUrl = o["url"]?.jsonPrimitive?.contentOrNull,
            trailer = o["trailer"]?.jsonObject?.let { t -> MediaTrailer(t["id"]?.jsonPrimitive?.contentOrNull, t["site"]?.jsonPrimitive?.contentOrNull, t["image_url"]?.jsonPrimitive?.contentOrNull) }
        )
    }

    private fun parseRev(o: JsonObject) = MALReview(
        id = o["mal_id"]?.jsonPrimitive?.intOrNull ?: 0,
        username = o["user"]?.jsonObject?.get("username")?.jsonPrimitive?.contentOrNull ?: "",
        score = o["score"]?.jsonPrimitive?.intOrNull ?: 0,
        review = o["review"]?.jsonPrimitive?.contentOrNull ?: "",
        date = o["date"]?.jsonPrimitive?.contentOrNull ?: "",
        avatar = o["user"]?.jsonObject?.get("images")?.jsonObject?.get("jpg")?.jsonObject?.get("image_url")?.jsonPrimitive?.contentOrNull
    )
}

data class MALReview(val id: Int, val username: String, val score: Int, val review: String, val date: String, val avatar: String? = null)

data class AnimeNews(
    val malId: Int,
    val title: String,
    val imageUrl: String? = null,
    val score: Float? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val status: String = "",
    val episodes: Int? = null
)

data class AnimeCharacter(
    val malId: Int,
    val name: String,
    val imageUrl: String? = null,
    val role: String = ""
)

data class AnimeStaff(
    val malId: Int,
    val name: String,
    val imageUrl: String? = null,
    val positions: List<String> = emptyList()
)

data class AnimeStatistics(
    val watching: Int = 0,
    val completed: Int = 0,
    val onHold: Int = 0,
    val dropped: Int = 0,
    val planToWatch: Int = 0,
    val total: Int = 0,
    val scoreDistribution: Map<Int, Int> = emptyMap()
)

data class AnimeRelation(
    val malId: Int,
    val title: String,
    val type: String,
    val relation: String
)

data class ExternalLink(
    val name: String,
    val url: String
)
