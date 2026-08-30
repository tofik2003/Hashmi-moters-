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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.components.GlassTextField
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import java.io.File

@Composable
fun SearchScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    onPartClick: (String) -> Unit,
    onAddPartClick: () -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                text = "Parts Catalog",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onImportClick) {
                Icon(Icons.Filled.UploadFile, "Import CSV", tint = BrandGoldBright)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Search bar
        GlassTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchChange(it) },
            label = "Search Parts",
            placeholder = "Search name, OEM, brand, barcode...",
            leadingIcon = Icons.Filled.Search,
            trailingIcon = {
                IconButton(onClick = onScanClick) {
                    Icon(Icons.Filled.QrCodeScanner, "Scan", tint = BrandGoldBright)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Search
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category chips
        if (state.categories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        name = "All",
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.onCategorySelect(null) }
                    )
                }
                items(state.categories) { cat ->
                    CategoryChip(
                        name = cat.name,
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.onCategorySelect(cat.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Low stock toggle & count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.parts.size} part${if (state.parts.size != 1) "s" else ""}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.toggleLowStockOnly() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (state.showLowStockOnly) Color(0xFFFFA000) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Low stock only",
                    color = if (state.showLowStockOnly) Color(0xFFFFA000) else Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = if (state.showLowStockOnly) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results list
        if (state.parts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.searchQuery.isBlank() && state.selectedCategoryId == null)
                            "No parts in catalog"
                        else "No matching parts found",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the + button to add a part, or import a CSV catalog",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.parts, key = { it.id }) { part ->
                    PartListItem(
                        part = part,
                        onClick = { onPartClick(part.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    // Floating add button
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 76.dp, end = 20.dp), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(BrandGold, CircleShape)
                .clickable { onAddPartClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Part",
                tint = Color(0xFF1A1A2E),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) BrandGold
                else Color.White.copy(alpha = 0.1f)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            color = if (selected) Color(0xFF1A1A2E) else Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PartListItem(
    part: Part,
    onClick: () -> Unit
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
            // Photo or Letter Monogram
            if (part.photoPaths.isNotEmpty() && File(part.photoPaths.first()).exists()) {
                AsyncImage(
                    model = File(part.photoPaths.first()),
                    contentDescription = part.name,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = BrandGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = part.name.take(2).uppercase(),
                        color = BrandGoldBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Part info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (part.oemNumbers.isNotEmpty()) {
                    Text(
                        text = "OEM: ${part.oemNumbers.first()}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                if (part.brand != null) {
                    Text(
                        text = part.brand,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
            // Price and stock
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${"%,.0f".format(part.sellingPrice)}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (part.mrp > part.sellingPrice) {
                    Text(
                        text = "MRP ₹${"%,.0f".format(part.mrp)}",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
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
