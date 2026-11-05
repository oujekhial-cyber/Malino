package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.work.ObligationTrash
import kotlinx.coroutines.launch

private data class TrashItem(val type: String, val id: Long, val title: String, val deletedAt: Long, val check: CheckEntity? = null, val debt: DebtEntity? = null)

@Composable
fun ObligationTrashScreen(vm: AppViewModel, onBack: () -> Unit) {
    val db = vm.repo.db
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val people by db.debtDao().observeTrashPeople().collectAsState(initial = emptyList())
    val debts by db.debtDao().observeTrashDebts().collectAsState(initial = emptyList())
    val loans by db.loanDao().observeTrash().collectAsState(initial = emptyList())
    val checks by db.checkDao().observeTrash().collectAsState(initial = emptyList())
    val activePeople by vm.debtPeople.collectAsState()
    var deleting by remember { mutableStateOf<TrashItem?>(null) }
    val items = (people.map { TrashItem("شخص", it.id, it.name, it.deletedAt!!) } +
        debts.map { TrashItem(if (it.kind == DebtKind.RECEIVABLE) "طلب" else "بدهی", it.id, "${it.title.ifBlank { "بدون عنوان" }} — ${activePeople.firstOrNull { person -> person.id == it.personId }?.name ?: "شخص نامشخص"}", it.deletedAt!!, debt = it) } +
        loans.map { TrashItem("وام", it.id, it.title, it.deletedAt!!) } +
        checks.map { TrashItem("چک", it.id, it.counterparty, it.deletedAt!!, check = it) }).sortedByDescending { it.deletedAt }
    BackHandler(onBack = onBack)
    LaunchedEffect(Unit) { ObligationTrash.purgeExpired(context, db) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("بازگشت") }
            Text("سطل زباله تعهدات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("وام‌ها، چک‌ها و طلب و بدهی‌ها تا ۳۰ روز قابل بازگرداندن‌اند؛ سپس هنگام اجرای برنامه یا کار دوره‌ای به‌طور خودکار پاک می‌شوند. تراکنش‌های مالی حساب‌ها حفظ می‌شوند.", style = MaterialTheme.typography.bodySmall)
        if (items.isEmpty()) Text("سطل زباله خالی است.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
            items(items, key = { "${it.type}-${it.id}" }) { item ->
                val days = ((item.deletedAt + ObligationTrash.RETENTION_MS - System.currentTimeMillis() + 86_399_999L) / 86_400_000L).coerceAtLeast(0)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.type}: ${item.title}", fontWeight = FontWeight.Bold)
                        Text("حذف‌شده در ${PersianDate.formatDateTime(item.deletedAt)} • ${Digits.toPersian(days.toString())} روز تا حذف خودکار", style = MaterialTheme.typography.bodySmall)
                        if (item.type == "شخص") Text("بازگردانی شخص، طلب‌ها و بدهی‌های فعال او را نیز بازمی‌گرداند؛ مواردی که جداگانه حذف شده‌اند در سطل می‌مانند.", style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { scope.launch {
                                when (item.type) {
                                    "شخص" -> db.debtDao().restorePerson(item.id)
                                    "طلب", "بدهی" -> db.debtDao().restoreDebt(item.id)
                                    "وام" -> db.loanDao().restore(item.id)
                                    "چک" -> item.check?.let { ObligationTrash.restoreCheck(context, db, it) }
                                }
                            } }, modifier = Modifier.weight(1f)) { Text("بازگرداندن") }
                            OutlinedButton(onClick = { deleting = item }, modifier = Modifier.weight(1f)) { Text("حذف همیشگی") }
                        }
                    }
                }
            }
        }
    }
    deleting?.let { item ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("حذف همیشگی ${item.type}؟") },
            text = { Text("«${item.title}» و سوابق وابسته آن ${if (item.type == "شخص") "از جمله همه طلب‌ها و بدهی‌های آن شخص، " else ""}برای همیشه حذف می‌شوند. تراکنش‌های حساب حذف نمی‌شوند.") },
            confirmButton = { TextButton(onClick = { deleting = null; scope.launch {
                when (item.type) {
                    "شخص" -> db.debtDao().deletePersonWithDebts(item.id)
                    "طلب", "بدهی" -> item.debt?.let { db.debtDao().permanentlyDeleteDebt(it) }
                    "وام" -> db.loanDao().permanentlyDelete(item.id)
                    "چک" -> item.check?.let { ObligationTrash.permanentlyDeleteCheck(context, db, it) }
                }
            } }) { Text("حذف همیشگی", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("انصراف") } })
    }
}
