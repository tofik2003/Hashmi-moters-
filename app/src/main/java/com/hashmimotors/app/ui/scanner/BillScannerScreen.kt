package com.hashmimotors.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Scans a paper bill with the camera, OCRs it with ML Kit, then lets the user
 * review & edit the recognized line items before adding them to a bill or
 * saving them as catalog parts.
 */
@Composable
fun BillScannerScreen(
    viewModel: BillScannerViewModel = hiltViewModel(),
    onAddToBill: (List<ScannedLine>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val pendingCapture = remember { AtomicBoolean(false) }
    var captured by remember { mutableStateOf<BillParseResult?>(null) }
    var lines by remember { mutableStateOf<List<ScannedLine>>(emptyList()) }
    var saving by remember { mutableStateOf(false) }

    val onText by rememberUpdatedState { text: String ->
        val parsed = BillTextParser.parse(text)
        captured = parsed
        lines = parsed.lines
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            val executor = ContextCompat.getMainExecutor(context)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            setImageAnalysisAnalyzer(executor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null && pendingCapture.getAndSet(false)) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val text = visionText.text
                            if (!text.isNullOrBlank()) onText(text)
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }
        }
    }
    controller.bindToLifecycle(lifecycleOwner)

    val result = captured
    if (result == null) {
        // ---- Live preview ----
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (hasPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.controller = controller
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.FlashOff, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera permission is needed to scan bills.",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Scan Bill", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 32.dp)
                    .align(Alignment.BottomCenter)
            ) {
                com.hashmimotors.app.ui.components.AnimatedBigButton(
                    text = "Capture Bill",
                    icon = Icons.Filled.DocumentScanner,
                    onClick = { pendingCapture.set(true) }
                )
            }
        }
    } else {
        // ---- Review results ----
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Review Scanned Items",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lines.size} item${if (lines.size != 1) "s" else ""} recognized — edit if needed",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lines.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DocumentScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No items could be read from this bill.\nTry capturing again with better lighting.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(lines, key = { index, _ -> index }) { index, line ->
                        ScannedLineRow(
                            line = line,
                            onName = { name -> lines = lines.toMutableList().also { it[index] = it[index].copy(name = name) } },
                            onQty = { qty -> lines = lines.toMutableList().also { it[index] = it[index].copy(qty = qty, rate = if (qty > 0) it[index].amount / qty else it[index].rate) } },
                            onRate = { rate -> lines = lines.toMutableList().also { it[index] = it[index].copy(rate = rate, amount = rate * it[index].qty) } }
                        )
                    }
                }
            }

            result.detectedTotal?.let { total ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Detected Total", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text(text = "₹${"%,.0f".format(total)}", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.hashmimotors.app.ui.components.GlassCard(
                    modifier = Modifier.weight(1f).clickable { captured = null; lines = emptyList() },
                    shape = RoundedCornerShape(14.dp),
                    containerAlpha = 0.12f
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rescan", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                com.hashmimotors.app.ui.components.GlassCard(
                    modifier = Modifier.weight(1f).clickable {
                        if (!saving) {
                            saving = true
                            viewModel.saveAsParts(lines) { onBack() }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    containerAlpha = 0.12f
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color(0xFF34D399))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (saving) "Saving..." else "Save as Parts", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            com.hashmimotors.app.ui.components.AnimatedBigButton(
                text = "Add ${lines.size} item${if (lines.size != 1) "s" else ""} to Bill",
                icon = Icons.Filled.Receipt,
                enabled = lines.isNotEmpty(),
                onClick = { onAddToBill(lines) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScannedLineRow(
    line: ScannedLine,
    onName: (String) -> Unit,
    onQty: (Int) -> Unit,
    onRate: (Double) -> Unit
) {
    com.hashmimotors.app.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        containerAlpha = 0.10f
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = line.name,
                onValueChange = onName,
                label = { Text("Item") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = line.qty.toString(),
                    onValueChange = { qty -> onQty(qty.filter { it.isDigit() }.toIntOrNull() ?: 1) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (line.rate == 0.0) "" else "%.2f".format(line.rate).trimEnd('0').trimEnd('.'),
                    onValueChange = { rate -> onRate(rate.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Rate") },
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        }
    }
}
