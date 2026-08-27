package com.hashmimotors.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.SegmentedControl
import com.hashmimotors.app.ui.components.SettingsRow
import com.hashmimotors.app.ui.components.SettingsSection
import com.hashmimotors.app.ui.theme.Ivory
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditShop: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.importBackup(uri)
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HmTopBar(title = "Settings", subtitle = "Free · offline · no subscriptions", onBack = onBack)
        Column(Modifier.padding(horizontal = 16.dp)) {
            SettingsSection("Shop") {
                SettingsRow("Shop profile") {
                    Text("Edit", color = Ivory, modifier = Modifier.clickable { onEditShop() })
                }
            }

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
                            "Atelier" to BackgroundStyle.GRADIENT_PARTICLES,
                            "Solid" to BackgroundStyle.SOLID
                        ),
                        selected = settings.backgroundStyle,
                        onSelect = { viewModel.setBackgroundStyle(it) }
                    )
                }
                SettingsRow("Accent") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccentColorType.values().forEach { color ->
                            val isSelected = settings.accentColor == color
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(viewModel.getAccentColor(color), CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = Ivory,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(color) }
                            )
                        }
                    }
                }
            }

            SettingsSection("Motion") {
                SettingsRow("Animations") {
                    Switch(
                        checked = settings.animationsEnabled,
                        onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                    )
                }
                SettingsRow("Speed") {
                    SegmentedControl(
                        options = listOf(
                            "Normal" to AnimationSpeed.NORMAL,
                            "Reduced" to AnimationSpeed.REDUCED
                        ),
                        selected = settings.animationSpeed,
                        onSelect = { viewModel.setAnimationSpeed(it) }
                    )
                }
                SettingsRow("Sounds") {
                    Switch(
                        checked = settings.soundsEnabled,
                        onCheckedChange = { viewModel.setSoundsEnabled(it) }
                    )
                }
                if (settings.soundsEnabled) {
                    SettingsRow("Volume ${settings.soundVolume}%") {
                        Slider(
                            value = settings.soundVolume.toFloat(),
                            onValueChange = { viewModel.setSoundVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth(0.45f)
                        )
                    }
                }
            }

            SettingsSection("Backup (local file)") {
                SettingsRow("Export JSON") {
                    Text(
                        "Share",
                        color = Ivory,
                        modifier = Modifier.clickable {
                            scope.launch {
                                val file = viewModel.exportBackup()
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export backup"))
                            }
                        }
                    )
                }
                SettingsRow("Restore JSON") {
                    Text(
                        "Choose file",
                        color = Ivory,
                        modifier = Modifier.clickable { importLauncher.launch("application/json") }
                    )
                }
            }

            SettingsSection("About") {
                SettingsRow("Version", "1.3.0")
                SettingsRow("Licence", "Personal use · all features included")
                SettingsRow("Data", "On this phone only")
                SettingsRow("Accounts", "None required")
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
