package com.hashmimotors.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hashmimotors.app.data.repository.SettingsRepository
import com.hashmimotors.app.domain.model.AppSettings
import com.hashmimotors.app.domain.model.BackgroundStyle
import com.hashmimotors.app.ui.billing.BillingScreen
import com.hashmimotors.app.ui.billing.InvoiceHistoryScreen
import com.hashmimotors.app.ui.billing.InvoicePreviewScreen
import com.hashmimotors.app.ui.catalog.AddPartScreen
import com.hashmimotors.app.ui.catalog.CatalogImportScreen
import com.hashmimotors.app.ui.catalog.SearchScreen
import com.hashmimotors.app.ui.components.AnimatedParticleBackground
import com.hashmimotors.app.ui.components.HashmiBottomBar
import com.hashmimotors.app.ui.customers.CustomerListScreen
import com.hashmimotors.app.ui.dashboard.DashboardScreen
import com.hashmimotors.app.ui.fitment.FitmentScreen
import com.hashmimotors.app.ui.inventory.AddStockScreen
import com.hashmimotors.app.ui.inventory.InventoryScreen
import com.hashmimotors.app.ui.lock.LockScreen
import com.hashmimotors.app.ui.lock.PinSetupScreen
import com.hashmimotors.app.ui.reports.ReportsScreen
import com.hashmimotors.app.ui.scanner.BarcodeScannerScreen
import com.hashmimotors.app.ui.settings.SettingsScreen
import com.hashmimotors.app.ui.shop.ShopSetupScreen
import com.hashmimotors.app.ui.splash.SplashScreen
import com.hashmimotors.app.ui.suppliers.SuppliersScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object Routes {
    const val SPLASH = "splash"
    const val LOCK = "lock"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val IMPORT = "import"
    const val SCAN = "scan"
    const val ADD_PART = "add_part?barcode={barcode}&name={name}&brand={brand}&price={price}"
    const val EDIT_PART = "edit_part/{partId}"
    const val FITMENT_WIZARD = "fitment_wizard"
    const val NEW_BILL = "new_bill"
    const val INVOICE_PREVIEW = "invoice_preview/{invoiceId}"
    const val INVOICE_HISTORY = "invoice_history"
    const val INVENTORY = "inventory"
    const val ADD_STOCK = "add_stock"
    const val REPORTS = "reports"
    const val CUSTOMERS = "customers"
    const val SUPPLIERS = "suppliers"
    const val PIN_SETUP = "pin_setup"
    const val SHOP_SETUP = "shop_setup"

    fun addPartRoute(
        barcode: String? = null,
        name: String? = null,
        brand: String? = null,
        price: Double? = null
    ): String {
        fun enc(s: String?): String = android.net.Uri.encode(s ?: "")
        return "add_part?barcode=${enc(barcode)}&name=${enc(name)}&brand=${enc(brand)}&price=${price ?: ""}"
    }
}

@HiltViewModel
class AppShellViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val settings = settingsRepository.getSettings()
}

@Composable
fun HashmiMotorsMainScreen(
    viewModel: AppShellViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val settings by viewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.DASHBOARD,
        Routes.SEARCH,
        Routes.NEW_BILL,
        Routes.INVENTORY,
        Routes.REPORTS
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                HashmiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f))
        ) {
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
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) + fadeIn(tween(250))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) + fadeOut(tween(250))
                },
                popEnterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) + fadeIn(tween(250))
                },
                popExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) + fadeOut(tween(250))
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

                composable(Routes.LOCK) {
                    LockScreen(
                        onUnlocked = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOCK) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        onSearch = { navController.navigate(Routes.SEARCH) },
                        onNewBill = { navController.navigate(Routes.NEW_BILL) },
                        onAddStock = { navController.navigate(Routes.ADD_STOCK) },
                        onAddPart = { navController.navigate(Routes.addPartRoute()) },
                        onFitment = { navController.navigate(Routes.FITMENT_WIZARD) },
                        onInventory = { navController.navigate(Routes.INVENTORY) },
                        onReports = { navController.navigate(Routes.REPORTS) },
                        onHistory = { navController.navigate(Routes.INVOICE_HISTORY) },
                        onSettings = { navController.navigate(Routes.SETTINGS) },
                        onCustomers = { navController.navigate(Routes.CUSTOMERS) },
                        onImport = { navController.navigate(Routes.IMPORT) },
                        onScan = { navController.navigate(Routes.SCAN) }
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToShopSetup = { navController.navigate(Routes.SHOP_SETUP) },
                        onNavigateToSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onNavigateToPinSetup = { navController.navigate(Routes.PIN_SETUP) },
                        onNavigateToImport = { navController.navigate(Routes.IMPORT) }
                    )
                }

                composable(Routes.PIN_SETUP) {
                    PinSetupScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.SHOP_SETUP) {
                    ShopSetupScreen(
                        onComplete = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.SUPPLIERS) {
                    SuppliersScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onPartClick = { id -> navController.navigate("edit_part/$id") },
                        onAddPartClick = { navController.navigate(Routes.addPartRoute()) },
                        onScanClick = { navController.navigate(Routes.SCAN) },
                        onImportClick = { navController.navigate(Routes.IMPORT) },
                        onBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.DASHBOARD)
                            }
                        }
                    )
                }

                composable(Routes.SCAN) {
                    BarcodeScannerScreen(
                        onBack = { navController.popBackStack() },
                        onEditPart = { id -> navController.navigate("edit_part/$id") },
                        onAddPart = { barcode, name, brand, price ->
                            navController.navigate(Routes.addPartRoute(barcode, name, brand, price))
                        }
                    )
                }

                composable(Routes.IMPORT) {
                    CatalogImportScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    Routes.ADD_PART,
                    arguments = listOf(
                        navArgument("barcode") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("brand") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("price") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val barcode = backStackEntry.arguments?.getString("barcode")?.takeIf { it.isNotBlank() }
                    val name = backStackEntry.arguments?.getString("name")?.takeIf { it.isNotBlank() }
                    val brand = backStackEntry.arguments?.getString("brand")?.takeIf { it.isNotBlank() }
                    val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull()
                    AddPartScreen(
                        partId = null,
                        initialBarcode = barcode,
                        initialName = name,
                        initialBrand = brand,
                        initialPrice = price,
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
                        onBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.DASHBOARD)
                            }
                        },
                        onSaved = { invoiceId ->
                            navController.navigate("invoice_preview/$invoiceId") {
                                popUpTo(Routes.NEW_BILL) { inclusive = true }
                            }
                        },
                        onScanToBill = { navController.navigate(Routes.SCAN) }
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
                        onBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.DASHBOARD)
                            }
                        },
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
                        onPartClick = { id -> navController.navigate("edit_part/$id") }
                    )
                }

                composable(Routes.REPORTS) {
                    ReportsScreen(
                        onBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.DASHBOARD)
                            }
                        }
                    )
                }

                composable(Routes.CUSTOMERS) {
                    CustomerListScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
