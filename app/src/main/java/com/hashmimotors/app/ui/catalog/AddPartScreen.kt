package com.hashmimotors.app.ui.catalog

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.AsyncImage
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.components.AnimatedBigButton
import kotlinx.coroutines.launch

@Composable
fun AddPartScreen(
    partId: String? = null,
    prefillBarcode: String? = null,
    viewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onScanBarcode: () -> Unit = {},
    scannedBarcode: String = "",
    onBarcodeConsumed: () -> Unit = {}
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
    var gstPercent by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var photoPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var warning by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoPaths = (photoPaths + it.toString()).distinct() }
    }

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
                costPrice = part.costPrice?.toString() ?: ""
                stockQty = part.stockQty.toString()
                reorderLevel = part.reorderLevel.toString()
                hsnCode = part.hsnCode ?: ""
                gstPercent = if (part.gstPercent > 0.0) part.gstPercent.toString() else ""
                notes = part.notes ?: ""
                barcode = part.barcode ?: ""
                photoPaths = part.photoPaths
                initialized = true
            }
        }
    }

    // Auto-generate SKU & barcode for brand-new parts.
    LaunchedEffect(partId) {
        if (partId == null && !initialized) {
            sku = viewModel.nextSku()
            barcode = viewModel.generateBarcode()
            initialized = true
        }
    }

    // Pre-fill barcode when launched with one (e.g. "add part with this barcode").
    LaunchedEffect(prefillBarcode) {
        if (!prefillBarcode.isNullOrBlank() && barcode.isBlank()) {
            barcode = prefillBarcode
        }
    }

    // Barcode scanned from the camera while editing this form.
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode.isNotBlank()) {
            barcode = scannedBarcode
            onBarcodeConsumed()
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

            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { photoPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val photo = photoPaths.firstOrNull()
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = "Part photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (photoPaths.isEmpty()) "Tap to add photo" else "Tap to change photo",
                            color = Color.White.copy(alpha = 0.5f),
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
                singleLine = true
            )

            // Quick-add suggestions from the bundled reference catalog
            if (name.isNotBlank()) {
                val suggestions = viewModel.searchReferenceParts(name, limit = 6)
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Suggested from catalog — tap to fill",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            suggestions.forEach { s ->
                                val cat = state.categories.firstOrNull {
                                    it.name.equals(s.category, ignoreCase = true)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            name = s.name
                                            categoryId = cat?.id
                                            hsnCode = s.hsnCode ?: hsnCode
                                            gstPercent = if (s.gstPercent > 0.0) s.gstPercent.toString() else gstPercent
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = s.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = listOfNotNull(
                                                s.category,
                                                s.hsnCode?.let { "HSN $it" },
                                                if (s.gstPercent > 0.0) "GST ${s.gstPercent}%" else null
                                            ).joinToString("  •  "),
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // SKU
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Part Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // OEM Numbers
            OutlinedTextField(
                value = oemNumbersText,
                onValueChange = { oemNumbersText = it },
                label = { Text("OEM Numbers (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 1
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Brand
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                    onSelect = { id ->
                        categoryId = id
                        val catName = state.categories.firstOrNull { it.id == id }?.name
                        if (catName != null) {
                            val defaults = viewModel.categoryDefaults(catName)
                            if (hsnCode.isBlank()) hsnCode = defaults?.defaultHsn ?: ""
                            if (gstPercent.isBlank() && defaults?.defaultGstPercent != null && defaults.defaultGstPercent > 0.0) {
                                gstPercent = defaults.defaultGstPercent.toString()
                            }
                        }
                    }
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
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Our Price *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Selling-price quick sets from MRP
            val mrpVal = mrp.toDoubleOrNull()
            if (mrpVal != null && mrpVal > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set price:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    listOf(0 to "MRP", 5 to "−5%", 10 to "−10%", 15 to "−15%", 20 to "−20%").forEach { (pct, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    val discount = mrpVal * pct / 100.0
                                    sellingPrice = String.format(java.util.Locale.US, "%.2f", mrpVal - discount)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Cost price + live margin
            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("Cost Price (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            val costVal = costPrice.toDoubleOrNull()
            val priceValNow = sellingPrice.toDoubleOrNull()
            if (costVal != null && priceValNow != null && priceValNow > 0.0) {
                val margin = ((priceValNow - costVal) / priceValNow * 100.0)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Margin: ${String.format(java.util.Locale.US, "%.1f", margin)}%",
                    color = if (margin >= 0.0) Color(0xFF34D399) else Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                    singleLine = true
                )
                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it.filter { c -> c.isDigit() } },
                    label = { Text("Reorder Level") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // HSN Code
            OutlinedTextField(
                value = hsnCode,
                onValueChange = { hsnCode = it },
                label = { Text("HSN Code (for GST)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // GST %
            OutlinedTextField(
                value = gstPercent,
                onValueChange = { gstPercent = it },
                label = { Text("GST % (auto-filled from category)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Barcode
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode (EAN/UPC/Code-128)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onScanBarcode) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan barcode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Warning (non-blocking duplicate name)
            if (warning != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = warning ?: "",
                    color = Color(0xFFFFA000),
                    fontSize = 13.sp
                )
            }

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
                    scope.launch {
                        error = null
                        warning = null
                        val trimmedName = name.trim()
                        val trimmedSku = sku.trim()
                        val trimmedBarcode = barcode.trim()
                        val mrpVal = mrp.toDoubleOrNull()
                        val priceVal = sellingPrice.toDoubleOrNull()

                        if (trimmedName.isBlank()) {
                            error = "Part name is required"
                            return@launch
                        }
                        if (mrpVal == null || mrpVal <= 0.0) {
                            error = "Please enter a valid MRP"
                            return@launch
                        }
                        if (priceVal == null || priceVal <= 0.0) {
                            error = "Please enter a valid selling price"
                            return@launch
                        }
                        if (priceVal > mrpVal) {
                            error = "Our price cannot be higher than MRP"
                            return@launch
                        }

                        // Duplicate detection (exact SKU / barcode block, name warns)
                        if (trimmedSku.isNotBlank()) {
                            val dup = viewModel.findBySku(trimmedSku)
                            if (dup != null && dup.id != partId) {
                                error = "SKU \"$trimmedSku\" is already used by ${dup.name}"
                                return@launch
                            }
                        }
                        if (trimmedBarcode.isNotBlank()) {
                            val dup = viewModel.findByBarcode(trimmedBarcode)
                            if (dup != null && dup.id != partId) {
                                error = "Barcode \"$trimmedBarcode\" is already used by ${dup.name}"
                                return@launch
                            }
                        }
                        val dupName = viewModel.findByName(trimmedName)
                        if (dupName != null && dupName.id != partId) {
                            warning = "A part named \"$trimmedName\" already exists — saving will create a duplicate."
                        }

                        saving = true
                        val oemList = oemNumbersText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        viewModel.savePart(
                            Part(
                                id = partId ?: java.util.UUID.randomUUID().toString(),
                                sku = trimmedSku.ifBlank { viewModel.nextSku() },
                                name = trimmedName,
                                oemNumbers = oemList,
                                brand = brand.ifBlank { null },
                                categoryId = categoryId,
                                mrp = mrpVal,
                                sellingPrice = priceVal,
                                costPrice = costPrice.toDoubleOrNull(),
                                gstPercent = gstPercent.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                                hsnCode = hsnCode.ifBlank { null },
                                stockQty = stockQty.toIntOrNull() ?: 0,
                                reorderLevel = reorderLevel.toIntOrNull() ?: 5,
                                barcode = trimmedBarcode.ifBlank { null },
                                photoPaths = photoPaths,
                                notes = notes.ifBlank { null }
                            )
                        )
                        onSaved()
                    }
                }
            )
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
