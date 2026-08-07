package com.rei.app.data.remote.shikimori

import com.rei.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShikimoriApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object { const val URL = "https://shikimori.one/api" }

    private suspend fun get(path: String): JsonElement = withContext(Dispatchers.IO) {
        val resp = client.newCall(Request.Builder().url("$URL/$path").addHeader("Accept","application/json").build()).execute()
        val body = resp.body?.string() ?: return@withContext JsonNull
        json.parseToJsonElement(body)
    }

    suspend fun getTopAnime(limit: Int = 20): List<Anime> =
        get("animes?order=ranked&limit=$limit").jsonArray.map { parseShiki(it.jsonObject) }

    suspend fun getFranchise(id: Int): List<Anime> =
        get("animes/$id/franchise").jsonArray.mapNotNull { it.jsonObject["anime"]?.jsonObject?.let { a -> parseShiki(a) } }

    suspend fun getRoles(id: Int): List<ShikiRole> =
        get("animes/$id/roles").jsonArray.take(8).map { parseRole(it.jsonObject) }

    suspend fun getRelated(id: Int): List<Anime> =
        get("animes/$id/related").jsonArray.mapNotNull { it.jsonObject["anime"]?.jsonObject?.let { a -> parseShiki(a) } }

    private fun parseShiki(o: JsonObject): Anime {
        val score = o["score"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull()
        return Anime(
            id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
            title = AnimeTitle(
                romaji = o["name"]?.jsonPrimitive?.contentOrNull,
                english = o["english"]?.jsonPrimitive?.contentOrNull,
                native = o["russian"]?.jsonPrimitive?.contentOrNull,
                userPreferred = o["name"]?.jsonPrimitive?.contentOrNull
            ),
            description = o["description"]?.jsonPrimitive?.contentOrNull,
            episodes = o["episodes_aired"]?.jsonPrimitive?.intOrNull ?: o["episodes"]?.jsonPrimitive?.intOrNull,
            meanScore = score?.let { (it * 10).toInt() },
            coverImage = CoverImage(
                large = o["image"]?.jsonObject?.get("original")?.jsonPrimitive?.contentOrNull?.let { "https://shikimori.one$it" },
                medium = o["image"]?.jsonObject?.get("preview")?.jsonPrimitive?.contentOrNull?.let { "https://shikimori.one$it" }
            ),
            genres = o["genres"]?.jsonArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull } ?: emptyList(),
            status = o["status"]?.jsonPrimitive?.contentOrNull?.let { s ->
                when(s) { "ongoing" -> MediaStatus.RELEASING; "released" -> MediaStatus.FINISHED; "anons" -> MediaStatus.NOT_YET_RELEASED; else -> null }
            }
        )
    }

    private fun parseRole(o: JsonObject) = ShikiRole(
        character = o["character"]?.jsonObject?.let { c -> ShikiCharacter(c["name"]?.jsonPrimitive?.contentOrNull ?: "", c["russian"]?.jsonPrimitive?.contentOrNull, c["image"]?.jsonObject?.get("original")?.jsonPrimitive?.contentOrNull?.let { "https://shikimori.one$it" }) },
        role = o["roles"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.contentOrNull ?: ""
    )
}

data class ShikiRole(val character: ShikiCharacter?, val role: String)
data class ShikiCharacter(val name: String, val russian: String? = null, val image: String? = null)
