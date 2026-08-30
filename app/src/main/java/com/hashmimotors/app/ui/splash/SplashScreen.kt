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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.data.repository.VehicleRepository
import com.hashmimotors.app.data.seed.DemoCatalogSeeder
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.ui.Routes
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import com.hashmimotors.app.ui.theme.GradientEnd
import com.hashmimotors.app.ui.theme.GradientStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val categoryRepository: CategoryRepository,
    private val vehicleRepository: VehicleRepository,
    private val demoCatalogSeeder: DemoCatalogSeeder,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun prepareApp(): String {
        // 1. Ensure shop exists
        if (shopRepository.getShopOnce() == null) {
            shopRepository.saveShop(
                Shop(name = "Hashmi Motors", isSetupComplete = true)
            )
        }
        // 2. Ensure default categories & vehicles are seeded
        categoryRepository.ensureSeeded()
        vehicleRepository.ensureSeeded()
        // 3. Seed demo inventory if catalog is empty
        demoCatalogSeeder.seedIfEmpty()

        // 4. Check if PIN lock is active
        val settings = settingsRepository.getSettings().first()
        return if (settings.pinHash != null) Routes.LOCK else Routes.DASHBOARD
    }
}

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = tween(durationMillis = 700),
        label = "scale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        val targetDestination = viewModel.prepareApp()
        delay(1400)
        onNavigate(targetDestination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, Color(0xFF0C0F26), GradientEnd)
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
                    .size(130.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(
                            width = 2.dp,
                            color = BrandGold.copy(alpha = 0.7f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HM",
                        color = BrandGoldBright,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hashmi Motors",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SPARE PARTS & BILLING",
                color = BrandGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Offline-First Counter System",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
