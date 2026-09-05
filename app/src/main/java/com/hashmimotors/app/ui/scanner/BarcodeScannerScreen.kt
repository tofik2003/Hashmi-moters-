package com.hashmimotors.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.hashmimotors.app.ui.theme.PremiumGold
import com.hashmimotors.app.util.QrProductParser
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Live barcode scanner using CameraX + ML Kit.
 *
 * Single mode (default): calls [onBarcode] once with the first barcode
 * detected, then stops scanning.
 *
 * Continuous mode ([continuous] = true): keeps the camera running and calls
 * [onBarcode] for every new code, with a short cooldown so the same physical
 * barcode isn't fired repeatedly. Shows a live "n items added" counter plus
 * [feedback] and a Done button that invokes [onDone] (falls back to [onBack]).
 */
@Composable
fun BarcodeScannerScreen(
    onBarcode: (String) -> Unit,
    onBack: () -> Unit,
    continuous: Boolean = false,
    onDone: (() -> Unit)? = null,
    feedback: String? = null,
    count: Int = 0
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

    val currentOnBarcode by rememberUpdatedState(onBarcode)
    val found = remember { AtomicBoolean(false) }

    // Cooldown state for continuous scanning (mutated on the main thread by the
    // image-analysis callback).
    var lastValue by remember { mutableStateOf("") }
    var lastTime by remember { mutableStateOf(0L) }

    // Track auto-detected product info from QR codes
    var detectedProductInfo by remember { mutableStateOf<String?>(null) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            val executor = ContextCompat.getMainExecutor(context)
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
            )
            setImageAnalysisAnalyzer(executor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            val value = barcodes.firstOrNull()?.rawValue
                            if (value != null) {
                                // Try to parse as full product data (QR code with embedded product info)
                                val product = QrProductParser.parse(value)
                                if (product != null) {
                                    // Rich QR code detected - notify UI with product details
                                    detectedProductInfo = "${product.name} • ₹${product.mrp ?: "?"}"
                                } else {
                                    // Standard barcode - just show the code
                                    detectedProductInfo = null
                                }
                                
                                if (continuous) {
                                    val now = System.currentTimeMillis()
                                    if (value != lastValue || now - lastTime >= COOLDOWN_MS) {
                                        lastValue = value
                                        lastTime = now
                                        currentOnBarcode(value)
                                    }
                                } else if (found.compareAndSet(false, true)) {
                                    currentOnBarcode(value)
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }
        }
    }
    controller.bindToLifecycle(lifecycleOwner)

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

            // Scan frame hint
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(Color.Transparent, RoundedCornerShape(24.dp))
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.FlashOff, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Camera permission is needed to scan barcodes.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (continuous && onDone != null) onDone() else onBack() }) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (continuous) "Scan Items" else "Scan Barcode",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom hint / continuous-scan summary with QR product detection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            if (continuous) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    if (feedback != null) {
                        Text(
                            text = feedback,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = "$count item${if (count != 1) "s" else ""} added",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
                            .clickable { (onDone ?: onBack)() }
                            .padding(horizontal = 36.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Point at barcode or QR code",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    // Show detected product info for rich QR codes
                    detectedProductInfo?.let { productInfo ->
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "📦 $productInfo",
                            color = PremiumGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private const val COOLDOWN_MS = 1200L
