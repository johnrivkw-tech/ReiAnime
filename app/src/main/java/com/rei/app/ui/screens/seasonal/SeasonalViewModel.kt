package com.rei.app.ui.screens.seasonal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SeasonalState(val season: Season = curSeason(), val year: Int = Calendar.getInstance().get(Calendar.YEAR), val anime: List<Anime> = emptyList(), val loading: Boolean = false, val sortPop: Boolean = true)
private fun curSeason() = when(Calendar.getInstance().get(2)) { in 0..2->Season.WINTER; in 3..5->Season.SPRING; in 6..8->Season.SUMMER; else->Season.FALL }

@HiltViewModel
class SeasonalViewModel @Inject constructor(private val repo: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow(SeasonalState())
    val state: StateFlow<SeasonalState> = _state.asStateFlow()
    init { refresh() }
    fun setSeason(s: Season) { _state.value = _state.value.copy(season = s); refresh() }
    fun setSort(pop: Boolean) { _state.value = _state.value.copy(sortPop = pop); refresh() }
    fun refresh() = viewModelScope.launch { val s = _state.value; _state.value = s.copy(loading = true); try { repo.getSeasonalAnime(s.season, s.year, sort = if (s.sortPop) MediaSort.POPULARITY_DESC else MediaSort.SCORE_DESC).collect { _state.value = _state.value.copy(anime = it.first, loading = false) } } catch (_: Exception) { _state.value = _state.value.copy(loading = false) } }
}
