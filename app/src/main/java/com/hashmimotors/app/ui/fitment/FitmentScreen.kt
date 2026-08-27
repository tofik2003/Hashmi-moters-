package com.hashmimotors.app.ui.fitment

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.Gold

@Composable
fun FitmentScreen(
    viewModel: FitmentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPartClick: (String) -> Unit = {},
    onVehicleSelected: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val step = if (state.selectedMake == null) 0
               else if (state.selectedVehicleId == null) 1
               else 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        HmTopBar(
            title = "Find parts by vehicle",
            subtitle = "Brand → model → compatible SKUs",
            onBack = {
                if (step == 0) onBack()
                else viewModel.reset()
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Brand", "Model", "Parts").forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (step >= idx) Gold
                            else Ivory.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (step) {
            0 -> {
                Text("Step 1: Select brand", color = Ivory, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                if (state.allMakes.isEmpty()) {
                    Text("Loading brands…", color = Ivory.copy(alpha = 0.6f))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.allMakes) { make ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clickable { viewModel.selectMake(make) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.DirectionsCar, null, tint = Gold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(make, color = Ivory, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                Text(
                    "Step 2: Select ${state.selectedMake} model",
                    color = Ivory,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.models, key = { it.id }) { vehicle ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectVehicle(vehicle.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    vehicle.model,
                                    color = Ivory,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${vehicle.yearFrom}-${vehicle.yearTo}",
                                    color = Ivory.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                val vehicle = state.models.find { it.id == state.selectedVehicleId }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text(
                            "${vehicle?.make.orEmpty()} ${vehicle?.model.orEmpty()}",
                            color = Ivory,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Years ${vehicle?.yearFrom}-${vehicle?.yearTo} · ${vehicle?.fuelTypes?.joinToString().orEmpty()}",
                            color = Ivory.copy(0.7f),
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Text(
                            if (state.compatibleParts.isEmpty()) "No linked parts yet — tap Link below"
                            else "${state.compatibleParts.size} compatible parts",
                            color = Ivory,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(state.compatibleParts, key = { it.id }) { part ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onPartClick(part.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Ivory.copy(0.1f))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(part.name, color = Ivory, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "₹${"%,.0f".format(part.sellingPrice)}  ·  Stock ${part.stockQty}",
                                    color = Ivory.copy(0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            "Catalog — tap to link to this vehicle",
                            color = Ivory.copy(0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    items(state.catalogParts, key = { "c-${it.id}" }) { part ->
                        val linked = state.compatibleParts.any { it.id == part.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!linked) viewModel.linkPart(part.id)
                                    vehicle?.id?.let(onVehicleSelected)
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (linked) Color(0xFF2E7D32).copy(0.35f)
                                else Ivory.copy(0.06f)
                            )
                        ) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(part.name, color = Ivory, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                Text(if (linked) "Linked" else "Link", color = Gold, fontSize = 12.sp)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
