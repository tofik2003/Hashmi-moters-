package com.hashmimotors.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopSetupViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {
    val shop: StateFlow<Shop?> = shopRepository.getShop()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveShop(shop: Shop, onComplete: () -> Unit) {
        viewModelScope.launch {
            shopRepository.saveShop(shop)
            onComplete()
        }
    }
}
