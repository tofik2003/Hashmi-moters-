package com.hashmimotors.app.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.sound.HapticManager
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SetupStep {
    ENTER_NEW,
    CONFIRM_NEW
}

@Composable
fun PinSetupScreen(
    viewModel: AppLockViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = remember { HapticManager(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(SetupStep.ENTER_NEW) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    fun handleKey(digit: String) {
        haptic.lightTap()
        when (step) {
            SetupStep.ENTER_NEW -> {
                if (firstPin.length < 4) {
                    val next = firstPin + digit
                    firstPin = next
                    if (next.length == 4) {
                        step = SetupStep.CONFIRM_NEW
                    }
                }
            }
            SetupStep.CONFIRM_NEW -> {
                if (confirmPin.length < 4) {
                    val next = confirmPin + digit
                    confirmPin = next
                    if (next.length == 4) {
                        if (next == firstPin) {
                            haptic.heavyClick()
                            viewModel.setPin(next)
                            onBack()
                        } else {
                            haptic.error()
                            isError = true
                            errorMessage = "PINs did not match, try again"
                            scope.launch {
                                shakeOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = keyframes {
                                        durationMillis = 400
                                        -20f at 50
                                        20f at 100
                                        -15f at 150
                                        15f at 200
                                        -8f at 250
                                        8f at 300
                                        0f at 400
                                    }
                                )
                                delay(200)
                                firstPin = ""
                                confirmPin = ""
                                step = SetupStep.ENTER_NEW
                                isError = false
                                errorMessage = null
                            }
                        }
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        haptic.lightTap()
        when (step) {
            SetupStep.ENTER_NEW -> {
                if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
            }
            SetupStep.CONFIRM_NEW -> {
                if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                else {
                    step = SetupStep.ENTER_NEW
                    firstPin = firstPin.dropLast(1)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, Color(0xFF0F122C), GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = if (settings.pinHash != null) "Change PIN" else "Set App PIN",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (settings.pinHash != null) {
                    TextButton(onClick = {
                        viewModel.removePin()
                        onBack()
                    }) {
                        Text("Remove", color = Color(0xFFFF6B6B))
                    }
                }
            }

            // Prompt + Dots
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(BrandGold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = BrandGoldBright,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (step) {
                        SetupStep.ENTER_NEW -> "Choose a 4-digit PIN"
                        SetupStep.CONFIRM_NEW -> "Confirm your 4-digit PIN"
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "Protects your prices, sales & customer records",
                    color = if (isError) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.offset {
                        IntOffset(shakeOffset.value.roundToInt(), 0)
                    }
                ) {
                    PinDots(
                        length = if (step == SetupStep.ENTER_NEW) firstPin.length else confirmPin.length,
                        error = isError
                    )
                }
            }

            // Keypad
            PinPad(
                onKey = { handleKey(it) },
                onBackspace = { handleBackspace() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
