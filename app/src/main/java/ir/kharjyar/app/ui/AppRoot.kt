package ir.kharjyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.ui.components.AppBackdrop
import ir.kharjyar.app.ui.components.BottomItem
import ir.kharjyar.app.ui.components.BottomNavBar
import ir.kharjyar.app.ui.components.greetingByHour
import ir.kharjyar.app.ui.components.todayHeaderLine
import ir.kharjyar.app.ui.screens.AccountEditScreen
import ir.kharjyar.app.ui.screens.AccountFromSmsScreen
import ir.kharjyar.app.ui.screens.AccountsScreen
import ir.kharjyar.app.ui.screens.BackupScreen
import ir.kharjyar.app.ui.screens.CategoriesScreen
import ir.kharjyar.app.ui.screens.DashboardScreen
import ir.kharjyar.app.ui.screens.ManualEntryScreen
import ir.kharjyar.app.ui.screens.QuickAddScreen
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
    val icon: ImageVector,
    val badge: Boolean = false
)

/** آیتم‌های کشو (همه مقصدها). */
private val drawerEntries = listOf(
    DrawerEntry("home", "خانه", Icons.Filled.Home),
    DrawerEntry("quickAdd", "بگو تا بنویسم", Icons.Filled.AutoAwesome),
    DrawerEntry("transactions", "تراکنش‌ها", Icons.AutoMirrored.Filled.ReceiptLong),
    DrawerEntry("reports", "گزارش‌ها", Icons.Filled.BarChart),
    DrawerEntry("accounts", "حساب‌ها", Icons.Filled.AccountBalance),
    DrawerEntry("categories", "دسته‌بندی‌ها", Icons.Filled.Category),
    DrawerEntry("review", "نیازمند بررسی", Icons.Filled.RateReview, badge = true),
    DrawerEntry("backup", "بکاپ", Icons.Filled.CloudUpload),
    DrawerEntry("settings", "تنظیمات", Icons.Filled.Settings)
)

/** چهار مقصد اصلی نوار پایین. */
private val bottomRoutes = listOf("home", "transactions", "reports", "accounts")

