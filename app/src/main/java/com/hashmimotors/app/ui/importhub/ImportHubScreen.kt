package com.hashmimotors.app.ui.importhub

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.catalog.CatalogViewModel
import com.hashmimotors.app.util.CsvImporter
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ImportHubScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onManual: () -> Unit,
    onScanBarcode: () -> Unit,
    onOcr: () -> Unit,
    onVoiceQuery: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = CsvImporter.parse(context, uri)
            result.onSuccess { parts ->
                if (parts.isEmpty()) {
                    status = "No valid rows found"
                } else {
                    viewModel.importParts(parts)
                    status = "Imported ${parts.size} parts"
                    Toast.makeText(context, "Imported ${parts.size} parts", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                status = it.message ?: "Import failed"
                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            }
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            onVoiceQuery(spoken)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                "Add parts",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Every method is live — camera, QR, CSV, voice, and sample catalog.",
            color = Color.White.copy(0.7f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))

        ImportCard(
            title = "Scan barcode / QR",
            subtitle = "Opens the camera and fills the barcode field",
            icon = Icons.Filled.QrCodeScanner,
            color = Color(0xFF4FC3F7),
            onClick = onScanBarcode
        )
        ImportCard(
            title = "Scan label with OCR",
            subtitle = "Point the camera at a bill or shelf tag",
            icon = Icons.Filled.TextFields,
            color = Color(0xFFAB47BC),
            onClick = onOcr
        )
        ImportCard(
            title = "Manual entry",
            subtitle = "Type name, price, stock, and photo",
            icon = Icons.Filled.CameraAlt,
            color = Color(0xFFFFA726),
            onClick = onManual
        )
        ImportCard(
            title = "Import CSV file",
            subtitle = "Columns: name, sku, brand, mrp, sellingPrice, stockQty, barcode",
            icon = Icons.Filled.Description,
            color = Color(0xFF66BB6A),
            onClick = { csvPicker.launch("*/*") }
        )
        ImportCard(
            title = "Voice search",
            subtitle = "Say a part name, then search the catalog",
            icon = Icons.Filled.Mic,
            color = Color(0xFFEC407A),
            onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a part name")
                }
                runCatching { voiceLauncher.launch(intent) }
                    .onFailure {
                        Toast.makeText(context, "Voice input not available on this device", Toast.LENGTH_SHORT).show()
                    }
            }
        )
        ImportCard(
            title = "Load sample catalog",
            subtitle = "15 Indian-market parts with barcodes (for testing scan & bills)",
            icon = Icons.Filled.Inventory,
            color = Color(0xFF26A69A),
            onClick = {
                scope.launch {
                    val n = viewModel.seedDemoCatalog()
                    status = if (n == 0) "Catalog already has parts" else "Added $n sample parts"
                    Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                }
            }
        )

        if (status != null) {
            Spacer(Modifier.height(12.dp))
            Text(status!!, color = Color(0xFFFFCC80), fontSize = 13.sp)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ImportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .background(color.copy(alpha = 0.28f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(0.65f), fontSize = 12.sp)
            }
        }
    }
}
