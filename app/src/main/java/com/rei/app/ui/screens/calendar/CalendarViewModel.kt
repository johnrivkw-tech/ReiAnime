package com.rei.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()
    init { loadForDay(_state.value.selectedDay) }
    fun setDay(day: DayOfWeek) { _state.value = _state.value.copy(selectedDay = day); loadForDay(day) }
    private fun loadForDay(day: DayOfWeek) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        try {
            repo.getPopularThisSeason().collect { (anime, _) ->
                // Filter to currently airing anime
                _state.value = _state.value.copy(anime = anime.filter { it.status == com.rei.app.domain.model.MediaStatus.RELEASING }, loading = false)
            }
        } catch (_: Exception) { _state.value = _state.value.copy(loading = false) }
    }
}
