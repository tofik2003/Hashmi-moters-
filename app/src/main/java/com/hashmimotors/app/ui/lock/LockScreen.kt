package com.hashmimotors.app.ui.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.sound.HapticManager
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright

/**
 * App-lock gate. Shown at startup when a PIN has been set.
 * Unlocks via 4-digit PIN or fingerprint (when available).
 */
@Composable
fun LockScreen(
    viewModel: AppLockViewModel = hiltViewModel(),
    onUnlocked: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = remember { HapticManager(context.applicationContext) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val biometricAvailable = remember {
        runCatching {
            BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        }.getOrDefault(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) == BiometricManager.BIOMETRIC_SUCCESS
    }
    val activity = context as? FragmentActivity

    fun unlock() {
        haptic.success()
        onUnlocked()
    }

    fun submitPin() {
        if (viewModel.verifyPin(entered, settings.pinHash)) {
            unlock()
        } else {
            haptic.error()
            error = true
            entered = ""
        }
    }

    // Safety net: no PIN configured → don't lock the user out.
    LaunchedEffect(settings.pinHash) {
        if (settings.pinHash == null) unlock()
    }

    // Offer biometric unlock automatically when the lock screen appears.
    LaunchedEffect(Unit) {
        if (biometricAvailable && activity != null) {
            launchBiometric(activity) { ok -> if (ok) unlock() }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand
            Text(
                text = "HASHMI MOTORS",
                color = BrandGoldBright,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(listOf(BrandGoldBright, BrandGold)),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PRO",
                    color = Color(0xFF1A1A2E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = BrandGoldBright,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Enter your PIN",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            PinDots(length = entered.length, error = error)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (error) "Incorrect PIN. Try again." else " ",
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            PinPad(
                onKey = { key ->
                    error = false
                    if (entered.length < 4) {
                        entered += key
                        if (entered.length == 4) submitPin()
                    }
                },
                onBackspace = {
                    entered = entered.dropLast(1)
                    error = false
                },
                biometricEnabled = biometricAvailable && activity != null,
                onBiometric = {
                    activity?.let { act ->
                        launchBiometric(act) { ok -> if (ok) unlock() }
                    }
                }
            )
        }
    }
}

private fun launchBiometric(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onResult(true)
        }
    })
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Hashmi Motors Pro")
        .setSubtitle("Use your fingerprint or face")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    prompt.authenticate(info)
}
