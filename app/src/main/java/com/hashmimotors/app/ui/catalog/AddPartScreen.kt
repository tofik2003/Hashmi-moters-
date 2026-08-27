package com.hashmimotors.app.ui.catalog

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.hashmimotors.app.domain.model.Part
import kotlinx.coroutines.launch
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.Gold

@Composable
fun AddPartScreen(
    partId: String? = null,
    incomingBarcode: String = "",
    incomingPhoto: String = "",
    incomingOcr: String = "",
    onIncomingConsumed: () -> Unit = {},
    onScanBarcode: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    viewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var oemNumbersText by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var mrp by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("0") }
    var reorderLevel by remember { mutableStateOf("5") }
    var hsnCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

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
                costPrice = part.costPrice?.toString().orEmpty()
                stockQty = part.stockQty.toString()
                reorderLevel = part.reorderLevel.toString()
                hsnCode = part.hsnCode ?: ""
                notes = part.notes ?: ""
                barcode = part.barcode ?: ""
                photoPath = part.photoPaths.firstOrNull().orEmpty()
            }
        }
    }

    LaunchedEffect(incomingBarcode, incomingPhoto, incomingOcr) {
        if (incomingBarcode.isNotBlank()) barcode = incomingBarcode
        if (incomingPhoto.isNotBlank()) photoPath = incomingPhoto
        if (incomingOcr.isNotBlank()) {
            val lines = incomingOcr.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (name.isBlank()) name = lines.firstOrNull().orEmpty().take(80)
            if (notes.isBlank()) notes = incomingOcr.take(400)
            val price = Regex("""(?:₹|Rs\.?)\s*([0-9]+(?:\.[0-9]+)?|[0-9]{2,})""")
                .find(incomingOcr)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            if (price != null && mrp.isBlank()) {
                mrp = price.toString()
                if (sellingPrice.isBlank()) sellingPrice = price.toString()
            }
        }
        if (incomingBarcode.isNotBlank() || incomingPhoto.isNotBlank() || incomingOcr.isNotBlank()) {
            onIncomingConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HmTopBar(
                title = if (partId == null) "Add Part" else "Edit Part",
                subtitle = "Name, price, stock, barcode, photo",
                onBack = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Ivory.copy(alpha = 0.1f))
                    .clickable { onTakePhoto() },
                contentAlignment = Alignment.Center
            ) {
                if (photoPath.isNotBlank()) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Part photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = Ivory,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to open camera",
                            color = Ivory.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Live preview — not just a button",
                            color = Ivory.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Part Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // SKU
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Part Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // OEM Numbers
            OutlinedTextField(
                value = oemNumbersText,
                onValueChange = { oemNumbersText = it },
                label = { Text("OEM Numbers (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 1,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Brand
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category
            if (state.categories.isNotEmpty()) {
                Text(
                    text = "Category",
                    color = Ivory,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = mrp,
                    onValueChange = { mrp = it },
                    label = { Text("MRP *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                colors = hmFieldColors()
            )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Our Price *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                colors = hmFieldColors()
            )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("Cost price (for profit)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Stock row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = stockQty,
                    onValueChange = { stockQty = it.filter { c -> c.isDigit() } },
                    label = { Text("Stock Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                colors = hmFieldColors()
            )
                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it.filter { c -> c.isDigit() } },
                    label = { Text("Reorder Level") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                colors = hmFieldColors()
            )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // HSN Code
            OutlinedTextField(
                value = hsnCode,
                onValueChange = { hsnCode = it },
                label = { Text("HSN Code (for GST)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode / QR") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onScanBarcode) {
                        Icon(Icons.Filled.QrCodeScanner, "Scan", tint = Gold)
                    }
                },
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = hmFieldColors()
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
                    val code = barcode.trim()
                    scope.launch {
                        try {
                        if (code.isNotBlank()) {
                            val existing = viewModel.findByBarcode(code)
                            if (existing != null && existing.id != partId) {
                                error = "Barcode already on “${existing.name}”"
                                saving = false
                                return@launch
                            }
                        }
                        viewModel.savePartNow(
                            Part(
                                id = partId ?: java.util.UUID.randomUUID().toString(),
                                sku = sku.trim(),
                                name = name.trim(),
                                oemNumbers = oemList,
                                brand = brand.ifBlank { null },
                                categoryId = categoryId,
                                mrp = mrpVal,
                                sellingPrice = priceVal,
                                costPrice = costPrice.toDoubleOrNull(),
                                gstPercent = 0.0,
                                hsnCode = hsnCode.ifBlank { null },
                                stockQty = stockQty.toIntOrNull() ?: 0,
                                reorderLevel = reorderLevel.toIntOrNull() ?: 5,
                                barcode = code.ifBlank { null },
                                notes = notes.ifBlank { null },
                                photoPaths = listOfNotNull(photoPath.ifBlank { null })
                            )
                        )
                        onSaved()
                        } catch (e: Exception) {
                            error = e.message ?: "Could not save part"
                            saving = false
                        }
                    }
                }
            )
            if (partId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.TextButton(
                    onClick = {
                        state.parts.find { it.id == partId }?.let { viewModel.deletePart(it) }
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFFFB4AB))
                    Spacer(Modifier.width(8.dp))
                    Text("Remove from catalog", color = Color(0xFFFFB4AB))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
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
