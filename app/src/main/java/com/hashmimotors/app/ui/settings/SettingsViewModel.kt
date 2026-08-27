package com.hashmimotors.app.ui.settings

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.backup.BackupRepository
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import com.hashmimotors.app.ui.theme.AccentBlue
import com.hashmimotors.app.ui.theme.AccentGreen
import com.hashmimotors.app.ui.theme.AccentIndigo
import com.hashmimotors.app.ui.theme.AccentOrange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setBackgroundStyle(style: BackgroundStyle) = update { it.copy(backgroundStyle = style) }
    fun setAccentColor(color: AccentColorType) = update { it.copy(accentColor = color) }
    fun setSoundsEnabled(enabled: Boolean) = update { it.copy(soundsEnabled = enabled) }
    fun setSoundVolume(volume: Int) = update { it.copy(soundVolume = volume) }
    fun setAnimationsEnabled(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }
    fun setAnimationSpeed(speed: AnimationSpeed) = update { it.copy(animationSpeed = speed) }

    fun getAccentColor(type: AccentColorType): Color = when (type) {
        AccentColorType.INDIGO -> AccentIndigo
        AccentColorType.BLUE -> AccentBlue
        AccentColorType.GREEN -> AccentGreen
        AccentColorType.ORANGE -> AccentOrange
    }

    suspend fun exportBackup(): File = backupRepository.exportToFile()

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupRepository.importFromUri(uri) }
                .onSuccess { n -> _message.value = "Restored $n records. Fully local — no account." }
                .onFailure { _message.value = it.message ?: "Restore failed" }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(transform(settings.value))
        }
    }
}
