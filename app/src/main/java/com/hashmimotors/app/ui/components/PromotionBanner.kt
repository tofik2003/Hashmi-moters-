package com.hashmimotors.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashmimotors.app.ui.promotions.Promotion
import com.hashmimotors.app.ui.promotions.SamplePromotions
import kotlinx.coroutines.delay

/**
 * Auto-scrolling promotional banner that cycles through offers.
 * Shows current promotions on the dashboard.
 */
@Composable
fun PromotionCarousel(
    promotions: List<Promotion> = SamplePromotions.offers,
    onPromotionClick: (Promotion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(promotions) {
        while (promotions.size > 1) {
            delay(4000)
            currentIndex = (currentIndex + 1) % promotions.size
        }
    }

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (slideInHorizontally(tween(600)) { it } + fadeIn(tween(600)))
                    .togetherWith(slideOutHorizontally(tween(600)) { -it } + fadeOut(tween(600)))
            },
            label = "promotion_carousel"
        ) { idx ->
            PromotionBannerCard(
                promotion = promotions[idx],
                onClick = { onPromotionClick(promotions[idx]) }
            )
        }
        if (promotions.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                promotions.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == currentIndex) 8.dp else 6.dp)
                            .background(
                                color = if (i == currentIndex) Color(0xFFFFA726)
                                else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionBannerCard(
    promotion: Promotion,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B35),
                        Color(0xFFD32F2F),
                        Color(0xFF8E24AA)
                    )
                )
            )
            .clickable { onClick() }
    ) {
        // Shimmer animation overlay
        val transition = rememberInfiniteTransition(label = "shimmer")
        val shimmerX by transition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        startX = shimmerX * 1000f,
                        endX = shimmerX * 1000f + 300f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = promotion.emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promotion.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = promotion.description,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Use code: ${promotion.id.take(6).uppercase()}",
                        color = Color(0xFFD32F2F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            DiscountPill(percent = promotion.discountPercent)
        }
    }
}

/**
 * Top selling parts horizontal scroll.
 */
@Composable
fun TopPartsRow(
    parts: List<com.hashmimotors.app.domain.model.Part>,
    onPartClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (parts.isEmpty()) return
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(parts.take(10), key = { it.id }) { part ->
            TopPartCard(part = part, onClick = { onPartClick(part.id) })
        }
    }
}

@Composable
private fun TopPartCard(
    part: com.hashmimotors.app.domain.model.Part,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            // Top: emoji placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4FC3F7).copy(alpha = 0.3f),
                                Color(0xFF66BB6A).copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = part.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = part.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "₹${"%,.0f".format(part.sellingPrice)}",
                color = Color(0xFF66BB6A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MRP ₹${"%,.0f".format(part.mrp)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
            )
        }
    }
}

/**
 * Animated stat counter that counts up from 0 to value.
 */
@Composable
fun AnimatedCounter(
    target: Double,
    prefix: String = "₹",
    suffix: String = "",
    durationMillis: Int = 1200,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: Int = 28
) {
    val animation = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(target) {
        animation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis)
        )
    }
    val current = target * animation.value
    Text(
        text = "$prefix${"%,.0f".format(current)}$suffix",
        color = textColor,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}
