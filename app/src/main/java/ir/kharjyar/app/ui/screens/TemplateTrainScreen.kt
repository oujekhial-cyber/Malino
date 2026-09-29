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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.sms.Extractor
import ir.kharjyar.app.core.sms.FieldRole
import ir.kharjyar.app.core.sms.FieldRule
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsTemplateEntity
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

/**
 * آموزش/اصلاح قالب پیامک:
 * - متن پیامک نمایش داده می‌شود و اعداد استخراج‌شده هایلایت می‌شوند.
 * - کاربر برای هر فیلد، مقدار درست را از فهرست اعداد/بخش‌ها انتخاب می‌کند.
 * - قبل از ذخیره، نتیجه استخراج روی همین نمونه نمایش داده می‌شود.
 */
@Composable
fun TemplateTrainScreen(viewModel: AppViewModel, nav: NavHostController, smsId: Long) {
    val scope = rememberCoroutineScope()
    var sms by remember { mutableStateOf<SmsCandidateEntity?>(null) }
    var loaded by remember { mutableStateOf(false) }
    val selections = remember { mutableStateMapOf<FieldRole, String>() }
    var amountUnit by remember { mutableStateOf("RIAL") }
    var directionType by remember { mutableStateOf("") } // "" | DEPOSIT | WITHDRAW
    var directionAnchor by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<ir.kharjyar.app.core.sms.ExtractionResult?>(null) }

    LaunchedEffect(smsId) {
        val found = viewModel.repo.smsDao.byId(smsId)
        sms = found
        // پیش‌پرکردن خودکار: هرچه خودِ برنامه از پیامک فهمیده، از قبل انتخاب
        // می‌شود تا کاربر فقط تأیید کند، نه اینکه همه چیز را دستی بچیند.
        if (found != null) {
            val auto = Extractor.autoExtract(found.body)
            val body = Digits.normalizeForMatch(found.body)
            val tokens = Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d+").findAll(body).map { it.value }.toList()
            fun tokenOf(value: Long?): String? {
                if (value == null) return null
                val plain = if (auto.amountUnit == "TOMAN") value / 10 else value
                return tokens.firstOrNull { Digits.parseAmount(it) == plain }
            }
            tokenOf(auto.amountRial)?.let { selections[FieldRole.AMOUNT] = it }
            tokenOf(auto.balanceRial)?.let { selections[FieldRole.BALANCE] = it }
            auto.accountIdHint?.let { hint ->
                val digits = hint.filter(Char::isDigit)
                val token = tokens.firstOrNull { it == digits }
                    ?: tokens.firstOrNull { digits.endsWith(it) && it.length >= 3 }
                if (token != null) selections[FieldRole.ACCOUNT_ID] = token
            }
            auto.dateText?.let { selections[FieldRole.DATE] = it }
            auto.timeText?.let { selections[FieldRole.TIME] = it }
            if (auto.amountUnit == "TOMAN") amountUnit = "TOMAN"
            when (auto.directionEnum()) {
                ir.kharjyar.app.core.sms.ExtractedDirection.DEPOSIT -> directionType = "DEPOSIT"
                ir.kharjyar.app.core.sms.ExtractedDirection.WITHDRAW -> directionType = "WITHDRAW"
                else -> {}
            }
            directionAnchor = Extractor.directionWordIn(found.body) ?: ""
        }
        loaded = true
    }
    if (!loaded) return
    val s = sms
    if (s == null) {
        Column(Modifier.fillMaxSize().padding(32.dp)) { Text("پیامک پیدا نشد") }
        return
    }

    // همان نرمال‌سازی‌ای که استخراج‌گر استفاده می‌کند تا انتخاب‌های
    // پیش‌پرشده دقیقاً با گزینه‌های روی صفحه یکی باشند
    val normalized = Digits.normalizeForMatch(s.body)
    val numbers = remember(s.body) {
        Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d+").findAll(normalized).map { it.value }.distinct().toList()
    }
    val words = remember(s.body) {
        normalized.split(' ', '\n', '\r', '\t').map { it.trim(':', '،', '.') }.filter { it.length in 2..20 && !it.all(Char::isDigit) }.distinct().take(30)
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
                Text(
            "آنچه خودکار تشخیص داده شده از قبل انتخاب شده است؛ اگر درست است فقط ذخیره کنید.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "فرستنده: ${s.sender}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // متن پیامک با هایلایت اعداد
        val highlightColor = MaterialTheme.colorScheme.primary
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                buildAnnotatedString {
                    var rest = normalized
                    while (rest.isNotEmpty()) {
                        val m = Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d+").find(rest)
                        if (m == null) { append(rest); break }
                        append(rest.substring(0, m.range.first))
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor))
                        append(m.value)
                        pop()
                        rest = rest.substring(m.range.last + 1)
                    }
                },
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        FieldSelector("مبلغ تراکنش (الزامی)", numbers, selections[FieldRole.AMOUNT]) { selections[FieldRole.AMOUNT] = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("واحد مبلغ:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            FilterChip(selected = amountUnit == "RIAL", onClick = { amountUnit = "RIAL" }, label = { Text("ریال") })
            FilterChip(selected = amountUnit == "TOMAN", onClick = { amountUnit = "TOMAN" }, label = { Text("تومان") })
        }
        FieldSelector("مانده حساب (اختیاری)", numbers, selections[FieldRole.BALANCE]) { selections[FieldRole.BALANCE] = it }
        FieldSelector("شناسه حساب/کارت (اختیاری)", numbers, selections[FieldRole.ACCOUNT_ID]) { selections[FieldRole.ACCOUNT_ID] = it }
        FieldSelector("شماره پیگیری (اختیاری)", numbers, selections[FieldRole.REF_NUMBER]) { selections[FieldRole.REF_NUMBER] = it }

        Text("جهت این نوع پیامک", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = directionType == "DEPOSIT", onClick = { directionType = "DEPOSIT" }, label = { Text("واریز") })
            FilterChip(selected = directionType == "WITHDRAW", onClick = { directionType = "WITHDRAW" }, label = { Text("برداشت") })
        }
        if (directionType.isNotBlank()) {
            Text("کدام واژه در متن نشانه این جهت است؟", style = MaterialTheme.typography.bodySmall)
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(words.size) { i ->
                    FilterChip(
                        selected = directionAnchor == words[i],
                        onClick = { directionAnchor = words[i] },
                        label = { Text(words[i]) }
                    )
                }
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // پیش‌نمایش استخراج
        preview?.let { p ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("پیش‌نمایش استخراج روی همین پیامک:", style = MaterialTheme.typography.titleSmall)
                    Text("مبلغ: " + (p.amountRial?.let { Money.format(it, ir.kharjyar.app.core.money.MoneyUnit.RIAL) } ?: "—"))
                    Text("جهت: " + when (p.directionEnum()) {
                        ir.kharjyar.app.core.sms.ExtractedDirection.DEPOSIT -> "واریز"
                        ir.kharjyar.app.core.sms.ExtractedDirection.WITHDRAW -> "برداشت"
                        else -> "نامشخص"
                    })
                    p.balanceRial?.let { Text("مانده: " + Money.format(it, ir.kharjyar.app.core.money.MoneyUnit.RIAL)) }
                    p.accountIdHint?.let { Text("شناسه: " + Digits.toPersian(it)) }
                }
            }
        }

        Button(
            onClick = {
                val fieldValues = buildMap {
                    selections.forEach { (role, value) -> if (value.isNotBlank()) put(role, value) }
                    if (directionType.isNotBlank() && directionAnchor.isNotBlank()) {
                        put(FieldRole.DIRECTION, "$directionAnchor|$directionType")
                    }
                }
                if (!fieldValues.containsKey(FieldRole.AMOUNT)) {
                    error = "مبلغ تراکنش را انتخاب کنید"
                    return@Button
                }
                val rules = Extractor.suggestRules(s.body, fieldValues)
                val validation = Extractor.validateRules(rules)
                if (validation.isNotEmpty()) {
                    error = validation.joinToString("، ")
                    return@Button
                }
                val result = Extractor.applyRules(s.body, rules, amountUnit)
                if (result.amountRial == null) {
                    error = "قالب نتوانست مبلغ را از همین نمونه استخراج کند؛ انتخاب‌ها را بازبینی کنید"
                    preview = result
                    return@Button
                }
                error = null
                preview = result
                scope.launch {
                    val templateId = viewModel.repo.templateDao.insert(
                        SmsTemplateEntity(
                            name = "قالب ${s.sender} ${if (directionType == "DEPOSIT") "واریز" else "برداشت"}",
                            sender = s.sender,
                            rulesJson = FieldRule.listToJson(rules),
                            sampleBody = s.body,
                            amountUnit = amountUnit,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // با قالب تازه، همه پیامک‌های منتظرِ قالب دوباره پردازش
                    // می‌شوند تا همان پرسش‌ها برای پیامک‌های هم‌شکل تکرار نشود
                    viewModel.repo.reprocessPending()
                    // پردازش دوباره همان پیامک
                    val accountId = s.matchedAccountId
                    val fresh = viewModel.repo.smsDao.byId(s.id)
                    if (fresh != null && accountId != null) {
                        when (val outcome = viewModel.repo.processWithAccount(fresh, accountId)) {
                            is ir.kharjyar.app.data.Repository.ProcessOutcome.DraftReady -> {
                                nav.navigate("tx/${outcome.txId}") { popUpTo("home") }
                                return@launch
                            }
                            else -> {}
                        }
                    } else if (fresh != null) {
                        viewModel.repo.processSms(fresh.id)
                    }
                    nav.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("آزمایش و ذخیره قالب") }
    }
}

@Composable
private fun FieldSelector(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(options.size) { i ->
                FilterChip(
                    selected = selected == options[i],
                    onClick = { onSelect(if (selected == options[i]) "" else options[i]) },
                    label = { Text(Digits.toPersian(options[i])) }
                )
            }
        }
    }
}
