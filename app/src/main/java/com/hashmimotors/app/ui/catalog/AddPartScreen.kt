package com.hashmimotors.app.ui.catalog

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.components.GlassTextField
import com.hashmimotors.app.ui.sound.Feedback
import com.hashmimotors.app.ui.sound.LocalAppFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AddPartScreen(
    partId: String? = null,
    initialBarcode: String? = null,
    initialName: String? = null,
    initialBrand: String? = null,
    initialPrice: Double? = null,
    viewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val feedback = LocalAppFeedback.current

    var name by remember { mutableStateOf(initialName ?: "") }
    var sku by remember { mutableStateOf("") }
    var oemNumbersText by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf(initialBrand ?: "") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var mrp by remember { mutableStateOf("") }
    var sellingPrice by remember {
        mutableStateOf(initialPrice?.let { if (it > 0) "%.0f".format(it) else "" } ?: "")
    }
    var stockQty by remember { mutableStateOf("0") }
    var reorderLevel by remember { mutableStateOf("5") }
    var hsnCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(initialBarcode ?: "") }
    var photoPaths by remember { mutableStateOf(emptyList<String>()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Camera capture state
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoFile?.let { f ->
                photoPaths = photoPaths + f.absolutePath
            }
        }
    }
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { u ->
            scope.launch {
                val path = withContext(Dispatchers.IO) { copyImageToInternal(context, u) }
                path?.let { photoPaths = photoPaths + it }
            }
        }
    }

    fun takePhoto() {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "part_${System.currentTimeMillis()}.jpg")
        currentPhotoFile = file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        takePicture.launch(uri)
    }

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(partId, state.parts) {
        partId?.let { id ->
            state.parts.find { it.id == id }?.let { part ->
                name = part.name
                sku = part.sku
                oemNumbersText = part.oemNumbers.joinToString(", ")
                brand = part.brand ?: ""
                categoryId = part.categoryId
                mrp = part.mrp.toString()
                sellingPrice = part.sellingPrice.toString()
                stockQty = part.stockQty.toString()
                reorderLevel = part.reorderLevel.toString()
                hsnCode = part.hsnCode ?: ""
                notes = part.notes ?: ""
                barcode = part.barcode ?: ""
                photoPaths = part.photoPaths
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = if (partId == null) "Add Part" else "Edit Part",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // === Photos ===
            Text(
                text = "Photos",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (photoPaths.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { takePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap to take a photo",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    photoPaths.forEach { path ->
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Part photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(11.dp))
                                    .clickable { photoPaths = photoPaths - path },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhotoActionButton("📷 Camera", Modifier.weight(1f)) { takePhoto() }
                PhotoActionButton(
                    "🖼️ Gallery",
                    Modifier.weight(1f)
                ) {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Fields ===
            GlassTextField(
                value = name,
                onValueChange = { name = it },
                label = "Part Name *",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = sku,
                onValueChange = { sku = it },
                label = "SKU / Part Code",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = oemNumbersText,
                onValueChange = { oemNumbersText = it },
                label = "OEM Numbers (comma separated)",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 1
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "Brand",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category
            if (state.categories.isNotEmpty()) {
                Text(
                    text = "Category",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategorySelector(
                    categories = state.categories.map { it.id to it.name },
                    selected = categoryId,
                    onSelect = { categoryId = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Prices row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = mrp,
                    onValueChange = { mrp = it },
                    label = "MRP *",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal
                )
                GlassTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = "Our Price *",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Stock row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = stockQty,
                    onValueChange = { stockQty = it.filter { c -> c.isDigit() } },
                    label = "Stock Qty",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                GlassTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it.filter { c -> c.isDigit() } },
                    label = "Reorder Level",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = hsnCode,
                onValueChange = { hsnCode = it },
                label = "HSN Code (for GST)",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = "Barcode (EAN/UPC/Code-128)",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )

            // Error
            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error ?: "",
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            AnimatedBigButton(
                text = if (saving) "Saving..." else "Save Part",
                icon = Icons.Filled.Save,
                enabled = !saving && name.isNotBlank() && mrp.isNotBlank() && sellingPrice.isNotBlank(),
                onClick = {
                    error = null
                    val mrpVal = mrp.toDoubleOrNull()
                    val priceVal = sellingPrice.toDoubleOrNull()
                    if (mrpVal == null || priceVal == null) {
                        error = "Please enter valid prices"
                        return@AnimatedBigButton
                    }
                    if (priceVal > mrpVal) {
                        error = "Our price cannot be higher than MRP"
                        return@AnimatedBigButton
                    }
                    saving = true
                    val oemList = oemNumbersText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    viewModel.savePart(
                        Part(
                            id = partId ?: java.util.UUID.randomUUID().toString(),
                            sku = sku.trim(),
                            name = name.trim(),
                            oemNumbers = oemList,
                            brand = brand.ifBlank { null },
                            categoryId = categoryId,
                            mrp = mrpVal,
                            sellingPrice = priceVal,
                            gstPercent = 0.0, // Composition scheme
                            hsnCode = hsnCode.ifBlank { null },
                            stockQty = stockQty.toIntOrNull() ?: 0,
                            reorderLevel = reorderLevel.toIntOrNull() ?: 5,
                            barcode = barcode.ifBlank { null },
                            photoPaths = photoPaths,
                            notes = notes.ifBlank { null }
                        )
                    )
                    Feedback.success(feedback)
                    onSaved()
                }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PhotoActionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategorySelector(
    categories: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = categories.size) { idx ->
            val (id, name) = categories[idx]
            CategoryChip(
                name = name,
                selected = selected == id,
                onClick = { onSelect(id) }
            )
        }
    }
}

private fun copyImageToInternal(context: Context, uri: Uri): String? {
    return runCatching {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val dest = File(dir, "part_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    }.getOrNull()
}
