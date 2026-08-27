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
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ink
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

@Composable
fun ScanStationScreen(
    initialAction: ScanAction = ScanAction.RECEIVE,
    viewModel: ScanStationViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onOpenPart: (String) -> Unit,
    onAddUnknown: (String) -> Unit,
    onBillPart: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(initialAction) { viewModel.setAction(initialAction) }

    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        permissionAsked = true
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasPermission = true else launcher.launch(Manifest.permission.CAMERA)
    }

    when {
        hasPermission -> StationBody(
            state = state,
            viewModel = viewModel,
            onClose = onClose,
            onOpenPart = onOpenPart,
            onAddUnknown = onAddUnknown,
            onBillPart = onBillPart
        )
        permissionAsked -> Column(
            Modifier.fillMaxSize().background(Color(0xFF0E0E16)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Camera needed for fast scan",
                color = Ivory,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
            ) { Text("Open Settings") }
            TextButton(onClick = onClose) { Text("Go back", color = Ivory) }
        }
        else -> Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Requesting camera…", color = Ivory)
        }
    }
}

@Composable
private fun StationBody(
    state: ScanStationUiState,
    viewModel: ScanStationViewModel,
    onClose: () -> Unit,
    onOpenPart: (String) -> Unit,
    onAddUnknown: (String) -> Unit,
    onBillPart: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var torchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val lastCode = remember { AtomicReference("") }
    val lastAt = remember { AtomicLong(0L) }
    val lastOcrAt = remember { AtomicLong(0L) }

    LaunchedEffect(previewView, state.action) {
        val view = previewView ?: return@LaunchedEffect
        val future = ProcessCameraProvider.getInstance(context)
        val provider = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            future.addListener({ cont.resume(future.get()) }, ContextCompat.getMainExecutor(context))
        }
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val identify = state.action == ScanAction.IDENTIFY
        analysis.setAnalyzer(executor) { proxy ->
            val main = ContextCompat.getMainExecutor(context)
            analyzeStationFrame(
                proxy = proxy,
                lastCode = lastCode,
                lastAt = lastAt,
                lastOcrAt = lastOcrAt,
                identify = identify,
                onCode = { code -> main.execute { viewModel.onBarcode(code) } },
                onOcr = { text -> main.execute { viewModel.onOcr(text) } }
            )
        }
        runCatching {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
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

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Box(
                        Modifier.size(40.dp).background(Color.Black.copy(0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, "Close", tint = Ivory)
                    }
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fast scan", color = Ivory, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(state.message, color = Ivory.copy(0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                IconButton(onClick = {
                    torchOn = !torchOn
                    runCatching { camera?.cameraControl?.enableTorch(torchOn) }
                }) {
                    Icon(if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff, "Torch", tint = Ivory)
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(240.dp).border(3.dp, Gold, RoundedCornerShape(24.dp)))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(Color(0xF214161E))
                    .padding(16.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val actions = listOf(
                        ScanAction.RECEIVE to "In",
                        ScanAction.CHECKOUT to "Out",
                        ScanAction.LOOKUP to "Find",
                        ScanAction.IDENTIFY to "Label"
                    )
                    items(actions, key = { it.first.name }) { (action, label) ->
                        FilterChip(
                            selected = state.action == action,
                            onClick = { viewModel.setAction(action) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Qty", color = IvoryMute, fontSize = 12.sp)
                    IconButton(onClick = { viewModel.setQty(state.qty - 1) }) {
                        Icon(Icons.Filled.Remove, "Less", tint = Ivory)
                    }
                    Text("${state.qty}", color = Ivory, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { viewModel.setQty(state.qty + 1) }) {
                        Icon(Icons.Filled.Add, "More", tint = Ivory)
                    }
                    Spacer(Modifier.width(8.dp))
                    listOf(1, 2, 5, 10).forEach { n ->
                        Text(
                            "$n",
                            color = if (state.qty == n) Ink else Ivory,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.qty == n) Gold else Ivory.copy(0.12f))
                                .clickable { viewModel.setQty(n) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                state.lastPart?.let { part ->
                    Spacer(Modifier.height(8.dp))
                    HitCard(part = part, onOpen = { onOpenPart(part.id) }, onBill = { onBillPart(part.id) })
                }

                state.unknownCode?.let { code ->
                    Spacer(Modifier.height(8.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x33C62828))
                            .padding(12.dp)
                    ) {
                        Text("Not in catalog", color = Ivory, fontWeight = FontWeight.SemiBold)
                        Text(code, color = IvoryMute, fontSize = 12.sp)
                        Row {
                            TextButton(onClick = { onAddUnknown(code) }) { Text("Add part", color = Gold) }
                            TextButton(onClick = { viewModel.dismissUnknown() }) { Text("Ignore", color = IvoryMute) }
                        }
                    }
                }

                if (state.identifyHits.isNotEmpty() && state.action == ScanAction.IDENTIFY) {
                    Spacer(Modifier.height(8.dp))
                    Text("Label matches", color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Column(Modifier.height(140.dp).verticalScroll(rememberScrollState())) {
                        state.identifyHits.forEach { part ->
                            HitCard(
                                part = part,
                                onOpen = { onOpenPart(part.id) },
                                onBill = { onBillPart(part.id) },
                                onUse = { viewModel.applyHit(part) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                if (state.session.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("This session", color = IvoryMute, fontSize = 11.sp)
                    state.session.take(4).forEach { ev ->
                        Text(
                            "${if (ev.delta > 0) "+" else ""}${ev.delta}  ${ev.partName}" +
                                (ev.stockAfter?.let { "  → $it" } ?: ""),
                            color = Ivory,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HitCard(
    part: Part,
    onOpen: () -> Unit,
    onBill: () -> Unit,
    onUse: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ivory.copy(0.08f))
            .clickable { onUse?.invoke() ?: onOpen() }
            .padding(12.dp)
    ) {
        Text(part.name, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            listOfNotNull(part.brand, part.sku.takeIf { it.isNotBlank() }, "Stock ${part.stockQty}").joinToString(" · "),
            color = IvoryMute,
            fontSize = 12.sp
        )
        Row {
            TextButton(onClick = onOpen) { Text("Open", color = Gold) }
            TextButton(onClick = onBill) { Text("Bill", color = Gold) }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun analyzeStationFrame(
    proxy: ImageProxy,
    lastCode: AtomicReference<String>,
    lastAt: AtomicLong,
    lastOcrAt: AtomicLong,
    identify: Boolean,
    onCode: (String) -> Unit,
    onOcr: (String) -> Unit
) {
    val media = proxy.image
    if (media == null) {
        proxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
    val pending = AtomicInteger(if (identify) 2 else 1)
    fun done() {
        if (pending.decrementAndGet() <= 0) proxy.close()
    }
    stationBarcode.process(image)
        .addOnSuccessListener { codes ->
            val value = codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue ?: return@addOnSuccessListener
            val now = System.currentTimeMillis()
            val same = value == lastCode.get() && now - lastAt.get() < 1600L
            if (!same) {
                lastCode.set(value)
                lastAt.set(now)
                onCode(value)
            }
        }
        .addOnCompleteListener { done() }
    if (identify) {
        val now = System.currentTimeMillis()
        if (now - lastOcrAt.get() < 900L) {
            done()
            return
        }
        lastOcrAt.set(now)
        stationOcr.process(image)
            .addOnSuccessListener { result ->
                val text = result.text.trim()
                if (text.length >= 3) onOcr(text)
            }
            .addOnCompleteListener { done() }
    }
}

private val stationBarcode by lazy {
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

private val stationOcr by lazy {
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
}
