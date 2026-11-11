package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.work.ObligationTrash
import kotlinx.coroutines.launch

private data class ObligationTrashItem(
    val type: String,
    val id: Long,
    val title: String,
    val deletedAt: Long,
    val check: CheckEntity? = null,
    val debt: DebtEntity? = null
)

/** سطل زباله uniesified: تراکنش‌ها و تعهدات (شخص، طلب/بدهی، وام، چک) در یک صفحه. */
@Composable
fun TrashScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val db = viewModel.repo.db
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activePeople by viewModel.debtPeople.collectAsState()

    val txTrash by viewModel.repo.txDao.observeTrash().collectAsState(initial = emptyList())
    val people by db.debtDao().observeTrashPeople().collectAsState(initial = emptyList())
    val debts by db.debtDao().observeTrashDebts().collectAsState(initial = emptyList())
    val loans by db.loanDao().observeTrash().collectAsState(initial = emptyList())
    val checks by db.checkDao().observeTrash().collectAsState(initial = emptyList())

    val obligationItems = remember(people, debts, loans, checks, activePeople) {
        (people.map { ObligationTrashItem("شخص", it.id, it.name, it.deletedAt!!) } +
            debts.map {
                ObligationTrashItem(
                    if (it.kind == DebtKind.RECEIVABLE) "طلب" else "بدهی",
                    it.id,
                    "${it.title.ifBlank { "بدون عنوان" }} — ${activePeople.firstOrNull { it.personId == it.id }?.name ?: "شخص نامشخص"}",
                    it.deletedAt!!,
                    debt = it
                )
            } +
            loans.map { ObligationTrashItem("وام", it.id, it.title, it.deletedAt!!) } +
            checks.map { ObligationTrashItem("چک", it.id, it.counterparty, it.deletedAt!!, check = it) })
            .sortedByDescending { it.deletedAt }
    }

    LaunchedEffect(Unit) { ObligationTrash.purgeExpired(context, db) }

    var tab by remember { mutableStateOf(0) } // 0 = تراکنش‌ها، 1 = تعهدات
    var permanentDeleteTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmEmptyTx by remember { mutableStateOf(false) }
    var deletingObligation by remember { mutableStateOf<ObligationTrashItem?>(null) }

    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("بازگشت") }
            Text(
                "سطل زباله",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        TabRow(selectedTabIndex = tab, modifier = Modifier.fillMaxWidth()) {
            Tab(
                text = { Text("تراکنش‌ها") },
                selected = tab == 0,
                onClick = { tab = 0 }
            )
            Tab(
                text = { Text("تعهدات") },
                selected = tab == 1,
                onClick = { tab = 1 }
            )
        }

        if (tab == 0) {
            Text(
                "تراکنش‌های حذف‌شده تا ۳۰ روز قابل بازگرداندن هستند و سپس خودکار برای همیشه پاک می‌شوند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (txTrash.isNotEmpty()) {
                OutlinedButton(
                    onClick = { confirmEmptyTx = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DeleteForever, null)
                    Spacer(Modifier.width(8.dp))
                    Text("خالی کردن سطل زباله")
                }
            }
            if (txTrash.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Text("سطل زباله تراکنش‌ها خالی است", Modifier.padding(28.dp).align(Alignment.CenterHorizontally))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    items(txTrash, key = { it.id }) { tx ->
                        val remaining = ((tx.deletedAt!! + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        tx.description.ifBlank { if (tx.direction == TxDirection.DEPOSIT) "واریز" else "برداشت" },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        Money.format(tx.amountRial, settings.moneyUnit),
                                        color = if (tx.direction == TxDirection.DEPOSIT) Color(0xFF15965A) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    "${accounts.firstOrNull { it.id == tx.accountId }?.title ?: "حساب حذف‌شده"} • ${PersianDate.formatDateTime(tx.occurredAt)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${Digits.toPersian(remaining.toString())} روز تا حذف خودکار",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                tx.transferGroupId?.let { viewModel.repo.txDao.restoreTransferFromTrash(it) }
                                                    ?: viewModel.repo.txDao.restoreFromTrash(tx.id)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Restore, null)
                                        Spacer(Modifier.width(5.dp))
                                        Text("بازگرداندن")
                                    }
                                    OutlinedButton(
                                        onClick = { permanentDeleteTx = tx },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("حذف همیشگی") }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                "وام‌ها، چک‌ها و طلب و بدهی‌ها تا ۳۰ روز قابل بازگرداندن‌اند؛ سپس هنگام اجرای برنامه یا کار دوره‌ای به‌طور خودکار پاک می‌شوند. تراکنش‌های مالي حساب‌ها حفظ می‌شوند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (obligationItems.isEmpty()) {
                Text("سطل زباله تعهدات خالی است.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    items(obligationItems, key = { "${it.type}-${it.id}" }) { item ->
                        val days = ((item.deletedAt + ObligationTrash.RETENTION_MS - System.currentTimeMillis() + 86_399_999L) / 86_400_000L).coerceAtLeast(0)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${item.type}: ${item.title}", fontWeight = FontWeight.Bold)
                                Text(
                                    "حذف‌شده در ${PersianDate.formatDateTime(item.deletedAt)} • ${Digits.toPersian(days.toString())} روز تا حذف خودکار",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (item.type == "شخص") {
                                    Text(
                                        "بازگردانی شخص، طلب‌ها و بدهی‌های فعال او را نیز بازمی‌گرداند؛ مواردی که جداگانه حذف شده‌اند در سطل می‌مانند.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                when (item.type) {
                                                    "شخص" -> db.debtDao().restorePerson(item.id)
                                                    "طلب", "بدهی" -> db.debtDao().restoreDebt(item.id)
                                                    "وام" -> db.loanDao().restore(item.id)
                                                    "چک" -> item.check?.let { ObligationTrash.restoreCheck(context, db, it) }
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("بازگرداندن") }
                                    OutlinedButton(
                                        onClick = { deletingObligation = item },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("حذف همیشگی") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // —— تراکنش: حذف همیشگی ——
    permanentDeleteTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { permanentDeleteTx = null },
            title = { Text("حذف همیشگی تراکنش") },
            text = { Text("این تراکنش دیگر قابل بازگرداندن نخواهد بود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        permanentDeleteTx = null
                        scope.launch {
                            tx.transferGroupId?.let { viewModel.repo.txDao.permanentlyDeleteTransfer(it) }
                                ?: viewModel.repo.txDao.permanentlyDelete(tx.id)
                            viewModel.repo.txDao.purgeOrphanAttachments()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("حذف همیشگی", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { permanentDeleteTx = null }) { Text("انصراف") } }
        )
    }

    // —— تراکنش: خالی کردن سطل ——
    if (confirmEmptyTx) {
        AlertDialog(
            onDismissRequest = { confirmEmptyTx = false },
            title = { Text("خالی کردن سطل زباله") },
            text = { Text("همه تراکنش‌های این بخش برای همیشه حذف می‌شوند و قابل بازگرداندن نیستند.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmEmptyTx = false
                        scope.launch {
                            viewModel.repo.txDao.emptyTrash()
                            viewModel.repo.txDao.purgeOrphanAttachments()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("حذف همه", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmEmptyTx = false }) { Text("انصراف") } }
        )
    }

    // —— تعهدات: حذف همیشگی ——
    deletingObligation?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingObligation = null },
            title = { Text("حذف همیشگی ${item.type}؟") },
            text = {
                Text(
                    "«${item.title}» و سوابق وابسته آن ${if (item.type == "شخص") "از جمله همه طلب‌ها و بدهی‌های آن شخص، " else ""}برای همیشه حذف می‌شوند. تراکنش‌های حساب حذف نمی‌شوند."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingObligation = null
                        scope.launch {
                            when (item.type) {
                                "شخص" -> db.debtDao().deletePersonWithDebts(item.id)
                                "طلب", "بدهی" -> item.debt?.let { db.debtDao().permanentlyDeleteDebt(it) }
                                "وام" -> db.loanDao().permanentlyDelete(item.id)
                                "چک" -> item.check?.let { ObligationTrash.permanentlyDeleteCheck(context, db, it) }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("حذف همیشگی", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingObligation = null }) { Text("انصراف") } }
        )
    }
}
