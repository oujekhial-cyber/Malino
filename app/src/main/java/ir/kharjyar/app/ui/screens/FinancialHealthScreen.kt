package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

/** Possible duplicates are suggestions, never proof or automatic deletions. */
internal fun potentialDuplicatePairs(transactions: List<TransactionEntity>): List<Pair<TransactionEntity, TransactionEntity>> =
    transactions.filter { it.deletedAt == null && it.status == TxStatus.CONFIRMED }
        .groupBy { Triple(it.accountId, it.direction, it.amountRial) }.values.flatMap { group ->
            group.sortedBy { it.occurredAt }.zipWithNext().filter { (a, b) -> b.occurredAt - a.occurredAt <= 120_000L }
        }

@Composable
fun FinancialHealthScreen(vm: AppViewModel, nav: NavHostController) {
    val accounts by vm.accounts.collectAsState()
    val txs by vm.allTransactions.collectAsState()
    val snapshots by vm.bankBalances.collectAsState()
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var reviewing by remember { mutableStateOf<AccountEntity?>(null) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val active = accounts.filterNot { it.archived }
    val incomplete = txs.filter { it.status == TxStatus.CONFIRMED && it.nature == TxNature.TRANSFER && it.transferGroupId != null }
        .groupBy { it.transferGroupId }.values.filter { it.size == 1 }.map { it.single() }
    val duplicates = remember(txs) { potentialDuplicatePairs(txs) }
    fun display(n: Long) = if (settings.amountsVisible) Money.format(n, settings.moneyUnit) else "••••••"
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("سلامت داده‌ها", style = MaterialTheme.typography.headlineSmall)
        Text("موارد پیشنهادی برای بررسی‌اند؛ هیچ موردی خودکار حذف یا اصلاح نمی‌شود.", style = MaterialTheme.typography.bodySmall)
        SkinCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("مبنای مانده پیامکی نیازمند بررسی", style = MaterialTheme.typography.titleMedium)
            val pending = active.filter { it.anchorReviewBalanceRial != null }
            if (pending.isEmpty()) Text("موردی در انتظار تأیید نیست.")
            pending.forEach { account ->
                Text("${account.title}: ${display(account.anchorReviewBalanceRial!!)} • منبع: اولین پیامک بانکی" + (account.anchorReviewAt?.let { " • ${PersianDate.formatDateTime(it)}" } ?: ""))
                TextButton(onClick = { reviewing = account; input = ""; error = null }) { Text("تأیید یا اصلاح مبنا") }
            }
        } }
        SkinCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("مغایرت حساب‌ها", style = MaterialTheme.typography.titleMedium)
            val differences = active.mapNotNull { account ->
                val snapshot = snapshots.firstOrNull { it.accountId == account.id } ?: return@mapNotNull null
                val value = AccountBalance.estimate(account, txs).rial ?: return@mapNotNull null
                if (value == snapshot.balanceRial) null else Triple(account, value, snapshot)
            }
            if (differences.isEmpty()) Text("مغایرت قابل مقایسه‌ای پیدا نشد؛ حساب‌های بدون مانده پیامکی قابل مقایسه نیستند.")
            differences.forEach { (account, value, snapshot) ->
                Text("${account.title}: برنامه ${display(value)}، پیامک ${display(snapshot.balanceRial)} • ${PersianDate.formatDateTime(snapshot.messageAt)}")
                TextButton(onClick = { nav.navigate("reviewImport?accountId=${account.id}") }) { Text("بررسی پیامک‌ها") }
            }
        } }
        SkinCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("انتقال‌های یک‌طرفه احتمالی: ${incomplete.size}", style = MaterialTheme.typography.titleMedium)
            incomplete.take(30).forEach { tx -> TextButton(onClick = { nav.navigate("tx/${tx.id}") }) { Text("${accounts.firstOrNull { it.id == tx.accountId }?.title ?: "حساب"} • ${display(tx.amountRial)} • بررسی") } }
        } }
        SkinCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("تراکنش‌های احتمالی تکراری: ${duplicates.size}", style = MaterialTheme.typography.titleMedium)
            Text("مبلغ و جهت یکسان در یک حساب و فاصله حداکثر دو دقیقه؛ ممکن است هر دو واقعی باشند.", style = MaterialTheme.typography.bodySmall)
            duplicates.take(30).forEach { (a,b) ->
                Text("${accounts.firstOrNull { it.id == a.accountId }?.title ?: "حساب"} • ${display(a.amountRial)} • ${PersianDate.formatDateTime(a.occurredAt)}")
                Row { TextButton(onClick = { nav.navigate("tx/${a.id}") }) { Text("مورد اول") }; TextButton(onClick = { nav.navigate("tx/${b.id}") }) { Text("مورد دوم") } }
            }
        } }
        Spacer(Modifier.height(24.dp))
    }
    reviewing?.let { account ->
        AlertDialog(onDismissRequest = { reviewing = null }, title = { Text("تأیید مبنای پیامکی") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("مبنای ثبت‌شده از اولین پیامک: ${display(account.anchorReviewBalanceRial ?: 0)}. تراکنش‌های بعدی حفظ می‌شوند. برای اصلاح، مانده صحیح در زمان همان پیامک را وارد کنید.")
                AmountTextField(value = input, onValueChange = { input = it; error = null }, label = "مانده صحیح در زمان پیامک", unit = settings.moneyUnit, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            } },
            confirmButton = { TextButton(onClick = { scope.launch {
                val original = account.anchorReviewBalanceRial ?: return@launch
                val value = if (input.isBlank()) original else runCatching { Money.inputToRial(input, settings.moneyUnit) }.getOrNull()
                if (value == null || value < 0 || !vm.repo.reviewSmsBalanceAnchor(account.id, value)) error = "مبلغ معتبر وارد کنید یا دوباره تلاش کنید."
                else reviewing = null
            } }) { Text(if (input.isBlank()) "تأیید مبنا" else "اصلاح و تأیید") } },
            dismissButton = { TextButton(onClick = { reviewing = null }) { Text("بعداً") } })
    }
}
