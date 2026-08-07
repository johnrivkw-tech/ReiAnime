package com.rei.app.ui.screens.manga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.mangadex.MangaDexEntry
import com.rei.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _trending = MutableStateFlow<List<MangaDexEntry>>(emptyList())
    val trending: StateFlow<List<MangaDexEntry>> = _trending.asStateFlow()

    private val _recent = MutableStateFlow<List<MangaDexEntry>>(emptyList())
    val recent: StateFlow<List<MangaDexEntry>> = _recent.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MangaDexEntry>>(emptyList())
    val searchResults: StateFlow<List<MangaDexEntry>> = _searchResults.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        try {
            _trending.value = repo.getTrendingManga()
            _recent.value = repo.getRecentlyUpdatedManga()
        } catch (_: Exception) { }
        _isLoading.value = false
    }

    fun search(q: String) {
        _query.value = q
        if (q.length < 2) { _searchResults.value = emptyList(); return }
        viewModelScope.launch {
            try { _searchResults.value = repo.searchManga(q) } catch (_: Exception) { _searchResults.value = emptyList() }
        }
    }

    fun clearSearch() { _query.value = ""; _searchResults.value = emptyList() }
}
