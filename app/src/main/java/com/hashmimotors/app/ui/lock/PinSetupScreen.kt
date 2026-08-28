package com.hashmimotors.app.ui.lock

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.theme.BrandGoldBright

private enum class PinStage { MENU, VERIFY_OLD, ENTER_NEW, CONFIRM_NEW }

/**
 * Create / change / remove the app-lock PIN. Reached from Settings → Security.
 */
@Composable
fun PinSetupScreen(
    viewModel: AppLockViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val hasPin = settings.pinHash != null

    var stage by remember { mutableStateOf(if (hasPin) PinStage.MENU else PinStage.ENTER_NEW) }
    var firstPin by remember { mutableStateOf("") }
    var attempt by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    fun resetEntry() {
        attempt = ""
        error = null
    }

    fun handleKey(key: String, onFilled: (String) -> Unit) {
        error = null
        if (attempt.length < 4) {
            attempt += key
            if (attempt.length == 4) onFilled(attempt)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = "App Lock",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            when (stage) {
                PinStage.MENU -> {
                    Text(
                        text = "PIN is enabled",
                        color = BrandGoldBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your data is protected with a 4-digit PIN.\nUse your fingerprint to unlock when available.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    AnimatedBigButton(
                        text = "Change PIN",
                        onClick = {
                            resetEntry()
                            stage = PinStage.VERIFY_OLD
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = { showRemoveConfirm = true }) {
                                Text("Remove PIN", color = Color(0xFFFF8A80), fontSize = 16.sp)
                            }
                        }
                    }
                }

                PinStage.VERIFY_OLD -> PinEntryColumn(
                    title = "Enter current PIN",
                    subtitle = "Verify it's you before changing",
                    attempt = attempt,
                    error = error,
                    onKey = { key ->
                        handleKey(key) { pin ->
                            if (viewModel.verifyPin(pin, settings.pinHash)) {
                                resetEntry()
                                stage = PinStage.ENTER_NEW
                            } else {
                                error = "Incorrect PIN"
                                attempt = ""
                            }
                        }
                    },
                    onBackspace = { attempt = attempt.dropLast(1) }
                )

                PinStage.ENTER_NEW -> PinEntryColumn(
                    title = "Create a new 4-digit PIN",
                    subtitle = "You'll use this to unlock the app",
                    attempt = attempt,
                    error = error,
                    onKey = { key ->
                        handleKey(key) { pin ->
                            firstPin = pin
                            resetEntry()
                            stage = PinStage.CONFIRM_NEW
                        }
                    },
                    onBackspace = { attempt = attempt.dropLast(1) }
                )

                PinStage.CONFIRM_NEW -> PinEntryColumn(
                    title = "Confirm new PIN",
                    subtitle = "Enter the same PIN again",
                    attempt = attempt,
                    error = error,
                    onKey = { key ->
                        handleKey(key) { pin ->
                            if (pin == firstPin) {
                                viewModel.setPin(pin)
                                onBack()
                            } else {
                                error = "PINs don't match"
                                firstPin = ""
                                attempt = ""
                                stage = PinStage.ENTER_NEW
                            }
                        }
                    },
                    onBackspace = { attempt = attempt.dropLast(1) }
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Remove PIN?") },
            text = { Text("Your app will no longer be protected with a PIN.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearPin()
                    showRemoveConfirm = false
                    onBack()
                }) {
                    Text("Remove", color = Color(0xFFFF8A80))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PinEntryColumn(
    title: String,
    subtitle: String,
    attempt: String,
    error: String?,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        PinDots(length = attempt.length, error = error != null)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = error ?: " ",
            color = Color(0xFFFF6B6B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        PinPad(
            onKey = onKey,
            onBackspace = onBackspace
        )
    }
}
