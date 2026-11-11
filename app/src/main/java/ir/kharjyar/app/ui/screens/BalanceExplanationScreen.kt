package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel

@Composable
fun BalanceExplanationScreen(vm: AppViewModel, nav: NavHostController, accountId: Long) {
    val accounts by vm.accounts.collectAsState()
    val transactions by vm.allTransactions.collectAsState()
    val settings by vm.settings.collectAsState()
    val account = accounts.firstOrNull { it.id == accountId }
    if (account == null) { Text("حساب پیدا نشد", Modifier.padding(16.dp)); return }
    val estimate = AccountBalance.estimate(account, transactions)
    val active = transactions.filter { it.accountId == accountId && it.deletedAt == null }
    val included = active.filter { it.status == TxStatus.CONFIRMED &&
        (account.programBalanceOffsetRial != null || account.initialBalanceRial == null || it.occurredAt >= (account.initialBalanceAt ?: Long.MIN_VALUE)) }
        .sortedWith(compareBy({ it.occurredAt }, { it.id }))
    val base = account.programBalanceOffsetRial ?: account.initialBalanceRial ?: 0L
    fun display(value: Long) = if (settings.amountsVisible) Money.format(value, settings.moneyUnit) else "••••••"
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("محاسبه مانده ${account.title}", style = MaterialTheme.typography.headlineSmall)
            Text("مبنای محاسباتی: ${if (estimate.rial == null) "نامشخص" else display(base)}", style = MaterialTheme.typography.titleMedium)
            Text("این مبنا ممکن است برای حفظ مانده قبلی تعدیل شده باشد و لزوماً همان موجودی اولیه یا آخرین مانده بانک نیست.", style = MaterialTheme.typography.bodySmall)
            Text("مانده برآوردی: ${estimate.rial?.let { display(it) } ?: "نامشخص"}", style = MaterialTheme.typography.titleMedium)
            Text("${included.size} تراکنش قطعی در محاسبه؛ ${estimate.pendingCount} پیش‌نویس تأییدنشده کنار گذاشته شده است.", style = MaterialTheme.typography.bodySmall)
        }
        items(included, key = { it.id }) { tx ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(tx.description.ifBlank { if (tx.direction == TxDirection.DEPOSIT) "واریز" else "برداشت" })
                        Text(PersianDate.formatDateTime(tx.occurredAt), style = MaterialTheme.typography.bodySmall)
                        Text("${if (tx.direction == TxDirection.DEPOSIT) "+" else "−"} ${display(tx.amountRial)}")
                    }
                    TextButton(onClick = { nav.navigate("tx/${tx.id}") }) { Text("جزئیات") }
                }
            }
        }
    }
}
