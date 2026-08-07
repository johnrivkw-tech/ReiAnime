package com.rei.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.User
import com.rei.app.economy.EconomyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState { data object Loading : ProfileState(); data class Success(val user: User? = null) : ProfileState() }

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: AnimeRepository,
    private val economy: EconomyManager
) : ViewModel() {
    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    val coinBalance: StateFlow<Int> = economy.balance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val streak: StateFlow<Int> = flow { emit(economy.getStreak()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            try {
                var user: User? = null
                repo.getViewer().collect { result -> user = result.first; throw kotlinx.coroutines.CancellationException() }
                _state.value = ProfileState.Success(user)
            } catch (_: Exception) {
                _state.value = ProfileState.Success(null)
            }
        }
        // Check daily streak on profile open
        viewModelScope.launch { economy.checkDailyStreak() }
    }
}
