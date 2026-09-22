package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import kotlinx.coroutines.launch

/**
 * جزئیات/ویرایش/تکمیل تراکنش. برای پیش‌نویس پیامکی، همین صفحه پنجره «خرید/واریز بابت چی بوده؟» است:
 * تأیید (ثبت)، اصلاح، یا «بعداً» (اطلاعات حذف نمی‌شود).
 */
@Composable
fun TransactionEditScreen(viewModel: AppViewModel, nav: NavHostController, txId: Long) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()

    var tx by remember { mutableStateOf<TransactionEntity?>(null) }
    var loaded by remember { mutableStateOf(false) }

    var accountId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(0) }
    var nature by remember { mutableStateOf(0) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(PersianDate.today()) }
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var suggestRule by remember { mutableStateOf(false) }

    LaunchedEffect(txId) {
        val loadedTx = viewModel.repo.txDao.byId(txId)
        tx = loadedTx
        loadedTx?.let { t ->
            accountId = t.accountId
            // نمایش مبلغ با واحد نمایش فعلی؛ ذخیره همیشه ریال
            amountText = when (settings.moneyUnit) {
                MoneyUnit.TOMAN -> if (t.amountRial % 10 == 0L) (t.amountRial / 10).toString() else t.amountRial.toString()
                MoneyUnit.RIAL -> t.amountRial.toString()
            }
            direction = t.direction
            nature = if (t.nature == 0) {
                if (t.direction == 0) 1 else 2 // پیشنهاد اولیه از جهت؛ کاربر تأیید می‌کند
            } else t.nature
            categoryId = t.categoryId
            description = t.description
            val pd = PersianDate.fromMillis(t.occurredAt)
            date = pd
            val zdt = java.time.Instant.ofEpochMilli(t.occurredAt).atZone(PersianDate.TEHRAN)
            hour = zdt.hour; minute = zdt.minute
        }
        loaded = true
    }

    if (!loaded) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
        return
    }
    val t = tx
    if (t == null) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("تراکنش پیدا نشد", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val isPending = t.status == TxStatus.PENDING
    // واحد ورودی مبلغ همان واحد نمایش است مگر مبلغ ریالی رند نباشد
    val amountUnitForInput = if (settings.moneyUnit == MoneyUnit.TOMAN && t.amountRial % 10 != 0L) MoneyUnit.RIAL else settings.moneyUnit

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (isPending) "تکمیل تراکنش — خرید/واریز بابت چی بوده؟" else "جزئیات و ویرایش تراکنش",
            style = MaterialTheme.typography.headlineSmall
        )
        if (isPending) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Text(
                    "این پیش‌نویس از پیامک بانکی ساخته شده و هنوز قطعی نیست. مقادیر را بررسی و تأیید کنید.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        if (t.balanceAfterRial != null) {
            Text(
                "مانده اعلام‌شده در پیامک: ${Money.format(t.balanceAfterRial!!, settings.moneyUnit)} (منبع: پیامک بانک، ${PersianDate.formatDateTime(t.occurredAt)})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AccountPicker(accounts.filter { !it.archived || it.id == t.accountId }, accountId) { accountId = it }

        AmountTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "مبلغ (${if (amountUnitForInput == MoneyUnit.TOMAN) "تومان" else "ریال"})",
            supportingText = Money.inputToRial(amountText, amountUnitForInput)
                ?.let { Money.format(it, settings.moneyUnit) },
            modifier = Modifier.fillMaxWidth()
        )

        NaturePicker(nature, direction, onNature = { nature = it }, onDirection = { direction = it })
        CategoryPicker(categories, categoryId, nature) { categoryId = it }
        DatePickerRow(date, hour, minute, onDate = { date = it }, onTime = { h, m -> hour = h; minute = m })
        if (t.timeIsApproximate) {
            Text(
                "زمان از پیامک قابل استخراج نبود؛ زمان دریافت پیامک به‌عنوان پیش‌فرض استفاده شده است.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("خرید/واریز بابت چی بوده؟") },
            modifier = Modifier.fillMaxWidth().keepAboveKeyboard()
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val acc = accountId
                val amount = Money.inputToRial(amountText, amountUnitForInput)
                when {
                    acc == null -> error = "حساب را انتخاب کنید"
                    amount == null || amount <= 0 -> error = "مبلغ معتبر وارد کنید"
                    nature == 0 -> error = "ماهیت تراکنش را مشخص کنید"
                    else -> {
                        error = null
                        scope.launch {
                            viewModel.repo.confirmTransaction(
                                txId = t.id,
                                accountId = acc,
                                amountRial = amount,
                                direction = direction,
                                nature = nature,
                                categoryId = categoryId,
                                description = description.trim(),
                                occurredAt = PersianDate.toMillis(date, hour, minute)
                            )
                            ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                            // پیشنهاد قانون: اگر دسته انتخاب شد و طرف مقابل معلوم است
                            if (categoryId != null && t.counterparty.isNotBlank() && categoryId != t.categoryId) {
                                suggestRule = true
                            } else {
                                nav.popBackStack()
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isPending) "تأیید و ثبت" else "ذخیره تغییرات") }

        if (isPending) {
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("بعداً (اطلاعات حفظ می‌شود)")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showDelete = true }) {
                Text("حذف تراکنش", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (suggestRule) {
        val catName = categories.firstOrNull { it.id == categoryId }?.name ?: ""
        AlertDialog(
            onDismissRequest = { suggestRule = false; nav.popBackStack() },
            title = { Text("ساخت قانون خودکار؟") },
            text = { Text("از این پس تراکنش‌های «${t.counterparty}» به‌صورت خودکار در دسته «$catName» پیشنهاد شوند؟") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        categoryId?.let { cid ->
                            viewModel.repo.categoryDao.insertRule(
                                ir.kharjyar.app.data.db.CategoryRuleEntity(
                                    keyword = t.counterparty,
                                    categoryId = cid,
                                    priority = 10,
                                    createdByUser = true,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        suggestRule = false
                        nav.popBackStack()
                    }
                }) { Text("بله، بساز") }
            },
            dismissButton = {
                TextButton(onClick = { suggestRule = false; nav.popBackStack() }) { Text("خیر") }
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("حذف تراکنش") },
            text = { Text("این تراکنش برای همیشه حذف می‌شود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.repo.txDao.delete(t.id)
                        showDelete = false
                        nav.popBackStack()
                    }
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("انصراف") } }
        )
    }
}
