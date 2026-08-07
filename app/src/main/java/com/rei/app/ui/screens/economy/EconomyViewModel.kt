package com.rei.app.ui.screens.economy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.local.UnlockEntity
import com.rei.app.economy.EconomyManager
import com.rei.app.economy.ReiShop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EconomyViewModel @Inject constructor(
    private val economy: EconomyManager
) : ViewModel() {
    val balance: StateFlow<Int> = economy.balance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val streak: StateFlow<Int> = flow { emit(economy.getStreak()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val unlockedIds: StateFlow<Set<String>> = economy.unlocks.map { list -> list.map { it.itemId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun purchase(item: ReiShop.ShopItem) = viewModelScope.launch {
        economy.purchase(item)
    }
}
