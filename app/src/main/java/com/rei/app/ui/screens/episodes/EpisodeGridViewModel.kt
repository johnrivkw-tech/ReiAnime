package com.rei.app.ui.screens.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.local.EpisodeEntity
import com.rei.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeGridViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _episodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodes: StateFlow<List<EpisodeEntity>> = _episodes.asStateFlow()

    fun load(mediaId: Int, totalEpisodes: Int) = viewModelScope.launch {
        repo.initEpisodes(mediaId, totalEpisodes)
        repo.getEpisodes(mediaId).collect { _episodes.value = it }
    }

    fun toggleEpisode(mediaId: Int, episode: Int, watched: Boolean) = viewModelScope.launch {
        repo.toggleEpisodeWatched(mediaId, episode, watched)
    }

    fun setNote(mediaId: Int, episode: Int, note: String) = viewModelScope.launch {
        repo.setEpisodeNote(mediaId, episode, note)
    }

    fun markAllWatched(mediaId: Int, total: Int) = viewModelScope.launch {
        for (ep in 1..total) {
            repo.toggleEpisodeWatched(mediaId, ep, true)
        }
    }

    fun markAllUnwatched(mediaId: Int) = viewModelScope.launch {
        _episodes.value.forEach { ep ->
            if (ep.watched) repo.toggleEpisodeWatched(mediaId, ep.episodeNumber, false)
        }
    }
}
