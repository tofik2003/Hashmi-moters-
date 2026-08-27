package com.hashmimotors.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.hashmimotors.app.ui.components.HmBottomBar
import com.hashmimotors.app.ui.components.MainTab
import com.hashmimotors.app.ui.customers.CustomerListScreen
import com.hashmimotors.app.ui.dashboard.DashboardScreen
import com.hashmimotors.app.ui.more.MoreScreen
import com.hashmimotors.app.ui.fitment.FitmentScreen
import com.hashmimotors.app.ui.importhub.ImportHubScreen
import com.hashmimotors.app.ui.inventory.AddStockScreen
import com.hashmimotors.app.ui.inventory.InventoryScreen
import com.hashmimotors.app.ui.inventory.SupplierScreen
import com.hashmimotors.app.ui.onboarding.OnboardingScreen
import com.hashmimotors.app.ui.reports.ReportsScreen
import com.hashmimotors.app.ui.scanner.ScannerMode
import com.hashmimotors.app.ui.scanner.ScannerScreen
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
    const val SUPPLIERS = "suppliers"
    const val REPORTS = "reports"
    const val CUSTOMERS = "customers"
    const val IMPORT_HUB = "import_hub"
    const val MORE = "more"
    const val SCANNER = "scanner/{mode}"

    fun scanner(mode: String) = "scanner/$mode"
    fun editPart(id: String) = "edit_part/$id"
    fun invoicePreview(id: String) = "invoice_preview/$id"
}

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
    val navBack by navController.currentBackStackEntryAsState()
    val route = navBack?.destination?.route
    val showBar = route in setOf(Routes.DASHBOARD, Routes.SEARCH, Routes.MORE)
    val selectedTab = when (route) {
        Routes.SEARCH -> MainTab.CATALOG
        Routes.MORE -> MainTab.MORE
        else -> MainTab.HOME
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
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.SHOP_SETUP) { inclusive = true }
                            }
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
                    onCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onScan = { navController.navigate(Routes.scanner("search")) },
                    onImport = { navController.navigate(Routes.IMPORT_HUB) },
                    onInvoiceClick = { id -> navController.navigate(Routes.invoicePreview(id)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onEditShop = { navController.navigate(Routes.SHOP_SETUP) }
                )
            }
            composable(Routes.MORE) {
                MoreScreen(
                    onInventory = { navController.navigate(Routes.INVENTORY) },
                    onAddStock = { navController.navigate(Routes.ADD_STOCK) },
                    onImport = { navController.navigate(Routes.IMPORT_HUB) },
                    onFitment = { navController.navigate(Routes.FITMENT_WIZARD) },
                    onCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                    onReports = { navController.navigate(Routes.REPORTS) },
                    onHistory = { navController.navigate(Routes.INVOICE_HISTORY) },
                    onShop = { navController.navigate(Routes.SHOP_SETUP) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SEARCH) { entry ->
                val barcode by entry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                val voice by entry.savedStateHandle
                    .getStateFlow("voice_query", "")
                    .collectAsStateWithLifecycle()
                SearchScreen(
                    incomingQuery = barcode.ifBlank { voice },
                    onIncomingConsumed = {
                        entry.savedStateHandle["scan_barcode"] = ""
                        entry.savedStateHandle["voice_query"] = ""
                    },
                    onPartClick = { id -> navController.navigate(Routes.editPart(id)) },
                    onAddPartClick = { navController.navigate(Routes.ADD_PART) },
                    onScanClick = { navController.navigate(Routes.scanner("search")) },
                    onAddToBill = { partId ->
                        navController.navigate(Routes.NEW_BILL)
                        runCatching {
                            navController.getBackStackEntry(Routes.NEW_BILL)
                                .savedStateHandle["add_part_id"] = partId
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADD_PART) { entry ->
                val barcode by entry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                val photo by entry.savedStateHandle
                    .getStateFlow("photo_path", "")
                    .collectAsStateWithLifecycle()
                val ocr by entry.savedStateHandle
                    .getStateFlow("ocr_text", "")
                    .collectAsStateWithLifecycle()
                AddPartScreen(
                    partId = null,
                    incomingBarcode = barcode,
                    incomingPhoto = photo,
                    incomingOcr = ocr,
                    onIncomingConsumed = {
                        entry.savedStateHandle["scan_barcode"] = ""
                        entry.savedStateHandle["photo_path"] = ""
                        entry.savedStateHandle["ocr_text"] = ""
                    },
                    onScanBarcode = { navController.navigate(Routes.scanner("add_part")) },
                    onTakePhoto = { navController.navigate(Routes.scanner("photo")) },
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                Routes.EDIT_PART,
                arguments = listOf(navArgument("partId") { type = NavType.StringType })
            ) { backStackEntry ->
                val partId = backStackEntry.arguments?.getString("partId")
                val barcode by backStackEntry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                val photo by backStackEntry.savedStateHandle
                    .getStateFlow("photo_path", "")
                    .collectAsStateWithLifecycle()
                AddPartScreen(
                    partId = partId,
                    incomingBarcode = barcode,
                    incomingPhoto = photo,
                    incomingOcr = "",
                    onIncomingConsumed = {
                        backStackEntry.savedStateHandle["scan_barcode"] = ""
                        backStackEntry.savedStateHandle["photo_path"] = ""
                    },
                    onScanBarcode = { navController.navigate(Routes.scanner("add_part")) },
                    onTakePhoto = { navController.navigate(Routes.scanner("photo")) },
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.NEW_BILL) { entry ->
                val barcode by entry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                val addPartId by entry.savedStateHandle
                    .getStateFlow("add_part_id", "")
                    .collectAsStateWithLifecycle()
                BillingScreen(
                    incomingBarcode = barcode,
                    incomingPartId = addPartId,
                    onIncomingConsumed = {
                        entry.savedStateHandle["scan_barcode"] = ""
                        entry.savedStateHandle["add_part_id"] = ""
                    },
                    onBack = { navController.popBackStack() },
                    onSaved = { invoiceId ->
                        navController.navigate(Routes.invoicePreview(invoiceId)) {
                            popUpTo(Routes.NEW_BILL) { inclusive = true }
                        }
                    },
                    onScanItem = { navController.navigate(Routes.scanner("billing")) }
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
                    onInvoiceClick = { id -> navController.navigate(Routes.invoicePreview(id)) }
                )
            }
            composable(Routes.INVENTORY) { entry ->
                val barcode by entry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                InventoryScreen(
                    incomingQuery = barcode,
                    onIncomingConsumed = { entry.savedStateHandle["scan_barcode"] = "" },
                    onBack = { navController.popBackStack() },
                    onPartClick = { id -> navController.navigate(Routes.editPart(id)) },
                    onAddStock = { navController.navigate(Routes.ADD_STOCK) },
                    onScan = { navController.navigate(Routes.scanner("inventory")) }
                )
            }
            composable(Routes.SUPPLIERS) {
                SupplierScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADD_STOCK) { entry ->
                val barcode by entry.savedStateHandle
                    .getStateFlow("scan_barcode", "")
                    .collectAsStateWithLifecycle()
                AddStockScreen(
                    incomingBarcode = barcode,
                    onIncomingConsumed = { entry.savedStateHandle["scan_barcode"] = "" },
                    onScan = { navController.navigate(Routes.scanner("stock")) },
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.FITMENT_WIZARD) {
                FitmentScreen(
                    onBack = { navController.popBackStack() },
                    onPartClick = { id -> navController.navigate(Routes.editPart(id)) }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CUSTOMERS) {
                CustomerListScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.IMPORT_HUB) {
                ImportHubScreen(
                    onBack = { navController.popBackStack() },
                    onManual = { navController.navigate(Routes.ADD_PART) },
                    onScanBarcode = { navController.navigate(Routes.scanner("add_part")) },
                    onOcr = { navController.navigate(Routes.scanner("ocr")) },
                    onVoiceQuery = { spoken ->
                        navController.navigate(Routes.SEARCH)
                        navController.currentBackStackEntry?.savedStateHandle?.set("voice_query", spoken)
                    }
                )
            }
            composable(
                Routes.SCANNER,
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { entry ->
                val modeArg = entry.arguments?.getString("mode") ?: "search"
                val scannerMode = when (modeArg) {
                    "photo" -> ScannerMode.PHOTO
                    "ocr" -> ScannerMode.OCR
                    else -> ScannerMode.BARCODE
                }
                ScannerScreen(
                    mode = scannerMode,
                    onBarcode = { code ->
                        when (modeArg) {
                            "search", "dashboard" -> {
                                navController.popBackStack()
                                val searchEntry = navController.currentBackStackEntry
                                if (searchEntry?.destination?.route == Routes.SEARCH) {
                                    searchEntry.savedStateHandle["scan_barcode"] = code
                                } else {
                                    navController.navigate(Routes.SEARCH)
                                    navController.currentBackStackEntry?.savedStateHandle?.set("scan_barcode", code)
                                }
                            }
                            "add_part" -> {
                                navController.popBackStack()
                                val current = navController.currentBackStackEntry
                                val route = current?.destination?.route
                                if (route == Routes.ADD_PART || route == Routes.EDIT_PART) {
                                    current.savedStateHandle["scan_barcode"] = code
                                } else {
                                    navController.navigate(Routes.ADD_PART)
                                    navController.currentBackStackEntry?.savedStateHandle?.set("scan_barcode", code)
                                }
                            }
                            else -> {
                                navController.previousBackStackEntry?.savedStateHandle?.set("scan_barcode", code)
                                navController.popBackStack()
                            }
                        }
                    },
                    onPhoto = { path ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("photo_path", path)
                        navController.popBackStack()
                    },
                    onOcr = { text ->
                        navController.popBackStack()
                        navController.navigate(Routes.ADD_PART)
                        navController.currentBackStackEntry?.savedStateHandle?.set("ocr_text", text)
                    },
                    onClose = { navController.popBackStack() }
                )
            }
        }

        if (showBar) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                HmBottomBar(selected = selectedTab) { tab ->
                    when (tab) {
                        MainTab.HOME -> navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = false }
                            launchSingleTop = true
                        }
                        MainTab.CATALOG -> navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.DASHBOARD)
                            launchSingleTop = true
                        }
                        MainTab.SCAN -> navController.navigate(Routes.scanner("search"))
                        MainTab.BILL -> navController.navigate(Routes.NEW_BILL)
                        MainTab.MORE -> navController.navigate(Routes.MORE) {
                            popUpTo(Routes.DASHBOARD)
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}
