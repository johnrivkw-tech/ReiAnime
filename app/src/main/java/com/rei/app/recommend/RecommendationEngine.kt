package com.rei.app.recommend

import com.rei.app.data.local.TrackingEntity
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaListStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local AI-powered recommendation engine.
 * Uses content-based filtering with weighted genre Jaccard similarity,
 * studio affinity, format preference, score correlation, and popularity decay.
 *
 * No API key required — runs entirely on-device with cached AniList data.
 */
@Singleton
class RecommendationEngine @Inject constructor(
    private val repo: AnimeRepository
) {
    /**
     * A recommendation with match percentage and reasoning.
     */
    data class Recommendation(
        val anime: Anime,
        val matchPercent: Int,
        val reasons: List<String>
    )

    /**
     * Generate personalized recommendations based on the user's tracking history.
     *
     * @param limit Max recommendations to return
     * @param minScore Minimum mean score filter for recommendations
     */
    suspend fun recommend(limit: Int = 20, minScore: Int = 60): List<Recommendation> {
        // 1. Build user profile from tracking
        val tracking = repo.getAllTracking().first()
        if (tracking.size < 2) return emptyList()

        val profile = buildUserProfile(tracking)

        // 2. Get candidate anime from multiple sources
        val candidates = fetchCandidates().filter { candidate ->
            // Exclude already tracked
            tracking.none { it.mediaId == candidate.id } &&
            // Minimum score threshold
            (candidate.meanScore ?: 0) >= minScore
        }.distinctBy { it.id }

        if (candidates.isEmpty()) return emptyList()

        // 3. Score each candidate against user profile
        val scored = candidates.mapNotNull { candidate ->
            val (score, reasons) = scoreCandidate(candidate, profile)
            if (score > 0.15) {
                Recommendation(candidate, (score * 100).toInt().coerceIn(1, 99), reasons)
            } else null
        }

        // 4. Sort by match score, diversify genres, return top N
        return diversify(scored.sortedByDescending { it.matchPercent }, limit)
    }

    /**
     * "Because you liked X" — find anime similar to a specific title.
     */
    suspend fun similarTo(animeId: Int, limit: Int = 10): List<Recommendation> {
        val source = try {
            var result: Anime? = null
            repo.getAnimeDetail(animeId).collect { result = it }
            result ?: return emptyList()
        } catch (_: Exception) { return emptyList() }

        val candidates = fetchCandidates().filter { it.id != animeId && (it.meanScore ?: 0) >= 50 }
            .distinctBy { it.id }

        val sourceGenres = source.genres.toSet()
        val sourceTags = source.tags.map { it.name }.toSet()
        val sourceStudio = source.studios.filter { it.isAnimationStudio }.map { it.name }.toSet()

        return candidates.mapNotNull { candidate ->
            val genreSim = jaccard(sourceGenres, candidate.genres.toSet())
            val tagSim = jaccard(sourceTags, candidate.tags.map { it.name }.toSet())
            val studioBonus = if (candidate.studios.any { it.isAnimationStudio && it.name in sourceStudio }) 0.15 else 0.0
            val score = genreSim * 0.45 + tagSim * 0.30 + studioBonus + (genreSim * tagSim * 0.1)

            if (score > 0.1) {
                val reasons = mutableListOf<String>()
                val sharedGenres = sourceGenres.intersect(candidate.genres.toSet())
                if (sharedGenres.isNotEmpty()) reasons.add("Shared: ${sharedGenres.take(3).joinToString(", ")}")
                if (studioBonus > 0) reasons.add("Same studio")
                Recommendation(candidate, (score * 100).toInt().coerceIn(5, 95), reasons)
            } else null
        }.sortedByDescending { it.matchPercent }.take(limit)
    }

    // ═══════════════════════════════════════════
    // Internal
    // ═══════════════════════════════════════════

    private data class UserProfile(
        val genreWeights: Map<String, Double>,
        val tagWeights: Map<String, Double>,
        val studioWeights: Map<String, Double>,
        val formatWeights: Map<String, Double>,
        val avgScore: Double,
        val topGenres: Set<String>,
        val completedCount: Int,
        val totalTracked: Int
    )

    private suspend fun buildUserProfile(tracking: List<TrackingEntity>): UserProfile {
        val genreW = mutableMapOf<String, Double>()
        val tagW = mutableMapOf<String, Double>()
        val studioW = mutableMapOf<String, Double>()
        val formatW = mutableMapOf<String, Double>()
        var totalScore = 0.0
        var scoredCount = 0

        tracking.forEach { entry ->
            // Weight by score: higher-rated anime contribute more
            val weight = when {
                entry.score >= 8f -> 3.0
                entry.score >= 6f -> 2.0
                entry.score > 0f -> 1.0
                entry.status == "COMPLETED" -> 1.5
                entry.status == "CURRENT" -> 1.2
                entry.status == "PLANNING" -> 0.5
                entry.status == "DROPPED" -> -0.5  // Negative weight for dropped
                else -> 0.3
            }

            if (entry.score > 0) {
                totalScore += entry.score.toDouble()
                scoredCount++
            }

            // We accumulate what we can from tracking notes (genre hints)
            // The real scoring happens when we cross-reference with AniList detail
            // For now, we use the score weight as a general affinity signal
        }

        // Fetch full details for top-scored tracking entries to build real profile
        // (This is the "AI" part — learning preferences from actual anime metadata)
        val topEntries = tracking.sortedByDescending { it.score }.take(30)
        topEntries.forEach { entry ->
            try {
                var anime: Anime? = null
                repo.getAnimeDetail(entry.mediaId).collect { anime = it }
                val a = anime ?: return@forEach

                val w = when {
                    entry.score >= 8f -> 3.0
                    entry.score >= 6f -> 2.0
                    entry.score > 0f -> 1.0
                    entry.status == "COMPLETED" -> 1.5
                    entry.status == "CURRENT" -> 1.2
                    entry.status == "DROPPED" -> -0.5
                    else -> 0.3
                }

                a.genres.forEach { g ->
                    genreW[g] = (genreW[g] ?: 0.0) + w
                }
                a.tags.take(5).forEach { t ->
                    tagW[t.name] = (tagW[t.name] ?: 0.0) + w * 0.6
                }
                a.studios.filter { it.isAnimationStudio }.forEach { s ->
                    studioW[s.name] = (studioW[s.name] ?: 0.0) + w * 0.8
                }
                a.format?.let { f ->
                    val key = f.name
                    formatW[key] = (formatW[key] ?: 0.0) + w * 0.4
                }
            } catch (_: Exception) { }
        }

        // Normalize weights
        val maxGenre = genreW.values.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0
        val normGenres = genreW.mapValues { it.value / maxGenre }

        return UserProfile(
            genreWeights = normGenres,
            tagWeights = tagW,
            studioWeights = studioW,
            formatWeights = formatW,
            avgScore = if (scoredCount > 0) totalScore / scoredCount else 7.0,
            topGenres = normGenres.filter { it.value > 0.5 }.keys,
            completedCount = tracking.count { it.status == "COMPLETED" },
            totalTracked = tracking.size
        )
    }

    private suspend fun fetchCandidates(): List<Anime> {
        val results = mutableListOf<Anime>()
        try {
            repo.getTrending(1).collect { results.addAll(it.first) }
        } catch (_: Exception) {}
        try {
            repo.getPopularThisSeason(1).collect { results.addAll(it.first) }
        } catch (_: Exception) {}
        try {
            repo.getAllTimePopular(1).collect { results.addAll(it.first) }
        } catch (_: Exception) {}
        return results.distinctBy { it.id }
    }

    private fun scoreCandidate(candidate: Anime, profile: UserProfile): Pair<Double, List<String>> {
        val reasons = mutableListOf<String>()
        var score = 0.0

        // 1. Genre affinity (40% weight) — the strongest signal
        var genreScore = 0.0
        val matchedGenres = mutableListOf<String>()
        candidate.genres.forEach { genre ->
            val weight = profile.genreWeights[genre] ?: 0.0
            if (weight > 0.3) {
                genreScore += weight
                matchedGenres.add(genre)
            } else if (weight < -0.2) {
                genreScore += weight * 0.5  // Penalty for disliked genres
            }
        }
        // Normalize by candidate genre count to avoid bias toward multi-genre anime
        if (candidate.genres.isNotEmpty()) {
            genreScore /= candidate.genres.size
        }
        score += genreScore * 0.40
        if (matchedGenres.isNotEmpty()) {
            reasons.add("You love ${matchedGenres.take(2).joinToString(" & ")}")
        }

        // 2. Tag affinity (20% weight)
        var tagScore = 0.0
        candidate.tags.take(8).forEach { tag ->
            val weight = profile.tagWeights[tag.name] ?: 0.0
            if (weight > 0.5) tagScore += weight
        }
        if (candidate.tags.isNotEmpty()) tagScore /= candidate.tags.size.coerceAtLeast(1)
        score += tagScore * 0.20

        // 3. Studio affinity (15% weight)
        val studioBonus = candidate.studios.filter { it.isAnimationStudio }.any { s ->
            (profile.studioWeights[s.name] ?: 0.0) > 1.0
        }
        if (studioBonus) {
            score += 0.15
            val matched = candidate.studios.filter { it.isAnimationStudio }.map { it.name }
                .firstOrNull { (profile.studioWeights[it] ?: 0.0) > 1.0 }
            if (matched != null) reasons.add("Studio: $matched")
        }

        // 4. Format preference (10% weight)
        candidate.format?.let { format ->
            val fw = profile.formatWeights[format.name] ?: 0.0
            if (fw > 1.0) {
                score += 0.10
                reasons.add("Matches your format preference")
            }
        }

        // 5. Score alignment (10% weight) — recommend anime with similar quality tier
        candidate.meanScore?.let { ms ->
            val diff = kotlin.math.abs(ms / 100.0 - profile.avgScore / 10.0)
            if (diff < 1.0) score += 0.10 * (1.0 - diff)
        }

        // 6. Popularity decay (5% weight) — slight boost for less-known gems
        candidate.popularity?.let { pop ->
            if (pop < 50000) score += 0.05
            if (pop < 10000) {
                score += 0.03
                reasons.add("Hidden gem")
            }
        }

        return score.coerceIn(0.0, 1.0) to reasons.take(3)
    }

    /** Jaccard similarity between two sets */
    private fun <T> jaccard(a: Set<T>, b: Set<T>): Double {
        if (a.isEmpty() && b.isEmpty()) return 0.0
        val intersection = a.intersect(b).size
        val union = a.union(b).size
        return intersection.toDouble() / union.toDouble()
    }

    /** Diversify results to avoid genre repetition */
    private fun diversify(sorted: List<Recommendation>, limit: Int): List<Recommendation> {
        val result = mutableListOf<Recommendation>()
        val genreCount = mutableMapOf<String, Int>()

        for (rec in sorted) {
            if (result.size >= limit) break

            // Allow max 3 recommendations per primary genre
            val primaryGenre = rec.anime.genres.firstOrNull() ?: ""
            val count = genreCount[primaryGenre] ?: 0
            if (count >= 3) continue

            result.add(rec)
            genreCount[primaryGenre] = count + 1
        }

        return result
    }
}
