package com.rei.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchState { data object Idle : SearchState(); data object Loading : SearchState(); data class Success(val results: List<Anime>) : SearchState(); data class Error(val msg: String) : SearchState() }

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state.asStateFlow()
    fun search(query: String, genre: String? = null, year: Int? = null, format: MediaFormat? = null) {
        if (query.isBlank() && genre == null) return
        viewModelScope.launch { _state.value = SearchState.Loading; try { repo.searchAnime(query.ifBlank { "*" }, genre = genre, year = year, format = format, sort = if (query.isBlank()) listOf(MediaSort.POPULARITY_DESC) else listOf(MediaSort.SEARCH_MATCH)).collect { _state.value = SearchState.Success(it.first) } } catch (e: Exception) { _state.value = SearchState.Error(e.message ?: "Failed") } }
    }
    fun clear() { _state.value = SearchState.Idle }
}
