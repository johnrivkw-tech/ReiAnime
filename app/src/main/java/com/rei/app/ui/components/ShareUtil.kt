package com.rei.app.ui.components

import android.content.Context
import android.content.Intent
import com.rei.app.domain.model.Anime

/**
 * Share utility for creating beautiful anime share cards.
 * Generates share text with anime info and a link.
 */
object ShareUtil {

    /** Share an anime with a pre-formatted premium card text */
    fun shareAnime(context: Context, anime: Anime) {
        val scoreText = anime.meanScore?.let { "  \u2605 ${it / 10.0}/10" } ?: ""
        val epsText = anime.episodes?.let { "  \u2022 $it episodes" } ?: ""
        val statusText = anime.status?.let { "  \u2022 ${it.name.replace("_", " ")}" } ?: ""
        val genresText = if (anime.genres.isNotEmpty()) "\n${anime.genres.take(4).joinToString(" \u2022 ")}" else ""

        val shareText = buildString {
            appendLine("\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510")
            appendLine("\u2502  Rei \u96F6 \u2014 Anime Card  \u2502")
            appendLine("\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2524")
            appendLine("\u2502 ${anime.title.primary.take(26).padEnd(26)} \u2502")
            anime.title.english?.let { eng ->
                if (eng != anime.title.primary) {
                    appendLine("\u2502 ${eng.take(26).padEnd(26)} \u2502")
                }
            }
            appendLine("\u2502${scoreText.padStart(28)}\u2502")
            appendLine("\u2502${epsText.padStart(28)}\u2502")
            appendLine("\u2502${statusText.padStart(28)}\u2502")
            if (genresText.isNotEmpty()) {
                appendLine("\u2502${genresText.trim().take(26).padStart(28)}\u2502")
            }
            anime.siteUrl?.let { url ->
                appendLine("\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2524")
                appendLine("\u2502 ${url.take(26).padEnd(26)} \u2502")
            }
            appendLine("\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, anime.title.primary)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${anime.title.primary}"))
    }

    /** Quick share with just title + link */
    fun shareQuick(context: Context, anime: Anime) {
        val text = "${anime.title.primary} ${anime.siteUrl ?: ""}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }

    /** Share tracking stats */
    fun shareStats(context: Context, totalAnime: Int, episodesWatched: Int, meanScore: Float) {
        val text = buildString {
            appendLine("My Rei \u96F6 Stats")
            appendLine("\u25C8 $totalAnime anime tracked")
            appendLine("\u25B6 $episodesWatched episodes watched")
            if (meanScore > 0) appendLine("\u2605 ${"%.1f".format(meanScore / 10)} mean score")
            appendLine("\nGet Rei \u96F6 \u2014 Your Anime Companion")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Stats"))
    }
}
