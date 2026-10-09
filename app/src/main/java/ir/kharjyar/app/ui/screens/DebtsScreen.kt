package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ThemedFloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.SwipeActionRow
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
                val receivableDebts = personDebts.filter { it.kind == DebtKind.RECEIVABLE }
                val payableDebts = personDebts.filter { it.kind == DebtKind.PAYABLE }
                val receivable = receivableDebts.sumOf(::remaining)
                val payable = payableDebts.sumOf(::remaining)
                // نزدیک‌ترین موعد باز برای دریافت طلب یا پرداخت بدهی روی کارت شخص دیده می‌شود.
                val receivableDate: Long? = receivableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()
                val payableDate: Long? = payableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()
                // کارت هر شخص علاوه بر موعد تسویه، تاریخ ایجاد اصل رابطه مالی را نیز
                // نشان می‌دهد: پرداخت پول در طلب و دریافت پول در بدهی.
                val receivableOriginDate: Long? = receivableDebts.maxOfOrNull { it.createdAt }
                val payableOriginDate: Long? = payableDebts.maxOfOrNull { it.createdAt }
                val isCreditor = receivable > payable
                val isDebtor = payable > receivable
                val relationAccent = when {
                    isCreditor -> Color(0xFF15966A)
                    isDebtor -> Color(0xFFE0525E)
                    else -> MaterialTheme.colorScheme.primary
                }
                val relationText = when {
                    isCreditor -> "شما از ${person.name} طلبکار هستید"
                    isDebtor -> "شما به ${person.name} بدهکار هستید"
                    else -> "طلب و بدهی شما با ${person.name} برابر است"
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, relationAccent.copy(alpha = .34f), RoundedCornerShape(24.dp))
                        .clickable { selectedPersonId = person.id },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        relationAccent.copy(alpha = .16f),
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(50.dp)
                                    .background(relationAccent.copy(alpha = .17f), CircleShape)
                                    .border(1.dp, relationAccent.copy(alpha = .38f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    person.name.trim().take(1).ifBlank { "؟" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = relationAccent
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(person.name, style = MaterialTheme.typography.titleMedium)
                                Surface(
                                    color = relationAccent.copy(alpha = .13f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            if (isCreditor) Icons.Filled.CallReceived else if (isDebtor) Icons.Filled.CallMade else Icons.Filled.Balance,
                                            contentDescription = null,
                                            tint = relationAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(relationText, style = MaterialTheme.typography.labelMedium, color = relationAccent)
                                    }
                                }
                            }
                            Icon(Icons.Filled.ChevronLeft, "مشاهده جزئیات", tint = relationAccent)
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            DebtAmountTile(
                                title = "طلب شما",
                                amount = Money.format(receivable, settings.moneyUnit),
                                accent = Color(0xFF15966A),
                                icon = Icons.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            DebtAmountTile(
                                title = "بدهی شما",
                                amount = Money.format(payable, settings.moneyUnit),
                                accent = Color(0xFFE0525E),
                                icon = Icons.Filled.TrendingDown,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (receivableOriginDate != null || payableOriginDate != null) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .055f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 11.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                receivableOriginDate?.let {
                                    DebtDateRow(
                                        label = "تاریخ پرداخت پول و قرض‌دادن",
                                        date = PersianDate.fromMillis(it).format(),
                                        accent = Color(0xFF15966A)
                                    )
                                }
                                payableOriginDate?.let {
                                    DebtDateRow(
                                        label = "تاریخ دریافت پول و قرض‌گرفتن",
                                        date = PersianDate.fromMillis(it).format(),
                                        accent = Color(0xFFE0525E)
                                    )
                                }
                            }
                        }
                        if (receivableDate != null || payableDate != null) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                receivableDate?.let {
                                    DebtDueSpotlight(
                                        label = "موعد دریافت طلب",
                                        dueAt = it,
                                        accent = Color(0xFF15966A),
                                        icon = Icons.Filled.CallReceived,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                payableDate?.let {
                                    DebtDueSpotlight(
                                        label = "موعد پرداخت بدهی",
                                        dueAt = it,
                                        accent = Color(0xFFE0525E),
                                        icon = Icons.Filled.CallMade,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${Digits.toPersian(personDebts.size.toString())} مورد ثبت‌شده",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("مشاهده گردش حساب", style = MaterialTheme.typography.labelSmall, color = relationAccent)
                        }
                    }
                }
            }
        }
        ThemedFloatingActionButton(onClick = { showChooser = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) { Icon(Icons.Filled.Add, "افزودن طلب یا بدهی") }
    }
    if(showChooser)ModernChoiceDialog("طلب یا بدهی","نوع تعهد مالی را انتخاب کنید",listOf(ModernChoiceOption("ثبت طلب","مبلغی که باید از شخص دیگری دریافت کنید",Color(0xFF1B8F52),Icons.Filled.CallReceived){showChooser=false;entryKind=DebtKind.RECEIVABLE},ModernChoiceOption("ثبت بدهی","مبلغی که باید به شخص دیگری پرداخت کنید",Color(0xFFD33B45),Icons.Filled.CallMade){showChooser=false;entryKind=DebtKind.PAYABLE}),{showChooser=false})
}

