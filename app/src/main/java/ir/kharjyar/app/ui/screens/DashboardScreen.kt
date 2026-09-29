package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.balance.TxSummarizer
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.BankCard
import ir.kharjyar.app.ui.components.EnterCard
import ir.kharjyar.app.ui.components.ScreenEnterAnimation
import ir.kharjyar.app.ui.components.GlassSnackbarHost
import ir.kharjyar.app.ui.components.HeroCard
import androidx.compose.material3.SnackbarResult
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.SkinCard
import androidx.compose.material.icons.filled.SwapHoriz
import ir.kharjyar.app.ui.components.EmbossedText
import ir.kharjyar.app.ui.theme.LocalAppSkin

@Composable
fun LiveMarketGlass(unit:ir.kharjyar.app.core.money.MoneyUnit) {
    val scope=rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var panelVisible by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var gold by remember { mutableStateOf<Long?>(null) }
    var dollar by remember { mutableStateOf<Long?>(null) }
    var failed by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if(expanded) 180f else 0f,animationSpec=tween(550),label="marketArrow")
    fun closePanel(){scope.launch{panelVisible=false;kotlinx.coroutines.delay(550);expanded=false}}
    LaunchedEffect(expanded,refreshKey) {
        if(!expanded)return@LaunchedEffect
        panelVisible=true;loading=true;failed=false
        coroutineScope {
            val goldRequest=async { ir.kharjyar.app.assets.GoldPriceService.gram18Rial() }
            val dollarRequest=async { ir.kharjyar.app.assets.GoldPriceService.dollarRial() }
            gold=goldRequest.await();dollar=dollarRequest.await()
        }
        failed=gold==null&&dollar==null;loading=false
    }
    androidx.compose.material3.Surface(
        modifier=Modifier.clickable { if(expanded)closePanel() else expanded=true },
        color=MaterialTheme.colorScheme.surface.copy(alpha=.72f),
        shape=RoundedCornerShape(50),
        border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.primary.copy(alpha=.28f)),
        tonalElevation=5.dp,shadowElevation=8.dp
    ) {
        Row(Modifier.padding(horizontal=12.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Filled.ShowChart,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(18.dp))
            Text("نبض بازار",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelLarge)
            Icon(Icons.Filled.ExpandMore,null,modifier=Modifier.size(18.dp).graphicsLayer(rotationZ=arrowRotation))
        }
    }
    if(expanded) Popup(alignment=Alignment.TopCenter,onDismissRequest={closePanel()},properties=PopupProperties(focusable=true,dismissOnBackPress=true,dismissOnClickOutside=false)) {
        Box(Modifier.fillMaxSize().clickable(onClick={closePanel()}).padding(top=58.dp,start=14.dp,end=14.dp),contentAlignment=Alignment.TopCenter) {
            AnimatedVisibility(visible=panelVisible,enter=expandVertically(expandFrom=Alignment.Top,animationSpec=tween(650))+fadeIn(tween(600)),exit=shrinkVertically(shrinkTowards=Alignment.Top,animationSpec=tween(550))+fadeOut(tween(450))) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=.07f),Color.Transparent,MaterialTheme.colorScheme.tertiary.copy(alpha=.05f)))).border(1.dp,MaterialTheme.colorScheme.outline.copy(alpha=.38f),RoundedCornerShape(24.dp)).clickable {}.padding(15.dp)) {
                    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                            Column { Text("نرخ لحظه‌ای بازار ایران",fontWeight=FontWeight.Bold);Text("دریافت آنلاین از بازار",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                            Icon(Icons.Filled.Refresh,"به‌روزرسانی",tint=MaterialTheme.colorScheme.primary,modifier=Modifier.clip(CircleShape).clickable(enabled=!loading){refreshKey++}.padding(8.dp))
                        }
                        if(loading) androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            MarketRateTile("طلای ۱۸ عیار","هر گرم",gold,unit,Color(0xFFD7A928),Modifier.weight(1f))
                            MarketRateTile("دلار آزاد","هر دلار",dollar,unit,Color(0xFF20A565),Modifier.weight(1f))
                        }
                        if(failed) Text("دریافت نرخ‌ها ممکن نشد؛ اینترنت را بررسی و دوباره تلاش کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
@Composable
private fun MarketRateTile(title:String,subtitle:String,value:Long?,unit:ir.kharjyar.app.core.money.MoneyUnit,accent:Color,modifier:Modifier=Modifier) {
    androidx.compose.material3.Surface(modifier=modifier,shape=RoundedCornerShape(18.dp),color=accent.copy(alpha=.10f),border=androidx.compose.foundation.BorderStroke(1.dp,accent.copy(alpha=.35f))) {
        Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) { Text(title,fontWeight=FontWeight.Bold,color=accent);Text(subtitle,style=MaterialTheme.typography.labelSmall);Text(value?.let{Money.format(it,unit)}?:"—",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodyMedium) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, nav: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recent by viewModel.scopedRecent.collectAsState()
    val reviewCount by viewModel.reviewCount.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val bankBalances by viewModel.bankBalances.collectAsState()
    val scopedTx by viewModel.scopedTransactions.collectAsState()
    val defaultAccount by viewModel.defaultAccount.collectAsState()
    val skin = LocalAppSkin.current
    // از تنظیمات خوانده می‌شود تا با رفتن به صفحه دیگر و برگشتن حفظ شود
    val amountVisible = settings.amountsVisible
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    // فیلتر فهرست «تراکنش‌های اخیر» با زدن چیپ واریز/برداشت روی کارت‌ها
    // ۰ = همه، ۱ = فقط واریزها، ۲ = فقط برداشت‌ها
    var recentFilter by remember { mutableStateOf(0) }
    var pendingDelete by remember { mutableStateOf<ir.kharjyar.app.data.db.TransactionEntity?>(null) }

    /** حذف تراکنش از فهرست «اخیر» با امکان بازگرداندن. */
    fun deleteWithUndo(tx: ir.kharjyar.app.data.db.TransactionEntity) {
        scope.launch {
            viewModel.repo.txDao.delete(tx.id)
            val res = snackbar.showSnackbar(
                message = "تراکنش حذف شد",
                actionLabel = "بازگرداندن",
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) viewModel.repo.txDao.restore(listOf(tx))
        }
    }

    pendingDelete?.let { tx ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("حذف تراکنش") }, text = { Text("آیا از حذف این تراکنش مطمئن هستید؟") }, confirmButton = { TextButton({ pendingDelete = null; deleteWithUndo(tx) }) { Text("حذف", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("انصراف") } })
    }

    Scaffold(
        containerColor = Color.Transparent,
        // پیام‌های کوتاه (مثل «شماره کارت کپی شد») بالاتر از دکمه گرد ثبت
        // تراکنش می‌نشینند تا پشت آن پنهان نشوند.
        snackbarHost = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                GlassSnackbarHost(snackbar)
            }
        },
        modifier = Modifier.imePadding(),
        // نوار بالا/پایین سیستم یک‌بار در AppRoot اعمال شده؛ تکرارش اینجا باعث
        // حاشیه مرده در بالای صفحه و بالای دکمه‌های پایین می‌شد.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        // انیمیشن ورود فقط برای نخستین نمایش صفحه؛ ردیف‌هایی که حین اسکرول
        // ساخته می‌شوند بدون تأخیر ظاهر می‌شوند.
        ScreenEnterAnimation {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // کلیک روی هر جای صفحه به‌جز خود ردیف‌ها و چیپ‌ها (که کلیک را
                // مصرف می‌کنند)، فیلتر واریز/برداشت را برمی‌دارد
                .pointerInput(recentFilter) {
                    if (recentFilter != 0) {
                        detectTapGestures { recentFilter = 0 }
                    }
                },
            // بدون فاصله مرده: کارت اصلی درست زیر نوار بالایی و فهرست تا خط نوار پایین
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- کیف پول: خلاصه ماه و کارت‌های بانکی در یک نوار ----------
            // قبلاً «کارت خلاصه» و «ردیف حساب‌ها» دو بخش جدا و زیر هم بودند و صفحه
            // را شلوغ می‌کردند. حالا یک نوار افقی است: صفحه نخست خلاصه همه حساب‌ها،
            // و بعد از آن هر حساب یک کارت با خلاصه واریز/برداشت خودش.
            item {
                val order=settings.dashboardAccountOrder
                val active = accounts.filter { !it.archived }.sortedBy { a -> order.indexOf(a.id).let { if(it<0) Int.MAX_VALUE else it } }
                val monthRange = remember { viewModel.repo.currentPersianMonthRange() }
                val wholeRange = summary.range == AppViewModel.SummaryRange.ALL
                // خلاصه هر حساب (یا همه حساب‌ها) با همان بازه‌ای که کاربر انتخاب کرده
                fun rangeSummary(accountId: Long?) =
                    if (wholeRange) TxSummarizer.summarize(allTx, accountId)
                    else TxSummarizer.summarize(allTx, accountId, monthRange.first, monthRange.second)

                // چرخ‌فلک کارت‌ها: با هر کشیدن انگشت دقیقاً یک کارت وسط صفحه می‌ایستد
                // (پهنای هر صفحه = عرض فهرست منهای دو لبه، و چسبیدن با snap).
                val rowState = rememberLazyListState()
                val snapFling = rememberSnapFlingBehavior(lazyListState = rowState)
                // سرعت پرتاب محدود می‌شود تا حتی با کشیدن محکم، دو یا سه کارت
                // یک‌جا رد نشود و کنترل همیشه نزدیک به یک کارت بماند.
                val controlledFling = remember(snapFling) {
                    object : androidx.compose.foundation.gestures.FlingBehavior {
                        override suspend fun androidx.compose.foundation.gestures.ScrollScope.performFling(initialVelocity: Float): Float =
                            with(snapFling) { performFling(initialVelocity.coerceIn(-1100f, 1100f)) }
                    }
                }
                val peek = 22.dp
                val pageWidth = (LocalConfiguration.current.screenWidthDp.dp - 32.dp - peek * 2)
                    .coerceAtLeast(180.dp)
                // وضعیت جابه‌جایی تا زمان رهاکردن ثابت می‌ماند؛ ترتیب فقط در پایان ذخیره می‌شود.
                var draggingId by remember { mutableStateOf<Long?>(null) }
                var dragOriginIndex by remember { mutableStateOf(-1) }
                var dragTargetIndex by remember { mutableStateOf(-1) }
                var floatingX by remember { mutableStateOf(0f) }
                var floatingY by remember { mutableStateOf(0f) }
                var floatingOriginX by remember { mutableStateOf(0f) }
                var floatingOriginY by remember { mutableStateOf(0f) }

                EnterCard(0) {
                    LazyRow(
                        state = rowState,
                        flingBehavior = controlledFling,
                        contentPadding = PaddingValues(horizontal = peek),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ----- صفحه نخست: خلاصه همه حساب‌ها -----
                        item {
                            val total = rangeSummary(null)
                            val totalRial = active.sumOf { acc -> AccountBalance.estimate(acc, allTx).rial ?: 0L }
                            HeroCard(
                                modifier = Modifier
                                    .width(pageWidth)
                                    .clickable { scope.launch { viewModel.settingsRepo.setDefaultAccount(null) } },
                                neon = settings.cardShine
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // عنوان، خودش کلید تغییر بازه است: «این ماه ⇄ همه»
                                        Row(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.toggleSummaryRange() }
                                                .padding(end = 6.dp, top = 2.dp, bottom = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            EmbossedText(
                                                "خلاصه ${summary.monthTitle}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = skin.onHero,
                                                maxLines = 1
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.SwapHoriz,
                                                contentDescription = "تغییر بازه",
                                                tint = skin.onHero.copy(alpha = 0.85f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        // نشان «همه حساب‌ها»: با زدنش فیلتر حساب برداشته می‌شود
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(heroChipBg(skin))
                                                .border(1.dp, skin.onHero.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (defaultAccount == null) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = skin.onHero,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(
                                                "همه حساب‌ها",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = skin.onHero,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // دکمه چشم برای پنهان/نمایش مبلغ
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(heroChipBg(skin))
                                                .border(1.dp, skin.onHero.copy(alpha = 0.28f), CircleShape)
                                                .clickable { scope.launch { viewModel.settingsRepo.setAmountsVisible(!amountVisible) } },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (amountVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (amountVisible) "پنهان کردن مبلغ" else "نمایش مبلغ",
                                                tint = skin.onHero,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            EmbossedText(
                                                if (amountVisible) Money.format(total.netRial, settings.moneyUnit) else "••••••••",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = skin.onHero,
                                                maxLines = 1,
                                                depth = 1.25f
                                            )
                                            Text(
                                                if (summary.range == AppViewModel.SummaryRange.MONTH) "خالص این ماه" else "خالص همه تراکنش‌ها",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = skin.onHero.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        SummaryChip(
                                            label = "واریز",
                                            value = if (amountVisible) Money.format(total.incomeRial, settings.moneyUnit) else "••••",
                                            tint = skin.incomeColor,
                                            deposit = true,
                                            selected = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryChip(
                                            label = "برداشت",
                                            value = if (amountVisible) Money.format(total.expenseRial, settings.moneyUnit) else "••••",
                                            tint = skin.expenseColor,
                                            deposit = false,
                                            selected = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // مجموع مانده حساب‌ها، همان چیزی که پیش‌تر کارت جدا داشت
                                    if (active.isNotEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            "مجموع مانده برآوردی ${Digits.toPersian(active.size.toString())} حساب: " +
                                                (if (amountVisible) Money.format(totalRial, settings.moneyUnit) else "••••••"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.9f),
                                            maxLines = 1
                                        )
                                    }
                                    if (summary.hasDataOutsideRange) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "در این ماه تراکنشی نیست؛ برای دیدن همه، روی عنوان بزنید.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.85f)
                                        )
                                    }
                                    if (total.pendingCount > 0) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "به‌جز ${Digits.toPersian(total.pendingCount.toString())} مورد تأییدنشده (در جمع بالا حساب نشده)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        // ----- صفحه‌های بعدی: هر حساب یک کارت، با خلاصه خودش -----
                        items(active.size, key = { active[it].id }) { idx ->
                            val account = active[idx]
                            val isDragging = draggingId == account.id
                            val displacement = when {
                                draggingId == null || idx == dragOriginIndex -> 0f
                                dragTargetIndex < dragOriginIndex && idx in dragTargetIndex until dragOriginIndex -> -1f
                                dragTargetIndex > dragOriginIndex && idx in (dragOriginIndex + 1)..dragTargetIndex -> 1f
                                else -> 0f
                            }
                            val neighborOffset by animateFloatAsState(
                                targetValue = displacement,
                                animationSpec = tween(durationMillis = 480),
                                label = "neighborCardShift"
                            )
                            val est = AccountBalance.estimate(account, allTx)
                            val accSum = rangeSummary(account.id)
                            val bankSnapshot = bankBalances.firstOrNull { it.accountId == account.id }
                            val hasDiscrepancy = est.rial != null && bankSnapshot != null && est.rial != bankSnapshot.balanceRial
                            // رنگ متن روی کارت، مثل خود BankCard از روشنایی رنگ حساب می‌آید
                            val onCard = if (Color(account.colorArgb).luminance() > 0.55f) Color(0xFF14121A) else Color.White
                            // مختصات هر کارت مستقل نگه داشته می‌شود؛ یک مختصات مشترک بین
                            // کارت‌ها باعث می‌شد کارت شناور هنگام شروع در محل کارت دیگری ظاهر شود.
                            var cardWindowPosition by remember(account.id) { mutableStateOf(Offset.Zero) }
                            BankCard(
                                title = account.title,
                                bankName = account.bankName,
                                colorArgb = account.colorArgb,
                                balanceText = when {
                                    !amountVisible -> "••••••"
                                    est.rial != null -> Money.format(est.rial, settings.moneyUnit)
                                    else -> "—"
                                },
                                balanceHint = "مانده برآوردی",
                                selected = defaultAccount?.id == account.id,
                                cardNumber = account.cardNumber,
                                accountNumber = account.accountNumber,
                                iban = account.iban,
                                expiry = account.cardExpiry,
                                cvv2 = account.cardCvv2,
                                showSecrets = amountVisible,
                                balanceColor = if (hasDiscrepancy) skin.expenseColor else null,
                                modifier = Modifier
                                    .width(pageWidth)
                                    .animateItemPlacement(animationSpec = tween(durationMillis = 480))
                                    .onGloballyPositioned { coordinates ->
                                        if (!isDragging) cardWindowPosition = coordinates.positionInWindow()
                                    }
                                    .graphicsLayer {
                                        translationX = neighborOffset * (size.width + 10.dp.toPx())
                                        alpha = if (isDragging) 0f else 1f
                                    }
                                    .pointerInput(account.id, active.map { it.id }) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                floatingOriginX = cardWindowPosition.x
                                                floatingOriginY = cardWindowPosition.y
                                                draggingId = account.id
                                                dragOriginIndex = idx
                                                dragTargetIndex = idx
                                                floatingX = 0f
                                                floatingY = 0f
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                floatingX += amount.x
                                                floatingY += amount.y
                                                val threshold = size.width * 0.34f
                                                val step = size.width + 10.dp.toPx()
                                                val moved = if (kotlin.math.abs(floatingX) < threshold) 0
                                                    else ((kotlin.math.abs(floatingX) - threshold) / step).toInt() + 1
                                                dragTargetIndex = if (floatingX > 0)
                                                    (dragOriginIndex - moved).coerceAtLeast(0)
                                                else
                                                    (dragOriginIndex + moved).coerceAtMost(active.lastIndex)
                                            },
                                            onDragEnd = {
                                                if (dragOriginIndex >= 0 && dragTargetIndex >= 0 && dragOriginIndex != dragTargetIndex) {
                                                    val ids = active.map { it.id }.toMutableList()
                                                    ids.removeAt(dragOriginIndex)
                                                    ids.add(dragTargetIndex, account.id)
                                                    scope.launch { viewModel.settingsRepo.setDashboardAccountOrder(ids) }
                                                }
                                                draggingId = null; floatingX = 0f; floatingY = 0f
                                            },
                                            onDragCancel = { draggingId = null; floatingX = 0f; floatingY = 0f }
                                        )
                                    },
                                onClick = {
                                    // انتخاب کارت = تغییر حساب پیش‌فرض داشبورد
                                    // و برداشتن فیلتر واریز/برداشت
                                    recentFilter = 0
                                    scope.launch {
                                        viewModel.settingsRepo.setDefaultAccount(
                                            if (defaultAccount?.id == account.id) null else account.id
                                        )
                                    }
                                },
                                onCopy = { label, text ->
                                    clipboard.setText(AnnotatedString(text))
                                    scope.launch {
                                        // پیام کوتاه تأیید؛ خودش بعد از چند ثانیه محو می‌شود
                                        snackbar.currentSnackbarData?.dismiss()
                                        snackbar.showSnackbar(
                                            message = "$label کپی شد",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ) {
                                // خلاصه همین حساب، روی خود کارت (ادغام کارت خلاصه و کارت بانکی)
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryChip(
                                        label = "واریز",
                                        value = if (amountVisible) Money.format(accSum.incomeRial, settings.moneyUnit) else "••••",
                                        tint = skin.incomeColor,
                                        deposit = true,
                                        onColor = onCard,
                                        selected = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryChip(
                                        label = "برداشت",
                                        value = if (amountVisible) Money.format(accSum.expenseRial, settings.moneyUnit) else "••••",
                                        tint = skin.expenseColor,
                                        deposit = false,
                                        onColor = onCard,
                                        selected = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                bankSnapshot?.let { bankBalance ->
                                    val estimated = est.rial
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "مانده آخرین پیامک بانک: " + if (amountVisible) Money.format(bankBalance.balanceRial, settings.moneyUnit) else "••••••",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onCard.copy(alpha = 0.92f)
                                    )
                                    if (estimated != null && estimated != bankBalance.balanceRial) {
                                        Text(
                                            "مغایرت ${Money.format(kotlin.math.abs(estimated - bankBalance.balanceRial), settings.moneyUnit)} — یافتن تراکنش ثبت‌نشده",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = skin.expenseColor,
                                            modifier = Modifier.clickable { nav.navigate("reviewImport") }
                                        )
                                    }
                                }
                            }
                            if (isDragging) {
                                Popup(
                                    // offset در overload مبتنی بر Alignment داخل RTL دوباره آینه می‌شود.
                                    // PositionProvider مختصات فیزیکی پنجره را مستقیماً برمی‌گرداند تا
                                    // حرکت چپ/راست کارت دقیقاً هم‌جهت حرکت انگشت باقی بماند.
                                    popupPositionProvider = object : PopupPositionProvider {
                                        override fun calculatePosition(
                                            anchorBounds: androidx.compose.ui.unit.IntRect,
                                            windowSize: IntSize,
                                            layoutDirection: LayoutDirection,
                                            popupContentSize: IntSize
                                        ): IntOffset = IntOffset(
                                            (floatingOriginX + floatingX).roundToInt(),
                                            (floatingOriginY + floatingY).roundToInt()
                                        )
                                    },
                                    properties = PopupProperties(focusable = false, clippingEnabled = false)
                                ) {
                                    BankCard(
                                        account = account,
                                        balanceText = when {
                                            !amountVisible -> "••••••"
                                            est.rial != null -> Money.format(est.rial, settings.moneyUnit)
                                            else -> "—"
                                        },
                                        balanceCaption = "مانده برآوردی",
                                        selected = defaultAccount?.id == account.id,
                                        masked = !amountVisible,
                                        modifier = Modifier.width(pageWidth).graphicsLayer {
                                            // اندازه در شروع Drag تغییر نمی‌کند تا نقطه‌ای که کاربر
                                            // گرفته دقیقاً زیر همان نقطه انگشت باقی بماند.
                                            scaleX = 1f
                                            scaleY = 1f
                                            // شکل سایه دقیقاً با گوشه‌های گرد کارت یکی است؛ رنگ کم‌غلظت
                                            // و ارتفاع بیشتر، لبه خطی را به هاله نرم تبدیل می‌کند.
                                            shape = RoundedCornerShape(22.dp)
                                            clip = false
                                            shadowElevation = 34.dp.toPx()
                                            ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                                            spotShadowColor = Color.Black.copy(alpha = 0.28f)
                                        }
                                    )
                                }
                            }
                        }

                        // ----- آخرین صفحه: افزودن حساب -----
                        item {
                            SkinCard(
                                modifier = Modifier
                                    .width(if (active.isEmpty()) pageWidth else pageWidth * 0.55f)
                                    .clickable { nav.navigate("accountEdit/0") }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(skin.accent.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = skin.accent)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        if (active.isEmpty()) "هنوز حسابی معرفی نکرده‌اید — افزودن حساب" else "افزودن حساب",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = skin.onBackdrop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------- میان‌بر «ثبت سریع» ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(skin.accent.copy(alpha = 0.22f), skin.accent.copy(alpha = 0.06f))
                            )
                        )
                        .border(1.dp, skin.accent.copy(alpha = 0.38f), RoundedCornerShape(30.dp))
                        .clickable { nav.navigate("quickAdd") }
                        .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(skin.accent.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            tint = skin.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "ثبت سریع",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = skin.onBackdrop,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = skin.accent.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ---------- نیازمند بررسی ----------
            if (reviewCount > 0) {
                item {
                    EnterCard(1) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("review") }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.RateReview, null, tint = skin.expenseColor)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${Digits.toPersian(reviewCount.toString())} مورد نیازمند بررسی",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = skin.onBackdrop
                                )
                            }
                        }
                    }
                }
            }

            // ---------- تراکنش‌های اخیر ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        when (recentFilter) {
                            1 -> "واریزهای اخیر"
                            2 -> "برداشت‌های اخیر"
                            else -> "تراکنش‌های اخیر"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = skin.onBackdrop
                    )
                    // با فیلتر فعال، راه برگشت به همه تراکنش‌ها
                    if (recentFilter != 0) {
                        Text(
                            "نمایش همه",
                            style = MaterialTheme.typography.labelLarge,
                            color = skin.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { recentFilter = 0 }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            val shownRecent = when (recentFilter) {
                1 -> recent.filter { it.direction == ir.kharjyar.app.data.db.TxDirection.DEPOSIT }
                2 -> recent.filter { it.direction == ir.kharjyar.app.data.db.TxDirection.WITHDRAW }
                else -> recent
            }
            if (shownRecent.isEmpty()) {
                item {
                    EmptyState(
                        when (recentFilter) {
                            1 -> "واریزی ثبت نشده"
                            2 -> "برداشتی ثبت نشده"
                            else -> "تراکنشی ثبت نشده"
                        },
                        if (recentFilter == 0) "از دکمه «ثبت تراکنش» شروع کنید یا منتظر پیامک بانکی بمانید"
                        else "برای دیدن بقیه تراکنش‌ها «نمایش همه» را بزنید"
                    )
                }
            } else {
                items(shownRecent.size) { idx ->
                    val tx = shownRecent[idx]
                    EnterCard(4 + idx) {
                        SwipeActionRow(
                            onDelete = { pendingDelete = tx },
                            onEdit = { nav.navigate("tx/${tx.id}") }
                        ) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("tx/${tx.id}") }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DirectionBadge(tx.direction, tx.nature)
                                        if (tx.status == TxStatus.PENDING) {
                                            Text(
                                                "در انتظار تأیید",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    Text(
                                        tx.description.ifBlank { tx.counterparty.ifBlank { "بدون توضیح" } },
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        color = skin.onBackdrop
                                    )
                                    Text(
                                        PersianDate.formatDateTime(tx.occurredAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = skin.onBackdrop.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    Money.format(tx.amountRial, settings.moneyUnit),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = skin.onBackdrop
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
        }
    }
}

/**
 * پس‌زمینه چیپ‌های روی کارت شاخص.
 * روی تم‌های تیره یک لایه مشکی و روی تم روشن یک لایه سفید می‌نشیند تا
 * متن در هر دو حالت کنتراست کافی داشته باشد.
 */
private fun heroChipBg(skin: ir.kharjyar.app.ui.theme.AppSkin): Color =
    if (skin.dark) Color.Black.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.72f)

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    tint: Color,
    deposit: Boolean,
    modifier: Modifier = Modifier,
    /** رنگ متن وقتی چیپ روی کارت بانکی (با رنگ خود حساب) می‌نشیند. */
    onColor: Color? = null,
    /** با زدن چیپ، فهرست پایین صفحه فیلتر می‌شود. */
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(16.dp)
    val fg = onColor ?: skin.onHero
    // روی متن روشن، پس‌زمینه تیره و برعکس؛ تا چیپ روی هر رنگ کارتی خوانا بماند
    val chipBg = if (onColor == null) heroChipBg(skin)
        else if (fg.luminance() > 0.5f) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.72f)
    Row(
        modifier = modifier
            .clip(shape)
            // پس‌زمینه کنتراست‌دار نسبت به کارت: روی تم روشن، روشن؛ روی تم تیره، تیره
            .background(if (selected) tint.copy(alpha = 0.22f) else chipBg)
            // حاشیه نازک هم‌رنگ مقدار؛ در حالت انتخاب‌شده پررنگ‌تر
            .border(if (selected) 2.dp else 1.dp, tint.copy(alpha = if (selected) 0.95f else 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // آیکون فلش داخل دایره هم‌رنگ، مطابق طرح کارت‌ها
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.20f))
                .border(1.dp, tint.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (deposit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = fg.copy(alpha = 0.92f),
                maxLines = 1
            )
            EmbossedText(
                value,
                style = MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.Start),
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
                depth = 0.8f
            )
        }
    }
}



/** سری روزانه درآمد/هزینه برای n روز اخیر (بر اساس روز شمسی/منطقه زمانی تهران). */
internal fun buildDailySeries(
    txs: List<ir.kharjyar.app.data.db.TransactionEntity>,
    days: Int
): Pair<List<Long>, List<Long>> {
    val today = PersianDate.today()
    val income = MutableList(days) { 0L }
    val expense = MutableList(days) { 0L }
    val dayStartList = (0 until days).map { today.plusDays(-(days - 1 - it)) }
    val starts = dayStartList.map { it.startOfDayMillis() }
    val ends = dayStartList.map { it.endOfDayMillisExclusive() }
    for (tx in txs) {
        if (tx.status != TxStatus.CONFIRMED) continue
        if (tx.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER) continue
        for (i in 0 until days) {
            if (tx.occurredAt >= starts[i] && tx.occurredAt < ends[i]) {
                val isIncome = tx.nature == ir.kharjyar.app.data.db.TxNature.INCOME ||
                    (tx.nature == ir.kharjyar.app.data.db.TxNature.UNKNOWN && tx.direction == ir.kharjyar.app.data.db.TxDirection.DEPOSIT)
                if (isIncome) income[i] += tx.amountRial else expense[i] += tx.amountRial
                break
            }
        }
    }
    return income to expense
}
