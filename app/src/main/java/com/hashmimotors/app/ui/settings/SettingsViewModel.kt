package com.hashmimotors.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import com.hashmimotors.app.ui.sound.SoundManager
import com.hashmimotors.app.ui.theme.AccentBlue
import com.hashmimotors.app.ui.theme.AccentGreen
import com.hashmimotors.app.ui.theme.AccentIndigo
import com.hashmimotors.app.ui.theme.AccentOrange
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundManager: SoundManager
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setBackgroundStyle(style: BackgroundStyle) = update { it.copy(backgroundStyle = style) }
    fun setAccentColor(color: AccentColorType) = update { it.copy(accentColor = color) }
    fun setSoundsEnabled(enabled: Boolean) {
        // Keep the live player in step with the persisted setting so the toggle
        // takes effect immediately instead of on the next app start.
        soundManager.setEnabled(enabled)
        update { it.copy(soundsEnabled = enabled) }
    }

    fun setSoundVolume(volume: Int) {
        soundManager.setVolume(volume)
        update { it.copy(soundVolume = volume) }
    }
    fun setAnimationsEnabled(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }
    fun setAnimationSpeed(speed: AnimationSpeed) = update { it.copy(animationSpeed = speed) }

    fun getAccentColor(type: AccentColorType): Color = when (type) {
        AccentColorType.INDIGO -> AccentIndigo
        AccentColorType.BLUE -> AccentBlue
        AccentColorType.GREEN -> AccentGreen
        AccentColorType.ORANGE -> AccentOrange
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(transform(settings.value))
        }
    }
}
