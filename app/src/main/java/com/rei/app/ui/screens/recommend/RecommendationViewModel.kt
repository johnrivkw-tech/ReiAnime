package com.rei.app.ui.screens.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.recommend.RecommendationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val engine: RecommendationEngine
) : ViewModel() {
    private val _state = MutableStateFlow(RecState())
    val state: StateFlow<RecState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val recs = engine.recommend(limit = 25)
            val uiItems = recs.map { RecUiItem(it.anime, it.matchPercent, it.reasons) }
            val avgMatch = if (uiItems.isNotEmpty()) uiItems.map { it.matchPercent }.average().toInt() else 0
            val genreCount = uiItems.flatMap { it.anime.genres }.distinct().size
            _state.value = RecState(isLoading = false, recs = uiItems, avgMatch = avgMatch, genreCount = genreCount)
        } catch (e: Exception) {
            _state.value = RecState(isLoading = false)
        }
    }
}
