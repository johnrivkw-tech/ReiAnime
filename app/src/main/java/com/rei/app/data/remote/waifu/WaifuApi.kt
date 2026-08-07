package com.rei.app.data.remote.waifu

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

// ═══════════════════════════════════════════
// Waifu.im API — Free, no key needed
// Random anime images for visual flair
// ═══════════════════════════════════════════

@Serializable
data class WaifuResponse(
    val images: List<WaifuImage> = emptyList()
)

@Serializable
data class WaifuImage(
    val url: String = "",
    val source: String = "",
    val dominant_color: String = "#000000",
    val is_nsfw: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val byte_size: Int = 0,
    val tags: List<WaifuTag> = emptyList()
)

@Serializable
data class WaifuTag(
    val tag_id: Int = 0,
    val name: String = "",
    val description: String = "",
    val is_nsfw: Boolean = false
)

@Serializable
data class WaifuTagResponse(
    val tags: List<WaifuTag> = emptyList()
)

class WaifuApi(
    private val client: OkHttpClient,
    private val json: Json
) {
    private val baseUrl = "https://api.waifu.im"

    /** Get random waifu images (sfw by default) */
    suspend fun getRandom(
        count: Int = 1,
        selectedTags: List<String> = emptyList(),
        excludedTags: List<String> = listOf("nsfw"),
        isNsfw: Boolean = false
    ): List<WaifuImage> {
        return try {
            val urlBuilder = StringBuilder("$baseUrl/search/?many=true&is_nsfw=$isNsfw")
            selectedTags.forEach { urlBuilder.append("&included_tags=$it") }
            excludedTags.forEach { urlBuilder.append("&excluded_tags=$it") }
            val request = okhttp3.Request.Builder()
                .url(urlBuilder.toString())
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            json.decodeFromString<WaifuResponse>(body).images.take(count)
        } catch (_: Exception) { emptyList() }
    }

    /** Get available tags for filtering */
    suspend fun getTags(isNsfw: Boolean = false): List<WaifuTag> {
        return try {
            val request = okhttp3.Request.Builder()
                .url("$baseUrl/tags/?is_nsfw=$isNsfw")
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            json.decodeFromString<WaifuTagResponse>(body).tags
        } catch (_: Exception) { emptyList() }
    }

    /** Get a single random waifu image URL — convenient for splash/backgrounds */
    suspend fun getRandomImageUrl(): String? {
        return try {
            getRandom(1).firstOrNull()?.url
        } catch (_: Exception) { null }
    }
}
