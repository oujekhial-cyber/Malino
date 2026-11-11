package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.db.RecurringTransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DateTimeField
import kotlinx.coroutines.launch

@Composable
fun RecurringTransactionsScreen(vm: AppViewModel, nav: NavHostController) {
    val schedules by vm.repo.db.recurringTransactionDao().observeAll().collectAsState(initial = emptyList())
    val occurrences by vm.repo.db.recurringTransactionDao().observeOccurrences().collectAsState(initial = emptyList())
    val accounts by vm.accounts.collectAsState()
    val categories by vm.categories.collectAsState()
    val transactions by vm.allTransactions.collectAsState()
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var accountId by remember { mutableStateOf<Long?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var direction by remember { mutableIntStateOf(TxDirection.WITHDRAW) }
    var date by remember { mutableStateOf(PersianDate.today()) }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val activeAccounts = accounts.filterNot { it.archived }
    val pendingIds = occurrences.map { it.transactionId }.toSet()
    val drafts = transactions.filter { it.id in pendingIds && it.status == TxStatus.PENDING }
    fun display(value: Long) = if (settings.amountsVisible) Money.format(value, settings.moneyUnit) else "••••••"
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("تراکنش‌های دوره‌ای", style = MaterialTheme.typography.headlineSmall)
        Text("هر ماه یک پیش‌نویس ساخته می‌شود؛ تا تأیید شما هیچ اثری بر موجودی ندارد. اجرا ممکن است با تأخیر انجام شود. انتخاب تاریخ گذشته یا فعال‌سازی دوباره برنامه متوقف‌شده می‌تواند چند پیش‌نویس برای موعدهای گذشته ایجاد کند.", style = MaterialTheme.typography.bodySmall)
        Text("پیش‌نویس‌های دوره‌ای: ${drafts.size}", style = MaterialTheme.typography.titleMedium)
        drafts.forEach { draft -> TextButton(onClick = { nav.navigate("tx/${draft.id}") }) {
            Text("${draft.description.ifBlank { "تراکنش دوره‌ای" }} • ${display(draft.amountRial)} • ${PersianDate.formatDateTime(draft.occurredAt)}")
        } }
        HorizontalDivider()
        Text("ساخت برنامه ماهانه", style = MaterialTheme.typography.titleMedium)
        AccountPicker(activeAccounts, accountId) { accountId = it }
        ComboBox(label = "نوع", options = listOf(TxDirection.WITHDRAW, TxDirection.DEPOSIT), selected = direction,
            onSelect = { direction = it }, labelOf = { if (it == TxDirection.DEPOSIT) "درآمد / واریز" else "هزینه / برداشت" })
        CategoryPicker(categories, categoryId, if (direction == TxDirection.DEPOSIT) TxNature.INCOME else TxNature.EXPENSE) { categoryId = it }
        AmountTextField(amount, { amount = it; error = null }, "مبلغ", unit = settings.moneyUnit, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("توضیح") }, modifier = Modifier.fillMaxWidth())
        Text("اولین موعد", style = MaterialTheme.typography.titleSmall)
        DateTimeField(date, hour, minute, { date = it }, { h,m -> hour=h; minute=m })
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val account = accountId
            val value = runCatching { Money.inputToRial(amount, settings.moneyUnit) }.getOrNull()
            if (account == null || activeAccounts.none { it.id == account } || value == null || value <= 0) error = "حساب و مبلغ معتبر انتخاب کنید."
            else scope.launch {
                val at = PersianDate.toMillis(date, hour, minute)
                vm.repo.db.recurringTransactionDao().insert(RecurringTransactionEntity(accountId = account, amountRial = value,
                    direction = direction, nature = if (direction == TxDirection.DEPOSIT) TxNature.INCOME else TxNature.EXPENSE,
                    categoryId = categoryId, description = description.trim(), nextAt = at, anchorDay = date.day))
                ir.kharjyar.app.core.recurring.createDueRecurringDrafts(vm.repo.db)
                amount = ""; description = ""; error = null
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("ذخیره برنامه") }
        HorizontalDivider()
        Text("برنامه‌های ثبت‌شده", style = MaterialTheme.typography.titleMedium)
        schedules.forEach { schedule -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
            Text(schedule.description.ifBlank { "تراکنش ماهانه" })
            Text("${accounts.firstOrNull { it.id == schedule.accountId }?.title ?: "حساب حذف‌شده"} • ${display(schedule.amountRial)} • موعد بعدی ${PersianDate.formatDateTime(schedule.nextAt)}")
            Row { Text(if (schedule.enabled) "فعال" else "متوقف"); Switch(checked = schedule.enabled, onCheckedChange = { enabled -> scope.launch {
                vm.repo.db.recurringTransactionDao().update(schedule.copy(enabled = enabled))
                if (enabled) ir.kharjyar.app.core.recurring.createDueRecurringDrafts(vm.repo.db)
            } }) }
        } } }
        Spacer(Modifier.height(24.dp))
    }
}
