package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.sms.*
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import kotlinx.coroutines.launch

/** جایگذاری دستی پیامک بانکی، تحلیل محلی و ساخت تراکنش قابل تأیید. */
@Composable
fun SmsPasteScreen(viewModel: AppViewModel, nav: NavHostController) {
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var body by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ExtractionResult?>(null) }
    var accountId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val active = accounts.filter { !it.archived }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("متن کامل پیامک بانک را اینجا جایگذاری کنید. تحلیل روی گوشی انجام می‌شود.")
        OutlinedTextField(body, { body = it }, label = { Text("متن پیامک بانکی") }, minLines = 7, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val extracted = Extractor.autoExtract(body)
            result = extracted
            accountId = when (val match = AccountNumberMatcher.match(body, active.map {
                MatchableAccount(it.id, it.maskedNumber, it.accountNumber, it.iban, it.cardNumber)
            })) { is AccountMatch.Single -> match.accountId; else -> null }
            error = if (extracted.amountRial == null || extracted.directionEnum() == ExtractedDirection.UNKNOWN) "مبلغ یا نوع گردش مشخص نشد؛ متن پیامک را کامل وارد کنید." else null
        }, enabled = body.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("تحلیل پیامک") }

        result?.let { r ->
            HorizontalDivider()
            Text("نتیجه تشخیص", style = MaterialTheme.typography.titleMedium)
            Text("مبلغ: ${r.amountRial?.let { Money.format(it, settings.moneyUnit) } ?: "نامشخص"}")
            Text("نوع: ${if (r.directionEnum() == ExtractedDirection.DEPOSIT) "واریز" else if (r.directionEnum() == ExtractedDirection.WITHDRAW) "برداشت" else "نامشخص"}")
            ComboBox(label = "حساب مربوط", options = active.map { it.id }, selected = accountId,
                labelOf = { id -> active.firstOrNull { it.id == id }?.title ?: "—" }, onSelect = { accountId = it })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                val id = accountId ?: return@Button
                val amount = r.amountRial ?: return@Button
                val direction = if (r.directionEnum() == ExtractedDirection.DEPOSIT) TxDirection.DEPOSIT else TxDirection.WITHDRAW
                scope.launch {
                    viewModel.repo.addManualTransaction(id, amount, direction,
                        if (direction == TxDirection.DEPOSIT) TxNature.INCOME else TxNature.EXPENSE,
                        null, "ثبت دستی از پیامک بانک", r.occurredAtMillis ?: System.currentTimeMillis(), "بانک")
                    nav.popBackStack("home", false)
                }
            }, enabled = accountId != null && r.amountRial != null && r.directionEnum() != ExtractedDirection.UNKNOWN,
                modifier = Modifier.fillMaxWidth()) { Text("ثبت این تراکنش") }
        }
    }
}
