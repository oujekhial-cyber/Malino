package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ModernChoiceDialog
import ir.kharjyar.app.ui.components.ModernChoiceOption
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.showSavedMessage
import kotlinx.coroutines.launch

private data class PendingExcessPayment(
    val debt: DebtEntity,
    val paidRial: Long,
    val appliedRial: Long,
    val surplusRial: Long,
    val paidAt: Long,
    val note: String
)

@Composable
fun DebtsScreen(vm: AppViewModel) {
    val people by vm.debtPeople.collectAsState()
    val debts by vm.debts.collectAsState()
    val payments by vm.debtPayments.collectAsState()
    val settings by vm.settings.collectAsState()
    var entryKind by remember { mutableStateOf<Int?>(null) }
    var showChooser by remember { mutableStateOf(false) }
    var selectedPersonId by remember { mutableStateOf<Long?>(null) }
    var filterKind by remember { mutableStateOf<Int?>(null) }
    var personQuery by remember { mutableStateOf("") }
    var filterFrom by remember { mutableStateOf(PersianDate.today().plusDays(-365)) }
    var filterTo by remember { mutableStateOf(PersianDate.today().plusDays(365)) }

    BackHandler(enabled = entryKind != null || selectedPersonId != null) {
        if (entryKind != null) entryKind = null else selectedPersonId = null
    }
    if (entryKind != null) {
        DebtEntryPage(vm, entryKind!!, people, onDone = { entryKind = null }, onCancel = { entryKind = null })
        return
    }
    selectedPersonId?.let { id ->
        val person = people.firstOrNull { it.id == id }
        if (person != null) {
            PersonDebtDetail(vm, person, debts.filter { it.personId == id }, payments, onBack = { selectedPersonId = null })
            return
        }
    }

    fun remaining(d: DebtEntity) = (d.amountRial - payments.filter { it.debtId == d.id }.sumOf { it.amountRial }).coerceAtLeast(0)
    val shownDebts = debts.filter { d ->
        (filterKind == null || d.kind == filterKind) &&
            d.createdAt >= filterFrom.startOfDayMillis() && d.createdAt < filterTo.endOfDayMillisExclusive() &&
            (personQuery.isBlank() || people.firstOrNull { it.id == d.personId }?.name?.contains(personQuery, true) == true)
    }
    val shownPeople = people.filter { person -> shownDebts.any { it.personId == person.id } }
    val totalReceivable = shownDebts.filter { it.kind == DebtKind.RECEIVABLE }.sumOf(::remaining)
    val totalPayable = shownDebts.filter { it.kind == DebtKind.PAYABLE }.sumOf(::remaining)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModernSummaryHero("طلب‌ها و بدهی‌ها","نمای کلی مانده حساب اشخاص",MaterialTheme.colorScheme.primary,listOf(SummaryMetric("مجموع طلب",Money.format(totalReceivable,settings.moneyUnit),Color(0xFF1B9A61),Icons.Filled.TrendingUp),SummaryMetric("مجموع بدهی",Money.format(totalPayable,settings.moneyUnit),Color(0xFFE24B57),Icons.Filled.TrendingDown)),Icons.Filled.AccountBalanceWallet)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(filterKind == null, { filterKind = null }, { Text("همه") })
                FilterChip(filterKind == DebtKind.RECEIVABLE, { filterKind = DebtKind.RECEIVABLE }, { Text("طلب‌ها") })
                FilterChip(filterKind == DebtKind.PAYABLE, { filterKind = DebtKind.PAYABLE }, { Text("بدهی‌ها") })
            }
            OutlinedTextField(personQuery, { personQuery = it }, label = { Text("جست‌وجوی نام شخص") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            var showDateFilters by remember { mutableStateOf(false) }
            TextButton({ showDateFilters = !showDateFilters }) { Text(if (showDateFilters) "بستن فیلتر تاریخ" else "فیلتر بر اساس تاریخ") }
            if (showDateFilters) {
                Text("از تاریخ"); PersianDateField(filterFrom, { filterFrom = it })
                Text("تا تاریخ"); PersianDateField(filterTo, { filterTo = it })
            }
            Text("افراد", style = MaterialTheme.typography.titleMedium)
            if (shownPeople.isEmpty()) Text("هنوز طلب یا بدهی ثبت نشده است.", modifier = Modifier.padding(vertical = 24.dp))
            shownPeople.forEach { person ->
                val personDebts = shownDebts.filter { it.personId == person.id }
                val receivable = personDebts.filter { it.kind == DebtKind.RECEIVABLE }.sumOf(::remaining)
                val payable = personDebts.filter { it.kind == DebtKind.PAYABLE }.sumOf(::remaining)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedPersonId = person.id },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(person.name, style = MaterialTheme.typography.titleMedium)
                            Text("مشاهده جزئیات ←", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("طلب: ${Money.format(receivable, settings.moneyUnit)}", color = Color(0xFF1B8F52))
                            Text("بدهی: ${Money.format(payable, settings.moneyUnit)}", color = Color(0xFFD33B45))
                        }
                        Text("${Digits.toPersian(personDebts.size.toString())} مورد ثبت‌شده", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showChooser = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) { Icon(Icons.Filled.Add, "افزودن طلب یا بدهی") }
    }
    if(showChooser)ModernChoiceDialog("طلب یا بدهی","نوع تعهد مالی را انتخاب کنید",listOf(ModernChoiceOption("ثبت طلب","مبلغی که باید از شخص دیگری دریافت کنید",Color(0xFF1B8F52),Icons.Filled.CallReceived){showChooser=false;entryKind=DebtKind.RECEIVABLE},ModernChoiceOption("ثبت بدهی","مبلغی که باید به شخص دیگری پرداخت کنید",Color(0xFFD33B45),Icons.Filled.CallMade){showChooser=false;entryKind=DebtKind.PAYABLE}),{showChooser=false})
}

