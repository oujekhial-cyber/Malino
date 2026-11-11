package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.AppViewModel

private data class SearchResult(val key: String, val title: String, val detail: String, val route: String)

@Composable
fun GlobalSearchScreen(vm: AppViewModel, nav: NavHostController) {
    val accounts by vm.accounts.collectAsState()
    val transactions by vm.allTransactions.collectAsState()
    val people by vm.debtPeople.collectAsState()
    val debts by vm.debts.collectAsState()
    val checks by vm.checks.collectAsState()
    val notes by vm.notes.collectAsState()
    val settings by vm.settings.collectAsState()
    var query by remember { mutableStateOf("") }
    val term = Digits.normalizeForMatch(query).trim()
    fun matches(vararg values: String) = term.isNotEmpty() && values.any { Digits.normalizeForMatch(it).contains(term, ignoreCase = true) }
    fun money(value: Long) = if (settings.amountsVisible) Money.format(value, settings.moneyUnit) else "••••••"
    val results = if (term.isBlank()) emptyList() else buildList {
        accounts.filter { matches(it.title, it.bankName, it.maskedNumber, it.accountNumber, it.cardNumber, it.iban) }.forEach {
            add(SearchResult("a${it.id}", "حساب • ${it.title}", it.bankName, "accountEdit/${it.id}"))
        }
        transactions.filter { matches(it.description, it.counterparty, it.refNumber, it.amountRial.toString()) }.forEach {
            add(SearchResult("t${it.id}", "تراکنش • ${it.description.ifBlank { "بدون توضیح" }}", "${money(it.amountRial)} • ${PersianDate.formatDateTime(it.occurredAt)}", "tx/${it.id}"))
        }
        people.filter { matches(it.name, it.phone, it.note) }.forEach { person ->
            val debt = debts.firstOrNull { it.personId == person.id }
            add(SearchResult("p${person.id}", "شخص • ${person.name}", if (debt == null) "مشاهده فهرست اشخاص" else "مشاهده پرونده", debt?.let { "debts/${it.id}" } ?: "debts"))
        }
        checks.filter { matches(it.counterparty, it.sayadId, it.serialNumber, it.note, it.bankName, it.amountRial.toString()) }.forEach {
            add(SearchResult("c${it.id}", "چک • ${it.counterparty}", "${money(it.amountRial)} • ${PersianDate.formatDateTime(it.dueAt)}", "checks/${it.id}"))
        }
        notes.filter { matches(it.title, it.body) }.forEach {
            add(SearchResult("n${it.id}", "یادداشت • ${it.title}", it.body.take(100), "searchNote/${it.id}"))
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("جست‌وجوی سراسری", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(query, { query = it }, label = { Text("نام، توضیح، شماره یا مبلغ ریالی") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text(if (term.isBlank()) "عبارتی وارد کنید." else "${results.size} نتیجه", style = MaterialTheme.typography.bodySmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results, key = { it.key }) { result -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(result.title, style = MaterialTheme.typography.titleSmall); Text(result.detail, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { nav.navigate(result.route) }) { Text("مشاهده") }
            } } }
        }
    }
}

@Composable
fun SearchNoteScreen(vm: AppViewModel, nav: NavHostController, id: Long) {
    val notes by vm.notes.collectAsState()
    val note = notes.firstOrNull { it.id == id }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (note == null) Text("یادداشت پیدا نشد.") else { Text(note.title, style = MaterialTheme.typography.headlineSmall); Text(note.body) }
        TextButton(onClick = { nav.navigate("notes") }) { Text("مدیریت یادداشت‌ها") }
    }
}
