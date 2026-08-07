package com.rei.app.ui.screens.imagesearch

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.tracemoe.TraceMatch
import com.rei.app.data.remote.tracemoe.TraceResult
import com.rei.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageSearchState(
    val loading: Boolean = false,
    val results: List<TraceMatch> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ImageSearchViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ImageSearchState())
    val state: StateFlow<ImageSearchState> = _state.asStateFlow()

    fun searchByUrl(url: String) = viewModelScope.launch {
        _state.value = ImageSearchState(loading = true)
        try {
            val result = repo.searchByImageUrl(url)
            _state.value = ImageSearchState(results = result.result?.sortedByDescending { it.similarity } ?: emptyList())
        } catch (e: Exception) {
            _state.value = ImageSearchState(error = e.message ?: "Search failed")
        }
    }

    fun searchByUri(context: Context, uri: Uri) = viewModelScope.launch {
        _state.value = ImageSearchState(loading = true)
        try {
            // Read the URI content into bytes
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Could not read image")
            val filename = uri.lastPathSegment ?: "image.jpg"
            val result = repo.searchByImageBytes(bytes, filename)
            _state.value = ImageSearchState(results = result.result?.sortedByDescending { it.similarity } ?: emptyList())
        } catch (e: Exception) {
            _state.value = ImageSearchState(error = e.message ?: "Search failed")
        }
    }
}
