package com.hashmimotors.app.ui.settings

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.backup.BackupRepository
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.data.seed.DemoCatalogSeeder
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
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
    private val demoCatalogSeeder: DemoCatalogSeeder,
    private val backupRepository: BackupRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(themeMode = mode))
        }
    }

    fun setBackgroundStyle(style: BackgroundStyle) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(backgroundStyle = style))
        }
    }

    fun setAccentColor(color: AccentColorType) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(accentColor = color))
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(animationsEnabled = enabled))
        }
    }

    fun setAnimationSpeed(speed: AnimationSpeed) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(animationSpeed = speed))
        }
    }

    fun setSoundsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(soundsEnabled = enabled))
        }
    }

    fun setSoundVolume(volume: Int) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(soundVolume = volume))
        }
    }

    fun seedSampleInventory() {
        viewModelScope.launch {
            _statusMessage.value = "Loading sample inventory..."
            val count = demoCatalogSeeder.seed()
            _statusMessage.value = "Successfully loaded $count auto parts & fitment data!"
        }
    }

    fun exportBackup(onExported: (File) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val file = backupRepository.exportToFile()
                _statusMessage.value = "Backup created successfully"
                onExported(file)
            }.onFailure {
                _statusMessage.value = "Failed to create backup: ${it.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val count = backupRepository.importFromUri(uri)
                _statusMessage.value = "Restored $count records from backup!"
            }.onFailure {
                _statusMessage.value = "Failed to restore backup: ${it.message}"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun getAccentColor(type: AccentColorType): Color {
        return when (type) {
            AccentColorType.INDIGO -> Color(0xFF6366F1)
            AccentColorType.EMERALD -> Color(0xFF10B981)
            AccentColorType.AMBER -> Color(0xFFF59E0B)
            AccentColorType.ROSE -> Color(0xFFF43F5E)
        }
    }
}
