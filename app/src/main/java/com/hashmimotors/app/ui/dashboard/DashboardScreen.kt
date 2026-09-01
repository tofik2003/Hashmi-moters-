package com.hashmimotors.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.ui.components.AnimatedBarChart
import com.hashmimotors.app.ui.components.AnimatedCounter
import com.hashmimotors.app.ui.components.GlassCard
import com.hashmimotors.app.ui.components.GradientHeroCard
import com.hashmimotors.app.ui.components.PromotionCarousel
import com.hashmimotors.app.ui.components.SectionHeader
import com.hashmimotors.app.ui.components.StatTile
import com.hashmimotors.app.ui.theme.PremiumGold
import com.hashmimotors.app.ui.theme.StatusWarning

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSearch: () -> Unit,
    onNewBill: () -> Unit,
    onAddStock: () -> Unit,
    onAddPart: () -> Unit,
    onFitment: () -> Unit,
    onInventory: () -> Unit,
    onReports: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onCustomers: () -> Unit = {},
    onScanBill: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val quickActions = remember {
        listOf(
            QuickAction("search", "Search", Icons.Filled.Search, Color(0xFF38BDF8)) { onSearch() },
            QuickAction("bill", "New Bill", Icons.Filled.Receipt, Color(0xFF34D399)) { onNewBill() },
            QuickAction("add_part", "Add Part", Icons.Filled.Build, Color(0xFFFBBF24)) { onAddPart() },
            QuickAction("stock", "Add Stock", Icons.Filled.Inventory, Color(0xFFA78BFA)) { onAddStock() },
            QuickAction("fitment", "Vehicle", Icons.Filled.DirectionsCar, Color(0xFFFB7185)) { onFitment() },
            QuickAction("scan", "Scan Bill", Icons.Filled.ShoppingCart, Color(0xFF22D3EE)) { onScanBill() },
            QuickAction("history", "History", Icons.Filled.Receipt, Color(0xFF818CF8)) { onHistory() },
            QuickAction("customers", "Customers", Icons.Filled.Person, Color(0xFF14B8A6)) { onCustomers() }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ===== Header =====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(10.dp, CircleShape, clip = false)
                        .background(
                            brush = Brush.linearGradient(listOf(PremiumGold, Color(0xFFF97316))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state.shopName.take(2).ifBlank { "HM" }).uppercase(),
                        color = Color(0xFF3B2200),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.shopName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onSettings) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ===== Today's Sales hero =====
        item {
            GradientHeroCard(
                modifier = Modifier.fillMaxWidth(),
                colors = listOf(Color(0xFFF5B942), Color(0xFFF59E0B), Color(0xFFFB7185).copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Sales",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AnimatedCounter(target = state.todaySales, fontSize = 40, textColor = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💰", fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeroChip(label = "${state.todayBills} bills")
                        HeroChip(label = "${state.totalParts} parts")
                        HeroChip(label = "${state.lowStockCount} low")
                    }
                }
            }
        }

        // ===== Quick actions =====
        item {
            Column {
                SectionHeader(title = "Quick Actions")
                Spacer(modifier = Modifier.height(10.dp))
                val rows = quickActions.chunked(4)
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { action ->
                            QuickActionTile(action = action, modifier = Modifier.weight(1f))
                        }
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (index < rows.size - 1) Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // ===== KPI tiles =====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "Total Parts",
                    value = state.totalParts.toString(),
                    icon = Icons.Filled.Inventory,
                    accent = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Stock Value",
                    value = "₹${\"%,.0f\".format(state.totalStockValue)}",
                    icon = Icons.Filled.BarChart,
                    accent = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "This Week",
                    value = "₹${\"%,.0f\".format(state.weekTotal)}",
                    icon = Icons.Filled.Receipt,
                    accent = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Low Stock",
                    value = state.lowStockCount.toString(),
                    icon = Icons.Filled.Warning,
                    accent = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== Weekly sales chart =====
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                containerAlpha = 0.08f,
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales This Week",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₹${\"%,.0f\".format(state.weekTotal)}",
                            color = PremiumGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedBarChart(
                        data = state.weekSales.map { it.value.toFloat() },
                        labels = state.weekSales.map { it.label }
                    )
                }
            }
        }

        // ===== Low stock alert =====
        if (state.lowStockCount > 0) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInventory() },
                    containerAlpha = 0.12f,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StatusWarning.copy(alpha = 0.30f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(StatusWarning.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Stock Alert",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.lowStockCount} parts need restocking",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // ===== Reports + promotions =====
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReports() },
                containerAlpha = 0.10f,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detailed Reports",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Daily, monthly & GST analytics",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            PromotionCarousel()
        }
    }
}

@Composable
private fun HeroChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickActionTile(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable { action.onClick() },
        shape = RoundedCornerShape(18.dp),
        containerAlpha = 0.10f,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(action.color.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(action.icon, contentDescription = null, tint = action.color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)
