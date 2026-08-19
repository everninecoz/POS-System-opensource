package com.posopensrc.core.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.ui.screens.customers.CustomerListScreen
import com.posopensrc.ui.screens.discounts.DiscountListScreen
import com.posopensrc.ui.screens.home.HomeScreen
import com.posopensrc.ui.screens.login.LoginScreen
import com.posopensrc.ui.screens.pos.PosScreen
import com.posopensrc.ui.screens.products.ProductListScreen
import com.posopensrc.ui.screens.reports.ProfitLossScreen
import com.posopensrc.ui.screens.reports.ReportsScreen
import com.posopensrc.ui.screens.settings.SettingsScreen
import com.posopensrc.ui.screens.shifts.ShiftScreen
import com.posopensrc.ui.screens.stock.StockOpnameScreen
import com.posopensrc.ui.screens.users.UserManagementScreen
import com.posopensrc.ui.screens.voidlog.VoidLogScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Beranda", Icons.Default.Home)
    data object Products : Screen("products", "Produk", Icons.Default.Inventory)
    data object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    data object Reports : Screen("reports", "Laporan", Icons.Default.BarChart)
    data object ProfitLoss : Screen("profit-loss", "Laba Rugi", Icons.Default.BarChart)
    data object Customers : Screen("customers", "Pelanggan", Icons.Default.People)
    data object Shifts : Screen("shifts", "Shift", Icons.Default.AccessTime)
    data object Discounts : Screen("discounts", "Diskon", Icons.Default.LocalOffer)
    data object StockOpname : Screen("stock-opname", "Stok", Icons.Default.Inventory)
    data object VoidLog : Screen("void-log", "Void", Icons.Default.Cancel)
    data object UserManagement : Screen("user-management", "User", Icons.Default.People)
    data object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    mainViewModel: MainViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass
) {
    val isLoggedIn by mainViewModel.sessionManager.isLoggedIn.collectAsState(initial = false)
    val role by mainViewModel.sessionManager.role.collectAsState(initial = "kasir")
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { }
        )
    } else {
        val primaryScreens = listOf(
            Screen.Home,
            Screen.Pos,
            Screen.Reports
        )

        val secondaryScreens = remember(role) {
            mutableListOf(
                Screen.Products,
                Screen.Customers,
                Screen.Shifts,
                Screen.Discounts,
                Screen.StockOpname,
                Screen.VoidLog
            ).apply {
                if (role == "admin") {
                    add(Screen.UserManagement)
                }
                add(Screen.Settings)
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Menu Pengelolaan",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider()
                        
                        secondaryScreens.forEach { screen ->
                            NavigationDrawerItem(
                                label = { Text(screen.title) },
                                selected = currentScreen == screen,
                                icon = { Icon(screen.icon, contentDescription = null) },
                                onClick = {
                                    currentScreen = screen
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider()
                        
                        NavigationDrawerItem(
                            label = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
                            selected = false,
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                scope.launch { drawerState.close() }
                                mainViewModel.logout() 
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val isSmallScreen = configuration.screenHeightDp < 480
            val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

            NavigationSuiteScaffold(
                layoutType = if (isLandscape || isExpanded) NavigationSuiteType.NavigationRail else NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
                navigationSuiteItems = {
                    primaryScreens.forEach { screen ->
                        item(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { if (!isLandscape || !isSmallScreen) Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen }
                        )
                    }
                    
                    // "Lainnya" item to open drawer
                    item(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Lainnya") },
                        label = { if (!isLandscape || !isSmallScreen) Text("Lainnya") },
                        selected = false,
                        onClick = { scope.launch { drawerState.open() } }
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        if (!isLandscape || !isSmallScreen) {
                            TopAppBar(
                                title = { Text(currentScreen.title) },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Main Content
                        when (currentScreen) {
                            Screen.Home -> HomeScreen()
                            Screen.Products -> ProductListScreen()
                            Screen.Pos -> PosScreen()
                            Screen.Reports -> ReportsScreen()
                            Screen.ProfitLoss -> ProfitLossScreen()
                            Screen.Customers -> CustomerListScreen()
                            Screen.Shifts -> ShiftScreen()
                            Screen.Discounts -> DiscountListScreen()
                            Screen.StockOpname -> StockOpnameScreen()
                            Screen.VoidLog -> VoidLogScreen()
                            Screen.UserManagement -> UserManagementScreen()
                            Screen.Settings -> SettingsScreen(
                                onToggleDarkMode = { mainViewModel.toggleDarkMode() },
                                onChangeLanguage = { mainViewModel.setLanguage(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
