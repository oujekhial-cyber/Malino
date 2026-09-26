package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.GlassSnackbarHost
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    viewModel: AppViewModel,
    nav: NavHostController,
    presetDirection: Int? = null
) {
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
    // میان‌بر چیپ کارت خانه، واریز/برداشت را بر اساس جهت بانکی فیلتر می‌کند؛
    // انتقال‌ها هم بسته به جهت خود در نتیجه باقی می‌مانند.
    var filterDirection by remember(presetDirection) { mutableStateOf(presetDirection) }
    var onlyPending by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

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
            (filterDirection == null || tx.direction == filterDirection) &&
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
        snackbarHost = { GlassSnackbarHost(snackbar) },
        // نوار بالا/پایین سیستم یک‌بار در AppRoot اعمال شده؛ اینجا نباید دوباره
        // فاصله اضافه شود وگرنه صفحه از بالا و پایین حاشیه مرده می‌گیرد.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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

            // به‌جای ردیف شلوغ چیپ‌ها، یک دکمه خلاصه پنجره فیلتر ساده را باز می‌کند.
            val activeFilterCount = listOf(
                filterDirection != null, filterNature != null, filterAccount != null, onlyPending
            ).count { it }
            androidx.compose.material3.OutlinedButton(
                onClick = { showFilters = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (activeFilterCount == 0) "فیلتر تراکنش‌ها" else "فیلتر تراکنش‌ها ($activeFilterCount فعال)")
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
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
                ) {
                    itemsIndexed(
                        items = filtered,
                        key = { _, item -> item.id }
                    ) { _, tx ->
                        TransactionRow(
                            tx = tx,
                            account = accounts.firstOrNull { it.id == tx.accountId },
                            categoryName = categories.firstOrNull { it.id == tx.categoryId }?.name,
                            unit = settings.moneyUnit,
                            selecting = selecting,
                            checked = tx.id in selected,
                            onToggle = {
                                if (tx.id in selected) selected.remove(tx.id) else selected.add(tx.id)
                            },
                            onOpen = { nav.navigate("tx/${tx.id}") },
                            onSwipeDelete = { deleteWithUndo(listOf(tx)) },
                            onSwipeEdit = { nav.navigate("tx/${tx.id}") }
                        )
                    }
                }
            }
        }
    }
    if (showFilters) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFilters = false },
            title = { Text("فیلتر تراکنش‌ها") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ir.kharjyar.app.ui.components.ComboBox(
                        label = "نوع گردش",
                        options = listOf(-1, TxDirection.DEPOSIT, TxDirection.WITHDRAW),
                        selected = filterDirection ?: -1,
                        labelOf = { when (it) { TxDirection.DEPOSIT -> "واریز"; TxDirection.WITHDRAW -> "برداشت"; else -> "همه" } },
                        onSelect = { filterDirection = if (it == -1) null else it }
                    )
                    ir.kharjyar.app.ui.components.ComboBox(
                        label = "ماهیت",
                        options = listOf(-1, TxNature.INCOME, TxNature.EXPENSE, TxNature.TRANSFER),
                        selected = filterNature ?: -1,
                        labelOf = { when (it) { TxNature.INCOME -> "درآمد"; TxNature.EXPENSE -> "هزینه"; TxNature.TRANSFER -> "انتقال"; else -> "همه" } },
                        onSelect = { filterNature = if (it == -1) null else it }
                    )
                    ir.kharjyar.app.ui.components.ComboBox(
                        label = "حساب",
                        options = listOf(0L) + accounts.map { it.id },
                        selected = filterAccount ?: 0L,
                        labelOf = { id -> if (id == 0L) "همه حساب‌ها" else accounts.firstOrNull { it.id == id }?.title ?: "—" },
                        onSelect = { filterAccount = if (it == 0L) null else it }
                    )
                    androidx.compose.material3.Checkbox(checked = onlyPending, onCheckedChange = { onlyPending = it })
                    Text("فقط تراکنش‌های تأییدنشده", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = { TextButton(onClick = { showFilters = false }) { Text("نمایش نتیجه") } },
            dismissButton = {
                TextButton(onClick = {
                    filterDirection = null; filterNature = null; filterAccount = null; onlyPending = false
                }) { Text("پاک کردن همه") }
            }
        )
    }
}

@Composable
private fun ProfessionalFilterChip(
    label: String,
    selected: Boolean,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) tint.copy(alpha = 0.20f) else Color.Transparent)
            .border(1.dp, tint.copy(alpha = if (selected) 0.85f else 0.28f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * یک ردیف تراکنش.
 * کشیدن به یک سمت حذف و به سمت مخالف ویرایش می‌کند؛ نگه‌داشتن، حالت انتخاب
 * چندتایی را باز می‌کند.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    account: ir.kharjyar.app.data.db.AccountEntity?,
    categoryName: String?,
    unit: ir.kharjyar.app.core.money.MoneyUnit,
    selecting: Boolean,
    checked: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onSwipeDelete: () -> Unit,
    onSwipeEdit: () -> Unit
) {
    SwipeActionRow(
        onDelete = onSwipeDelete,
        onEdit = onSwipeEdit,
        // در حالت انتخاب چندتایی، کشیدن غیرفعال است تا با انتخاب تداخل نکند
        enabled = !selecting
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
                    account?.let { acc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ir.kharjyar.app.ui.components.BankLogo(bankName = acc.bankName, size = 22.dp)
                            Text(
                                acc.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
