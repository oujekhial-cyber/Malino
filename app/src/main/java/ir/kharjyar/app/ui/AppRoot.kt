package ir.kharjyar.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.screens.AccountEditScreen
import ir.kharjyar.app.ui.screens.AccountFromSmsScreen
import ir.kharjyar.app.ui.screens.AccountsScreen
import ir.kharjyar.app.ui.screens.BackupScreen
import ir.kharjyar.app.ui.screens.CategoriesScreen
import ir.kharjyar.app.ui.screens.DashboardScreen
import ir.kharjyar.app.ui.screens.ManualEntryScreen
import ir.kharjyar.app.ui.screens.OnboardingScreen
import ir.kharjyar.app.ui.screens.ReportsScreen
import ir.kharjyar.app.ui.screens.ReviewScreen
import ir.kharjyar.app.ui.screens.SettingsScreen
import ir.kharjyar.app.ui.screens.TemplateTrainScreen
import ir.kharjyar.app.ui.screens.TransactionEditScreen
import ir.kharjyar.app.ui.screens.TransactionsScreen
import ir.kharjyar.app.ui.theme.KharjYarTheme

@Composable
fun AppRoot(
    viewModel: AppViewModel,
    initialDestination: String?,
    onRequestBiometric: (onSuccess: () -> Unit) -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val locked by viewModel.locked.collectAsState()

    KharjYarTheme(themeMode = settings.themeMode, palette = settings.palette) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when {
                    !settings.onboardingDone -> OnboardingScreen(viewModel)
                    locked -> LockScreen(onUnlockRequest = { onRequestBiometric { viewModel.unlock() } })
                    else -> MainScaffold(viewModel, initialDestination)
                }
            }
        }
    }
}

@Composable
private fun LockScreen(onUnlockRequest: () -> Unit) {
    LaunchedEffect(Unit) { onUnlockRequest() }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "خرج‌یار قفل است",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "اطلاعات مالی تا تأیید هویت نمایش داده نمی‌شوند",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(onClick = onUnlockRequest) { Text("باز کردن قفل") }
    }
}

private data class NavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
private fun MainScaffold(viewModel: AppViewModel, initialDestination: String?) {
    val navController = rememberNavController()
    val reviewCount by viewModel.reviewCount.collectAsState()
    val pendingDest by MainActivity.PendingDest.collectAsState()

    LaunchedEffect(initialDestination) {
        initialDestination?.let { navController.navigate(it) }
    }
    LaunchedEffect(pendingDest) {
        pendingDest?.let {
            navController.navigate(it)
            MainActivity.PendingDest.value = null
        }
    }

    val items = listOf(
        NavItem("home", "خانه") { Icon(Icons.Filled.Home, contentDescription = "خانه") },
        NavItem("transactions", "تراکنش‌ها") { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "تراکنش‌ها") },
        NavItem("reports", "گزارش‌ها") { Icon(Icons.Filled.BarChart, contentDescription = "گزارش‌ها") },
        NavItem("settings", "تنظیمات") { Icon(Icons.Filled.Settings, contentDescription = "تنظیمات") }
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = currentRoute in items.map { it.route }, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.route == "home" && reviewCount > 0) {
                                    BadgedBox(badge = { Badge { Text(Digits.toPersian(reviewCount.toString())) } }) { item.icon() }
                                } else item.icon()
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { DashboardScreen(viewModel, navController) }
            composable("transactions") { TransactionsScreen(viewModel, navController) }
            composable("reports") { ReportsScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel, navController) }
            composable("manual") { ManualEntryScreen(viewModel, navController) }
            composable("review") { ReviewScreen(viewModel, navController) }
            composable("accounts") { AccountsScreen(viewModel, navController) }
            composable("accountEdit/{id}") { entry ->
                AccountEditScreen(viewModel, navController, entry.arguments?.getString("id")?.toLongOrNull() ?: 0L)
            }
            composable("accountFromSms/{smsId}") { entry ->
                AccountFromSmsScreen(viewModel, navController, entry.arguments?.getString("smsId")?.toLongOrNull() ?: 0L)
            }
            composable("template/{smsId}") { entry ->
                TemplateTrainScreen(viewModel, navController, entry.arguments?.getString("smsId")?.toLongOrNull() ?: 0L)
            }
            composable("tx/{txId}") { entry ->
                TransactionEditScreen(viewModel, navController, entry.arguments?.getString("txId")?.toLongOrNull() ?: 0L)
            }
            composable("categories") { CategoriesScreen(viewModel) }
            composable("backup") { BackupScreen(viewModel) }
        }
    }
}
