package com.hashmimotors.app.ui.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashmimotors.app.ui.theme.BrandGoldBright

/**
 * The four PIN dots that fill as the user types.
 */
@Composable
fun PinDots(length: Int, error: Boolean = false, total: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(total) { index ->
            val filled = index < length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when {
                            error -> Color(0xFFFF6B6B)
                            filled -> BrandGoldBright
                            else -> Color.White.copy(alpha = 0.25f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Numeric keypad for PIN entry.
 */
@Composable
fun PinPad(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    biometricEnabled: Boolean = false,
    onBiometric: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9").chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key ->
                    PinKey(label = key, onClick = { onKey(key) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (biometricEnabled && onBiometric != null) {
                PinKey(
                    label = "",
                    icon = Icons.Filled.Fingerprint,
                    onClick = onBiometric,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            PinKey(label = "0", onClick = { onKey("0") }, modifier = Modifier.weight(1f))
            PinKey(
                label = "",
                icon = Icons.Filled.Backspace,
                onClick = onBackspace,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        label = "pin_key_scale"
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = if (pressed) Color.White.copy(alpha = 0.22f)
                else Color.White.copy(alpha = 0.12f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGoldBright,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = label,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
