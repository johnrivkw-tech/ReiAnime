package com.rei.app.ui.screens.random

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow(RandomState())
    val state: StateFlow<RandomState> = _state.asStateFlow()
    fun roll() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, anime = null)
        try { val a = repo.getRandomAnime(); _state.value = _state.value.copy(anime = a, loading = false) }
        catch (_: Exception) { _state.value = _state.value.copy(loading = false) }
    }
}
