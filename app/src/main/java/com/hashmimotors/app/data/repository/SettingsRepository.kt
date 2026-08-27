package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.SettingsDao
import com.hashmimotors.app.data.local.SettingsEntity
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun getSettings(): Flow<AppSettings> = settingsDao.get().map { entity ->
        entity?.toDomain() ?: AppSettings()
    }

    suspend fun getSettingsOnce(): AppSettings = settingsDao.getOnce()?.toDomain() ?: AppSettings()

    suspend fun saveSettings(settings: AppSettings) {
        settingsDao.upsert(settings.toEntity())
    }

    /**
     * Compute accent color from user selection.
     */
    fun getAccentColor(type: AccentColorType) = when (type) {
        AccentColorType.INDIGO -> com.hashmimotors.app.ui.theme.AccentIndigo
        AccentColorType.BLUE -> com.hashmimotors.app.ui.theme.AccentBlue
        AccentColorType.GREEN -> com.hashmimotors.app.ui.theme.AccentGreen
        AccentColorType.ORANGE -> com.hashmimotors.app.ui.theme.AccentOrange
    }
}

fun SettingsEntity.toDomain() = AppSettings(
    id = id,
    themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.AUTO),
    backgroundStyle = runCatching { BackgroundStyle.valueOf(backgroundStyle) }.getOrDefault(BackgroundStyle.GRADIENT_PARTICLES),
    accentColor = runCatching { AccentColorType.valueOf(accentColor) }.getOrDefault(AccentColorType.INDIGO),
    soundsEnabled = soundsEnabled,
    soundVolume = soundVolume,
    animationsEnabled = animationsEnabled,
    animationSpeed = runCatching { AnimationSpeed.valueOf(animationSpeed) }.getOrDefault(AnimationSpeed.NORMAL),
    tutorialShown = tutorialShown,
    pinHash = pinHash,
    linkedUserEmails = linkedUserEmails
)

fun AppSettings.toEntity() = SettingsEntity(
    id = id,
    themeMode = themeMode.name,
    backgroundStyle = backgroundStyle.name,
    accentColor = accentColor.name,
    soundsEnabled = soundsEnabled,
    soundVolume = soundVolume,
    animationsEnabled = animationsEnabled,
    animationSpeed = animationSpeed.name,
    tutorialShown = tutorialShown,
    pinHash = pinHash,
    linkedUserEmails = linkedUserEmails
)
