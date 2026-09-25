package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
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
import ir.kharjyar.app.ui.components.LineChart
import androidx.compose.material3.SnackbarResult
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.SkinCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.text.style.TextOverflow
import ir.kharjyar.app.ui.components.EmbossedText
import ir.kharjyar.app.ui.theme.LocalAppSkin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, nav: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recent by viewModel.scopedRecent.collectAsState()
    val reviewCount by viewModel.reviewCount.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val scopedTx by viewModel.scopedTransactions.collectAsState()
    val defaultAccount by viewModel.defaultAccount.collectAsState()
    val skin = LocalAppSkin.current
    // از تنظیمات خوانده می‌شود تا با رفتن به صفحه دیگر و برگشتن حفظ شود
    val amountVisible = settings.amountsVisible
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

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

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { GlassSnackbarHost(snackbar) },
        modifier = Modifier.imePadding()
    ) { padding ->
        // انیمیشن ورود فقط برای نخستین نمایش صفحه؛ ردیف‌هایی که حین اسکرول
        // ساخته می‌شوند بدون تأخیر ظاهر می‌شوند.
        ScreenEnterAnimation {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            // بدون فاصله مرده: کارت اصلی درست زیر نوار بالایی و فهرست تا خط نوار پایین
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- کیف پول: خلاصه ماه و کارت‌های بانکی در یک نوار ----------
            // قبلاً «کارت خلاصه» و «ردیف حساب‌ها» دو بخش جدا و زیر هم بودند و صفحه
            // را شلوغ می‌کردند. حالا یک نوار افقی است: صفحه نخست خلاصه همه حساب‌ها،
            // و بعد از آن هر حساب یک کارت با خلاصه واریز/برداشت خودش.
            item {
                val active = accounts.filter { !it.archived }
                val monthRange = remember { viewModel.repo.currentPersianMonthRange() }
                val wholeRange = summary.range == AppViewModel.SummaryRange.ALL
                // خلاصه هر حساب (یا همه حساب‌ها) با همان بازه‌ای که کاربر انتخاب کرده
                fun rangeSummary(accountId: Long?) =
                    if (wholeRange) TxSummarizer.summarize(allTx, accountId)
                    else TxSummarizer.summarize(allTx, accountId, monthRange.first, monthRange.second)

                // چرخ‌فلک کارت‌ها: با هر کشیدن انگشت دقیقاً یک کارت وسط صفحه می‌ایستد
                // (پهنای هر صفحه = عرض فهرست منهای دو لبه، و چسبیدن با snap).
                val rowState = rememberLazyListState()
                val peek = 22.dp
                val pageWidth = (LocalConfiguration.current.screenWidthDp.dp - 32.dp - peek * 2)
                    .coerceAtLeast(180.dp)

                EnterCard(0) {
                    LazyRow(
                        state = rowState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = rowState),
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
                                            modifier = Modifier.weight(1f)
                                        )
                                        SummaryChip(
                                            label = "برداشت",
                                            value = if (amountVisible) Money.format(total.expenseRial, settings.moneyUnit) else "••••",
                                            tint = skin.expenseColor,
                                            deposit = false,
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
                        items(active.size) { idx ->
                            val account = active[idx]
                            val est = AccountBalance.estimate(account, allTx)
                            val accSum = rangeSummary(account.id)
                            // رنگ متن روی کارت، مثل خود BankCard از روشنایی رنگ حساب می‌آید
                            val onCard = if (Color(account.colorArgb).luminance() > 0.55f) Color(0xFF14121A) else Color.White
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
                                modifier = Modifier.width(pageWidth),
                                onClick = {
                                    // انتخاب کارت = تغییر حساب پیش‌فرض داشبورد
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
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryChip(
                                        label = "برداشت",
                                        value = if (amountVisible) Money.format(accSum.expenseRial, settings.moneyUnit) else "••••",
                                        tint = skin.expenseColor,
                                        deposit = false,
                                        onColor = onCard,
                                        modifier = Modifier.weight(1f)
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

            // ---------- میان‌بر «بگو تا بنویسم» ----------
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
                        "بگو تا بنویسم",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = skin.onBackdrop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "«۲۵۰ هزار تومن نان»",
                        style = MaterialTheme.typography.bodySmall,
                        color = skin.onBackdrop.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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

            // ---------- نمودار ۳۰ روز اخیر ----------
            item {
                EnterCard(3) {
                    SkinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text("روند ۳۰ روز اخیر", style = MaterialTheme.typography.titleSmall, color = skin.onBackdrop)
                            Spacer(Modifier.height(6.dp))
                            val (income, expense) = buildDailySeries(scopedTx, 30)
                            if (income.all { it == 0L } && expense.all { it == 0L }) {
                                EmptyState("داده‌ای برای نمودار نیست", "با ثبت اولین تراکنش، نمودار اینجا شکل می‌گیرد")
                            } else {
                                // ارتفاع نصف شد تا کارت نمودار جای کمتری بگیرد
                                LineChart(incomeSeries = income, expenseSeries = expense, height = 78.dp)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    LegendDot(skin.incomeColor, "واریز")
                                    LegendDot(skin.expenseColor, "برداشت")
                                }
                            }
                        }
                    }
                }
            }

            // ---------- تراکنش‌های اخیر ----------
            item {
                Text("تراکنش‌های اخیر", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)
            }
            if (recent.isEmpty()) {
                item { EmptyState("تراکنشی ثبت نشده", "از دکمه «ثبت تراکنش» شروع کنید یا منتظر پیامک بانکی بمانید") }
            } else {
                items(recent.size) { idx ->
                    val tx = recent[idx]
                    EnterCard(4 + idx) {
                        SwipeActionRow(
                            onDelete = { deleteWithUndo(tx) },
                            onEdit = { nav.navigate("tx/${tx.id}") }
                        ) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("tx/${tx.id}") }) {
                            Row(
                                modifier = Modifier.padding(14.dp),
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
    onColor: Color? = null
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
            .background(chipBg)
            // حاشیه نازک هم‌رنگ مقدار، تا چیپ از پس‌زمینه جدا شود
            .border(1.dp, tint.copy(alpha = 0.55f), shape)
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

@Composable
private fun LegendDot(color: Color, label: String) {
    val skin = LocalAppSkin.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = skin.onBackdrop)
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
