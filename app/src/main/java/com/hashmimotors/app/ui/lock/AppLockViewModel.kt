package com.hashmimotors.app.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun verifyPin(pin: String): Boolean {
        val currentHash = settings.value.pinHash
        if (currentHash == null) {
            _isUnlocked.value = true
            return true
        }
        val match = PinUtils.sha256(pin) == currentHash
        if (match) {
            _isUnlocked.value = true
        }
        return match
    }

    fun unlockWithBiometric() {
        _isUnlocked.value = true
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val hash = if (pin.isBlank()) null else PinUtils.sha256(pin)
            settingsRepository.updateSettings(settings.value.copy(pinHash = hash))
        }
    }

    fun removePin() {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(pinHash = null))
        }
    }
}
