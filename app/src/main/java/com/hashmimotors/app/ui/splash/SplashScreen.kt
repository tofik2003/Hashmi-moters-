package com.hashmimotors.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.ui.Routes
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Ensure a default shop exists so the app never blocks on setup.
            if (shopRepository.getShopOnce() == null) {
                shopRepository.saveShop(
                    Shop(name = "Hashmi", isSetupComplete = true)
                )
            }
        }
    }
}

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        // Straight to the dashboard — no onboarding or setup blocking.
        onNavigate(Routes.DASHBOARD)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, Color.Black, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                contentAlignment = Alignment.Center
            ) {
                // Premium gold "H" monogram in a ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFFC107).copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        color = Color(0xFFFFC107),
                        fontSize = 84.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = "Hashmi",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "P R E M I U M",
                color = Color(0xFFFFC107),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Premium Spare Parts Manager",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .alpha(alphaAnim)
            )
        }
    }
}
