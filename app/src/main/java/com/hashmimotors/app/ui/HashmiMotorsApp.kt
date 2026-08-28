package com.hashmimotors.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.ui.billing.BillingScreen
import com.hashmimotors.app.ui.billing.InvoiceHistoryScreen
import com.hashmimotors.app.ui.billing.InvoicePreviewScreen
import com.hashmimotors.app.ui.catalog.AddPartScreen
import com.hashmimotors.app.ui.catalog.SearchScreen
import com.hashmimotors.app.ui.components.AnimatedParticleBackground
import com.hashmimotors.app.ui.components.HashmiBottomBar
import com.hashmimotors.app.ui.customers.CustomerListScreen
import com.hashmimotors.app.ui.dashboard.DashboardScreen
import com.hashmimotors.app.ui.fitment.FitmentScreen
import com.hashmimotors.app.ui.inventory.AddStockScreen
import com.hashmimotors.app.ui.inventory.InventoryScreen
import com.hashmimotors.app.ui.onboarding.OnboardingScreen
import com.hashmimotors.app.ui.reports.ReportsScreen
import com.hashmimotors.app.ui.settings.SettingsScreen
import com.hashmimotors.app.ui.shop.ShopSetupScreen
import com.hashmimotors.app.ui.splash.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SHOP_SETUP = "shop_setup"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val ADD_PART = "add_part"
    const val EDIT_PART = "edit_part/{partId}"
    const val FITMENT_WIZARD = "fitment_wizard"
    const val NEW_BILL = "new_bill"
    const val INVOICE_PREVIEW = "invoice_preview/{invoiceId}"
    const val INVOICE_HISTORY = "invoice_history"
    const val INVENTORY = "inventory"
    const val ADD_STOCK = "add_stock"
    const val REPORTS = "reports"
    const val CUSTOMERS = "customers"
}

/** Routes that show the persistent bottom navigation bar. */
val bottomNavRoutes = setOf(
    Routes.DASHBOARD,
    Routes.SEARCH,
    Routes.NEW_BILL,
    Routes.INVENTORY,
    Routes.REPORTS
)

@HiltViewModel
class AppShellViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    val settings = settingsRepository.getSettings()

    init {
        viewModelScope.launch {
            categoryRepository.ensureSeeded()
        }
    }
}

@Composable
fun HashmiMotorsMainScreen(
    viewModel: AppShellViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val settings by viewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateTab(route: String) {
        if (route == currentRoute) return
        navController.navigate(route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (settings.backgroundStyle == BackgroundStyle.GRADIENT_PARTICLES && settings.animationsEnabled) {
            AnimatedParticleBackground(modifier = Modifier.fillMaxSize())
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
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
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onCustomers = { navController.navigate(Routes.CUSTOMERS) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onPartClick = { id -> navController.navigate("edit_part/$id") },
                    onAddPartClick = { navController.navigate(Routes.ADD_PART) },
                    onScanClick = { },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADD_PART) {
                AddPartScreen(
                    partId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                Routes.EDIT_PART,
                arguments = listOf(navArgument("partId") { type = NavType.StringType })
            ) { backStackEntry ->
                val partId = backStackEntry.arguments?.getString("partId")
                AddPartScreen(
                    partId = partId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.NEW_BILL) {
                BillingScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { invoiceId ->
                        navController.navigate("invoice_preview/$invoiceId") {
                            popUpTo(Routes.NEW_BILL) { inclusive = true }
                        }
                    },
                    onAddItem = { }
                )
            }
            composable(
                Routes.INVOICE_PREVIEW,
                arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
                InvoicePreviewScreen(
                    invoiceId = invoiceId,
                    onBack = {
                        navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                    }
                )
            }
            composable(Routes.INVOICE_HISTORY) {
                InvoiceHistoryScreen(
                    onBack = { navController.popBackStack() },
                    onInvoiceClick = { id -> navController.navigate("invoice_preview/$id") }
                )
            }
            composable(Routes.INVENTORY) {
                InventoryScreen(
                    onBack = { navController.popBackStack() },
                    onPartClick = { id -> navController.navigate("edit_part/$id") }
                )
            }
            composable(Routes.ADD_STOCK) {
                AddStockScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.FITMENT_WIZARD) {
                FitmentScreen(
                    onBack = { navController.popBackStack() },
                    onVehicleSelected = { }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CUSTOMERS) {
                CustomerListScreen(onBack = { navController.popBackStack() })
            }
        }

        // Persistent premium bottom navigation bar (main tabs only)
        AnimatedVisibility(
            visible = currentRoute in bottomNavRoutes,
            enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HashmiBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateTab(route) }
            )
        }
    }
}