@Composable
private fun DebtMiniMetric(label:String,value:String,valueColor:Color,modifier:Modifier=Modifier){Surface(modifier=modifier,color=MaterialTheme.colorScheme.surface.copy(alpha=.62f),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.titleSmall,color=valueColor)}}}

@Composable
private fun DebtDueSpotlight(
    label: String,
    dueAt: Long,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val due = PersianDate.fromMillis(dueAt)
    val days = (due.toLocalDate().toEpochDay() - PersianDate.today().toLocalDate().toEpochDay()).toInt()
    val timing = when {
        days < 0 -> "${Digits.toPersian((-days).toString())} روز از موعد گذشته"
        days == 0 -> "امروز"
        days == 1 -> "فردا"
        else -> "${Digits.toPersian(days.toString())} روز دیگر"
    }
    Card(
        modifier = modifier.border(1.5.dp, accent.copy(alpha = .48f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = .24f), accent.copy(alpha = .08f), MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(30.dp).background(accent.copy(alpha = .16f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
            }
            Text(
                due.format(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(color = accent.copy(alpha = .14f), shape = RoundedCornerShape(50)) {
                Text(
                    timing,
                    Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }
        }
    }
}

@Composable
private fun DebtDateRow(label: String, date: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(Icons.Filled.EventAvailable, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(date, style = MaterialTheme.typography.labelLarge, color = accent)
    }
}

@Composable
private fun DebtAmountTile(
    title: String,
    amount: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = .09f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = .2f))
    ) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = accent)
            }
            Text(amount, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PersonDebtDetail(vm: AppViewModel, person: DebtPersonEntity, debts: List<DebtEntity>, payments: List<DebtPaymentEntity>, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingExcess by remember { mutableStateOf<PendingExcessPayment?>(null) }
    var editingDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var pendingDeleteDebt by remember { mutableStateOf<DebtEntity?>(null) }
    fun remaining(d: DebtEntity) = (d.amountRial - payments.filter { it.debtId == d.id }.sumOf { it.amountRial }).coerceAtLeast(0)
    val totalReceivable = debts.filter { it.kind == DebtKind.RECEIVABLE }.sumOf(::remaining)
    val totalPayable = debts.filter { it.kind == DebtKind.PAYABLE }.sumOf(::remaining)
    val completedCount = debts.count { remaining(it) == 0L }
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
        ModernSummaryHero(
            person.name,
            "دفتر مالی و سابقه بازپرداخت‌های این شخص",
            MaterialTheme.colorScheme.primary,
            listOf(
                SummaryMetric("طلب باقی‌مانده", Money.format(totalReceivable, settings.moneyUnit), Color(0xFF15966A), Icons.Filled.TrendingUp),
                SummaryMetric("بدهی باقی‌مانده", Money.format(totalPayable, settings.moneyUnit), Color(0xFFE0525E), Icons.Filled.TrendingDown)
            ),
            Icons.Filled.Person
        )
        if (person.phone.isNotBlank() || person.note.isNotBlank()) {
            Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer.copy(alpha=.32f)),shape=RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    if (person.phone.isNotBlank()) Text("تلفن: ${person.phone}",style=MaterialTheme.typography.bodyMedium)
                    if (person.note.isNotBlank()) Text(person.note, style = MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card(
            colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=.42f)),
            shape=RoundedCornerShape(18.dp),
            border=BorderStroke(1.dp,MaterialTheme.colorScheme.tertiary.copy(alpha=.28f))
        ) {
            Row(Modifier.fillMaxWidth().padding(13.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha=.14f),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Payments,null,tint=MaterialTheme.colorScheme.tertiary)}
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                    Text("بازپرداخت چندمرحله‌ای",style=MaterialTheme.typography.titleSmall)
                    Text("لازم نیست کل مبلغ را یک‌جا تسویه کنید. در کارت هر طلب یا بدهی، مبلغ هر مرحله و تاریخ آن را وارد کنید؛ مانده خودکار محاسبه می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color=MaterialTheme.colorScheme.tertiary.copy(alpha=.13f),shape=RoundedCornerShape(50)){Text("${Digits.toPersian(completedCount.toString())} تسویه",Modifier.padding(horizontal=9.dp,vertical=4.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.tertiary)}
            }
        }

        Text("طلب‌ها و بدهی‌های این فرد", style = MaterialTheme.typography.titleMedium)
        debts.sortedByDescending { it.createdAt }.forEach { debt ->
            val remain = remaining(debt)
            val receivable = debt.kind == DebtKind.RECEIVABLE
            val accent = if (receivable) Color(0xFF20A565) else Color(0xFFE14B55)
            val debtPayments=payments.filter { it.debtId==debt.id }
            val paidTotal=debtPayments.sumOf { it.amountRial }
            val progress=(paidTotal.toFloat()/debt.amountRial.coerceAtLeast(1L)).coerceIn(0f,1f)
            var amount by remember(debt.id) { mutableStateOf("") }
            var note by remember(debt.id) { mutableStateOf("") }
            var paidDate by remember(debt.id) { mutableStateOf(PersianDate.today()) }
            SwipeActionRow(
                onDelete = { pendingDeleteDebt = debt },
                onEdit = { editingDebt = debt },
                removeOnDelete = false
            ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent),shape=RoundedCornerShape(22.dp),elevation=CardDefaults.cardElevation(3.dp),modifier = Modifier.fillMaxWidth().border(1.dp,accent.copy(.34f),RoundedCornerShape(22.dp))) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent.copy(.16f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Box(Modifier.size(42.dp).background(accent.copy(.15f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(if(receivable)Icons.Filled.CallReceived else Icons.Filled.CallMade,null,tint=accent)};Column(Modifier.weight(1f)){Text(debt.title.ifBlank { if (receivable) "طلب" else "بدهی" }, style = MaterialTheme.typography.titleMedium);Text(if(receivable)"طلب از ${person.name}" else "بدهی به ${person.name}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Surface(color=accent.copy(.13f),shape=RoundedCornerShape(50)){Text(if(receivable)"طلب" else "بدهی",Modifier.padding(horizontal=9.dp,vertical=4.dp),color=accent,style=MaterialTheme.typography.labelMedium)}}
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){DebtMiniMetric("مبلغ اولیه",Money.format(debt.amountRial,settings.moneyUnit),MaterialTheme.colorScheme.onSurface,Modifier.weight(1f));DebtMiniMetric("مانده",Money.format(remain,settings.moneyUnit),accent,Modifier.weight(1f))}
                    Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("پیشرفت بازپرداخت",style=MaterialTheme.typography.labelMedium);Text("${Digits.toPersian(debtPayments.size.toString())} مرحله ثبت‌شده",style=MaterialTheme.typography.labelSmall,color=accent)};Box(Modifier.fillMaxWidth().height(7.dp).background(accent.copy(.12f),RoundedCornerShape(50))){Box(Modifier.fillMaxWidth(progress).height(7.dp).background(accent,RoundedCornerShape(50)))}}
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Icon(Icons.Filled.EventAvailable,null,tint=accent,modifier=Modifier.size(17.dp));Text("تاریخ ایجاد: ${PersianDate.fromMillis(debt.createdAt).format()}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);debt.dueAt?.let{Text(" • سررسید: ${PersianDate.fromMillis(it).format()}",style=MaterialTheme.typography.bodySmall,color=accent)}}
                    if (remain > 0) {
                        Card(colors=CardDefaults.cardColors(containerColor=accent.copy(.075f)),shape=RoundedCornerShape(16.dp),border=BorderStroke(1.dp,accent.copy(.22f))){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(if(receivable)"ثبت دریافت مرحله‌ای طلب" else "ثبت پرداخت مرحله‌ای بدهی",style=MaterialTheme.typography.titleSmall,color=accent);Text(if(receivable)"هر مبلغی که از ${person.name} دریافت کرده‌اید وارد کنید؛ مانده طلب کم می‌شود." else "هر مبلغی که به ${person.name} پرداخت کرده‌اید وارد کنید؛ مانده بدهی کم می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        AmountTextField(amount, { amount = it }, if(receivable)"مبلغ دریافتی این مرحله" else "مبلغ پرداختی این مرحله", unit = settings.moneyUnit)
                        Text("تاریخ این مرحله", style = MaterialTheme.typography.labelMedium)
                        PersianDateField(paidDate, { paidDate = it })
                        OutlinedTextField(note, { note = it }, label = { Text("توضیحات این مرحله (اختیاری)") }, modifier = Modifier.fillMaxWidth())
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
                        }, enabled = (Money.inputToRial(amount, settings.moneyUnit) ?: 0) > 0, modifier = Modifier.fillMaxWidth()) { Text(if(receivable)"ثبت دریافت این مرحله" else "ثبت پرداخت این مرحله") }
                        }}
                    } else Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF15966A).copy(.10f)),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(13.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Icon(Icons.Filled.CheckCircle,null,tint=Color(0xFF15966A));Text("این مورد به‌طور کامل تسویه شده است",color=Color(0xFF15966A),style=MaterialTheme.typography.titleSmall)}}
                }
            }
            }
        }

        HorizontalDivider()
        Text("سوابق پرداخت", style = MaterialTheme.typography.titleMedium)
        val personDebtIds = debts.map { it.id }.toSet()
        val history = payments.filter { it.debtId in personDebtIds }.sortedByDescending { it.paidAt }
        if (history.isEmpty()) Text("هنوز پرداختی برای این فرد ثبت نشده است.")
        history.forEachIndexed { index, payment ->
            val debt = debts.firstOrNull { it.id == payment.debtId }
            val historyAccent=if(debt?.kind==DebtKind.RECEIVABLE)Color(0xFF15966A) else Color(0xFFE0525E)
            Card(Modifier.fillMaxWidth().border(1.dp,historyAccent.copy(.22f),RoundedCornerShape(17.dp)),colors=CardDefaults.cardColors(containerColor=historyAccent.copy(.065f)),shape=RoundedCornerShape(17.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(historyAccent.copy(.14f),CircleShape),contentAlignment=Alignment.Center){Text(Digits.toPersian((history.size-index).toString()),color=historyAccent,style=MaterialTheme.typography.titleSmall)};Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(debt?.title?.ifBlank { if (debt.kind == DebtKind.RECEIVABLE) "دریافت طلب" else "پرداخت بدهی" } ?: "بازپرداخت",style=MaterialTheme.typography.titleSmall);Text(PersianDate.fromMillis(payment.paidAt).format(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(payment.note.ifBlank{"بدون توضیحات"},style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(Money.format(payment.amountRial,settings.moneyUnit),color=historyAccent,style=MaterialTheme.typography.titleSmall)} }
        }
    }

    editingDebt?.let { debt ->
        var editTitle by remember(debt.id) { mutableStateOf(debt.title) }
        var editAmount by remember(debt.id) { mutableStateOf(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN&&debt.amountRial%10L==0L)(debt.amountRial/10L).toString()else debt.amountRial.toString()) }
        var editOriginDate by remember(debt.id) { mutableStateOf(PersianDate.fromMillis(debt.createdAt)) }
        var editDueDate by remember(debt.id) { mutableStateOf(debt.dueAt?.let(PersianDate::fromMillis) ?: PersianDate.today()) }
        val editedRial=Money.inputToRial(editAmount,settings.moneyUnit)
        val alreadyPaid=payments.filter{it.debtId==debt.id}.sumOf{it.amountRial}
        AlertDialog(
            onDismissRequest={editingDebt=null},
            title={Text(if(debt.kind==DebtKind.RECEIVABLE)"ویرایش طلب" else "ویرایش بدهی")},
            text={Column(verticalArrangement=Arrangement.spacedBy(9.dp)){OutlinedTextField(editTitle,{editTitle=it},label={Text("عنوان")},modifier=Modifier.fillMaxWidth());AmountTextField(editAmount,{editAmount=it},"مبلغ کل",Modifier.fillMaxWidth(),unit=settings.moneyUnit);Text(if(debt.kind==DebtKind.RECEIVABLE)"تاریخ قرض‌دادن" else "تاریخ قرض‌گرفتن");PersianDateField(editOriginDate,{editOriginDate=it});Text("تاریخ سررسید تسویه");PersianDateField(editDueDate,{editDueDate=it});if(editedRial!=null&&editedRial<alreadyPaid)Text("مبلغ کل نمی‌تواند کمتر از پرداخت‌های ثبت‌شده باشد.",color=MaterialTheme.colorScheme.error)}},
            confirmButton={TextButton(enabled=editedRial!=null&&editedRial>0&&editedRial>=alreadyPaid,onClick={scope.launch{vm.repo.db.debtDao().updateDebt(debt.copy(title=editTitle.trim(),amountRial=editedRial!!,createdAt=editOriginDate.startOfDayMillis(),dueAt=editDueDate.startOfDayMillis(),reminderAt=editDueDate.startOfDayMillis(),settled=editedRial==alreadyPaid));editingDebt=null}}){Text("ذخیره تغییرات")}},
            dismissButton={TextButton({editingDebt=null}){Text("انصراف")}}
        )
    }
    pendingDeleteDebt?.let { debt ->
        val paymentCount=payments.count{it.debtId==debt.id}
        AlertDialog(
            onDismissRequest={pendingDeleteDebt=null},
            title={Text(if(debt.kind==DebtKind.RECEIVABLE)"حذف طلب؟" else "حذف بدهی؟")},
            text={Text("«${debt.title.ifBlank{if(debt.kind==DebtKind.RECEIVABLE)"طلب" else "بدهی"}}» حذف شود؟"+(if(paymentCount>0)" ${Digits.toPersian(paymentCount.toString())} سابقه پرداخت مرتبط نیز حذف می‌شود." else ""))},
            confirmButton={TextButton({scope.launch{vm.repo.db.debtDao().deletePaymentsOf(debt.id);vm.repo.db.debtDao().deleteDebt(debt);pendingDeleteDebt=null}}){Text("حذف",color=MaterialTheme.colorScheme.error)}},
            dismissButton={TextButton({pendingDeleteDebt=null}){Text("انصراف")}}
        )
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
    val accounts by vm.accounts.collectAsState()
    val activeAccounts = accounts.filter { !it.archived }
    var person by remember { mutableStateOf("") }; var title by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var originDate by remember { mutableStateOf(PersianDate.today()) }; var due by remember { mutableStateOf(PersianDate.today()) }
    var registerAccountTransaction by remember { mutableStateOf(false) }
    var accountId by remember { mutableStateOf<Long?>(null) }
    val receivable = kind == DebtKind.RECEIVABLE
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onCancel) { Icon(Icons.Filled.ArrowBack, null); Text("بازگشت به فهرست") }
        ModernSummaryHero(if(receivable)"ثبت طلب" else "ثبت بدهی","مشخصات شخص، مبلغ و سررسید",if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45),listOf(SummaryMetric("مبلغ",Money.inputToRial(amount,settings.moneyUnit)?.let{Money.format(it,settings.moneyUnit)}?:"—",if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45),if(receivable)Icons.Filled.CallReceived else Icons.Filled.CallMade),SummaryMetric("سررسید",due.format(),MaterialTheme.colorScheme.primary,Icons.Filled.EventAvailable)),Icons.Filled.AccountBalanceWallet)
        OutlinedTextField(person, { person = it }, label = { Text("نام شخص") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(title, { title = it }, label = { Text("عنوان یا توضیح") }, modifier = Modifier.fillMaxWidth())
        AmountTextField(amount, { amount = it }, "مبلغ", Modifier.fillMaxWidth(), unit = settings.moneyUnit)
        Text(if (receivable) "تاریخ پرداخت پول و قرض‌دادن" else "تاریخ دریافت پول و قرض‌گرفتن")
        PersianDateField(originDate, { originDate = it })
        Text("تاریخ سررسید تسویه"); PersianDateField(due, { due = it })
        Card(
            colors = CardDefaults.cardColors(containerColor = (if (receivable) Color(0xFF1B8F52) else Color(0xFFD33B45)).copy(alpha = .09f)),
            border = BorderStroke(1.dp, (if (receivable) Color(0xFF1B8F52) else Color(0xFFD33B45)).copy(alpha = .25f))
        ) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("اعمال در حساب و ثبت تراکنش", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (receivable) "مبلغ طلب از حساب انتخابی کم می‌شود" else "مبلغ بدهی به حساب انتخابی اضافه می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = registerAccountTransaction,
                        onCheckedChange = {
                            registerAccountTransaction = it
                            if (it && accountId == null) accountId = activeAccounts.firstOrNull()?.id
                        },
                        enabled = activeAccounts.isNotEmpty()
                    )
                }
                if (registerAccountTransaction) {
                    ComboBox(
                        label = if (receivable) "پرداخت طلب از حساب" else "واریز مبلغ بدهی به حساب",
                        options = activeAccounts.map { it.id },
                        selected = accountId,
                        labelOf = { id -> activeAccounts.firstOrNull { it.id == id }?.title ?: "انتخاب حساب" },
                        onSelect = { accountId = it }
                    )
                    Text(
                        if (receivable) "یک تراکنش برداشت با ماهیت انتقال ثبت می‌شود." else "یک تراکنش واریز با ماهیت انتقال ثبت می‌شود.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("بدون تغییر مانده حساب و بدون ساخت تراکنش", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Button(onClick = { scope.launch {
            val value = Money.inputToRial(amount, settings.moneyUnit) ?: 0
            val now = System.currentTimeMillis()
            val originAt = originDate.startOfDayMillis()
            val old = people.firstOrNull { it.name.trim() == person.trim() }
            val personId = old?.id ?: vm.repo.db.debtDao().insertPerson(DebtPersonEntity(name = person.trim(), createdAt = now))
            vm.repo.db.debtDao().insertDebt(DebtEntity(personId = personId, kind = kind, amountRial = value, title = title.trim(), createdAt = originAt, dueAt = due.startOfDayMillis(), reminderAt = due.startOfDayMillis()))
            if (registerAccountTransaction) {
                val selectedAccount = accountId ?: return@launch
                vm.repo.addManualTransaction(
                    accountId = selectedAccount,
                    amountRial = value,
                    direction = if (receivable) TxDirection.WITHDRAW else TxDirection.DEPOSIT,
                    nature = TxNature.TRANSFER,
                    categoryId = null,
                    description = title.trim().ifBlank { if (receivable) "پرداخت به ${person.trim()} و ثبت طلب" else "دریافت از ${person.trim()} و ثبت بدهی" },
                    occurredAt = originAt,
                    counterparty = person.trim()
                )
            }
            showSavedMessage(context, "طلب یا بدهی"); onDone()
        } }, enabled = person.isNotBlank() && (Money.inputToRial(amount, settings.moneyUnit) ?: 0) > 0 && (!registerAccountTransaction || accountId != null), modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.5.dp, if (receivable) Color(0xFF1B8F52) else Color(0xFFD33B45))) { Text(if (receivable) "ثبت طلب و یادآور" else "ثبت بدهی و یادآور") }
    }
}
