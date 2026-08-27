package com.hashmimotors.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
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
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientMiddle
import com.hashmimotors.app.ui.theme.GradientStart
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
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
    onNavigate: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.86f,
        animationSpec = tween(900),
        label = "scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1100),
        label = "alpha"
    )
    val isSetup = viewModel.isShopSetup.collectAsState().value

    LaunchedEffect(isSetup) {
        startAnimation = true
        delay(1600)
        val destination = if (isSetup == true) Routes.DASHBOARD else Routes.ONBOARDING
        onNavigate(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientMiddle, GradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleAnim).alpha(alphaAnim)
        ) {
            Image(
                painter = painterResource(R.drawable.hm_emblem),
                contentDescription = null,
                modifier = Modifier.size(148.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(28.dp))
            Text("HASHMI MOTORS", color = Ivory, fontSize = 22.sp, fontWeight = FontWeight.Medium, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("Spare parts atelier", color = Gold, fontSize = 13.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(6.dp))
            Text("100% free · every tool included", color = IvoryMute, fontSize = 12.sp)
        }
    }
}
