package ir.kharjyar.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.pdf.PdfExporter
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.LineChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class RangeKind(val label: String) {
    DAY("امروز"), WEEK("هفته"), MONTH("ماه شمسی"), YEAR("سال شمسی")
}

@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var range by remember { mutableStateOf(RangeKind.MONTH) }
    var filterAccount by remember { mutableStateOf<Long?>(null) }
    var filterCategory by remember { mutableStateOf<Long?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    val today = PersianDate.today()
    val (from, to) = when (range) {
        RangeKind.DAY -> today.startOfDayMillis() to today.endOfDayMillisExclusive()
        RangeKind.WEEK -> today.plusDays(-6).startOfDayMillis() to today.endOfDayMillisExclusive()
        RangeKind.MONTH -> today.firstOfMonth().startOfDayMillis() to today.lastOfMonth().endOfDayMillisExclusive()
        RangeKind.YEAR -> PersianDate(today.year, 1, 1).startOfDayMillis() to
            PersianDate(today.year, 12, PersianDate.monthLength(today.year, 12)).endOfDayMillisExclusive()
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
                                        TxNature.INCOME -> "درآمد"
                                        TxNature.EXPENSE -> "هزینه"
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
                    exportMessage = "فایل PDF ذخیره شد. توجه: این فایل رمزنگاری نشده و حاوی اطلاعات مالی است."
                } catch (e: Exception) {
                    exportMessage = "خطا در ساخت PDF"
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RangeKind.entries.forEach { r ->
                FilterChip(selected = range == r, onClick = { range = r }, label = { Text(r.label) })
            }
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accounts.size) { i ->
                val a = accounts[i]
                FilterChip(
                    selected = filterAccount == a.id,
                    onClick = { filterAccount = if (filterAccount == a.id) null else a.id },
                    label = { Text(a.title) }
                )
            }
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories.count { !it.archived }) { i ->
                val c = categories.filter { !it.archived }[i]
                FilterChip(
                    selected = filterCategory == c.id,
                    onClick = { filterCategory = if (filterCategory == c.id) null else c.id },
                    label = { Text(c.name) }
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("درآمد: " + Money.format(income, settings.moneyUnit), style = MaterialTheme.typography.titleSmall)
                Text("هزینه: " + Money.format(expense, settings.moneyUnit), style = MaterialTheme.typography.titleSmall)
                Text("خالص: " + Money.format(income - expense, settings.moneyUnit), style = MaterialTheme.typography.titleMedium)
                if (pending.isNotEmpty()) {
                    Text(
                        "${Digits.toPersian(pending.size.toString())} مورد تأییدنشده جداگانه است و در جمع بالا نیامده (مجموع: ${Money.format(pending.sumOf { it.amountRial }, settings.moneyUnit)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    "انتقال بین حساب‌های خودتان در درآمد/هزینه حساب نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp)) {
                Text("نمودار درآمد و هزینه", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                if (incomeSeries.all { it == 0L } && expenseSeries.all { it == 0L }) {
                    EmptyState("داده‌ای در این بازه نیست", "بازه یا فیلترها را تغییر دهید")
                } else {
                    LineChart(incomeSeries = incomeSeries, expenseSeries = expenseSeries)
                }
            }
        }

        Button(
            onClick = { savePdf.launch("kharjyar-report-${today.format(persianDigits = false).replace("/", "-")}.pdf") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("خروجی PDF") }

        exportMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

private fun maskId(id: String): String {
    val digits = id.filter(Char::isDigit)
    return if (digits.length > 4) "****" + digits.takeLast(4) else id
}
