package com.hashmimotors.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashmimotors.app.ui.Routes
import com.hashmimotors.app.ui.sound.Feedback
import com.hashmimotors.app.ui.sound.LocalAppFeedback
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import com.hashmimotors.app.ui.theme.GradientStart

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.SEARCH, "Catalog", Icons.Filled.Search),
    BottomNavItem(Routes.NEW_BILL, "New Bill", Icons.Filled.AddCircle),
    BottomNavItem(Routes.INVENTORY, "Stock", Icons.Filled.Inventory),
    BottomNavItem(Routes.REPORTS, "Reports", Icons.Filled.Assessment)
)

/**
 * Premium glassy bottom navigation bar shown on main tabs.
 */
@Composable
fun HashmiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = LocalAppFeedback.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181D45),
                        GradientStart
                    )
                )
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    targetValue = if (selected) BrandGoldBright else Color.White.copy(alpha = 0.55f),
                    label = "navIconColor"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (selected) BrandGold else Color.White.copy(alpha = 0.7f),
                    label = "navLabelColor"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.9f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
                    label = "navScale"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        Feedback.tap(feedback)
                        onNavigate(item.route)
                    },
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(scale)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(BrandGoldBright, BrandGold)
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            }
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) Color(0xFF1A1A2E) else iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = labelColor,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = BrandGoldBright,
                        unselectedIconColor = Color.White.copy(alpha = 0.55f),
                        selectedTextColor = BrandGold,
                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        disabledIconColor = Color.White.copy(alpha = 0.3f),
                        disabledTextColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
