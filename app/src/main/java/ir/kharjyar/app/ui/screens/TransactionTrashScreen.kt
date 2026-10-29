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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun TransactionTrashScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val trash by viewModel.repo.txDao.observeTrash().collectAsState(initial = emptyList())
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var permanentDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmEmpty by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("بازگشت") }
            Text("سطل زباله تراکنش‌ها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Text("تراکنش‌های حذف‌شده تا ۳۰ روز قابل بازگرداندن هستند و سپس خودکار برای همیشه پاک می‌شوند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (trash.isNotEmpty()) {
            OutlinedButton(onClick = { confirmEmpty = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.DeleteForever, null); Spacer(Modifier.width(8.dp)); Text("خالی کردن سطل زباله")
            }
        }
        if (trash.isEmpty()) {
            Card(Modifier.fillMaxWidth()) { Text("سطل زباله خالی است", Modifier.padding(28.dp).align(Alignment.CenterHorizontally)) }
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
            items(trash, key = { it.id }) { tx ->
                val remaining = ((tx.deletedAt!! + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tx.description.ifBlank { if (tx.direction == TxDirection.DEPOSIT) "واریز" else "برداشت" }, fontWeight = FontWeight.Bold)
                            Text(Money.format(tx.amountRial, settings.moneyUnit), color = if (tx.direction == TxDirection.DEPOSIT) Color(0xFF15965A) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        Text("${accounts.firstOrNull { it.id == tx.accountId }?.title ?: "حساب حذف‌شده"} • ${PersianDate.formatDateTime(tx.occurredAt)}", style = MaterialTheme.typography.bodySmall)
                        Text("${Digits.toPersian(remaining.toString())} روز تا حذف خودکار", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { scope.launch { tx.transferGroupId?.let { viewModel.repo.txDao.restoreTransferFromTrash(it) } ?: viewModel.repo.txDao.restoreFromTrash(tx.id) } }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Restore, null); Spacer(Modifier.width(5.dp)); Text("بازگرداندن")
                            }
                            OutlinedButton(onClick = { permanentDelete = tx }, modifier = Modifier.weight(1f)) { Text("حذف همیشگی") }
                        }
                    }
                }
            }
        }
    }

    permanentDelete?.let { tx -> AlertDialog(onDismissRequest = { permanentDelete = null }, title = { Text("حذف همیشگی تراکنش") }, text = { Text("این تراکنش دیگر قابل بازگرداندن نخواهد بود. ادامه می‌دهید؟") }, confirmButton = { TextButton(onClick = { permanentDelete = null; scope.launch { tx.transferGroupId?.let { viewModel.repo.txDao.permanentlyDeleteTransfer(it) } ?: viewModel.repo.txDao.permanentlyDelete(tx.id); viewModel.repo.txDao.purgeOrphanAttachments() } }) { Text("حذف همیشگی", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { permanentDelete = null }) { Text("انصراف") } }) }
    if (confirmEmpty) AlertDialog(onDismissRequest = { confirmEmpty = false }, title = { Text("خالی کردن سطل زباله") }, text = { Text("همه تراکنش‌های این بخش برای همیشه حذف می‌شوند و قابل بازگرداندن نیستند.") }, confirmButton = { TextButton(onClick = { confirmEmpty = false; scope.launch { viewModel.repo.txDao.emptyTrash(); viewModel.repo.txDao.purgeOrphanAttachments() } }) { Text("حذف همه", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirmEmpty = false }) { Text("انصراف") } })
}
