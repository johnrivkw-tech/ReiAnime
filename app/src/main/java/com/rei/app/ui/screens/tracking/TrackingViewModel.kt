package com.rei.app.ui.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaListStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackState(val status: MediaListStatus = MediaListStatus.CURRENT, val anime: List<Anime> = emptyList())

@HiltViewModel
class TrackingViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow(TrackState())
    val state: StateFlow<TrackState> = _state.asStateFlow()
    fun setStatus(s: MediaListStatus) { _state.value = _state.value.copy(status = s) }
}
