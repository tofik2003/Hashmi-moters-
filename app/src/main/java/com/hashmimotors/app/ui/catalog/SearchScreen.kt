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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.util.QrProduct
import com.hashmimotors.app.util.QrProductParser

@Composable
fun SearchScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    onPartClick: (String) -> Unit,
    onAddPartClick: () -> Unit,
    onAddPartWithBarcode: (String) -> Unit,
    onAddPartWithProduct: (QrProduct) -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit,
    scannedBarcode: String = "",
    onBarcodeConsumed: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    var scannedValue by remember { mutableStateOf("") }

    // Route a scanned code: exact match opens the part; a full product payload
    // goes to Add Part pre-filled; otherwise fall back to a plain search.
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode.isNotBlank()) {
            val match = viewModel.findByBarcode(scannedBarcode)
            when {
                match != null -> onPartClick(match.id)
                else -> {
                    val product = QrProductParser.parse(scannedBarcode)
                    if (product != null) {
                        onAddPartWithProduct(product)
                    } else {
                        scannedValue = scannedBarcode
                        viewModel.onSearchChange(scannedBarcode)
                    }
                }
            }
            onBarcodeConsumed()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                text = "Search Parts",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onSearchChange(it) },
            placeholder = { Text("Search name, OEM, brand, SKU, barcode…") },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.6f)) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Filled.Clear, "Clear", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Filled.QrCodeScanner, "Scan", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.onSearchSubmit(query)
                focusManager.clearFocus()
            }),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Recent searches
        if (query.isBlank() && state.recentSearches.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.clearRecentSearches() }) {
                    Text("Clear", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.recentSearches, key = { it }) { recent ->
                    SearchChip(text = recent, onClick = { viewModel.selectRecentSearch(recent) })
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Reference catalog type-ahead suggestions
        if (query.isNotBlank()) {
            val suggestions = viewModel.searchReferenceParts(query, limit = 8)
            if (suggestions.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(suggestions, key = { it.name }) { s ->
                        SuggestionChip(
                            name = s.name,
                            onClick = { viewModel.onSearchChange(s.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Category chips
        if (state.categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        label = "All",
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.onCategorySelect(null) }
                    )
                }
                items(state.categories) { cat ->
                    FilterChip(
                        label = cat.name,
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.onCategorySelect(cat.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Stock status chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(StockFilter.values().toList(), key = { it.name }) { stock ->
                FilterChip(
                    label = stock.label,
                    selected = state.stockFilter == stock,
                    onClick = { viewModel.onStockFilter(stock) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Sort & brand filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortDropdown(
                selected = state.sortMode,
                onSelect = { viewModel.onSortChange(it) }
            )
            BrandDropdown(
                selected = state.brandFilter,
                brands = state.availableBrands,
                onSelect = { viewModel.onBrandFilter(it) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results count
        Text(
            text = "${state.parts.size} part${if (state.parts.size != 1) "s" else ""} found",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Results list
        if (state.parts.isEmpty()) {
            EmptySearchState(
                hasQuery = query.isNotBlank() || state.selectedCategoryId != null ||
                    state.brandFilter != null || state.stockFilter != StockFilter.ALL,
                scannedValue = scannedValue,
                onAddPartClick = onAddPartClick,
                onAddPartWithBarcode = onAddPartWithBarcode
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.parts, key = { it.id }) { part ->
                    PartListItem(
                        part = part,
                        query = query,
                        onClick = { onPartClick(part.id) }
                    )
                }
            }
        }
    }

    // Floating add button
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onAddPartClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Part",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun EmptySearchState(
    hasQuery: Boolean,
    scannedValue: String,
    onAddPartClick: () -> Unit,
    onAddPartWithBarcode: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasQuery) "No parts match your search" else "No parts yet",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (scannedValue.isNotBlank())
                    "Barcode \"$scannedValue\" isn't in your catalog yet"
                else
                    "Add it to your catalog to start selling",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (scannedValue.isNotBlank()) {
                TextButton(onClick = { onAddPartWithBarcode(scannedValue) }) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Add part with this barcode",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                TextButton(onClick = onAddPartClick) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add a part", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(label = name, selected = selected, onClick = onClick)
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.White.copy(alpha = 0.1f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SearchChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text = text, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

@Composable
private fun SuggestionChip(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(name, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SortDropdown(
    selected: SortMode,
    onSelect: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterPill("Sort: ${selected.label}", onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onSelect(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BrandDropdown(
    selected: String?,
    brands: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterPill("Brand: ${selected ?: "All"}", onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All brands") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            brands.forEach { brand ->
                DropdownMenuItem(
                    text = { Text(brand) },
                    onClick = {
                        onSelect(brand)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
    }
}

@Composable
fun PartListItem(
    part: Part,
    onClick: () -> Unit,
    query: String = ""
) {
    val isLowStock = part.stockQty <= part.reorderLevel
    val isOutOfStock = part.stockQty == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = part.name.take(2).uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Part info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightQuery(part.name, query),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                val oem = part.oemNumbers.firstOrNull()
                if (oem != null) {
                    Text(
                        text = "OEM: $oem",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                val meta = listOfNotNull(
                    part.brand,
                    part.sku.takeIf { it.isNotBlank() },
                    part.barcode
                ).joinToString("  •  ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            // Price and stock
            Column(horizontalAlignment = Alignment.End) {
                com.hashmimotors.app.ui.components.PriceWithDiscount(
                    originalPrice = part.mrp,
                    discountedPrice = part.sellingPrice
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isOutOfStock -> Color(0xFFC62828)
                                isLowStock -> Color(0xFFFFA000)
                                else -> Color(0xFF2E7D32)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isOutOfStock) "OUT" else "Stock: ${part.stockQty}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Highlights every case-insensitive occurrence of [query] inside [text]. */
@Composable
private fun highlightQuery(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    val q = query.trim()
    return buildAnnotatedString {
        if (q.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }
        var start = 0
        while (true) {
            val idx = text.indexOf(q, start, ignoreCase = true)
            if (idx < 0) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, idx))
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                append(text.substring(idx, idx + q.length))
            }
            start = idx + q.length
        }
    }
}
