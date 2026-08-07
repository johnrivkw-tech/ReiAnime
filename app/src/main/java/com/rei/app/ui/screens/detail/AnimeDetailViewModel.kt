package com.rei.app.ui.screens.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaListStatus
import com.rei.app.economy.EconomyManager
import com.rei.app.economy.ReiRewards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailState { data object Loading : DetailState(); data class Success(val anime: Anime) : DetailState(); data class Error(val msg: String) : DetailState() }

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val repo: AnimeRepository,
    private val economy: EconomyManager
) : ViewModel() {
    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()
    private var isFav = false

    fun load(id: Int) = viewModelScope.launch {
        _state.value = DetailState.Loading
        try {
            repo.getAnimeDetail(id).collect { result ->
                isFav = result.isFavourite
                _state.value = DetailState.Success(result)
            }
        } catch (e: Exception) {
            _state.value = DetailState.Error(e.message ?: "Failed")
        }
    }

    fun updateStatus(id: Int, s: MediaListStatus) = viewModelScope.launch {
        try { repo.updateMediaListEntry(id, s); load(id) } catch (_: Exception) {}
    }

    fun toggleFavorite() {
        isFav = !isFav
        val current = _state.value
        if (current is DetailState.Success) {
            _state.value = DetailState.Success(current.anime.copy(isFavourite = isFav))
        }
        if (isFav) {
            viewModelScope.launch {
                val id = (current as? DetailState.Success)?.anime?.id ?: return@launch
                economy.earn(ReiRewards.FAVORITE, "favorite", id.toString())
            }
        }
    }

    fun shareAnime(context: Context) {
        val anime = (_state.value as? DetailState.Success)?.anime ?: return
        val shareText = buildString {
            append(anime.title.primary)
            anime.title.english?.let { if (it != anime.title.primary) append(" ($it)") }
            append(" \u2014 Rei \u96F6 Anime Tracker")
            anime.meanScore?.let { append("\n\u2605 ${it / 10.0}/10") }
            anime.episodes?.let { append(" \u2022 $it episodes") }
            anime.genres.take(3).let { if (it.isNotEmpty()) append("\n${it.joinToString(", ")}") }
            append("\n\nhttps://anilist.co/anime/${anime.id}")
        }
        viewModelScope.launch {
            economy.earn(ReiRewards.SHARE_ANIME, "share", anime.id.toString())
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Anime"))
    }
}
