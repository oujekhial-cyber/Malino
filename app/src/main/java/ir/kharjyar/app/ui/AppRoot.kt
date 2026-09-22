package ir.kharjyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.components.AppBackdrop
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
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

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
                AppBackdrop {
                    when {
                        !settings.onboardingDone -> OnboardingScreen(viewModel)
                        locked -> LockScreen(onUnlockRequest = { onRequestBiometric { viewModel.unlock() } })
                        else -> MainScaffold(viewModel, initialDestination)
                    }
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

private data class DrawerEntry(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: Boolean = false
)

private val drawerEntries = listOf(
    DrawerEntry("home", "خانه", Icons.Filled.Home),
    DrawerEntry("transactions", "تراکنش‌ها", Icons.AutoMirrored.Filled.ReceiptLong),
    DrawerEntry("reports", "گزارش‌ها", Icons.Filled.BarChart),
    DrawerEntry("accounts", "حساب‌ها", Icons.Filled.AccountBalance),
    DrawerEntry("categories", "دسته‌بندی‌ها", Icons.Filled.Category),
    DrawerEntry("review", "نیازمند بررسی", Icons.Filled.RateReview, badge = true),
    DrawerEntry("backup", "بکاپ", Icons.Filled.CloudUpload),
    DrawerEntry("settings", "تنظیمات", Icons.Filled.Settings)
)

private fun titleOf(route: String?): String = when {
    route == null -> "خرج‌یار"
    route.startsWith("accountEdit") -> "ویرایش حساب"
    route.startsWith("accountFromSms") -> "معرفی حساب از پیامک"
    route.startsWith("template") -> "آموزش قالب پیامک"
    route.startsWith("tx/") -> "ویرایش تراکنش"
    route == "manual" -> "ثبت تراکنش"
    else -> drawerEntries.firstOrNull { it.route == route }?.label ?: "خرج‌یار"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(viewModel: AppViewModel, initialDestination: String?) {
    val navController = rememberNavController()
    val reviewCount by viewModel.reviewCount.collectAsState()
    val pendingDest by MainActivity.PendingDest.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val skin = LocalAppSkin.current

    LaunchedEffect(initialDestination) {
        initialDestination?.let { navController.navigate(it) }
    }
    LaunchedEffect(pendingDest) {
        pendingDest?.let {
            navController.navigate(it)
            MainActivity.PendingDest.value = null
        }
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = skin.dialogColor,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // هدر گرادیانی با آیکون و نام برنامه
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.linearGradient(skin.heroGradient))
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("خرج‌یار", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Text(
                                "دستیار خرج و دخل شما",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                drawerEntries.forEach { entry ->
                    NavigationDrawerItem(
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        selected = currentRoute == entry.route,
                        label = { Text(entry.label) },
                        icon = { Icon(entry.icon, contentDescription = entry.label) },
                        badge = {
                            if (entry.badge && reviewCount > 0) {
                                Badge { Text(Digits.toPersian(reviewCount.toString())) }
                            }
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != entry.route) {
                                navController.navigate(entry.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.imePadding(),
            topBar = {
                TopAppBar(
                    title = { Text(titleOf(currentRoute)) },
                    // دکمه همبرگری در سمت چپ برنامه (RTL ⇒ actions سمت چپ قرار می‌گیرد)
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "منو")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = skin.onBackdrop,
                        actionIconContentColor = skin.onBackdrop
                    )
                )
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
}