private fun titleOf(route: String?): String = when {
    route == null -> "خرج‌یار"
    route.startsWith("accountEdit") -> "ویرایش حساب"
    route.startsWith("accountFromSms") -> "معرفی حساب از پیامک"
    route.startsWith("template") -> "آموزش قالب پیامک"
    route.startsWith("tx/") -> "ویرایش تراکنش"
    route == "manual" -> "ثبت تراکنش"
    route == "manual/{dir}" -> "ثبت تراکنش"
    route == "quickAdd" -> "بگو تا بنویسم"
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
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()

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
    // پیش از باز کردن فرم، نوع تراکنش پرسیده می‌شود تا هر صفحه ساده‌تر بماند.
    var showDirectionChooser by rememberSaveable { mutableStateOf(false) }

    fun go(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // کشو در سمت راست: در RTL پیش‌فرض ModalNavigationDrawer از راست باز می‌شود.
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        drawerContent = {
            ModalDrawerSheet(
                // پهنای جمع‌وجور به‌جای پهنای پیش‌فرض ۳۶۰dp
                modifier = Modifier.width(270.dp),
                drawerContainerColor = Color.Transparent,
                drawerContentColor = skin.onBackdrop,
                drawerShape = RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp),
                windowInsets = WindowInsets(0)
            ) {
                DrawerBody(
                    skin = skin,
                    opened = drawerState.isOpen,
                    currentRoute = currentRoute,
                    reviewCount = reviewCount,
                    accountCount = accounts.count { !it.archived },
                    defaultAccountName = accounts.firstOrNull { it.id == settings.defaultAccountId }?.title,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        go(route)
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.imePadding(),
            topBar = {
                TopAppBar(
                    title = {
                        if (currentRoute == "home") {
                            // سلام و تاریخ در بالای برنامه، کنار منوی همبرگری
                            Column {
                                Text(
                                    greetingByHour(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    todayHeaderLine(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = skin.onBackdrop.copy(alpha = 0.72f),
                                    maxLines = 1
                                )
                            }
                        } else {
                            Text(titleOf(currentRoute))
                        }
                    },
                    // منوی همبرگری سمت راست: در RTL، navigationIcon سمت راست قرار می‌گیرد.
                    navigationIcon = {
                        val menuRotation by animateFloatAsState(
                            targetValue = if (drawerState.isOpen) 90f else 0f,
                            animationSpec = tween(300),
                            label = "menuRotation"
                        )
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) {
                            Icon(
                                if (drawerState.isOpen) Icons.Filled.Close else Icons.Filled.Menu,
                                contentDescription = if (drawerState.isOpen) "بستن منو" else "منو",
                                modifier = Modifier.graphicsLayer { rotationZ = menuRotation }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = skin.onBackdrop,
                        navigationIconContentColor = skin.onBackdrop
                    )
                )
            },
            bottomBar = {
                if (currentRoute in bottomRoutes) {
                    BottomNavBar(
                        items = listOf(
                            BottomItem("home", "خانه", Icons.Filled.Home),
                            BottomItem("transactions", "تراکنش‌ها", Icons.AutoMirrored.Filled.ReceiptLong),
                            BottomItem("reports", "گزارش‌ها", Icons.Filled.BarChart),
                            BottomItem("accounts", "حساب‌ها", Icons.Filled.AccountBalance)
                        ),
                        currentRoute = currentRoute,
                        onSelect = { go(it) },
                        onFabClick = { showDirectionChooser = true }
                    )
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
                composable("manual/{dir}") { entry ->
                    ManualEntryScreen(
                        viewModel,
                        navController,
                        presetDirection = when (entry.arguments?.getString("dir")) {
                            "deposit" -> TxDirection.DEPOSIT
                            "withdraw" -> TxDirection.WITHDRAW
                            else -> null
                        }
                    )
                }
                composable("quickAdd") { QuickAddScreen(viewModel, navController) }
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

        if (showDirectionChooser) {
            DirectionChooserDialog(
                skin = skin,
                onDismiss = { showDirectionChooser = false },
                onPick = { dir ->
                    showDirectionChooser = false
                    navController.navigate("manual/$dir")
                }
            )
        }
    }
}

/**
 * پرسش «واریز یا برداشت؟» پیش از باز شدن فرم ثبت تراکنش.
 * با این کار فرم بعدی کوتاه‌تر و بدون گزینه‌های اضافه باز می‌شود.
 */
@Composable
private fun DirectionChooserDialog(
    skin: ir.kharjyar.app.ui.theme.AppSkin,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = skin.dialogColor,
            contentColor = skin.onBackdrop,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "چه چیزی ثبت کنیم؟",
                    style = MaterialTheme.typography.titleMedium,
                    color = skin.onBackdrop
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "پول وارد حساب شد یا از حساب خارج شد؟",
                    style = MaterialTheme.typography.bodySmall,
                    color = skin.onBackdrop.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DirectionChoiceCard(
                        title = "واریز",
                        subtitle = "پول گرفتم",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        tint = skin.incomeColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onPick("deposit") }
                    )
                    DirectionChoiceCard(
                        title = "برداشت",
                        subtitle = "پول دادم",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        tint = skin.expenseColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onPick("withdraw") }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "انصراف",
                    style = MaterialTheme.typography.labelLarge,
                    color = skin.onBackdrop.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DirectionChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.14f))
            .border(1.5.dp, tint.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalAppSkin.current.onBackdrop.copy(alpha = 0.7f)
        )
    }
}

/** بدنه کشو: هدر گرادیانی با آواتار، خلاصه وضعیت، و آیتم‌های کپسولی. */
@Composable
private fun DrawerBody(
    skin: ir.kharjyar.app.ui.theme.AppSkin,
    opened: Boolean,
    currentRoute: String?,
    reviewCount: Int,
    accountCount: Int,
    defaultAccountName: String?,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skin.backgroundColors + skin.backgroundColors.last()))
    ) {
        // ---------- هدر ----------
        val headerAlpha by animateFloatAsState(
            targetValue = if (opened) 1f else 0f,
            animationSpec = tween(320),
            label = "drawerHeaderAlpha"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .background(Brush.linearGradient(skin.heroGradient))
        ) {
            // تصویر اختصاصی تم پشت هدر (طلایی/شکوفه/اقیانوس)
            skin.heroImage?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = if (skin.onHero.luminance() > 0.5f) 0.22f else 0.06f))
                )
            }
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .graphicsLayer { alpha = headerAlpha }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = skin.onHero,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("خرج‌یار", style = MaterialTheme.typography.titleMedium, color = skin.onHero)
                        Text(
                            "دستیار خرج و دخل شما",
                            style = MaterialTheme.typography.bodySmall,
                            color = skin.onHero.copy(alpha = 0.85f)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DrawerStat("حساب‌ها", Digits.toPersian(accountCount.toString()), skin, Modifier.weight(1f))
                    DrawerStat("بررسی", Digits.toPersian(reviewCount.toString()), skin, Modifier.weight(1f))
                }
                defaultAccountName?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "حساب پیش‌فرض: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = skin.onHero.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // ---------- آیتم‌ها ----------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            drawerEntries.forEachIndexed { index, entry ->
                // ورود پلکانی آیتم‌ها هنگام باز شدن کشو
                val delay = 40 + index * 35
                val alpha by animateFloatAsState(
                    targetValue = if (opened) 1f else 0f,
                    animationSpec = tween(260, delayMillis = if (opened) delay else 0),
                    label = "drawerItemAlpha"
                )
                val offsetX by animateDpAsState(
                    targetValue = if (opened) 0.dp else 26.dp,
                    animationSpec = tween(300, delayMillis = if (opened) delay else 0),
                    label = "drawerItemOffset"
                )
                DrawerRow(
                    entry = entry,
                    selected = currentRoute == entry.route,
                    badgeCount = if (entry.badge) reviewCount else 0,
                    skin = skin,
                    modifier = Modifier
                        .graphicsLayer {
                            this.alpha = alpha
                            // در RTL آیتم‌ها از سمت راست وارد می‌شوند
                            translationX = offsetX.toPx()
                        },
                    onClick = { onNavigate(entry.route) }
                )
            }
        }

        // ---------- پانویس ----------
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "نسخه ۱.۰.۰",
                style = MaterialTheme.typography.labelSmall,
                color = skin.onBackdrop.copy(alpha = 0.5f)
            )
            Text(
                PersianDate.today().format(),
                style = MaterialTheme.typography.labelSmall,
                color = skin.onBackdrop.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun DrawerStat(
    label: String,
    value: String,
    skin: ir.kharjyar.app.ui.theme.AppSkin,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = skin.onHero)
        Text(label, style = MaterialTheme.typography.labelSmall, color = skin.onHero.copy(alpha = 0.85f))
    }
}

@Composable
private fun DrawerRow(
    entry: DrawerEntry,
    selected: Boolean,
    badgeCount: Int,
    skin: ir.kharjyar.app.ui.theme.AppSkin,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) Brush.horizontalGradient(
                    listOf(skin.accent.copy(alpha = 0.22f), Color.Transparent)
                ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // نوار نشانگر انتخاب
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(CircleShape)
                .background(if (selected) skin.accent else Color.Transparent)
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            entry.icon,
            contentDescription = entry.label,
            tint = if (selected) skin.accent else skin.onBackdrop.copy(alpha = 0.75f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            entry.label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) skin.accent else skin.onBackdrop,
            modifier = Modifier.weight(1f)
        )
        if (badgeCount > 0) {
            Badge(containerColor = skin.expenseColor) {
                Text(Digits.toPersian(badgeCount.toString()))
            }
        }
    }
}
