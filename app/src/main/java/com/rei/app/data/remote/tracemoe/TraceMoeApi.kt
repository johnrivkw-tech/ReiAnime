package com.rei.app.data.remote.tracemoe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trace.moe API — Anime scene search by image
 * Upload a screenshot → get anime name, episode, timestamp
 * Docs: https://github.com/soruly/trace.moe-api
 */
@Singleton
class TraceMoeApi @Inject constructor(private val client: OkHttpClient, private val json: Json) {
    companion object {
        const val URL = "https://api.trace.moe/search"
        const val ANILIST_URL = "https://api.trace.moe/search?anilistInfo"
    }

    suspend fun searchByUrl(imageUrl: String): TraceResult = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$ANILIST_URL&url=${java.net.URLEncoder.encode(imageUrl, "UTF-8")}")
            .addHeader("Accept", "application/json")
            .build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("Trace.moe: ${resp.code}")
        parseResult(body)
    }

    suspend fun searchByFile(file: File): TraceResult = withContext(Dispatchers.IO) {
        val mediaType = when (file.extension.lowercase()) { "png" -> "image/png"; "gif" -> "image/gif"; "webp" -> "image/webp"; else -> "image/jpeg" }.toMediaType()
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image", file.name, file.asRequestBody(mediaType)).build()
        val req = Request.Builder().url(ANILIST_URL).addHeader("Accept", "application/json").post(multipartBody).build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("Trace.moe: ${resp.code}")
        parseResult(body)
    }

    suspend fun searchByBytes(bytes: ByteArray, filename: String = "image.jpg"): TraceResult = withContext(Dispatchers.IO) {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image", filename, bytes.toRequestBody("image/jpeg".toMediaType())).build()
        val req = Request.Builder().url(ANILIST_URL).addHeader("Accept", "application/json").post(multipartBody).build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("Trace.moe: ${resp.code}")
        parseResult(body)
    }

    private fun parseResult(body: String): TraceResult {
        val parsed = json.parseToJsonElement(body).jsonObject
        val frameCount = parsed["frameCount"]?.jsonPrimitive?.intOrNull ?: 0
        val rawDocs = parsed["result"]?.jsonArray ?: JsonArray(emptyList())
        val matches = rawDocs.map { doc ->
            val obj = doc.jsonObject
            val anilist = obj["anilist"]?.jsonObject
            val title = anilist?.get("title")?.jsonObject
            val synonyms = anilist?.get("synonyms")?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
            TraceMatch(
                anilistId = anilist?.get("id")?.jsonPrimitive?.intOrNull ?: 0,
                malId = anilist?.get("idMal")?.jsonPrimitive?.intOrNull,
                title = TraceTitle(title?.get("romaji")?.jsonPrimitive?.contentOrNull, title?.get("english")?.jsonPrimitive?.contentOrNull, title?.get("native")?.jsonPrimitive?.contentOrNull),
                synonyms = synonyms,
                isAdult = anilist?.get("isAdult")?.jsonPrimitive?.booleanOrNull ?: false,
                episode = obj["episode"]?.jsonPrimitive?.intOrNull,
                from = obj["from"]?.jsonPrimitive?.floatOrNull ?: 0f,
                to = obj["to"]?.jsonPrimitive?.floatOrNull ?: 0f,
                similarity = obj["similarity"]?.jsonPrimitive?.floatOrNull ?: 0f,
                videoPreview = obj["video"]?.jsonPrimitive?.contentOrNull,
                imagePreview = obj["image"]?.jsonPrimitive?.contentOrNull
            )
        }.sortedByDescending { it.similarity }
        return TraceResult(frameCount, matches)
    }
}

data class TraceResult(val frameCount: Int, val matches: List<TraceMatch>)
data class TraceMatch(
    val anilistId: Int, val malId: Int? = null, val title: TraceTitle,
    val synonyms: List<String> = emptyList(), val isAdult: Boolean = false,
    val episode: Int? = null, val from: Float = 0f, val to: Float = 0f,
    val similarity: Float = 0f, val videoPreview: String? = null, val imagePreview: String? = null
) {
    val displayTitle: String get() = title.english ?: title.romaji ?: title.native ?: "Unknown"
    val similarityPercent: String get() = "${(similarity * 100).toInt()}%"
    val timestamp: String get() { val t = from.toInt(); return "${t / 60}:${(t % 60).toString().padStart(2, '0')}" }
}
data class TraceTitle(val romaji: String? = null, val english: String? = null, val native: String? = null)