@Composable
private fun PersonDebtDetail(vm: AppViewModel, person: DebtPersonEntity, debts: List<DebtEntity>, payments: List<DebtPaymentEntity>, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingExcess by remember { mutableStateOf<PendingExcessPayment?>(null) }
    fun remaining(d: DebtEntity) = (d.amountRial - payments.filter { it.debtId == d.id }.sumOf { it.amountRial }).coerceAtLeast(0)
    fun applyPayment(p: PendingExcessPayment, createOpposite: Boolean) {
        scope.launch {
            vm.repo.db.debtDao().insertPayment(DebtPaymentEntity(debtId = p.debt.id, amountRial = if (createOpposite) p.paidRial else p.appliedRial, paidAt = p.paidAt, note = p.note))
            vm.repo.db.debtDao().updateDebt(p.debt.copy(settled = true))
            if (createOpposite && p.surplusRial > 0) {
                val oppositeKind = if (p.debt.kind == DebtKind.RECEIVABLE) DebtKind.PAYABLE else DebtKind.RECEIVABLE
                val title = if (oppositeKind == DebtKind.RECEIVABLE) "طلب ناشی از مازاد پرداخت" else "بدهی ناشی از مازاد پرداخت"
                vm.repo.db.debtDao().insertDebt(DebtEntity(personId = person.id, kind = oppositeKind, amountRial = p.surplusRial, title = title, createdAt = p.paidAt, dueAt = null, reminderAt = null))
            }
            pendingExcess = null
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onBack) { Icon(Icons.Filled.ArrowBack, null); Text("بازگشت به فهرست افراد") }
        Text(person.name, style = MaterialTheme.typography.headlineSmall)
        if (person.phone.isNotBlank()) Text("تلفن: ${person.phone}")
        if (person.note.isNotBlank()) Text(person.note, style = MaterialTheme.typography.bodySmall)

        Text("طلب‌ها و بدهی‌های این فرد", style = MaterialTheme.typography.titleMedium)
        debts.sortedByDescending { it.createdAt }.forEach { debt ->
            val remain = remaining(debt)
            val receivable = debt.kind == DebtKind.RECEIVABLE
            val accent = if (receivable) Color(0xFF20A565) else Color(0xFFE14B55)
            var amount by remember(debt.id) { mutableStateOf("") }
            var note by remember(debt.id) { mutableStateOf("") }
            var paidDate by remember(debt.id) { mutableStateOf(PersianDate.today()) }
            Card(colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .10f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(debt.title.ifBlank { if (receivable) "طلب" else "بدهی" }, style = MaterialTheme.typography.titleSmall); Text(if (receivable) "طلب" else "بدهی", color = accent) }
                    Text("مبلغ اولیه: ${Money.format(debt.amountRial, settings.moneyUnit)}")
                    Text("مانده: ${Money.format(remain, settings.moneyUnit)}", color = accent, style = MaterialTheme.typography.titleSmall)
                    Text("تاریخ ثبت: ${PersianDate.fromMillis(debt.createdAt).format()}")
                    if (remain > 0) {
                        AmountTextField(amount, { amount = it }, "مبلغ پرداخت", unit = settings.moneyUnit)
                        Text("تاریخ پرداخت", style = MaterialTheme.typography.labelMedium)
                        PersianDateField(paidDate, { paidDate = it })
                        OutlinedTextField(note, { note = it }, label = { Text("توضیحات پرداخت") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            val value = Money.inputToRial(amount, settings.moneyUnit) ?: return@Button
                            val paidAt = paidDate.startOfDayMillis()
                            if (value > remain) {
                                pendingExcess = PendingExcessPayment(debt, value, remain, value - remain, paidAt, note.trim())
                            } else scope.launch {
                                vm.repo.db.debtDao().insertPayment(DebtPaymentEntity(debtId = debt.id, amountRial = value, paidAt = paidAt, note = note.trim()))
                                if (value == remain) vm.repo.db.debtDao().updateDebt(debt.copy(settled = true))
                                amount = ""; note = ""
                            }
                        }, enabled = (Money.inputToRial(amount, settings.moneyUnit) ?: 0) > 0, modifier = Modifier.fillMaxWidth()) { Text("ثبت پرداخت") }
                    } else Text("تسویه‌شده", color = accent)
                }
            }
        }

        HorizontalDivider()
        Text("سوابق پرداخت", style = MaterialTheme.typography.titleMedium)
        val personDebtIds = debts.map { it.id }.toSet()
        val history = payments.filter { it.debtId in personDebtIds }.sortedByDescending { it.paidAt }
        if (history.isEmpty()) Text("هنوز پرداختی برای این فرد ثبت نشده است.")
        history.forEach { payment ->
            val debt = debts.firstOrNull { it.id == payment.debtId }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Money.format(payment.amountRial, settings.moneyUnit), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(PersianDate.fromMillis(payment.paidAt).format())
                }
                Text(debt?.title?.ifBlank { if (debt.kind == DebtKind.RECEIVABLE) "پرداخت طلب" else "پرداخت بدهی" } ?: "پرداخت")
                Text(payment.note.ifBlank { "بدون توضیحات" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } }
        }
    }

    pendingExcess?.let { p ->
        val oppositeLabel = if (p.debt.kind == DebtKind.RECEIVABLE) "بدهی جدید" else "طلب جدید"
        AlertDialog(
            onDismissRequest = { pendingExcess = null },
            title = { Text("مبلغ پرداخت بیشتر از مانده است") },
            text = { Text("مبلغ واردشده ${Money.format(p.paidRial, settings.moneyUnit)} است و ${Money.format(p.surplusRial, settings.moneyUnit)} بیشتر از ${if (p.debt.kind == DebtKind.RECEIVABLE) "طلب" else "بدهی"} باقی‌مانده شماست. آیا مبلغ مازاد به‌عنوان $oppositeLabel برای «${person.name}» ثبت شود یا نادیده گرفته شود؟") },
            confirmButton = { TextButton({ applyPayment(p, true) }) { Text("ثبت به‌عنوان $oppositeLabel") } },
            dismissButton = { Row {
                TextButton({ applyPayment(p, false) }) { Text("نادیده‌گرفتن مبلغ مازاد") }
                TextButton({ pendingExcess = null }) { Text("انصراف") }
            } }
        )
    }
}

