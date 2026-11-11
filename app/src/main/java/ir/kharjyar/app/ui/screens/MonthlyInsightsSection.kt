package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.report.monthSummary
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

@Composable
fun MonthlyInsightsSection(vm: AppViewModel, transactions: List<TransactionEntity>, categories: List<CategoryEntity>) {
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val today = PersianDate.today()
    val current = remember(transactions, today.year, today.month) { monthSummary(transactions, today) }
    val previous = remember(transactions, today.year, today.month) { monthSummary(transactions, today.plusMonths(-1)) }
    var selected by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val active = categories.filterNot { it.archived }
    val changes = (current.categories.keys + previous.categories.keys).map { id ->
        id to ((current.categories[id] ?: 0L) - (previous.categories[id] ?: 0L))
    }.filter { it.second != 0L }.sortedByDescending { kotlin.math.abs(it.second) }.take(3)
    fun display(value: Long) = if (settings.amountsVisible) Money.format(value, settings.moneyUnit) else "••••••"
    SkinCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("مقایسه ماه جاری با ماه قبل", style = MaterialTheme.typography.titleMedium)
            Text("ماه جاری ممکن است هنوز کامل نشده باشد؛ همه حساب‌ها، فقط تراکنش‌های قطعی.", style = MaterialTheme.typography.bodySmall)
            Text("درآمد: ${display(current.income)} | ماه قبل: ${display(previous.income)}")
            Text("هزینه: ${display(current.expense)} | ماه قبل: ${display(previous.expense)}")
            if (changes.isEmpty()) Text("تغییر دسته‌ای ثبت نشده است.", style = MaterialTheme.typography.bodySmall)
            changes.forEach { (id, difference) ->
                val name = categories.firstOrNull { it.id == id }?.name ?: "بدون دسته"
                Text("$name: ${if (difference > 0) "افزایش" else "کاهش"} ${display(kotlin.math.abs(difference))}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    SkinCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("بودجه ماهانه دسته‌ها", style = MaterialTheme.typography.titleMedium)
            Text("سقف‌ها هر ماه تکرار می‌شوند؛ فقط هزینه‌های قطعی همه حساب‌ها محاسبه می‌شوند.", style = MaterialTheme.typography.bodySmall)
            settings.categoryBudgets.forEach { (id, limit) ->
                val name = categories.firstOrNull { it.id == id }?.name ?: return@forEach
                val spent = current.categories[id] ?: 0L
                val state = when { spent >= limit -> "از سقف عبور کرده یا به سقف رسیده"; spent.toDouble() / limit >= 0.8 -> "نزدیک سقف"; else -> "در محدوده" }
                Text("$name: ${display(spent)} از ${display(limit)} — $state", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { scope.launch { vm.settingsRepo.setCategoryBudget(id, null) } }) { Text("حذف بودجه $name") }
            }
            if (active.isNotEmpty()) {
                ComboBox(label = "دسته", options = active, selected = active.firstOrNull { it.id == selected }, onSelect = { selected = it.id }, labelOf = { it.name })
                AmountTextField(value = amount, onValueChange = { amount = it; error = null }, label = "سقف ماهانه", unit = settings.moneyUnit, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = {
                    val id = selected
                    val value = runCatching { Money.inputToRial(amount, settings.moneyUnit) }.getOrNull()
                    if (id == null || value == null || value <= 0) error = "دسته و مبلغ معتبر را وارد کنید."
                    else scope.launch { vm.settingsRepo.setCategoryBudget(id, value); amount = ""; error = null }
                }) { Text("ذخیره سقف") }
            }
        }
    }
}
