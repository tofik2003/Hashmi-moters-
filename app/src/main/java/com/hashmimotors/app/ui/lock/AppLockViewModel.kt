package com.hashmimotors.app.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the app-lock PIN (stored as a SHA-256 hash in AppSettings,
 * so no database schema change is required).
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setPin(pin: String) {
        if (!PinUtils.isValidPin(pin)) return
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(current.copy(pinHash = PinUtils.sha256(pin)))
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(current.copy(pinHash = null))
        }
    }

    fun verifyPin(pin: String, storedHash: String?): Boolean =
        storedHash != null && PinUtils.sha256(pin) == storedHash
}
