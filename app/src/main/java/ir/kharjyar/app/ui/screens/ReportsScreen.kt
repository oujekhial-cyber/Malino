package ir.kharjyar.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.pdf.PdfExporter
import ir.kharjyar.app.report.ExcelExporter
import ir.kharjyar.app.notify.Notifier
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.DateTimeField
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.theme.LocalAppSkin
import ir.kharjyar.app.ui.components.LineChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class RangeKind(val label: String) {
    DAY("امروز"), WEEK("هفته"), MONTH("ماه"), YEAR("سال"), CUSTOM("دلخواه")
}

@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val skin = LocalAppSkin.current

    var range by remember { mutableStateOf(RangeKind.MONTH) }
    var filterAccount by remember { mutableStateOf<Long?>(null) }
    var filterCategory by remember { mutableStateOf<Long?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    val today = PersianDate.today()
    var customFromDate by remember { mutableStateOf(today.firstOfMonth()) }
    var customFromHour by remember { mutableStateOf(0) }
    var customFromMinute by remember { mutableStateOf(0) }
    var customToDate by remember { mutableStateOf(today) }
    var customToHour by remember { mutableStateOf(23) }
    var customToMinute by remember { mutableStateOf(59) }
    val (from, to) = when (range) {
        RangeKind.DAY -> today.startOfDayMillis() to today.endOfDayMillisExclusive()
        RangeKind.WEEK -> today.plusDays(-6).startOfDayMillis() to today.endOfDayMillisExclusive()
        RangeKind.MONTH -> today.firstOfMonth().startOfDayMillis() to today.lastOfMonth().endOfDayMillisExclusive()
        RangeKind.YEAR -> PersianDate(today.year, 1, 1).startOfDayMillis() to
            PersianDate(today.year, 12, PersianDate.monthLength(today.year, 12)).endOfDayMillisExclusive()
        RangeKind.CUSTOM -> PersianDate.toMillis(customFromDate, customFromHour, customFromMinute) to
            (PersianDate.toMillis(customToDate, customToHour, customToMinute) + 60_000L)
    }

    val filtered = allTx.filter { tx ->
        tx.occurredAt >= from && tx.occurredAt < to &&
            (filterAccount == null || tx.accountId == filterAccount) &&
            (filterCategory == null || tx.categoryId == filterCategory)
    }
    val confirmed = filtered.filter { it.status == TxStatus.CONFIRMED && it.nature != TxNature.TRANSFER }
    val pending = filtered.filter { it.status == TxStatus.PENDING }
    val income = confirmed.filter { it.nature == TxNature.INCOME || (it.nature == TxNature.UNKNOWN && it.direction == TxDirection.DEPOSIT) }.sumOf { it.amountRial }
    val expense = confirmed.filter { it.nature == TxNature.EXPENSE || (it.nature == TxNature.UNKNOWN && it.direction == TxDirection.WITHDRAW) }.sumOf { it.amountRial }

    val days = ((to - from) / 86_400_000L).toInt().coerceIn(2, 366)
    val (incomeSeries, expenseSeries) = buildDailySeries(filtered, days)

    val savePdf = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            val rows = filtered.sortedByDescending { it.occurredAt }.map { tx ->
                                PdfExporter.ReportRow(
                                    dateText = PersianDate.formatDateTime(tx.occurredAt),
                                    accountTitle = accounts.firstOrNull { it.id == tx.accountId }?.let { a ->
                                        a.title + if (a.maskedNumber.isNotBlank()) " (${maskId(a.maskedNumber)})" else ""
                                    } ?: "؟",
                                    description = tx.description,
                                    categoryName = categories.firstOrNull { it.id == tx.categoryId }?.name ?: "",
                                    natureText = when (tx.nature) {
                                        TxNature.INCOME -> "واریز"
                                        TxNature.EXPENSE -> "برداشت"
                                        TxNature.TRANSFER -> "انتقال"
                                        else -> if (tx.direction == TxDirection.DEPOSIT) "واریز (تأییدنشده)" else "برداشت (تأییدنشده)"
                                    } + if (tx.status == TxStatus.PENDING) " *" else "",
                                    amountText = Money.format(tx.amountRial, settings.moneyUnit)
                                )
                            }
                            PdfExporter.export(
                                context,
                                PdfExporter.ReportData(
                                    title = "گزارش خرج‌یار",
                                    rangeText = "بازه: ${range.label} — ${today.format()}",
                                    filtersText = listOfNotNull(
                                        filterAccount?.let { fa -> "حساب: " + (accounts.firstOrNull { it.id == fa }?.title ?: "") },
                                        filterCategory?.let { fc -> "دسته: " + (categories.firstOrNull { it.id == fc }?.name ?: "") }
                                    ).joinToString("، "),
                                    incomeText = Money.format(income, settings.moneyUnit),
                                    expenseText = Money.format(expense, settings.moneyUnit),
                                    netText = Money.format(income - expense, settings.moneyUnit),
                                    pendingNote = if (pending.isNotEmpty())
                                        "* ${Digits.toPersian(pending.size.toString())} مورد تأییدنشده در جمع بالا حساب نشده است." else null,
                                    incomeSeries = incomeSeries,
                                    expenseSeries = expenseSeries,
                                    rows = rows
                                ),
                                out
                            )
                        }
                    }
                    Notifier.notifyExportReady(context, uri, "application/pdf")
                    exportMessage = "فایل PDF ذخیره شد. توجه: این فایل رمزنگاری نشده و حاوی اطلاعات مالی است."
                } catch (e: Exception) {
                    exportMessage = "خطا در ساخت PDF"
                }
            }
        }
    }

    val saveExcel = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        ExcelExporter.export(filtered.sortedByDescending { it.occurredAt }.map { tx ->
                            ExcelExporter.Row(
                                PersianDate.formatDateTime(tx.occurredAt),
                                accounts.firstOrNull { it.id == tx.accountId }?.title ?: "؟",
                                when (tx.nature) { TxNature.INCOME -> "واریز"; TxNature.EXPENSE -> "برداشت"; TxNature.TRANSFER -> "انتقال"; else -> "تأییدنشده" },
                                categories.firstOrNull { it.id == tx.categoryId }?.name ?: "",
                                tx.description,
                                Money.format(tx.amountRial, settings.moneyUnit)
                            )
                        }, out)
                    }
                }
                Notifier.notifyExportReady(context, uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                exportMessage = "فایل Excel ذخیره شد."
            } catch (_: Exception) { exportMessage = "خطا در ساخت Excel" }
        }
    }

    // ---------- تفکیک هزینه بر اساس دسته ----------
    val byCategory = confirmed
        .filter { it.nature == TxNature.EXPENSE || (it.nature == TxNature.UNKNOWN && it.direction == TxDirection.WITHDRAW) }
        .groupBy { it.categoryId }
        .map { (catId, list) ->
            val name = categories.firstOrNull { it.id == catId }?.name ?: "بدون دسته"
            val color = categories.firstOrNull { it.id == catId }?.colorArgb ?: 0xFF8A8F98
            Triple(name, list.sumOf { it.amountRial }, color)
        }
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- بازه زمانی: نوار بخش‌بندی‌شده ----------
        SegmentedRange(
            options = RangeKind.entries.toList(),
            selected = range,
            labelOf = { it.label },
            onSelect = { range = it }
        )

        if (range == RangeKind.CUSTOM) {
            SkinCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("شروع بازه", style = MaterialTheme.typography.titleSmall)
                    DateTimeField(customFromDate, customFromHour, customFromMinute,
                        onDate = { customFromDate = it }, onTime = { h, m -> customFromHour = h; customFromMinute = m })
                    Text("پایان بازه", style = MaterialTheme.typography.titleSmall)
                    DateTimeField(customToDate, customToHour, customToMinute,
                        onDate = { customToDate = it }, onTime = { h, m -> customToHour = h; customToMinute = m })
                }
            }
        }

        // ---------- کارت خلاصه ----------
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "خالص این بازه",
                    style = MaterialTheme.typography.labelMedium,
                    color = skin.onBackdrop.copy(alpha = 0.7f)
                )
                Text(
                    Money.format(income - expense, settings.moneyUnit),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = skin.bigNumberColor
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = "واریز",
                        value = Money.format(income, settings.moneyUnit),
                        tint = skin.incomeColor,
                        up = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "برداشت",
                        value = Money.format(expense, settings.moneyUnit),
                        tint = skin.expenseColor,
                        up = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pending.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${Digits.toPersian(pending.size.toString())} مورد تأییدنشده در جمع بالا نیامده " +
                            "(${Money.format(pending.sumOf { it.amountRial }, settings.moneyUnit)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = skin.expenseColor
                    )
                }
            }
        }

        // ---------- فیلترها، جمع‌وجور در کمبوباکس ----------
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "فیلترها",
                    style = MaterialTheme.typography.titleSmall,
                    color = skin.onBackdrop
                )
                ComboBox(
                    label = "حساب",
                    options = listOf(0L) + accounts.filter { !it.archived }.map { it.id },
                    selected = filterAccount ?: 0L,
                    labelOf = { id ->
                        if (id == 0L) "همه حساب‌ها"
                        else accounts.firstOrNull { it.id == id }?.title ?: "—"
                    },
                    onSelect = { filterAccount = if (it == 0L) null else it }
                )
                ComboBox(
                    label = "دسته‌بندی",
                    options = listOf(0L) + categories.filter { !it.archived }.map { it.id },
                    selected = filterCategory ?: 0L,
                    labelOf = { id ->
                        if (id == 0L) "همه دسته‌ها"
                        else categories.firstOrNull { it.id == id }?.name ?: "—"
                    },
                    onSelect = { filterCategory = if (it == 0L) null else it }
                )
            }
        }

        // ---------- نمودار روند ----------
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "روند واریز و برداشت",
                    style = MaterialTheme.typography.titleMedium,
                    color = skin.onBackdrop
                )
                Spacer(Modifier.height(12.dp))
                if (incomeSeries.all { it == 0L } && expenseSeries.all { it == 0L }) {
                    EmptyState("داده‌ای در این بازه نیست", "بازه یا فیلترها را تغییر دهید")
                } else {
                    LineChart(incomeSeries = incomeSeries, expenseSeries = expenseSeries)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ChartLegend(skin.incomeColor, "واریز")
                        ChartLegend(skin.expenseColor, "برداشت")
                    }
                }
            }
        }

        // ---------- هزینه بر اساس دسته ----------
        if (byCategory.isNotEmpty()) {
            SkinCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "برداشت بر اساس دسته",
                        style = MaterialTheme.typography.titleMedium,
                        color = skin.onBackdrop
                    )
                    Spacer(Modifier.height(12.dp))
                    val slices = byCategory.take(6)
                    val total = slices.sumOf { it.second }.coerceAtLeast(1L)
                    Canvas(Modifier.size(210.dp).align(Alignment.CenterHorizontally)) {
                        var start = -90f
                        slices.forEach { (_, amount, argb) ->
                            val sweep = amount.toFloat() / total * 360f
                            drawArc(Color(argb), start, sweep, useCenter = true)
                            start += sweep
                        }
                        drawCircle(color = skin.backdrop, radius = size.minDimension * 0.24f)
                    }
                    Spacer(Modifier.height(12.dp))
                    slices.forEach { (name, amount, colorArgb) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ChartLegend(Color(colorArgb), name)
                            Text(Money.format(amount, settings.moneyUnit), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(7.dp))
                    }
                }
            }
        }

        // ---------- خروجی ----------
        Button(
            onClick = { savePdf.launch("kharjyar-report-${today.format(persianDigits = false).replace("/", "-")}.pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("خروجی PDF")
        }

        Button(
            onClick = { saveExcel.launch("kharjyar-report-${today.format(persianDigits = false).replace("/", "-")}.xlsx") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("خروجی Excel") }

        exportMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = skin.accent)
        }
        Spacer(Modifier.height(90.dp))
    }
}

