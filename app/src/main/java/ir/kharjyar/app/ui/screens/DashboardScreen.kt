package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.balance.BalanceSource
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.EnterCard
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
                    SkinCard(modifier = Modifier.fillMaxWidth(), tonal = true) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "خلاصه ${summary.monthTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = skin.onHero
                                )
                                Text(
                                    defaultAccount?.title ?: "همه حساب‌ها",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = skin.onHero.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .clickable { nav.navigate("settings") }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                Money.format(summary.incomeRial - summary.expenseRial, settings.moneyUnit),
                                style = MaterialTheme.typography.headlineMedium,
                                color = skin.onHero
                            )
                            Text(
                                "خالص این ماه",
                                style = MaterialTheme.typography.labelMedium,
                                color = skin.onHero.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SummaryChip(
                                    "درآمد",
                                    Money.format(summary.incomeRial, settings.moneyUnit),
                                    skin.incomeColor,
                                    Modifier.weight(1f)
                                )
                                SummaryChip(
                                    "هزینه",
                                    Money.format(summary.expenseRial, settings.moneyUnit),
                                    skin.expenseColor,
                                    Modifier.weight(1f)
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
                                items(active.size) { idx ->
                                    val account = active[idx]
                                    val est = AccountBalance.estimate(account, allTx)
                                    SkinCard(
                                        modifier = Modifier
                                            .width(230.dp)
                                            .clickable { nav.navigate("accountEdit/${account.id}") }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .background(Color(account.colorArgb), CircleShape)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    account.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    maxLines = 1,
                                                    color = skin.onBackdrop
                                                )
                                            }
                                            Text(
                                                account.bankName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = skin.onBackdrop.copy(alpha = 0.7f)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "مانده برآوردی",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = skin.onBackdrop.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                if (est.rial != null) Money.format(est.rial, settings.moneyUnit) else "—",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = skin.bigNumberColor,
                                                maxLines = 1
                                            )
                                            Text(
                                                est.sourceLabel(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = skin.onBackdrop.copy(alpha = 0.65f),
                                                maxLines = 2
                                            )
                                            est.asOf?.let {
                                                Text(
                                                    "تا ${PersianDate.formatDateTime(it)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = skin.onBackdrop.copy(alpha = 0.65f)
                                                )
                                            }
                                            if (est.source != BalanceSource.NONE && est.pendingCount > 0) {
                                                Text(
                                                    "${Digits.toPersian(est.pendingCount.toString())} مورد تأییدنشده محاسبه نشده",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = skin.expenseColor
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
private fun SummaryChip(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    val skin = LocalAppSkin.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (skin.dark) 0.12f else 0.30f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(tint, CircleShape))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = skin.onHero.copy(alpha = 0.85f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = skin.onHero, textAlign = TextAlign.Start)
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
