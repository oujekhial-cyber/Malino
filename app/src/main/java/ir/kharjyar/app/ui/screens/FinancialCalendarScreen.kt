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
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.AppViewModel

private data class CalendarItem(val time: Long, val title: String, val detail: String, val route: String)

@Composable
fun FinancialCalendarScreen(vm: AppViewModel, nav: NavHostController) {
    val recurring by vm.repo.db.recurringTransactionDao().observeAll().collectAsState(initial = emptyList())
    val checks by vm.checks.collectAsState()
    val debts by vm.debts.collectAsState()
    val loans by vm.loans.collectAsState()
    val installments by vm.loanInstallments.collectAsState()
    val reminders by vm.repo.db.reminderDao().observeAll().collectAsState(initial = emptyList())
    val settings by vm.settings.collectAsState()
    var day by remember { mutableStateOf(PersianDate.today()) }
    val month = day.firstOfMonth()
    fun display(amount: Long) = if (settings.amountsVisible) Money.format(amount, settings.moneyUnit) else "••••••"
    val events = buildList {
        checks.forEach { add(CalendarItem(it.dueAt, "چک • ${it.counterparty}", display(it.amountRial), "checks/${it.id}")) }
        debts.forEach { debt -> debt.dueAt?.let { add(CalendarItem(it, "طلب/بدهی • ${debt.title}", if (debt.settled) "تسویه‌شده" else display(debt.amountRial), "debts/${debt.id}")) } }
        installments.filterNot { it.noInstallment }.forEach { installment ->
            val loan = loans.firstOrNull { it.id == installment.loanId } ?: return@forEach
            add(CalendarItem(installment.dueAt, "قسط ${installment.number} • ${loan.title}", if (installment.paid) "پرداخت‌شده" else display(installment.amountRial), "loans/${loan.id}/${installment.id}"))
        }
        recurring.filter { it.enabled }.forEach { add(CalendarItem(it.nextAt, "برنامه تراکنش • ${it.description.ifBlank { "ماهیانه" }}", "موعد بعدی • ${display(it.amountRial)}", "recurringTransactions")) }
        reminders.filter { it.enabled }.forEach { add(CalendarItem(it.nextAt, "یادآور • ${it.title}", it.category, "reminders/${it.id}")) }
    }
    val monthEvents = events.filter { PersianDate.fromMillis(it.time).let { p -> p.year == month.year && p.month == month.month } }
    val dayEvents = monthEvents.filter { PersianDate.fromMillis(it.time).day == day.day }.sortedBy { it.time }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("تقویم مالی", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { day = month.plusMonths(-1) }) { Text("ماه قبل") }
            Text("${month.monthName()} ${Digits.toPersian(month.year.toString())}", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { day = month.plusMonths(1) }) { Text("ماه بعد") }
        }
        Row(Modifier.fillMaxWidth()) { listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium) } }
        val offset = (month.toLocalDate().dayOfWeek.value + 1) % 7
        val cells = ((offset + month.monthLength() + 6) / 7) * 7
        for (week in 0 until cells / 7) {
            Row(Modifier.fillMaxWidth()) {
                for (column in 0..6) {
                    val n = week * 7 + column - offset + 1
                    if (n !in 1..month.monthLength()) Spacer(Modifier.weight(1f))
                    else TextButton(onClick = { day = PersianDate(month.year, month.month, n) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        val hasEvent = monthEvents.any { PersianDate.fromMillis(it.time).day == n }
                        Text((if (n == day.day) "[" else "") + Digits.toPersian(n.toString()) + (if (n == day.day) "]" else "") + if (hasEvent) "•" else "")
                    }
                }
            }
        }
        Text("${day.format()} • ${dayEvents.size} مورد", style = MaterialTheme.typography.titleMedium)
        Text("برای یادآورها و تراکنش‌های دوره‌ای فقط نوبت بعدی نمایش داده می‌شود.", style = MaterialTheme.typography.bodySmall)
        if (dayEvents.isEmpty()) Text("برای این روز موردی ثبت نشده است.")
        dayEvents.forEach { event -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall); Text(event.detail)
            TextButton(onClick = { nav.navigate(event.route) }) { Text("مشاهده جزئیات") }
        } } }
    }
}
