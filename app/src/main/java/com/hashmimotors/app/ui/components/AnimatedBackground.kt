package com.hashmimotors.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientMiddle
import com.hashmimotors.app.ui.theme.GradientStart
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated gradient background with slow-moving particles.
 * Subtle, non-distracting. Can be toggled off in Settings.
 */
@Composable
fun AnimatedParticleBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "background")

    val particleOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Pre-compute particle starting positions (stable across recompositions)
    val particles = remember {
        List(40) {
            Triple(
                Random.nextFloat(),       // x position (0-1)
                Random.nextFloat(),       // y position (0-1)
                Random.nextFloat() * 0.6f + 0.4f  // size
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMiddle,
                        GradientEnd
                    )
                )
            )
    ) {
        // Draw particles drifting upward
        particles.forEach { (xRatio, yRatio, particleScale) ->
            val drift = (particleOffset + yRatio) % 1f
            val x = xRatio * size.width
            val y = drift * size.height
            val alpha = (0.3f + 0.3f * sin(drift * Math.PI.toFloat() * 2)) * pulse
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.5f),
                radius = particleScale * 6f,
                center = Offset(x, y)
            )
        }

        // Soft glow circles that pulse
        drawCircle(
            color = GradientStart.copy(alpha = 0.2f * pulse),
            radius = size.width * 0.4f,
            center = Offset(size.width * 0.2f, size.height * 0.3f)
        )
        drawCircle(
            color = GradientEnd.copy(alpha = 0.15f * pulse),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.8f, size.height * 0.7f)
        )
    }
}
