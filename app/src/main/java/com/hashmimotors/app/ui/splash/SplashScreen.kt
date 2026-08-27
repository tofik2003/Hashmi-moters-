package com.hashmimotors.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.ui.Routes
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Minimum time the splash stays on screen, so the animation is never cut short. */
private const val SPLASH_MIN_DURATION_MS = 1800L

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

    val isSetup = viewModel.isShopSetup.collectAsState().value

    // Kick the entrance animation off immediately, independent of the DB read.
    LaunchedEffect(Unit) { startAnimation = true }

    // Decide where to go only once the shop-setup state is known. Navigating on a
    // null value would send an already-configured shop back through onboarding
    // whenever the first database read is slower than the splash animation.
    val splashStartedAt = remember { System.currentTimeMillis() }
    LaunchedEffect(isSetup) {
        val setupComplete = isSetup ?: return@LaunchedEffect
        val elapsed = System.currentTimeMillis() - splashStartedAt
        if (elapsed < SPLASH_MIN_DURATION_MS) delay(SPLASH_MIN_DURATION_MS - elapsed)
        onNavigate(if (setupComplete) Routes.DASHBOARD else Routes.ONBOARDING)
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
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(120.dp)
                )
            }
            Text(
                text = "Hashmi Motors",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Text(
                text = "Spare Parts Manager",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .alpha(alphaAnim)
            )
        }
    }
}
