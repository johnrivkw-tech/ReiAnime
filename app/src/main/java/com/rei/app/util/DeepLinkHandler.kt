package com.rei.app.util

import android.content.Intent
import android.net.Uri

/**
 * Handles deep links from external sources:
 * - anilist.co/anime/{id}
 * - myanimelist.net/anime/{id}
 * - www.myanimelist.net/anime/{id}
 * - rei://anime/{id}
 * - rei://anilist-auth-callback?code=...
 * - rei://mal-auth-callback?code=...
 */
object DeepLinkHandler {

    sealed class DeepLinkResult {
        data class AnimeDetail(val animeId: Int, val source: String) : DeepLinkResult()
        data class AniListAuth(val uri: Uri) : DeepLinkResult()
        data class MalAuth(val uri: Uri) : DeepLinkResult()
        data object Unhandled : DeepLinkResult()
    }

    /** Parse an incoming intent and extract a deep link result */
    fun resolve(intent: Intent): DeepLinkResult {
        val uri = intent.data ?: return DeepLinkResult.Unhandled
        return resolve(uri)
    }

    /** Parse a URI and extract a deep link result */
    fun resolve(uri: Uri): DeepLinkResult {
        val scheme = uri.scheme ?: return DeepLinkResult.Unhandled
        val host = uri.host ?: ""

        // AniList OAuth callback: rei://anilist-auth-callback?code=...
        if (scheme == "rei" && host == "anilist-auth-callback") {
            return DeepLinkResult.AniListAuth(uri)
        }

        // MAL OAuth callback: rei://mal-auth-callback?code=...
        if (scheme == "rei" && host == "mal-auth-callback") {
            return DeepLinkResult.MalAuth(uri)
        }

        // Rei internal deep link: rei://anime/12345
        if (scheme == "rei" && host == "anime") {
            val id = uri.pathSegments.firstOrNull()?.toIntOrNull()
            if (id != null) return DeepLinkResult.AnimeDetail(id, "rei")
        }

        // AniList web URL: https://anilist.co/anime/12345
        if ((scheme == "https" || scheme == "http") && host == "anilist.co") {
            val segments = uri.pathSegments
            if (segments.size >= 2 && segments[0] == "anime") {
                val id = segments[1].toIntOrNull()
                if (id != null) return DeepLinkResult.AnimeDetail(id, "anilist")
            }
        }

        // MAL web URL: https://myanimelist.net/anime/12345 or https://www.myanimelist.net/anime/12345
        if ((scheme == "https" || scheme == "http") &&
            (host == "myanimelist.net" || host == "www.myanimelist.net")) {
            val segments = uri.pathSegments
            if (segments.size >= 2 && segments[0] == "anime") {
                val id = segments[1].toIntOrNull()
                if (id != null) return DeepLinkResult.AnimeDetail(id, "mal")
            }
        }

        return DeepLinkResult.Unhandled
    }

    /** Build a shareable Rei deep link for an anime */
    fun buildAnimeLink(anilistId: Int): String = "rei://anime/$anilistId"

    /** Build a shareable AniList web URL for an anime */
    fun buildAnilistWebLink(anilistId: Int): String = "https://anilist.co/anime/$anilistId"

    /** Build a shareable MAL web URL for an anime */
    fun buildMalWebLink(malId: Int): String = "https://myanimelist.net/anime/$malId"
}
