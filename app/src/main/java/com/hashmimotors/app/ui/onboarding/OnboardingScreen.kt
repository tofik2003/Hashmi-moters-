package com.hashmimotors.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientStart
import com.hashmimotors.app.ui.theme.Ink
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            "Hashmi Motors",
            "A complete spare-parts counter on this phone. Catalog, stock, GST bills — nothing locked behind a plan.",
            Icons.Filled.Storefront
        ),
        OnboardingPage(
            "Find any part",
            "Search by name, OEM, brand, or barcode. Open the camera for QR and labels.",
            Icons.Filled.QrCodeScanner
        ),
        OnboardingPage(
            "Bills in one pass",
            "Add lines, save a Bill of Supply, share PDF or WhatsApp. Invoice QR is included.",
            Icons.Filled.ReceiptLong
        ),
        OnboardingPage(
            "Yours, offline",
            "Data stays on the device. Export a JSON backup whenever you like. No account, no fees.",
            Icons.Filled.Inventory2
        )
    )
    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, Ink, GradientEnd)))
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (currentPage < pages.lastIndex) {
                    TextButton(onClick = onComplete) { Text("Skip", color = IvoryMute) }
                }
            }
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally(tween(400)) { it } + fadeIn(tween(400)))
                        .togetherWith(slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(400)))
                },
                label = "onboarding"
            ) { page ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(160.dp).background(Gold.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(pages[page].icon, null, tint = Gold, modifier = Modifier.size(72.dp))
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(pages[page].title, color = Ivory, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(pages[page].description, color = IvoryMute, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                    pages.indices.forEach { index ->
                        Box(
                            Modifier
                                .size(if (index == currentPage) 10.dp else 6.dp)
                                .background(if (index == currentPage) Gold else Ivory.copy(0.25f), CircleShape)
                        )
                    }
                }
                Button(
                    onClick = { if (currentPage < pages.lastIndex) currentPage++ else onComplete() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
                ) {
                    Text(
                        if (currentPage < pages.lastIndex) "Continue" else "Enter the shop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
