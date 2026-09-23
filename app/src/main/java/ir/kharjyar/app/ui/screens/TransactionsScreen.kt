package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val all by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val skin = LocalAppSkin.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var query by remember { mutableStateOf("") }
    var filterAccount by remember { mutableStateOf<Long?>(null) }
    var filterNature by remember { mutableStateOf<Int?>(null) }
    var onlyPending by remember { mutableStateOf(false) }

    // انتخاب چندتایی: با نگه‌داشتن روی یک ردیف فعال می‌شود
    val selected = remember { mutableStateListOf<Long>() }
    val selecting = selected.isNotEmpty()

    val filtered = all.filter { tx ->
        (query.isBlank() ||
            tx.description.contains(query) || tx.counterparty.contains(query) ||
            Digits.normalize(query).let { q ->
                q.isNotBlank() && tx.amountRial.toString().contains(q.filter(Char::isDigit))
            }) &&
            (filterAccount == null || tx.accountId == filterAccount) &&
            (filterNature == null || tx.nature == filterNature) &&
            (!onlyPending || tx.status == TxStatus.PENDING)
    }

    /** حذف با امکان بازگرداندن تا ۴ ثانیه. */
    fun deleteWithUndo(items: List<TransactionEntity>) {
        if (items.isEmpty()) return
        scope.launch {
            viewModel.repo.txDao.deleteAll(items.map { it.id })
            val label = if (items.size == 1) "تراکنش حذف شد"
            else "${Digits.toPersian(items.size.toString())} تراکنش حذف شد"
            val result = snackbar.showSnackbar(
                message = label,
                actionLabel = "بازگرداندن",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.repo.txDao.restore(items)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            // ---------- نوار انتخاب چندتایی ----------
            if (selecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(skin.accent.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "لغو انتخاب",
                        tint = skin.onBackdrop,
                        modifier = Modifier.size(20.dp).clickable { selected.clear() }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${Digits.toPersian(selected.size.toString())} مورد انتخاب شد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = skin.onBackdrop,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        val ids = filtered.map { it.id }
                        selected.clear()
                        selected.addAll(ids)
                    }) { Text("همه") }
                    TextButton(onClick = {
                        val items = all.filter { it.id in selected }
                        selected.clear()
                        deleteWithUndo(items)
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = skin.expenseColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("حذف", color = skin.expenseColor)
                    }
                }
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    placeholder = { Text("جست‌وجو در توضیح، طرف مقابل یا مبلغ") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = onlyPending,
                        onClick = { onlyPending = !onlyPending },
                        label = { Text("تأییدنشده") })
                }
                item {
                    FilterChip(
                        selected = filterNature == TxNature.EXPENSE,
                        onClick = { filterNature = if (filterNature == TxNature.EXPENSE) null else TxNature.EXPENSE },
                        label = { Text("هزینه") })
                }
                item {
                    FilterChip(
                        selected = filterNature == TxNature.INCOME,
                        onClick = { filterNature = if (filterNature == TxNature.INCOME) null else TxNature.INCOME },
                        label = { Text("درآمد") })
                }
                item {
                    FilterChip(
                        selected = filterNature == TxNature.TRANSFER,
                        onClick = { filterNature = if (filterNature == TxNature.TRANSFER) null else TxNature.TRANSFER },
                        label = { Text("انتقال") })
                }
                items(accounts.size) { i ->
                    val a = accounts[i]
                    FilterChip(
                        selected = filterAccount == a.id,
                        onClick = { filterAccount = if (filterAccount == a.id) null else a.id },
                        label = { Text(a.title) })
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    title = if (all.isEmpty()) "هنوز تراکنشی ندارید" else "چیزی پیدا نشد",
                    subtitle = if (all.isEmpty()) "تراکنش دستی ثبت کنید یا منتظر پیامک بانکی بمانید"
                    else "فیلترها را تغییر دهید"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, bottom = 110.dp)
                ) {
                    items(
                        count = filtered.size,
                        key = { idx -> filtered[idx].id }
                    ) { i ->
                        val tx = filtered[i]
                        TransactionRow(
                            tx = tx,
                            categoryName = categories.firstOrNull { it.id == tx.categoryId }?.name,
                            unit = settings.moneyUnit,
                            selecting = selecting,
                            checked = tx.id in selected,
                            onToggle = {
                                if (tx.id in selected) selected.remove(tx.id) else selected.add(tx.id)
                            },
                            onOpen = { nav.navigate("tx/${tx.id}") },
                            onSwipeDelete = { deleteWithUndo(listOf(tx)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * یک ردیف تراکنش.
 * کشیدن انگشت آن را حذف می‌کند و نگه‌داشتن، حالت انتخاب چندتایی را باز می‌کند.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    categoryName: String?,
    unit: ir.kharjyar.app.core.money.MoneyUnit,
    selecting: Boolean,
    checked: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val skin = LocalAppSkin.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onSwipeDelete()
                true
            } else false
        },
        // فاصله لازم برای حذف: کمی بیش از یک‌سوم عرض تا تصادفی حذف نشود
        positionalThreshold = { total -> total * 0.38f }
    )

    SwipeToDismissBox(
        state = dismissState,
        // در حالت انتخاب چندتایی، کشیدن غیرفعال است تا با انتخاب تداخل نکند
        gesturesEnabled = !selecting,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(skin.cardCorner))
                    .background(skin.expenseColor.copy(alpha = 0.22f))
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = skin.expenseColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("حذف", color = skin.expenseColor, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) {
        SkinCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { if (selecting) onToggle() else onOpen() },
                    onLongClick = onToggle
                )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selecting) {
                    Checkbox(checked = checked, onCheckedChange = { onToggle() })
                    Spacer(Modifier.width(4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DirectionBadge(tx.direction, tx.nature)
                        if (tx.status == TxStatus.PENDING) {
                            Text(
                                "در انتظار تأیید",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        categoryName?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Text(
                        tx.description.ifBlank { tx.counterparty.ifBlank { "بدون توضیح" } },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Text(
                        PersianDate.formatDateTime(tx.occurredAt) +
                            if (tx.timeIsApproximate) " (زمان دریافت)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    Money.format(tx.amountRial, unit),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
