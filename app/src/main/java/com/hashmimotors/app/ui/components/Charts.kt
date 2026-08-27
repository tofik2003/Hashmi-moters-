package com.hashmimotors.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * Animated bar chart for sales data.
 * Bars rise from bottom with a spring-like animation.
 */
@Composable
fun AnimatedBarChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF66BB6A),
    highlightColor: Color = Color(0xFFFFA726)
) {
    val maxValue = data.maxOrNull() ?: 1f
    val animation = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animation.animateTo(1f, tween(durationMillis = 1000))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val heightFraction = (value / maxValue) * animation.value
                val isHighlighted = value == maxValue && data.size > 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((140 * heightFraction).dp.coerceAtLeast(4.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isHighlighted)
                                        listOf(highlightColor, highlightColor.copy(alpha = 0.5f))
                                    else
                                        listOf(barColor, barColor.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
            }
        }
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Animated line/area chart for trend visualization.
 */
@Composable
fun AnimatedLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4FC3F7),
    fillColor: Color = Color(0xFF4FC3F7).copy(alpha = 0.3f)
) {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animation.animateTo(1f, tween(durationMillis = 1200))
    }

    if (data.size < 2) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = max(maxValue - minValue, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val w = size.width
        val h = size.height
        val padding = 16f
        val plotWidth = w - padding * 2
        val plotHeight = h - padding * 2

        // Draw grid lines
        for (i in 0..4) {
            val y = padding + (plotHeight * i / 4)
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(padding, y),
                end = Offset(w - padding, y),
                strokeWidth = 1f
            )
        }

        // Build path
        val path = Path()
        val fillPath = Path()
        val stepX = plotWidth / (data.size - 1)

        data.forEachIndexed { index, value ->
            val x = padding + stepX * index
            val normalizedY = (value - minValue) / range
            val y = padding + plotHeight * (1f - normalizedY)
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(padding + stepX * (data.size - 1), h - padding)
        fillPath.close()

        // Apply animation by clipping
        val clipPath = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    offset = Offset(0f, 0f),
                    size = Size(w * animation.value, h)
                )
            )
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = h
            )
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )

        // Draw points
        data.forEachIndexed { index, value ->
            if (index.toFloat() / (data.size - 1) > animation.value) return@forEachIndexed
            val x = padding + stepX * index
            val normalizedY = (value - minValue) / range
            val y = padding + plotHeight * (1f - normalizedY)
            drawCircle(
                color = lineColor,
                radius = 5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = Offset(x, y)
            )
        }
    }

    // Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Donut chart for category distribution.
 */
@Composable
fun DonutChart(
    segments: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF66BB6A),
        Color(0xFF4FC3F7),
        Color(0xFFFFA726),
        Color(0xFFAB47BC),
        Color(0xFFEC407A),
        Color(0xFFFFCA28)
    )
) {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        animation.animateTo(1f, tween(durationMillis = 1200))
    }
    val total = segments.sumOf { it.second.toDouble() }.toFloat()
    if (total == 0f) return

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val outerRadius = minOf(w, h) / 2 * 0.85f
            val innerRadius = outerRadius * 0.55f
            val center = Offset(w / 2, h / 2)
            val strokeWidth = outerRadius - innerRadius

            var startAngle = -90f
            segments.forEachIndexed { index, (_, value) ->
                val sweep = (value / total) * 360f * animation.value
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
    }
}

/**
 * KPI card with sparkline + change indicator.
 */
@Composable
fun SparklineCard(
    title: String,
    value: String,
    change: String? = null,
    changePositive: Boolean = true,
    sparklineData: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sparklineColor = if (changePositive) Color(0xFF66BB6A) else Color(0xFFEF5350)

    Column(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        if (change != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = sparklineColor,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    color = sparklineColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (sparklineData.size >= 2) {
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                val max = sparklineData.maxOrNull() ?: 1f
                val min = sparklineData.minOrNull() ?: 0f
                val range = max(max - min, 1f)
                val stepX = size.width / (sparklineData.size - 1)
                val path = Path()
                sparklineData.forEachIndexed { i, v ->
                    val x = stepX * i
                    val y = size.height - ((v - min) / range) * size.height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = sparklineColor, style = Stroke(width = 3f))
            }
        }
    }
}
