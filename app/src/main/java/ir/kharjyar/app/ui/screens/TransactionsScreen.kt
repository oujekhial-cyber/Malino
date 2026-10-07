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
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.GlassSnackbarHost
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

private data class TransactionMonthPeriod(val year:Int?,val month:Int?,val label:String){
 fun contains(time:Long):Boolean=year==null||PersianDate.fromMillis(time).let{it.year==year&&it.month==month}
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    val monthPeriods=remember(all){
        val current=PersianDate.today()
        val keys=(all.map{PersianDate.fromMillis(it.occurredAt)}.map{it.year to it.month}+(current.year to current.month)).distinct().sortedWith(compareByDescending<Pair<Int,Int>>{it.first}.thenByDescending{it.second})
        listOf(TransactionMonthPeriod(null,null,"همه ماه‌ها"))+keys.map{(year,month)->TransactionMonthPeriod(year,month,"${PersianDate(year,month,1).monthName()} ${Digits.toPersian(year.toString())}")}
    }
    var filterPeriod by remember { mutableStateOf(TransactionMonthPeriod(null,null,"همه ماه‌ها")) }
    // میان‌بر چیپ کارت خانه، واریز/برداشت را بر اساس جهت بانکی فیلتر می‌کند؛
    // انتقال‌ها هم بسته به جهت خود در نتیجه باقی می‌مانند.
    var filterDirection by remember(presetDirection) { mutableStateOf(presetDirection) }
    var onlyPending by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

    // انتخاب چندتایی: با نگه‌داشتن روی یک ردیف فعال می‌شود
    val selected = remember { mutableStateListOf<Long>() }
    val selecting = selected.isNotEmpty()

    // جست‌وجو با هر تغییر متن فوراً روی فهرست اعمال می‌شود. قبلاً برای عبارت‌های
    // غیرعددی، رشته خالیِ استخراج‌شده از رقم داخل همه مبلغ‌ها پیدا می‌شد و در
    // نتیجه تمام تراکنش‌ها نمایش داده می‌شدند؛ همین باعث می‌شد جست‌وجو ظاهراً کار نکند.
    val normalizedQuery = Digits.normalizeForMatch(query).trim()
    val queryDigits = Digits.normalize(query).filter(Char::isDigit)
    val accountTitles = accounts.associate { it.id to Digits.normalizeForMatch(it.title) }
    val categoryTitles = categories.associate { it.id to Digits.normalizeForMatch(it.name) }
    val filtered = all.filter { tx ->
        val textMatches = normalizedQuery.isNotBlank() && listOf(
            tx.description,
            tx.counterparty,
            tx.refNumber,
            accountTitles[tx.accountId].orEmpty(),
            tx.categoryId?.let(categoryTitles::get).orEmpty()
        ).any { Digits.normalizeForMatch(it).contains(normalizedQuery, ignoreCase = true) }
        val amountMatches = queryDigits.isNotBlank() && tx.amountRial.toString().contains(queryDigits)
        (query.isBlank() || textMatches || amountMatches) &&
            (filterAccount == null || tx.accountId == filterAccount) &&
            (filterNature == null || tx.nature == filterNature) &&
            (filterDirection == null || tx.direction == filterDirection) &&
            filterPeriod.contains(tx.occurredAt) &&
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
                        pendingDelete = items
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
                    placeholder = { Text("جست‌وجوی زنده در تراکنش‌ها، حساب و مبلغ") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true
                )
            }

            // انتخاب ماه، کمبوباکس کلاسیک روی خود صفحه است و فهرست آن مستقیم زیر
            // کنترل باز می‌شود؛ برای انتخاب بازه پنجره یا دیالوگ جدا نمایش نمی‌دهیم.
            ComboBox("ماه",monthPeriods,filterPeriod,{filterPeriod=it},labelOf={it.label})

