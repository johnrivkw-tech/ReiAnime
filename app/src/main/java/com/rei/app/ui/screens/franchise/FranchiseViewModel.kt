package com.rei.app.ui.screens.franchise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FranchiseViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _nodes = MutableStateFlow<List<FranchiseNode>>(emptyList())
    val nodes: StateFlow<List<FranchiseNode>> = _nodes.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun load(animeId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            // Get franchise from Shikimori
            val franchise = repo.getFranchise(animeId)
            // Get related entries from Shikimori
            val related = repo.getShikiRelated(animeId)

            val nodes = mutableListOf<FranchiseNode>()

            // Parse franchise data (Shikimori returns a tree structure)
            // Franchise entries typically come as a list with relationship info
            if (franchise.isNotEmpty()) {
                // The first entry is typically the main series
                franchise.forEachIndexed { index, anime ->
                    val isMain = index == 0 || anime.format?.name in listOf("TV", "TV_SHORT", "MOVIE")
                    val depth = if (isMain) 0 else 1
                    nodes.add(FranchiseNode(
                        id = anime.id,
                        title = anime.title.primary,
                        coverImage = anime.coverImage.best,
                        relationType = if (index == 0) "MAIN" else "RELATED",
                        format = anime.format?.name?.replace("_", " "),
                        year = anime.seasonYear,
                        depth = depth,
                        isMainLine = isMain
                    ))
                }
            }

            // Add related entries from the related API
            if (related.isNotEmpty()) {
                val existingIds = nodes.map { it.id }.toSet()
                related.filter { it.id !in existingIds }.forEach { anime ->
                    nodes.add(FranchiseNode(
                        id = anime.id,
                        title = anime.title.primary,
                        coverImage = anime.coverImage.best,
                        relationType = "RELATED",
                        format = anime.format?.name?.replace("_", " "),
                        year = anime.seasonYear,
                        depth = 1,
                        isMainLine = false
                    ))
                }
            }

            // If we still have no data, try AniList recommendations
            if (nodes.isEmpty()) {
                try {
                    var detail: Anime? = null
                    repo.getAnimeDetail(animeId).collect { result -> detail = result.first; throw kotlinx.coroutines.CancellationException() }
                    detail?.recommendations?.forEach { rec ->
                        nodes.add(FranchiseNode(
                            id = rec.id,
                            title = rec.title.primary,
                            coverImage = rec.coverImage.best,
                            relationType = "RE/REC",
                            format = rec.format?.name?.replace("_", " "),
                            year = rec.seasonYear,
                            depth = 1,
                            isMainLine = false
                        ))
                    }
                } catch (_: Exception) {}
            }

            _nodes.value = nodes
        } catch (e: Exception) {
            _nodes.value = emptyList()
        } finally {
            _loading.value = false
        }
    }
}