@Composable
private fun DebtEntryPage(vm: AppViewModel, kind: Int, people: List<DebtPersonEntity>, onDone: () -> Unit, onCancel: () -> Unit) {
    val scope = rememberCoroutineScope(); val context = LocalContext.current; val settings by vm.settings.collectAsState()
    var person by remember { mutableStateOf("") }; var title by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var due by remember { mutableStateOf(PersianDate.today()) }
    val receivable = kind == DebtKind.RECEIVABLE
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onCancel) { Icon(Icons.Filled.ArrowBack, null); Text("بازگشت به فهرست") }
        ModernSummaryHero(if(receivable)"ثبت طلب" else "ثبت بدهی","مشخصات شخص، مبلغ و سررسید",if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45),listOf(SummaryMetric("مبلغ",Money.inputToRial(amount,settings.moneyUnit)?.let{Money.format(it,settings.moneyUnit)}?:"—",if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45),if(receivable)Icons.Filled.CallReceived else Icons.Filled.CallMade),SummaryMetric("سررسید",due.format(),MaterialTheme.colorScheme.primary,Icons.Filled.EventAvailable)),Icons.Filled.AccountBalanceWallet)
        OutlinedTextField(person, { person = it }, label = { Text("نام شخص") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(title, { title = it }, label = { Text("عنوان یا توضیح") }, modifier = Modifier.fillMaxWidth())
        AmountTextField(amount, { amount = it }, "مبلغ", Modifier.fillMaxWidth(), unit = settings.moneyUnit)
        Text("تاریخ سررسید"); PersianDateField(due, { due = it })
        Button(onClick = { scope.launch {
            val old = people.firstOrNull { it.name.trim() == person.trim() }
            val personId = old?.id ?: vm.repo.db.debtDao().insertPerson(DebtPersonEntity(name = person.trim(), createdAt = System.currentTimeMillis()))
            vm.repo.db.debtDao().insertDebt(DebtEntity(personId = personId, kind = kind, amountRial = Money.inputToRial(amount, settings.moneyUnit) ?: 0, title = title.trim(), createdAt = System.currentTimeMillis(), dueAt = due.startOfDayMillis(), reminderAt = due.startOfDayMillis()))
            showSavedMessage(context, "طلب یا بدهی"); onDone()
        } }, enabled = person.isNotBlank() && (Money.inputToRial(amount, settings.moneyUnit) ?: 0) > 0, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.5.dp, if (receivable) Color(0xFF1B8F52) else Color(0xFFD33B45))) { Text(if (receivable) "ثبت طلب و یادآور" else "ثبت بدهی و یادآور") }
    }
}
