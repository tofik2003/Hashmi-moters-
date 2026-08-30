package com.hashmimotors.app.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.domain.model.AccentColorType
import com.hashmimotors.app.domain.model.AnimationSpeed
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.domain.model.ThemeMode
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.components.SegmentedControl
import com.hashmimotors.app.ui.components.SettingsRow
import com.hashmimotors.app.ui.components.SettingsSection
import com.hashmimotors.app.ui.sound.Feedback
import com.hashmimotors.app.ui.sound.LocalAppFeedback
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToShopSetup: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onNavigateToPinSetup: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val context = LocalContext.current
    val feedback = LocalAppFeedback.current

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

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

            // Status message
            if (statusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = BrandGoldBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Shop Management Section
            SettingsSection("Shop & Security") {
                ActionRow(
                    title = "Shop Profile & GST",
                    subtitle = "Address, phone, GSTIN, invoice prefix",
                    icon = Icons.Filled.Storefront,
                    onClick = onNavigateToShopSetup
                )
                ActionRow(
                    title = "Suppliers Directory",
                    subtitle = "Manage parts vendors and contacts",
                    icon = Icons.Filled.LocalShipping,
                    onClick = onNavigateToSuppliers
                )
                ActionRow(
                    title = "App Lock (4-Digit PIN)",
                    subtitle = if (settings.pinHash != null) "PIN Protection is Active" else "Not configured",
                    icon = Icons.Filled.Lock,
                    onClick = onNavigateToPinSetup
                )
            }

            // Quick Data Actions
            SettingsSection("Inventory Tools & Backup") {
                ActionRow(
                    title = "Load 50 Sample Indian Auto Parts",
                    subtitle = "Instantly populates Bosch, Mann, NGK parts & Swift fitments",
                    icon = Icons.Filled.AutoFixHigh,
                    onClick = {
                        Feedback.tap(feedback)
                        viewModel.seedSampleInventory()
                    }
                )
                ActionRow(
                    title = "Export Full Backup (JSON)",
                    subtitle = "Save all parts, customers, invoices to device",
                    icon = Icons.Filled.Download,
                    onClick = {
                        viewModel.exportBackup { file ->
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Backup File"))
                        }
                    }
                )
                ActionRow(
                    title = "Restore Backup (JSON)",
                    subtitle = "Load shop data from previously exported backup",
                    icon = Icons.Filled.Upload,
                    onClick = {
                        importPicker.launch(arrayOf("application/json", "*/*"))
                    }
                )
            }

            SettingsSection("Appearance") {
                SettingsRow("Theme") {
                    SegmentedControl(
                        options = listOf(
                            "Dark" to ThemeMode.DARK,
                            "Light" to ThemeMode.LIGHT,
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
                                    .size(36.dp)
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

            SettingsSection("About") {
                SettingsRow("Version", "1.5.0")
                SettingsRow("Edition", "Hashmi Motors (Personal)")
                SettingsRow("GST Mode", "Composition (Bill of Supply)")
                SettingsRow("Storage", "Room SQLite (Local & Offline-First)")
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BrandGold.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = BrandGoldBright, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Text("→", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
        }
    }
}
