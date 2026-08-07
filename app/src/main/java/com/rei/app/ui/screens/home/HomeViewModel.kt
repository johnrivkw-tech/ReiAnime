package com.rei.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.local.TrackingEntity
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(
        val trending: List<Anime> = emptyList(),
        val popular: List<Anime> = emptyList(),
        val upcoming: List<Anime> = emptyList(),
        val allTime: List<Anime> = emptyList(),
        val continueWatching: List<TrackingEntity> = emptyList()
    ) : HomeState()
    data class Error(val msg: String) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Continue watching from Room (always live)
    val continueWatching: StateFlow<List<TrackingEntity>> = repo.getContinueWatching()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = HomeState.Loading
        try {
            val t = mutableListOf<Anime>(); repo.getTrending().collect { t.addAll(it.first) }
            val p = mutableListOf<Anime>(); repo.getPopularThisSeason().collect { p.addAll(it.first) }
            val u = mutableListOf<Anime>(); repo.getUpcomingNextSeason().collect { u.addAll(it.first) }
            val a = mutableListOf<Anime>(); repo.getAllTimePopular().collect { a.addAll(it.first) }
            val cw = continueWatching.value
            _state.value = HomeState.Success(t, p, u, a, cw)
        } catch (e: Exception) { _state.value = HomeState.Error(e.message ?: "Failed") }
    }

    // Update continue watching in the success state when the flow emits
    init {
        viewModelScope.launch {
            continueWatching.collect { cw ->
                val current = _state.value
                if (current is HomeState.Success) {
                    _state.value = current.copy(continueWatching = cw)
                }
            }
        }
    }
}
