package com.hashmimotors.app.ui.shop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.ui.components.AnimatedBigButton

@Composable
fun ShopSetupScreen(
    viewModel: ShopSetupViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val existingShop by viewModel.shop.collectAsStateWithLifecycle(initialValue = null)

    var name by remember { mutableStateOf("Hashmi Motors") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    // Pre-fill if shop already exists
    androidx.compose.runtime.LaunchedEffect(existingShop) {
        existingShop?.let {
            name = it.name
            address = it.address
            city = it.city
            state = it.state
            pincode = it.pincode
            phone = it.phone
            gstin = it.gstin
            email = it.email ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Shop Details",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This information will appear on your bills. You can edit later in Settings.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Shop Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state,
                onValueChange = {
                    state = it
                    // Auto-fill state code
                },
                label = { Text("State (e.g. Maharashtra)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = pincode,
                onValueChange = { pincode = it },
                label = { Text("PIN Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it.uppercase() },
                label = { Text("GSTIN (15 characters)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedBigButton(
                text = if (saving) "Saving..." else "Continue",
                icon = Icons.Filled.ArrowForward,
                enabled = !saving && name.isNotBlank() && gstin.isNotBlank(),
                onClick = {
                    saving = true
                    val stateCode = getStateCode(state)
                    viewModel.saveShop(
                        Shop(
                            name = name,
                            address = address,
                            city = city,
                            state = state,
                            stateCode = stateCode,
                            pincode = pincode,
                            phone = phone,
                            email = email.ifBlank { null },
                            gstin = gstin,
                            isSetupComplete = true
                        )
                    ) {
                        saving = false
                        onComplete()
                    }
                }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun getStateCode(state: String): String {
    val codes = mapOf(
        "Andhra Pradesh" to "37", "Arunachal Pradesh" to "12", "Assam" to "18",
        "Bihar" to "10", "Chhattisgarh" to "22", "Goa" to "30", "Gujarat" to "24",
        "Haryana" to "06", "Himachal Pradesh" to "02", "Jharkhand" to "20",
        "Karnataka" to "29", "Kerala" to "32", "Madhya Pradesh" to "23",
        "Maharashtra" to "27", "Manipur" to "14", "Meghalaya" to "17",
        "Mizoram" to "15", "Nagaland" to "13", "Odisha" to "21", "Punjab" to "03",
        "Rajasthan" to "08", "Sikkim" to "11", "Tamil Nadu" to "33",
        "Telangana" to "36", "Tripura" to "16", "Uttar Pradesh" to "09",
        "Uttarakhand" to "05", "West Bengal" to "19", "Delhi" to "07",
        "Jammu and Kashmir" to "01", "Ladakh" to "98", "Chandigarh" to "04",
        "Puducherry" to "34"
    )
    return codes[state] ?: ""
}
