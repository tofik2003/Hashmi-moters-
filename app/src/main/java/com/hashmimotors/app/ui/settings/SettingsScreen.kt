package com.hashmimotors.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import com.hashmimotors.app.ui.components.SegmentedControl
import com.hashmimotors.app.ui.components.SettingsRow
import com.hashmimotors.app.ui.components.SettingsSection
import com.hashmimotors.app.ui.theme.BrandGoldBright

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSecurity: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection("Appearance") {
                SettingsRow("Theme") {
                    SegmentedControl(
                        options = listOf(
                            "Light" to ThemeMode.LIGHT,
                            "Dark" to ThemeMode.DARK,
                            "Auto" to ThemeMode.AUTO
                        ),
                        selected = settings.themeMode,
                        onSelect = { viewModel.setThemeMode(it) }
                    )
                }
                SettingsRow("Background") {
                    SegmentedControl(
                        options = listOf(
                            "Gradient" to BackgroundStyle.GRADIENT_PARTICLES,
                            "Solid" to BackgroundStyle.SOLID
                        ),
                        selected = settings.backgroundStyle,
                        onSelect = { viewModel.setBackgroundStyle(it) }
                    )
                }
                SettingsRow("Accent Color") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccentColorType.values().forEach { color ->
                            val isSelected = settings.accentColor == color
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = viewModel.getAccentColor(color),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(color) }
                            )
                        }
                    }
                }
            }

            SettingsSection("Animations & Sound") {
                SettingsRow("Enable Animations") {
                    Switch(
                        checked = settings.animationsEnabled,
                        onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                    )
                }
                SettingsRow("Animation Speed") {
                    SegmentedControl(
                        options = listOf(
                            "Normal" to AnimationSpeed.NORMAL,
                            "Reduced" to AnimationSpeed.REDUCED
                        ),
                        selected = settings.animationSpeed,
                        onSelect = { viewModel.setAnimationSpeed(it) }
                    )
                }
                SettingsRow("Enable Sounds") {
                    Switch(
                        checked = settings.soundsEnabled,
                        onCheckedChange = { viewModel.setSoundsEnabled(it) }
                    )
                }
                if (settings.soundsEnabled) {
                    SettingsRow("Volume: ${settings.soundVolume}%") {
                        Slider(
                            value = settings.soundVolume.toFloat(),
                            onValueChange = { viewModel.setSoundVolume(it.toInt()) },
                            valueRange = 0f..100f
                        )
                    }
                }
            }

            SettingsSection("Security") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSecurity() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = BrandGoldBright
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Lock (PIN)",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (settings.pinHash != null)
                                "PIN enabled — fingerprint unlock available"
                            else
                                "Set a 4-digit PIN to protect your data",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    Text("→", color = BrandGoldBright, fontSize = 20.sp)
                }
            }

            SettingsSection("About") {
                SettingsRow("App", "Hashmi Motors Pro")
                SettingsRow("Version", "2.1.0 Premium")
                SettingsRow("Build", "3")
                SettingsRow("Database", "Local (encrypted)")
                SettingsRow("Sync", "Firebase (optional)")
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
