package com.hashmimotors.app.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.R
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.ui.Routes
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientMiddle
import com.hashmimotors.app.ui.theme.GradientStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {
    private val _isShopSetup = MutableStateFlow<Boolean?>(null)
    val isShopSetup: StateFlow<Boolean?> = _isShopSetup.asStateFlow()

    init {
        viewModelScope.launch {
            val shop = shopRepository.getShopOnce()
            _isShopSetup.value = shop?.isSetupComplete == true
        }
    }
}

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    requirePin: Boolean = false,
    onNavigate: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label = "alpha"
    )

    // Soft pulsing glow behind the emblem
    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowScale by glowTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val isSetup = viewModel.isShopSetup.collectAsState().value

    LaunchedEffect(isSetup, requirePin) {
        startAnimation = true
        delay(1900)
        val destination = when {
            requirePin -> Routes.LOCK
            isSetup == true -> Routes.DASHBOARD
            else -> Routes.ONBOARDING
        }
        onNavigate(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblem with pulsing gold glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(glowScale)
                        .background(
                            color = BrandGold.copy(alpha = glowAlpha),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "Hashmi Motors",
                    modifier = Modifier
                        .size(150.dp)
                        .scale(scaleAnim)
                        .alpha(alphaAnim)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HASHMI",
                    color = BrandGoldBright,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MOTORS",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BrandGoldBright, BrandGold)
                            ),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PRO",
                        color = Color(0xFF1A1A2E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Premium Spare Parts Manager",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading dots
            Row(
                modifier = Modifier.alpha(alphaAnim),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val dotAlpha by glowTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                delayMillis = index * 150,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = BrandGold.copy(alpha = dotAlpha),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
