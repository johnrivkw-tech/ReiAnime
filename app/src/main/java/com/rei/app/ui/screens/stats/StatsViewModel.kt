package com.rei.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.local.TrackingEntity
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    init {
        // Load stats from local tracking DB (real Room data)
        viewModelScope.launch {
            repo.getAllTracking().collect { entries ->
                val statusCounts = entries.groupingBy { it.status }.eachCount()
                val scoreDist = entries.filter { it.score > 0 }.groupingBy { (it.score / 10).toInt() }.eachCount()
                // Genre breakdown from cached anime — use title keywords as proxy
                val genreMap = mutableMapOf<String, Int>()
                entries.forEach { entry ->
                    // Extract genre hints from notes if available
                    entry.notes?.split(",")?.forEach { genre ->
                        val trimmed = genre.trim()
                        if (trimmed.isNotBlank()) genreMap[trimmed] = (genreMap[trimmed] ?: 0) + 1
                    }
                }
                // Also populate with status-based categories for visual richness
                if (genreMap.isEmpty()) {
                    statusCounts.forEach { (status, count) ->
                        val label = when (status) { "CURRENT" -> "Watching"; "COMPLETED" -> "Completed"; "PLANNING" -> "Planning"; "DROPPED" -> "Dropped"; "PAUSED" -> "On Hold"; "REPEATING" -> "Rewatching"; else -> status }
                        genreMap[label] = count
                    }
                }
                val avgScore = if (entries.isNotEmpty()) entries.filter { it.score > 0 }.map { it.score }.average().toFloat() else 0f
                val totalEps = entries.sumOf { it.progress }
                val totalMinutes = totalEps * 24 // rough estimate: 24 min per episode
                val daysWatched = if (totalMinutes > 0) totalMinutes / 1440 else 0

                _stats.value = UserStats(
                    totalCount = entries.size,
                    watchingCount = statusCounts["CURRENT"] ?: 0,
                    completedCount = statusCounts["COMPLETED"] ?: 0,
                    planningCount = statusCounts["PLANNING"] ?: 0,
                    droppedCount = statusCounts["DROPPED"] ?: 0,
                    pausedCount = statusCounts["PAUSED"] ?: 0,
                    repeatingCount = statusCounts["REPEATING"] ?: 0,
                    meanScore = avgScore / 10f,
                    episodesWatched = totalEps,
                    minutesWatched = totalMinutes,
                    scoreDistribution = scoreDist,
                    genreBreakdown = genreMap,
                    daysWatched = daysWatched
                )
            }
        }

        // Also try to fetch user stats from AniList for richer data
        viewModelScope.launch {
            try {
                repo.getViewer().collect { result ->
                    result?.let { user ->
                        user.statistics?.anime?.let { animeStats ->
                            val current = _stats.value
                            _stats.value = current.copy(
                                episodesWatched = animeStats.episodesWatched,
                                minutesWatched = animeStats.minutesWatched,
                                meanScore = animeStats.meanScore,
                                daysWatched = animeStats.minutesWatched / 1440
                            )
                        }
                    }
                }
            } catch (_: Exception) { /* Use local stats only */ }
        }
    }
}
