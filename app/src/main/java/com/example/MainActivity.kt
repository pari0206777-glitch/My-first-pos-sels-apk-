package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ShopDetails
import com.example.data.ShopPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.ProductRepository
import com.example.data.update.AppUpdateCheckResult
import com.example.data.update.AppUpdateInfo
import com.example.data.update.AppUpdateManager
import com.example.ui.screens.AppUpdateDialog
import com.example.ui.screens.AppUpdateSettingsDialog
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.CategoryBrandScreen
import com.example.ui.screens.ComingSoonDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.ProductManagementScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ShopSetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.viewmodel.ProductViewModel
import com.example.ui.viewmodel.ProductViewModelFactory

enum class BottomNavSection(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "bottom_nav_home"),
    BILLING("Billing", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "bottom_nav_billing"),
    INVENTORY("Inventory", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "bottom_nav_inventory"),
    REPORTS("Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment, "bottom_nav_reports"),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "bottom_nav_more")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VyaparPosApp()
            }
        }
    }
}

@Composable
fun VyaparPosApp() {
    val context = LocalContext.current
    val shopPrefs = remember { ShopPreferences(context) }

    var currentShopDetails by remember { mutableStateOf(shopPrefs.getShopDetails()) }
    var isEditingShop by remember { mutableStateOf(false) }

    if (currentShopDetails == null || isEditingShop) {
        ShopSetupScreen(
            initialDetails = currentShopDetails,
            onSaveSuccess = { newDetails ->
                shopPrefs.saveShopDetails(newDetails)
                currentShopDetails = newDetails
                isEditingShop = false
            }
        )
    } else {
        MainScreen(
            shopDetails = currentShopDetails!!,
            onEditShopDetails = { isEditingShop = true },
            onResetShopData = {
                shopPrefs.clearShopDetails()
                currentShopDetails = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    shopDetails: ShopDetails,
    onEditShopDetails: () -> Unit,
    onResetShopData: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val repository = remember {
        ProductRepository(
            productDao = database.productDao(),
            categoryDao = database.categoryDao(),
            brandDao = database.brandDao()
        )
    }
    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(repository)
    )

    val updateManager = remember { AppUpdateManager(context) }
    var activeUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isUpdateMandatory by remember { mutableStateOf(false) }
    var showUpdateSettingsDialog by remember { mutableStateOf(false) }

    // Automatic silent check on app startup (offline-resilient, non-blocking)
    LaunchedEffect(Unit) {
        val checkResult = updateManager.checkForUpdate(isManualCheck = false)
        if (checkResult is AppUpdateCheckResult.UpdateAvailable && checkResult.shouldAutoPrompt) {
            activeUpdateInfo = checkResult.updateInfo
            isUpdateMandatory = checkResult.isMandatory
        }
    }

    var selectedSection by remember { mutableStateOf(BottomNavSection.HOME) }
    var activeDetailFeature by remember { mutableStateOf<String?>(null) }

    // Intercept back button gracefully
    BackHandler(enabled = activeDetailFeature != null || selectedSection != BottomNavSection.HOME) {
        if (activeDetailFeature == "Categories & Brands") {
            activeDetailFeature = "Products"
        } else if (activeDetailFeature != null) {
            activeDetailFeature = null
        } else if (selectedSection != BottomNavSection.HOME) {
            selectedSection = BottomNavSection.HOME
        }
    }

    if (activeDetailFeature == "Products" || activeDetailFeature == "Products Catalog") {
        ProductManagementScreen(
            viewModel = productViewModel,
            onBackClick = { activeDetailFeature = null },
            onOpenCategoryBrandScreen = { activeDetailFeature = "Categories & Brands" }
        )
        return
    }

    if (activeDetailFeature == "Categories & Brands") {
        val categories by productViewModel.allCategories.collectAsState()
        val brands by productViewModel.allBrands.collectAsState()
        CategoryBrandScreen(
            categories = categories,
            brands = brands,
            onBackClick = { activeDetailFeature = "Products" },
            onSaveCategory = { cat, isEdit -> productViewModel.saveCategory(cat, isEdit) {} },
            onDeleteCategory = { cat -> productViewModel.deleteCategory(cat) },
            onSaveBrand = { br, isEdit -> productViewModel.saveBrand(br, isEdit) {} },
            onDeleteBrand = { br -> productViewModel.deleteBrand(br) }
        )
        return
    }

    if (activeDetailFeature != null) {
        ComingSoonDetailScreen(
            featureTitle = activeDetailFeature ?: "",
            onBackClick = { activeDetailFeature = null }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedSection) {
                            BottomNavSection.HOME -> "VyaparPOS"
                            BottomNavSection.BILLING -> "Billing & Checkout"
                            BottomNavSection.INVENTORY -> "Inventory & Stock"
                            BottomNavSection.REPORTS -> "Business Reports"
                            BottomNavSection.MORE -> "More Options"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(TealAccent.copy(alpha = 0.35f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = shopDetails.shopName.ifEmpty { "Active Store" }.take(18),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                BottomNavSection.values().forEach { section ->
                    val isSelected = selectedSection == section
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                                contentDescription = section.title
                            )
                        },
                        label = {
                            Text(
                                text = section.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = TealAccent.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(section.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedSection) {
                BottomNavSection.HOME -> {
                    HomeScreen(
                        shopDetails = shopDetails,
                        onActionClick = { actionTitle ->
                            when (actionTitle) {
                                "Create Bill" -> selectedSection = BottomNavSection.BILLING
                                "Inventory" -> selectedSection = BottomNavSection.INVENTORY
                                "Reports" -> selectedSection = BottomNavSection.REPORTS
                                else -> activeDetailFeature = actionTitle
                            }
                        }
                    )
                }
                BottomNavSection.BILLING -> {
                    BillingScreen()
                }
                BottomNavSection.INVENTORY -> {
                    InventoryScreen(
                        onOpenProducts = { activeDetailFeature = "Products" }
                    )
                }
                BottomNavSection.REPORTS -> {
                    ReportsScreen()
                }
                BottomNavSection.MORE -> {
                    MoreScreen(
                        shopDetails = shopDetails,
                        onEditShopDetails = onEditShopDetails,
                        onResetShopData = onResetShopData,
                        onOpenProducts = { activeDetailFeature = "Products" },
                        onOpenCategoriesBrands = { activeDetailFeature = "Categories & Brands" },
                        onOpenAppUpdates = { showUpdateSettingsDialog = true }
                    )
                }
            }
        }
    }

    // App Update Notification Dialog
    if (activeUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = activeUpdateInfo!!,
            isMandatory = isUpdateMandatory,
            currentVersionName = updateManager.currentVersionName,
            currentVersionCode = updateManager.currentVersionCode,
            updateManager = updateManager,
            onDismiss = { activeUpdateInfo = null }
        )
    }

    // App Update Settings Dialog
    if (showUpdateSettingsDialog) {
        AppUpdateSettingsDialog(
            updateManager = updateManager,
            onDismiss = { showUpdateSettingsDialog = false },
            onUpdateFound = { info, mandatory ->
                activeUpdateInfo = info
                isUpdateMandatory = mandatory
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MyApplicationTheme {
        MainScreen(
            shopDetails = ShopDetails(
                shopName = "Super Bazaar",
                ownerName = "Rajesh Gupta",
                mobileNumber = "9876543210",
                address = "Shop 12, Main Street",
                city = "Mumbai",
                state = "Maharashtra"
            ),
            onEditShopDetails = {},
            onResetShopData = {}
        )
    }
}
