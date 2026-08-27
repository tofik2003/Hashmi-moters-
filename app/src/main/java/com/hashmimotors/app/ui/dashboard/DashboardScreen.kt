package com.hashmimotors.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.hashmimotors.app.ui.components.PromotionCarousel
import com.hashmimotors.app.ui.components.SparklineCard
import com.hashmimotors.app.ui.components.SavingsHighlight
import com.hashmimotors.app.ui.theme.StatusWarning
import androidx.compose.runtime.remember

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
    onCustomers: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            // Top bar with greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.shopName,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    val greeting = remember {
                        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        when {
                            hour < 12 -> "Good morning ☀️"
                            hour < 17 -> "Good afternoon 🌤️"
                            else -> "Good evening 🌙"
                        }
                    }
                    Text(
                        text = greeting,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onSettings) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // === PROMOTIONS CAROUSEL ===
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PromotionCarousel()
        }

        // === TODAY'S SALES WITH ANIMATED COUNTER ===
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Today's sales card with animated counter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = false
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Sales",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedCounter(
                                target = state.todaySales,
                                fontSize = 32
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                StatChip(label = "Bills", value = state.todayBills.toString())
                                StatChip(label = "Items", value = state.totalParts.toString())
                            }
                        }
                        // Decorative element
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFA726),
                                            Color(0xFFFF6B35)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💰", fontSize = 32.sp)
                        }
                    }
                }
            }
        }

        // === KPI SPARKLINE CARDS ===
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SparklineCard(
                    title = "Total Parts",
                    value = state.totalParts.toString(),
                    change = "+12% this week",
                    changePositive = true,
                    sparklineData = listOf(20f, 22f, 25f, 24f, 28f, 30f, state.totalParts.toFloat().coerceAtLeast(1f)),
                    modifier = Modifier.weight(1f)
                )
                SparklineCard(
                    title = "Stock Value",
                    value = "₹${"%,.0f".format(state.totalStockValue)}",
                    change = "+8% this week",
                    changePositive = true,
                    sparklineData = listOf(40f, 42f, 45f, 44f, 48f, 50f, state.totalStockValue.toFloat() / 1000f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // === WEEKLY SALES BAR CHART ===
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Sales This Week",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Total: ₹${"%,.0f".format(state.todaySales * 7)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedBarChart(
                        data = listOf(
                            state.todaySales.toFloat() * 0.7f,
                            state.todaySales.toFloat() * 0.85f,
                            state.todaySales.toFloat() * 1.1f,
                            state.todaySales.toFloat() * 0.6f,
                            state.todaySales.toFloat() * 0.95f,
                            state.todaySales.toFloat() * 1.2f,
                            state.todaySales.toFloat()
                        ),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today"),
                        barColor = Color(0xFF4FC3F7),
                        highlightColor = Color(0xFFFFA726)
                    )
                }
            }
        }

        // === QUICK ACTIONS GRID ===
        item {
            Text(
                text = "Quick Actions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(quickActions.chunked(2)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { action ->
                    QuickActionCard(
                        title = action.title,
                        icon = action.icon,
                        color = action.color,
                        onClick = when (action.id) {
                            "search" -> onSearch
                            "new_bill" -> onNewBill
                            "add_part" -> onAddPart
                            "add_stock" -> onAddStock
                            "fitment" -> onFitment
                            "inventory" -> onInventory
                            "history" -> onHistory
                            "customers" -> onCustomers
                            else -> onNewBill
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // === LOW STOCK ALERT ===
        if (state.lowStockCount > 0) {
            item {
                AnimatedVisibility(
                    visible = state.lowStockCount > 0,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInventory() }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusWarning.copy(alpha = 0.95f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
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
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp
                                )
                            }
                            Text("→", color = Color.White, fontSize = 24.sp)
                        }
                    }
                }
            }
        }

        // === SAVINGS HIGHLIGHT (if applicable) ===
        if (state.todayBills > 0) {
            item {
                val estimatedSavings = state.todaySales * 0.08
                SavingsHighlight(amount = estimatedSavings)
            }
        }

        // === VIEW REPORTS ===
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReports() }
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📈", fontSize = 24.sp)
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
                            text = "Daily, monthly, GST analytics",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Text("→", color = Color.White, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column {
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = color.copy(alpha = 0.3f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

private val quickActions = listOf(
    QuickAction("search", "Search Parts", Icons.Filled.Search, Color(0xFF4FC3F7)),
    QuickAction("new_bill", "New Bill", Icons.Filled.Receipt, Color(0xFF66BB6A)),
    QuickAction("add_part", "Add Part", Icons.Filled.Add, Color(0xFFFFA726)),
    QuickAction("add_stock", "Add Stock", Icons.Filled.Inventory, Color(0xFFAB47BC)),
    QuickAction("fitment", "Find by Vehicle", Icons.Filled.ShoppingCart, Color(0xFFEC407A)),
    QuickAction("history", "Bill History", Icons.Filled.Receipt, Color(0xFF7E57C2)),
    QuickAction("customers", "Customers", Icons.Filled.Person, Color(0xFF26A69A))
)
