package com.hashmimotors.app.ui.inventory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.catalog.PartListItem
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.components.hmFieldColors

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPartClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(
            title = "Inventory",
            subtitle = "Stock value: ₹${"%,.0f".format(state.totalValue)}",
            onBack = onBack
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Ivory
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All (${state.allStock.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Low (${state.lowStock.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, OEM...") },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Ivory.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
                colors = hmFieldColors()
            )

        Spacer(modifier = Modifier.height(12.dp))

        // List
        val parts = if (selectedTab == 0) state.allStock else state.lowStock
        val filtered = if (searchQuery.isBlank()) parts
        else parts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.oemNumbers.any { oem -> oem.contains(searchQuery, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 1) "No low stock items! 🎉" else "No parts yet",
                    color = Ivory.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { part ->
                    PartListItem(part = part, onClick = { onPartClick(part.id) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