            // فیلترهای پرکاربرد همیشه جلوی چشم و با یک لمس قابل انتخاب‌اند.
            val activeFilterCount = listOf(
                filterDirection != null, filterNature != null, filterAccount != null, filterPeriod.year != null, onlyPending
            ).count { it }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    ProfessionalFilterChip("همه", activeFilterCount == 0) {
                        filterDirection = null; filterNature = null; filterAccount = null; filterPeriod=monthPeriods.first(); onlyPending = false
                    }
                }
                item {
                    ProfessionalFilterChip("واریز", filterDirection == TxDirection.DEPOSIT, skin.incomeColor) {
                        filterDirection = if (filterDirection == TxDirection.DEPOSIT) null else TxDirection.DEPOSIT
                    }
                }
                item {
                    ProfessionalFilterChip("برداشت", filterDirection == TxDirection.WITHDRAW, skin.expenseColor) {
                        filterDirection = if (filterDirection == TxDirection.WITHDRAW) null else TxDirection.WITHDRAW
                    }
                }
                item {
                    ProfessionalFilterChip("تأییدنشده", onlyPending, MaterialTheme.colorScheme.tertiary) { onlyPending = !onlyPending }
                }
                item {
                    ProfessionalFilterChip(
                        if (activeFilterCount == 0) "فیلترهای بیشتر" else "بیشتر ($activeFilterCount)",
                        filterNature != null || filterAccount != null || filterPeriod.year != null
                    ) { showFilters = true }
                }
            }
            if (filterAccount != null || filterNature != null || filterPeriod.year != null) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فعال:", style = MaterialTheme.typography.labelSmall, color = skin.onBackdrop.copy(alpha = .65f))
                    filterAccount?.let { id ->
                        ProfessionalFilterChip(accounts.firstOrNull { it.id == id }?.title ?: "حساب", true) { filterAccount = null }
                    }
                    filterNature?.let { nature ->
                        ProfessionalFilterChip(when (nature) { TxNature.INCOME -> "درآمد"; TxNature.EXPENSE -> "هزینه"; else -> "انتقال" }, true) { filterNature = null }
                    }
                    if(filterPeriod.year!=null) ProfessionalFilterChip(filterPeriod.label,true){filterPeriod=monthPeriods.first()}
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
                            onSwipeDelete = { pendingDelete = listOf(tx) },
                            onSwipeEdit = { nav.navigate("tx/${tx.id}") }
                        )
                    }
                }
            }
        }
    }
    if (showFilters) {
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("فیلتر تراکنش‌ها", style = MaterialTheme.typography.titleLarge)
                Text("نوع گردش", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(null to "همه", TxDirection.DEPOSIT to "واریز", TxDirection.WITHDRAW to "برداشت").forEach { (value, label) ->
                        ProfessionalFilterChip(label, filterDirection == value, when(value){TxDirection.DEPOSIT->skin.incomeColor;TxDirection.WITHDRAW->skin.expenseColor;else->MaterialTheme.colorScheme.primary}) { filterDirection = value }
                    }
                }
                Text("ماهیت تراکنش", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(4) { index ->
                        val values = listOf<Int?>(null, TxNature.INCOME, TxNature.EXPENSE, TxNature.TRANSFER)
                        val labels = listOf("همه", "درآمد", "هزینه", "انتقال")
                        ProfessionalFilterChip(labels[index], filterNature == values[index]) { filterNature = values[index] }
                    }
                }
                Text("حساب", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    item { ProfessionalFilterChip("همه حساب‌ها", filterAccount == null) { filterAccount = null } }
                    items(accounts.size) { index -> val account = accounts[index]; ProfessionalFilterChip(account.title, filterAccount == account.id) { filterAccount = account.id } }
                }
                SkinCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("فقط تأییدنشده‌ها"); Text("پیامک‌ها و تراکنش‌های نیازمند بررسی", style = MaterialTheme.typography.labelSmall, color = skin.onBackdrop.copy(alpha = .65f)) }
                        androidx.compose.material3.Switch(checked = onlyPending, onCheckedChange = { onlyPending = it })
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = { filterDirection=null;filterNature=null;filterAccount=null;filterPeriod=monthPeriods.first();onlyPending=false }, modifier = Modifier.weight(1f)) { Text("پاک کردن") }
                    androidx.compose.material3.Button(onClick = { showFilters=false }, modifier = Modifier.weight(1f)) { Text("نمایش ${Digits.toPersian(filtered.size.toString())} نتیجه") }
                }
            }
        }
    }
    if (pendingDelete.isNotEmpty()) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = emptyList() },
            title = { Text("حذف تراکنش") },
            text = { Text(if (pendingDelete.size == 1) "آیا این تراکنش حذف شود؟" else "آیا ${pendingDelete.size} تراکنش انتخاب‌شده حذف شوند؟") },
            confirmButton = { TextButton(onClick = { val values = pendingDelete; pendingDelete = emptyList(); deleteWithUndo(values) }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { pendingDelete = emptyList() }) { Text("لغو") } }
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
