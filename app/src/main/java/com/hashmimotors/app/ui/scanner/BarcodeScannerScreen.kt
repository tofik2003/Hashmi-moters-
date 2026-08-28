package com.hashmimotors.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.hashmimotors.app.data.remote.OnlineProduct
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.components.AnimatedBigButton

/**
 * Live camera barcode/QR scanner.
 *
 * 1. Decodes a code with on-device ML Kit (works offline, free).
 * 2. Looks up the code in the local catalog — if found, identifies the part instantly.
 * 3. If unknown, offers online identification or manual add with the code pre-filled.
 */
@Composable
fun BarcodeScannerScreen(
    viewModel: BarcodeScannerViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditPart: (String) -> Unit,
    onAddPart: (barcode: String, name: String?, brand: String?, price: Double?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner, hasCameraPermission) {
        if (hasCameraPermission) {
            runCatching {
                val executor = ContextCompat.getMainExecutor(context)
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(
                    executor,
                    BarcodeAnalyzer { raw -> viewModel.onBarcode(raw) }
                )
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }
        }
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            // Camera preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

        } else {
            // Permission prompt
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📷", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera permission needed",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera access to scan barcodes and QR codes.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                AnimatedBigButton(
                    text = "Grant Permission",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    gradient = false
                )
            }
        }

        // Top bar (over camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Scan Barcode",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Point camera at a barcode or QR code",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        // Result card (bottom)
        if (state.raw != null) {
            ScanResultCard(
                state = state,
                onEditPart = { state.matchedPart?.let { onEditPart(it.id) } },
                onAddManually = {
                    onAddPart(state.raw.orEmpty(), null, null, null)
                },
                onUseOnline = {
                    state.onlineProduct?.let { p ->
                        onAddPart(state.raw.orEmpty(), p.name, p.brand, p.price)
                    }
                },
                onLookupOnline = { viewModel.lookupOnline() },
                onScanAgain = { viewModel.clearResult() }
            )
        }
    }
}

@Composable
private fun ScanResultCard(
    state: ScanUiState,
    onEditPart: () -> Unit,
    onAddManually: () -> Unit,
    onUseOnline: () -> Unit,
    onLookupOnline: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.matchedPart?.let { "✓ Identified" }
                            ?: state.onlineProduct?.let { "🌐 Found online" }
                            ?: "Scanned",
                        color = when {
                            state.matchedPart != null -> Color(0xFF66BB6A)
                            state.onlineProduct != null -> Color(0xFF4FC3F7)
                            else -> Color(0xFFFFC107)
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = state.raw.orEmpty(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    state.matchedPart != null -> MatchedPartContent(state.matchedPart!!)
                    state.onlineProduct != null -> OnlineProductContent(state.onlineProduct!!)
                    state.isLookingUpOnline -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Searching online…", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    else -> {
                        Text(
                            text = "Not in your catalog yet.",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.error!!,
                                color = Color(0xFFFF8A80),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                when {
                    state.matchedPart != null -> {
                        AnimatedBigButton(
                            text = "Edit / Restock",
                            icon = Icons.Filled.Edit,
                            onClick = onEditPart
                        )
                    }
                    state.onlineProduct != null -> {
                        AnimatedBigButton(
                            text = "Use These Details",
                            onClick = onUseOnline
                        )
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedBigButton(
                                text = "Identify Online",
                                icon = Icons.Filled.Search,
                                onClick = onLookupOnline,
                                gradient = false
                            )
                            AnimatedBigButton(
                                text = "Add Manually",
                                onClick = onAddManually,
                                gradient = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scan again
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScanAgain() }
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Autorenew, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan another",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchedPartContent(part: Part) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = part.name.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = part.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (part.brand != null) {
                Text(
                    text = part.brand,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stock: ${part.stockQty}   •   ₹${"%,.0f".format(part.sellingPrice)}",
                color = Color(0xFF66BB6A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnlineProductContent(product: OnlineProduct) {
    Column {
        Text(
            text = product.name.orEmpty(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (product.brand != null) {
            Text(
                text = "Brand: ${product.brand}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
        if (product.category != null) {
            Text(
                text = "Category: ${product.category}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
        if (product.price != null) {
            Text(
                text = "Reference price: ₹${"%,.0f".format(product.price)}",
                color = Color(0xFFFFC107),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Source: ${product.source}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * ML Kit barcode analyzer running on the camera's ImageAnalysis stream.
 */
private class BarcodeAnalyzer(
    private val onBarcode: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let(onBarcode)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
