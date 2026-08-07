package com.rei.app.ui.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val repo: AnimeRepository
) : ViewModel() {
    private val _reviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val reviews: StateFlow<List<ReviewItem>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadReviews(animeId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val malReviews = repo.getAnimeReviews(animeId)
                _reviews.value = malReviews.map { r ->
                    ReviewItem(
                        id = r.malReviewId,
                        username = r.user?.username ?: "Anonymous",
                        score = (r.score * 10).toInt().coerceIn(0, 100),
                        text = r.review,
                        date = r.date,
                        helpful = r.reviewerReactions?.overall ?: 0
                    )
                }
            } catch (_: Exception) {
                _reviews.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}
