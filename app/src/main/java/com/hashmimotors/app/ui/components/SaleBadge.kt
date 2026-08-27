package com.hashmimotors.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashmimotors.app.ui.theme.StatusError
import com.hashmimotors.app.ui.theme.StatusWarning
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Starburst / Sunburst-style sale badge with rotation animation.
 * Shows percentage off or "SALE" prominently.
 */
@Composable
fun StarburstSaleBadge(
    discountPercent: Int,
    modifier: Modifier = Modifier,
    sizeDp: Int = 80,
    label: String = "OFF"
) {
    val transition = rememberInfiniteTransition(label = "starburst")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val outerRadius = this.size.minDimension / 2
            val innerRadius = outerRadius * 0.7f
            val spikes = 16
            val path = Path()

            for (i in 0 until spikes * 2) {
                val angle = (i * PI / spikes).toFloat()
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF6B35),
                        Color(0xFFD32F2F)
                    ),
                    center = Offset(cx, cy),
                    radius = outerRadius
                )
            )
            drawPath(
                path = path,
                color = Color(0xFFFFD700).copy(alpha = 0.4f),
                style = Stroke(width = 3f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$discountPercent%",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Simple pill-shaped discount tag.
 */
@Composable
fun DiscountPill(
    percent: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF6B35), Color(0xFFD32F2F))
                )
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$percent%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "OFF",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Strikethrough price + discounted price widget.
 * Shows MRP struck through, with new price in a colored box.
 */
@Composable
fun PriceWithDiscount(
    originalPrice: Double,
    discountedPrice: Double,
    modifier: Modifier = Modifier
) {
    val savings = originalPrice - discountedPrice
    val percentOff = if (originalPrice > 0) ((savings / originalPrice) * 100).toInt() else 0
    val showDiscount = percentOff > 0

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "₹${"%,.0f".format(discountedPrice)}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (showDiscount) {
            Text(
                text = "₹${"%,.0f".format(originalPrice)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
            )
            DiscountPill(percent = percentOff)
        }
    }
}

/**
 * Savings highlight banner.
 */
@Composable
fun SavingsHighlight(
    amount: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💰", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "You saved",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Text(
                    "₹${"%,.0f".format(amount)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
