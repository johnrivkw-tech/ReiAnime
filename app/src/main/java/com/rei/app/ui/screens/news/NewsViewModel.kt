package com.rei.app.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.jikan.AnimeNews
import com.rei.app.data.remote.jikan.JikanApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(private val jikan: JikanApi) : ViewModel() {
    private val _news = MutableStateFlow<List<AnimeNews>>(emptyList())
    val news: StateFlow<List<AnimeNews>> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentPage = 1

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        currentPage = 1
        _hasMore.value = true
        _isLoading.value = true
        try {
            val result = jikan.getRecentNews()
            _news.value = result
            _hasMore.value = result.size >= 25
        } catch (_: Exception) { }
        _isLoading.value = false
    }

    fun loadMore() = viewModelScope.launch {
        if (!_hasMore.value || _isLoading.value) return@launch
        currentPage++
        _isLoading.value = true
        try {
            val more = jikan.getTopAnime(currentPage)
            val mapped = more.map { AnimeNews(
                malId = it.malId,
                title = it.title,
                imageUrl = it.images?.get("jpg")?.imageUrl ?: "",
                score = it.score ?: 0.0,
                synopsis = it.synopsis ?: "",
                genres = it.genres?.map { g -> g.name } ?: emptyList(),
                status = it.status ?: "",
                episodes = it.episodes
            ) }
            if (mapped.isEmpty()) _hasMore.value = false
            _news.value = _news.value + mapped
        } catch (_: Exception) { _hasMore.value = false }
        _isLoading.value = false
    }
}
