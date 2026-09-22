package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState

@Composable
fun TransactionsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val all by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var query by remember { mutableStateOf("") }
    var filterAccount by remember { mutableStateOf<Long?>(null) }
    var filterNature by remember { mutableStateOf<Int?>(null) }
    var onlyPending by remember { mutableStateOf(false) }

    val filtered = all.filter { tx ->
        (query.isBlank() ||
            tx.description.contains(query) || tx.counterparty.contains(query) ||
            Digits.normalize(query).let { q -> q.isNotBlank() && tx.amountRial.toString().contains(Digits.normalize(q).filter(Char::isDigit)) }) &&
            (filterAccount == null || tx.accountId == filterAccount) &&
            (filterNature == null || tx.nature == filterNature) &&
            (!onlyPending || tx.status == TxStatus.PENDING)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            placeholder = { Text("جست‌وجو در توضیح، طرف مقابل یا مبلغ") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = onlyPending, onClick = { onlyPending = !onlyPending }, label = { Text("تأییدنشده") })
            }
            item {
                FilterChip(
                    selected = filterNature == TxNature.EXPENSE,
                    onClick = { filterNature = if (filterNature == TxNature.EXPENSE) null else TxNature.EXPENSE },
                    label = { Text("هزینه") }
                )
            }
            item {
                FilterChip(
                    selected = filterNature == TxNature.INCOME,
                    onClick = { filterNature = if (filterNature == TxNature.INCOME) null else TxNature.INCOME },
                    label = { Text("درآمد") }
                )
            }
            item {
                FilterChip(
                    selected = filterNature == TxNature.TRANSFER,
                    onClick = { filterNature = if (filterNature == TxNature.TRANSFER) null else TxNature.TRANSFER },
                    label = { Text("انتقال") }
                )
            }
            items(accounts.size) { i ->
                val a = accounts[i]
                FilterChip(
                    selected = filterAccount == a.id,
                    onClick = { filterAccount = if (filterAccount == a.id) null else a.id },
                    label = { Text(a.title) }
                )
            }
        }
        if (filtered.isEmpty()) {
            EmptyState(
                title = if (all.isEmpty()) "هنوز تراکنشی ندارید" else "چیزی پیدا نشد",
                subtitle = if (all.isEmpty()) "تراکنش دستی ثبت کنید یا منتظر پیامک بانکی بمانید" else "فیلترها را تغییر دهید"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                items(filtered.size) { i ->
                    val tx = filtered[i]
                    val catName = categories.firstOrNull { it.id == tx.categoryId }?.name
                    SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("tx/${tx.id}") }) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    DirectionBadge(tx.direction, tx.nature)
                                    if (tx.status == TxStatus.PENDING) {
                                        Text("در انتظار تأیید", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    catName?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) }
                                }
                                Text(tx.description.ifBlank { tx.counterparty.ifBlank { "بدون توضیح" } }, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                Text(
                                    PersianDate.formatDateTime(tx.occurredAt) + if (tx.timeIsApproximate) " (زمان دریافت)" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(Money.format(tx.amountRial, settings.moneyUnit), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}
