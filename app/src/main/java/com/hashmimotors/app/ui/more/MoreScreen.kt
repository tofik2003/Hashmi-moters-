package com.hashmimotors.app.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.hashmimotors.app.ui.components.HmCard
import com.hashmimotors.app.ui.components.HmIconWell
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute

@Composable
fun MoreScreen(
    onInventory: () -> Unit,
    onAddStock: () -> Unit,
    onImport: () -> Unit,
    onFitment: () -> Unit,
    onCustomers: () -> Unit,
    onReports: () -> Unit,
    onHistory: () -> Unit,
    onShop: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        HmTopBar(title = "Workshop", subtitle = "Everything in the shop, no subscriptions")
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MoreRow("Inventory", "Stock levels and low-stock parts", Icons.Filled.Inventory, onInventory)
            MoreRow("Receive stock", "Add quantity to a part", Icons.Filled.Storefront, onAddStock)
            MoreRow("Add / import parts", "Manual, camera, CSV, sample catalog", Icons.Filled.UploadFile, onImport)
            MoreRow("Fitment", "Find parts by vehicle", Icons.Filled.DirectionsCar, onFitment)
            MoreRow("Customers", "Phone book and purchase totals", Icons.Filled.People, onCustomers)
            MoreRow("Reports", "Today and this month, from your bills", Icons.Filled.Assessment, onReports)
            MoreRow("Bill history", "Open any saved bill", Icons.Filled.History, onHistory)
            MoreRow("Shop profile", "Name, address, GSTIN on invoices", Icons.Filled.Storefront, onShop)
            MoreRow("Settings", "Look, sound, backup", Icons.Filled.Settings, onSettings)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoreRow(title: String, body: String, icon: ImageVector, onClick: () -> Unit) {
    HmCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth()) {
            HmIconWell(icon)
            Spacer(Modifier.padding(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(body, color = IvoryMute, fontSize = 12.sp)
            }
        }
    }
}
