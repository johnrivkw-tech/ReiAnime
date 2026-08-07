package com.rei.app.ui.screens.waifu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.remote.waifu.WaifuImage
import com.rei.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaifuGalleryViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _images = MutableStateFlow<List<WaifuImage>>(emptyList())
    val images: StateFlow<List<WaifuImage>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMore(category: String = "waifu") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newImages = repo.getRandomWaifu(6)
                _images.value = _images.value + newImages
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun refresh(category: String = "waifu") {
        _images.value = emptyList()
        loadMore(category)
    }

    init { loadMore() }
}
