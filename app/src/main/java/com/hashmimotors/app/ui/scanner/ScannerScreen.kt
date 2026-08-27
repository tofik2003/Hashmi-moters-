package com.hashmimotors.app.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ink
import com.hashmimotors.app.ui.theme.Ivory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

enum class ScannerMode {
    BARCODE,
    PHOTO,
    OCR
}

@Composable
fun ScannerScreen(
    mode: ScannerMode,
    onBarcode: (String) -> Unit,
    onPhoto: (String) -> Unit,
    onOcr: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionAsked = true
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasPermission = true
        else launcher.launch(Manifest.permission.CAMERA)
    }

    when {
        hasPermission -> CameraBody(
            mode = mode,
            onBarcode = onBarcode,
            onPhoto = onPhoto,
            onOcr = onOcr,
            onClose = onClose
        )
        permissionAsked -> PermissionDenied(onClose = onClose)
        else -> Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Requesting camera…", color = Ivory)
        }
    }
}

@Composable
private fun PermissionDenied(onClose: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E16))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📷", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Camera permission needed",
            color = Ivory,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Enable camera in Settings so you can scan QR codes, barcodes, and take part photos.",
            color = Ivory.copy(0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
        ) { Text("Open Settings") }
        TextButton(onClick = onClose) { Text("Go back", color = Ivory) }
    }
}

@Composable
private fun CameraBody(
    mode: ScannerMode,
    onBarcode: (String) -> Unit,
    onPhoto: (String) -> Unit,
    onOcr: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    var torchOn by remember { mutableStateOf(false) }
    var useFront by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(hintFor(mode)) }
    var ocrPreview by remember { mutableStateOf("") }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val handled = remember { AtomicBoolean(false) }

    LaunchedEffect(previewView, useFront, mode) {
        val view = previewView ?: return@LaunchedEffect
        val future = ProcessCameraProvider.getInstance(context)
        val provider = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            future.addListener(
                { cont.resume(future.get()) },
                ContextCompat.getMainExecutor(context)
            )
        }
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCapture = capture

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(executor) { proxy ->
            val main = ContextCompat.getMainExecutor(context)
            when (mode) {
                ScannerMode.BARCODE -> analyzeBarcode(proxy, handled) { code ->
                    main.execute {
                        status = "Found: $code"
                        onBarcode(code)
                    }
                }
                ScannerMode.OCR -> analyzeOcr(proxy) { text ->
                    if (text.isNotBlank()) {
                        main.execute {
                            ocrPreview = text
                            status = "Text detected — tap Use text"
                        }
                    }
                }
                ScannerMode.PHOTO -> proxy.close()
            }
        }

        val selector = if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA
        runCatching {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner, selector, preview, capture, analysis
            )
            runCatching { camera?.cameraControl?.enableTorch(torchOn) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            executor.shutdown()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Box(
                        Modifier.size(40.dp).background(Color.Black.copy(0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, "Close", tint = Ivory)
                    }
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (mode) {
                            ScannerMode.BARCODE -> "Scan QR / Barcode"
                            ScannerMode.PHOTO -> "Part photo"
                            ScannerMode.OCR -> "Scan text (OCR)"
                        },
                        color = Ivory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(status, color = Ivory.copy(0.75f), fontSize = 12.sp)
                }
                IconButton(onClick = {
                    torchOn = !torchOn
                    runCatching { camera?.cameraControl?.enableTorch(torchOn) }
                }) {
                    Icon(
                        if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        "Torch",
                        tint = Ivory
                    )
                }
                IconButton(onClick = { useFront = !useFront }) {
                    Icon(Icons.Filled.Cameraswitch, "Flip", tint = Ivory)
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (mode != ScannerMode.PHOTO) {
                    Box(
                        Modifier
                            .size(240.dp)
                            .border(3.dp, Gold, RoundedCornerShape(24.dp))
                    )
                }
            }

            AnimatedVisibility(visible = mode == ScannerMode.OCR && ocrPreview.isNotBlank()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(0.72f))
                        .padding(12.dp)
                ) {
                    Text(
                        ocrPreview.take(400),
                        color = Ivory,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .height(96.dp)
                            .verticalScroll(rememberScrollState())
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onOcr(ocrPreview) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
                    ) {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use this text")
                    }
                }
            }

            if (mode == ScannerMode.PHOTO) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val capture = imageCapture ?: return@IconButton
                            val dir = File(context.filesDir, "photos").apply { mkdirs() }
                            val file = File(dir, "part_${System.currentTimeMillis()}.jpg")
                            val options = ImageCapture.OutputFileOptions.Builder(file).build()
                            capture.takePicture(
                                options,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        onPhoto(file.absolutePath)
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        status = exception.message ?: "Capture failed"
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .size(78.dp)
                            .background(Ivory, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            "Capture",
                            tint = Ink,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

private fun hintFor(mode: ScannerMode) = when (mode) {
    ScannerMode.BARCODE -> "Point at a QR code or barcode"
    ScannerMode.PHOTO -> "Frame the part, then tap the shutter"
    ScannerMode.OCR -> "Point at a label, bill, or shelf tag"
}

@SuppressLint("UnsafeOptInUsageError")
private fun analyzeBarcode(proxy: ImageProxy, handled: AtomicBoolean, onFound: (String) -> Unit) {
    val media = proxy.image
    if (media == null || handled.get()) {
        proxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
    val scanner = barcodeScanner
    scanner.process(image)
        .addOnSuccessListener { codes ->
            val value = codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
            if (value != null && handled.compareAndSet(false, true)) {
                onFound(value)
            }
        }
        .addOnCompleteListener { proxy.close() }
}

@SuppressLint("UnsafeOptInUsageError")
private fun analyzeOcr(proxy: ImageProxy, onText: (String) -> Unit) {
    val media = proxy.image
    if (media == null) {
        proxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
    ocrRecognizer.process(image)
        .addOnSuccessListener { result ->
            val text = result.text.trim()
            if (text.length >= 3) onText(text)
        }
        .addOnCompleteListener { proxy.close() }
}

private val barcodeScanner by lazy {
    BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_CODABAR
            )
            .build()
    )
}

private val ocrRecognizer by lazy {
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
}
