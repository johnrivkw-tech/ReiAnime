package com.rei.app.ui.screens.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.livechart.LiveChartApi
import com.rei.app.data.remote.livechart.LiveChartAnime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamingState(
    val anime: List<LiveChartAnime> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val filter: String? = null
)

@HiltViewModel
class StreamingViewModel @Inject constructor(private val api: LiveChartApi) : ViewModel() {
    private val _state = MutableStateFlow(StreamingState(loading = true))
    val state: StateFlow<StreamingState> = _state.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        try {
            val season = api.getCurrentSeason()
            _state.value = _state.value.copy(anime = season, loading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message ?: "Failed to load")
        }
    }

    fun setFilter(filter: String?) { _state.value = _state.value.copy(filter = filter) }
}
