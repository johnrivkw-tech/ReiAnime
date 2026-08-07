package com.rei.app.data.remote.anilist

import com.rei.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AniList GraphQL API Client
 * Public API — no API key needed! Only needs OAuth token for user auth (writing lists).
 * Docs: https://anilist.github.io/ApiV2GraphQLDocs/
 */
@Singleton
class AniListApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object {
        const val URL = "https://graphql.anilist.co"
        const val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize"
        const val TOKEN_URL = "https://anilist.co/api/v2/oauth/token"
        const val REDIRECT_URI = "rei://auth"
        // Client ID only needed for OAuth login flow — public reads work without it
        var CLIENT_ID: String? = null
    }

    private var authToken: String? = null
    fun setAuthToken(token: String?) { authToken = token }

    private suspend fun query(q: String, vars: Map<String, JsonElement> = emptyMap()): JsonObject = withContext(Dispatchers.IO) {
        val body = buildJsonObject { put("query", q); put("variables", buildJsonObject { vars.forEach { (k, v) -> put(k, v) } }) }
        val req = Request.Builder().url(URL).post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Content-Type", "application/json").addHeader("Accept", "application/json")
        authToken?.let { req.addHeader("Authorization", "Bearer $it") }
        val resp = client.newCall(req.build()).execute()
        val responseBody = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("API Error: ${resp.code} - $responseBody")
        val parsed = json.parseToJsonElement(responseBody).jsonObject
        if (parsed.containsKey("errors")) throw Exception("GraphQL Error: ${parsed["errors"]}")
        parsed["data"]?.jsonObject ?: throw Exception("No data in response")
    }

    suspend fun getTrending(page: Int = 1, perPage: Int = 20): Pair<List<Anime>, Boolean> {
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(sort:TRENDING_DESC,type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun getPopularSeason(page: Int = 1, perPage: Int = 20): Pair<List<Anime>, Boolean> {
        val (s, y) = curSeason()
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int,\$s:MediaSeason,\$y:Int){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(season:\$s,seasonYear:\$y,sort:POPULARITY_DESC,type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage), "s" to JsonPrimitive(s.name), "y" to JsonPrimitive(y)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun getUpcoming(page: Int = 1, perPage: Int = 20): Pair<List<Anime>, Boolean> {
        val (s, y) = nextSeason()
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int,\$s:MediaSeason,\$y:Int){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(season:\$s,seasonYear:\$y,sort:POPULARITY_DESC,type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage), "s" to JsonPrimitive(s.name), "y" to JsonPrimitive(y)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun getAllTimePopular(page: Int = 1, perPage: Int = 20): Pair<List<Anime>, Boolean> {
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(sort:POPULARITY_DESC,type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun search(search: String, page: Int = 1, perPage: Int = 25, genre: String? = null, year: Int? = null, season: Season? = null, format: MediaFormat? = null, sort: List<MediaSort> = listOf(MediaSort.SEARCH_MATCH)): Pair<List<Anime>, Boolean> {
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int,\$s:String,\$g:String,\$y:Int,\$sn:MediaSeason,\$f:MediaFormat,\$so:[MediaSort]){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(search:\$s,genre_in:[\$g],seasonYear:\$y,season:\$sn,format:\$f,sort:\$so,type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage), "s" to JsonPrimitive(search), "so" to JsonArray(sort.map { JsonPrimitive(it.name) }), "g" to (genre?.let { JsonPrimitive(it) } ?: JsonNull), "y" to (year?.let { JsonPrimitive(it) } ?: JsonNull), "sn" to (season?.let { JsonPrimitive(it.name) } ?: JsonNull), "f" to (format?.let { JsonPrimitive(it.name) } ?: JsonNull)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun getDetail(id: Int): Anime {
        val d = query("${FRAG_DQ}query(\$id:Int){Media(id:\$id,type:ANIME){...df}}", mapOf("id" to JsonPrimitive(id)))
        return parseMedia(d["Media"]!!.jsonObject)
    }

    suspend fun getSeasonal(season: Season, year: Int, page: Int = 1, perPage: Int = 30, sort: MediaSort = MediaSort.POPULARITY_DESC): Pair<List<Anime>, Boolean> {
        val d = query("${FRAG_Q}query(\$p:Int,\$pp:Int,\$s:MediaSeason,\$y:Int,\$so:MediaSort){Page(page:\$p,perPage:\$pp){pageInfo{hasNextPage}media(season:\$s,seasonYear:\$y,sort:[\$so],type:ANIME,isAdult:false){...f}}}", mapOf("p" to JsonPrimitive(page), "pp" to JsonPrimitive(perPage), "s" to JsonPrimitive(season.name), "y" to JsonPrimitive(year), "so" to JsonPrimitive(sort.name)))
        val p = d["Page"]!!.jsonObject; return parseList(p) to p["pageInfo"]!!.jsonObject["hasNextPage"]!!.jsonPrimitive.boolean
    }

    suspend fun getViewer(): User? {
        val d = query("query{Viewer{id name avatar{large medium}bannerImage about statistics{anime{count meanScore minutesWatched}manga{count meanScore chaptersRead volumesRead}}siteUrl}}")
        val v = d["Viewer"]?.jsonObject ?: return null
        return User(v["id"]!!.jsonPrimitive.int, v["name"]!!.jsonPrimitive.content, UserAvatar(v["avatar"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull, v["avatar"]?.jsonObject?.get("medium")?.jsonPrimitive?.contentOrNull), v["bannerImage"]?.jsonPrimitive?.contentOrNull, v["about"]?.jsonPrimitive?.contentOrNull, UserStatistics(v["statistics"]?.jsonObject?.get("anime")?.jsonObject?.let { a -> AnimeStatistics(a["count"]?.jsonPrimitive?.intOrNull ?: 0, a["meanScore"]?.jsonPrimitive?.floatOrNull ?: 0f, a["minutesWatched"]?.jsonPrimitive?.intOrNull ?: 0) }, v["statistics"]?.jsonObject?.get("manga")?.jsonObject?.let { m -> MangaStatistics(m["count"]?.jsonPrimitive?.intOrNull ?: 0, m["meanScore"]?.jsonPrimitive?.floatOrNull ?: 0f) }), v["siteUrl"]?.jsonPrimitive?.contentOrNull)
    }

    suspend fun updateEntry(mediaId: Int, status: MediaListStatus, score: Float? = null, progress: Int? = null) {
        query("mutation(\$mid:Int,\$st:MediaListStatus,\$sc:Float,\$pr:Int){SaveMediaListEntry(mediaId:\$mid,status:\$st,score:\$sc,progress:\$pr){id}}", mapOf("mid" to JsonPrimitive(mediaId), "st" to JsonPrimitive(status.name), "sc" to (score?.let { JsonPrimitive(it) } ?: JsonNull), "pr" to (progress?.let { JsonPrimitive(it) } ?: JsonNull)))
    }

    /** Look up AniList ID from MAL ID — cross-reference for sync */
    suspend fun lookupByMalId(idMal: Int): Anime? {
        val d = query("${FRAG_Q}query(\$idMal:Int){Media(idMal:\$idMal,type:ANIME){...f}}", mapOf("idMal" to JsonPrimitive(idMal)))
        val media = d["Media"]?.jsonObject ?: return null
        return parseMedia(media)
    }

    /** Get user's anime list from AniList for sync */
    suspend fun getUserAnimeList(userId: Int, status: MediaListStatus? = null): List<AniListListItem> {
        val statusArg = status?.let { ",status:\$st" } ?: ""
        val vars = mutableMapOf<String, JsonElement>("uid" to JsonPrimitive(userId))
        if (status != null) vars["st"] = JsonPrimitive(status.name)
        val d = query("query(\$uid:Int${if (status != null) ",\$st:MediaListStatus" else ""}){MediaListCollection(userId:\$uid,type:ANIME${statusArg}){lists{entries{mediaId status score progress media{id idMal title{romaji english native userPreferred}coverImage{extraLarge large medium}episodes}updatedAt}}}}", vars)
        val lists = d["MediaListCollection"]?.jsonObject?.get("lists")?.jsonArray ?: return emptyList()
        return lists.flatMap { list ->
            (list.jsonObject["entries"]?.jsonArray ?: emptyList()).map { entry ->
                val e = entry.jsonObject
                val m = e["media"]?.jsonObject
                AniListListItem(
                    mediaId = e["mediaId"]?.jsonPrimitive?.intOrNull ?: 0,
                    status = try { MediaListStatus.valueOf(e["status"]?.jsonPrimitive?.contentOrNull ?: "") } catch (_: Exception) { MediaListStatus.CURRENT },
                    score = e["score"]?.jsonPrimitive?.floatOrNull ?: 0f,
                    progress = e["progress"]?.jsonPrimitive?.intOrNull ?: 0,
                    idMal = m?.get("idMal")?.jsonPrimitive?.intOrNull,
                    title = m?.let { parseMedia(it).title } ?: AnimeTitle(),
                    coverImage = m?.let { parseMedia(it).coverImage } ?: CoverImage(),
                    episodes = m?.get("episodes")?.jsonPrimitive?.intOrNull,
                    updatedAt = e["updatedAt"]?.jsonPrimitive?.longOrNull ?: 0L
                )
            }
        }
    }

    private fun curSeason(): Pair<Season, Int> { val c = java.util.Calendar.getInstance(); val m = c.get(2); val y = c.get(1); return (when(m) { in 0..2->Season.WINTER; in 3..5->Season.SPRING; in 6..8->Season.SUMMER; else->Season.FALL }) to y }
    private fun nextSeason(): Pair<Season, Int> { val (s,y) = curSeason(); return when(s) { Season.WINTER->Season.SPRING to y; Season.SPRING->Season.SUMMER to y; Season.SUMMER->Season.FALL to y; Season.FALL->Season.WINTER to (y+1) } }
    private fun parseList(p: JsonObject) = p["media"]!!.jsonArray.map { parseMedia(it.jsonObject) }
    private fun parseMedia(o: JsonObject) = Anime(
        id = o["id"]!!.jsonPrimitive.int, idMal = o["idMal"]?.jsonPrimitive?.intOrNull,
        title = AnimeTitle(o["title"]?.jsonObject?.get("romaji")?.jsonPrimitive?.contentOrNull, o["title"]?.jsonObject?.get("english")?.jsonPrimitive?.contentOrNull, o["title"]?.jsonObject?.get("native")?.jsonPrimitive?.contentOrNull, o["title"]?.jsonObject?.get("userPreferred")?.jsonPrimitive?.contentOrNull),
        format = o["format"]?.jsonPrimitive?.contentOrNull?.let { runCatching { MediaFormat.valueOf(it) }.getOrNull() },
        status = o["status"]?.jsonPrimitive?.contentOrNull?.let { runCatching { MediaStatus.valueOf(it) }.getOrNull() },
        description = o["description"]?.jsonPrimitive?.contentOrNull,
        startDate = o["startDate"]?.jsonObject?.let { FuzzyDate(it["year"]?.jsonPrimitive?.intOrNull, it["month"]?.jsonPrimitive?.intOrNull, it["day"]?.jsonPrimitive?.intOrNull) },
        season = o["season"]?.jsonPrimitive?.contentOrNull?.let { runCatching { Season.valueOf(it) }.getOrNull() },
        seasonYear = o["seasonYear"]?.jsonPrimitive?.intOrNull, episodes = o["episodes"]?.jsonPrimitive?.intOrNull,
        duration = o["duration"]?.jsonPrimitive?.intOrNull,
        coverImage = CoverImage(o["coverImage"]?.jsonObject?.get("extraLarge")?.jsonPrimitive?.contentOrNull, o["coverImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull, o["coverImage"]?.jsonObject?.get("medium")?.jsonPrimitive?.contentOrNull, o["coverImage"]?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull),
        bannerImage = o["bannerImage"]?.jsonPrimitive?.contentOrNull,
        genres = o["genres"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
        averageScore = o["averageScore"]?.jsonPrimitive?.intOrNull, meanScore = o["meanScore"]?.jsonPrimitive?.intOrNull,
        popularity = o["popularity"]?.jsonPrimitive?.intOrNull, trending = o["trending"]?.jsonPrimitive?.intOrNull,
        favourites = o["favourites"]?.jsonPrimitive?.intOrNull, siteUrl = o["siteUrl"]?.jsonPrimitive?.contentOrNull,
        nextAiringEpisode = o["nextAiringEpisode"]?.jsonObject?.let { AiringSchedule(it["airingAt"]!!.jsonPrimitive.long, it["timeUntilAiring"]!!.jsonPrimitive.int, it["episode"]!!.jsonPrimitive.int) },
        isFavourite = o["isFavourite"]?.jsonPrimitive?.booleanOrNull ?: false,
        mediaListEntry = o["mediaListEntry"]?.jsonObject?.let { e -> MediaList(e["id"]!!.jsonPrimitive.int, MediaListStatus.valueOf(e["status"]!!.jsonPrimitive.content), e["score"]?.jsonPrimitive?.floatOrNull ?: 0f, e["progress"]?.jsonPrimitive?.intOrNull ?: 0) }
    )

    private val FRAG_Q = "fragment f on Media{id idMal title{romaji english native userPreferred}type format status startDate{year month day}season seasonYear episodes duration coverImage{extraLarge large medium color}bannerImage genres averageScore meanScore popularity trending favourites siteUrl isFavourite nextAiringEpisode{airingAt timeUntilAiring episode}mediaListEntry{id status score progress}}"
    private val FRAG_DQ = "fragment df on Media{id idMal title{romaji english native userPreferred}type format status description(asHtml:false)startDate{year month day}endDate{year month day}season seasonYear episodes duration chapters volumes coverImage{extraLarge large medium color}bannerImage genres tags{id name rank isMediaSpoiler}studios{isMain node{id name}}averageScore meanScore popularity trending favourites siteUrl trailer{id site thumbnail}isFavourite nextAiringEpisode{airingAt timeUntilAiring episode}mediaListEntry{id status score progress repeat private notes startedAt{year month day} completedAt{year month day}}recommendations(perPage:6){nodes{media{id title{romaji english native userPreferred}coverImage{extraLarge large medium color}meanScore}}}}"
}
