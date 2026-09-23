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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.balance.BalanceSource
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.BankCard
import ir.kharjyar.app.ui.components.EnterCard
import ir.kharjyar.app.ui.components.HeroCard
import ir.kharjyar.app.ui.components.LineChart
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.theme.LocalAppSkin

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

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.imePadding()
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- کارت خلاصه ماه (hero) ----------
            item {
                EnterCard(0) {
                    HeroCard(
                        modifier = Modifier.fillMaxWidth(),
                        neon = settings.cardShine
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "خلاصه ${summary.monthTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    defaultAccount?.title ?: "همه حساب‌ها",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.34f))
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.28f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { nav.navigate("settings") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // دکمه چشم برای پنهان/نمایش مبلغ
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.34f))
                                        .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                        .clickable { scope.launch { viewModel.settingsRepo.setAmountsVisible(!amountVisible) } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (amountVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (amountVisible) "پنهان کردن مبلغ" else "نمایش مبلغ",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (amountVisible)
                                            Money.format(summary.incomeRial - summary.expenseRial, settings.moneyUnit)
                                        else "••••••••",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "خالص این ماه",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SummaryChip(
                                    label = "درآمد",
                                    value = if (amountVisible) Money.format(summary.incomeRial, settings.moneyUnit) else "••••",
                                    tint = skin.incomeColor,
                                    deposit = true,
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryChip(
                                    label = "هزینه",
                                    value = if (amountVisible) Money.format(summary.expenseRial, settings.moneyUnit) else "••••",
                                    tint = skin.expenseColor,
                                    deposit = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (summary.pendingCount > 0) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "به‌جز ${Digits.toPersian(summary.pendingCount.toString())} مورد تأییدنشده (در جمع بالا حساب نشده)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = skin.onHero.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
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

            // ---------- حساب‌ها با مانده برآوردی ----------
            item {
                EnterCard(2) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حساب‌ها", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)
                            Text(
                                "مدیریت",
                                style = MaterialTheme.typography.labelLarge,
                                color = skin.accent,
                                modifier = Modifier.clickable { nav.navigate("accounts") }.padding(8.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val active = accounts.filter { !it.archived }
                        if (active.isEmpty()) {
                            SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("accountEdit/0") }) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccountBalance, null, tint = skin.accent)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "هنوز حسابی معرفی نکرده‌اید — افزودن حساب",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = skin.onBackdrop
                                    )
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // کارت «همه حساب‌ها»: مجموع همه حساب‌ها
                                item {
                                    val totalRial = active.sumOf { acc ->
                                        AccountBalance.estimate(acc, allTx).rial ?: 0L
                                    }
                                    BankCard(
                                        title = "همه حساب‌ها",
                                        bankName = "${Digits.toPersian(active.size.toString())} حساب فعال",
                                        colorArgb = skin.accent.toArgb().toLong() and 0xFFFFFFFFL,
                                        balanceText = if (amountVisible)
                                            Money.format(totalRial, settings.moneyUnit) else "••••••",
                                        balanceHint = "مجموع مانده برآوردی",
                                        selected = defaultAccount == null,
                                        modifier = Modifier.width(250.dp),
                                        onClick = {
                                            scope.launch { viewModel.settingsRepo.setDefaultAccount(null) }
                                        }
                                    )
                                }
                                items(active.size) { idx ->
                                    val account = active[idx]
                                    val est = AccountBalance.estimate(account, allTx)
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
                                        modifier = Modifier.width(250.dp),
                                        onClick = {
                                            // انتخاب کارت = تغییر حساب پیش‌فرض داشبورد
                                            scope.launch {
                                                viewModel.settingsRepo.setDefaultAccount(
                                                    if (defaultAccount?.id == account.id) null else account.id
                                                )
                                            }
                                        },
                                        onCopy = { text ->
                                            clipboard.setText(AnnotatedString(text))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------- نمودار ۳۰ روز اخیر ----------
            item {
                EnterCard(3) {
                    SkinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("روند ۳۰ روز اخیر", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)
                            Spacer(Modifier.height(12.dp))
                            val (income, expense) = buildDailySeries(scopedTx, 30)
                            if (income.all { it == 0L } && expense.all { it == 0L }) {
                                EmptyState("داده‌ای برای نمودار نیست", "با ثبت اولین تراکنش، نمودار اینجا شکل می‌گیرد")
                            } else {
                                LineChart(incomeSeries = income, expenseSeries = expense)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    LegendDot(skin.incomeColor, "درآمد")
                                    LegendDot(skin.expenseColor, "هزینه")
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
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    tint: Color,
    deposit: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(shape)
            // پس‌زمینه تیره مات به‌جای سفیدِ نیمه‌شفاف: روی کارت روشن هم خوانا می‌ماند
            .background(Color.Black.copy(alpha = 0.34f))
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
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                textAlign = TextAlign.Start,
                maxLines = 1
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
