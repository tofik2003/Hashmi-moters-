package com.hashmimotors.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.ui.components.AnimatedParticleBackground
import com.hashmimotors.app.ui.dashboard.DashboardScreen
import com.hashmimotors.app.ui.onboarding.OnboardingScreen
import com.hashmimotors.app.ui.settings.SettingsScreen
import com.hashmimotors.app.ui.shop.ShopSetupScreen
import com.hashmimotors.app.ui.splash.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SHOP_SETUP = "shop_setup"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val CATALOG = "catalog"
    const val SEARCH = "search"
    const val ADD_PART = "add_part"
    const val PART_DETAIL = "part_detail/{partId}"
    const val FITMENT_WIZARD = "fitment_wizard"
    const val NEW_BILL = "new_bill"
    const val INVOICE_PREVIEW = "invoice_preview/{invoiceId}"
    const val INVOICE_HISTORY = "invoice_history"
    const val INVENTORY = "inventory"
    const val ADD_STOCK = "add_stock"
    const val REPORTS = "reports"
}

@HiltViewModel
class AppShellViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : androidx.lifecycle.ViewModel() {
    val settings = settingsRepository.getSettings()
}

@Composable
fun HashmiMotorsApp(
    viewModel: AppShellViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val settings by viewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer
        if (settings.backgroundStyle == BackgroundStyle.GRADIENT_PARTICLES && settings.animationsEnabled) {
            AnimatedParticleBackground(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                com.hashmimotors.app.ui.theme.GradientStart,
                                com.hashmimotors.app.ui.theme.GradientMiddle,
                                com.hashmimotors.app.ui.theme.GradientEnd
                            )
                        )
                    )
            )
        }

        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                ) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                ) + fadeOut(tween(300))
            }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.SHOP_SETUP) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.SHOP_SETUP) {
                ShopSetupScreen(
                    onComplete = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SHOP_SETUP) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onSearch = { navController.navigate(Routes.SEARCH) },
                    onNewBill = { navController.navigate(Routes.NEW_BILL) },
                    onAddStock = { navController.navigate(Routes.ADD_STOCK) },
                    onAddPart = { navController.navigate(Routes.ADD_PART) },
                    onFitment = { navController.navigate(Routes.FITMENT_WIZARD) },
                    onInventory = { navController.navigate(Routes.INVENTORY) },
                    onReports = { navController.navigate(Routes.REPORTS) },
                    onHistory = { navController.navigate(Routes.INVOICE_HISTORY) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            // Other routes are added by individual screens
        }
    }
}