/** نوار بخش‌بندی‌شده برای انتخاب بازه، به‌جای چیپ‌های پراکنده. */
@Composable
private fun <T> SegmentedRange(
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    val skin = LocalAppSkin.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(skin.cardColor.copy(alpha = skin.cardAlpha))
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) skin.accent else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    labelOf(option),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) skin.onHero else skin.onBackdrop.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

/** کاشی درآمد/هزینه با آیکون فلش. */
@Composable
private fun StatTile(
    label: String,
    value: String,
    tint: Color,
    up: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.45f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (up) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = LocalAppSkin.current.onBackdrop.copy(alpha = 0.8f)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1
            )
        }
    }
}

/** نوار افقی سهم هر دسته از کل هزینه. */
@Composable
private fun CategoryBar(
    name: String,
    amountText: String,
    fraction: Float,
    share: Float,
    color: Color
) {
    val skin = LocalAppSkin.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(7.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.bodySmall,
                    color = skin.onBackdrop,
                    maxLines = 1
                )
            }
            Text(
                amountText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = skin.onBackdrop
            )
        }
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(skin.onBackdrop.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                Digits.toPersian((share * 100).toInt().toString()) + "٪",
                style = MaterialTheme.typography.labelSmall,
                color = skin.onBackdrop.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalAppSkin.current.onBackdrop.copy(alpha = 0.8f)
        )
    }
}

private fun maskId(id: String): String {
    val digits = id.filter(Char::isDigit)
    return if (digits.length > 4) "****" + digits.takeLast(4) else id
}
