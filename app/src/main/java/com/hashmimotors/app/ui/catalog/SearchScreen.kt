package com.hashmimotors.app.ui.catalog

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.Gold

@Composable
fun SearchScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    incomingQuery: String = "",
    onIncomingConsumed: () -> Unit = {},
    onPartClick: (String) -> Unit,
    onAddPartClick: () -> Unit,
    onScanClick: () -> Unit,
    onAddToBill: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(incomingQuery) {
        if (incomingQuery.isNotBlank()) {
            viewModel.onSearchChange(incomingQuery)
            onIncomingConsumed()
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) viewModel.onSearchChange(spoken)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            HmTopBar(title = "Catalog", subtitle = "Search, scan, or add a part", onBack = onBack)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                placeholder = { Text("Name, OEM, brand, barcode…") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Ivory.copy(alpha = 0.6f)) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a part name")
                            }
                            runCatching { voiceLauncher.launch(intent) }
                                .onFailure {
                                    Toast.makeText(context, "Voice not available", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Icon(Icons.Filled.Mic, "Voice", tint = Ivory)
                        }
                        IconButton(onClick = onScanClick) {
                            Icon(Icons.Filled.QrCodeScanner, "Scan", tint = Gold)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(16.dp),
                colors = hmFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.categories.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleLowStockOnly() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (state.showLowStockOnly) MaterialTheme.colorScheme.primary else Ivory.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Show low stock only",
                    color = Ivory,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "${state.parts.size} part${if (state.parts.size != 1) "s" else ""} found",
                color = Ivory.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.parts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Ivory.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.searchQuery.isBlank() && state.selectedCategoryId == null)
                                "Catalog is empty"
                            else "No parts match",
                            color = Ivory,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Scan a barcode, add a part, or load sample stock to try the app.",
                            color = Ivory.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onScanClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF07080C))
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open camera scanner")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val n = viewModel.seedDemoCatalog()
                                    Toast.makeText(
                                        context,
                                        if (n == 0) "Already has parts" else "Loaded $n sample parts",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Ivory.copy(0.18f), contentColor = Ivory)
                        ) {
                            Text("Load sample catalog")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 120.dp)
                ) {
                    items(state.parts, key = { it.id }) { part ->
                        PartListItem(
                            part = part,
                            onClick = { onPartClick(part.id) },
                            onBillClick = { onAddToBill(part.id) }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Gold, CircleShape)
                    .clickable { onAddPartClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Part",
                    tint = Color(0xFF07080C),
                    modifier = Modifier.size(28.dp)
                )
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) Gold
                else Ivory.copy(alpha = 0.1f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = if (selected) Color(0xFF07080C) else Ivory.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun PartListItem(
    part: Part,
    onClick: () -> Unit,
    onBillClick: (() -> Unit)? = null
) {
    val isLowStock = part.stockQty <= part.reorderLevel
    val isOutOfStock = part.stockQty == 0
    val photo = part.photoPaths.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivory.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (photo != null) {
                    AsyncImage(
                        model = File(photo),
                        contentDescription = part.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = part.name.take(2).uppercase(),
                        color = Ivory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.name,
                    color = Ivory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (part.oemNumbers.isNotEmpty()) {
                    Text(
                        text = "OEM: ${part.oemNumbers.first()}",
                        color = Ivory.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                val meta = listOfNotNull(part.brand, part.barcode?.let { "QR $it" })
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta.joinToString(" · "),
                        color = Ivory.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
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
                        color = Ivory,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

