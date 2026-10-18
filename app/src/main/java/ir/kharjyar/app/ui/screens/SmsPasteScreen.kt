package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var body by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ExtractionResult?>(null) }
    var accountId by remember { mutableStateOf<Long?>(null) }
    var nature by remember { mutableIntStateOf(TxNature.EXPENSE) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val active = accounts.filter { !it.archived }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("متن کامل پیامک بانک را اینجا جایگذاری کنید. تحلیل روی گوشی انجام می‌شود.")
        OutlinedTextField(body, { body = it }, label = { Text("متن پیامک بانکی") }, minLines = 7, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            // با پایان ورود متن، فوکوس را هم آزاد می‌کنیم؛ hide به‌تنهایی در بعضی
            // کیبوردها (از جمله MIUI) با recomposition دوباره صفحه‌کلید را باز می‌کند.
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val extracted = Extractor.autoExtract(body)
            result = extracted
            nature = if(extracted.directionEnum()==ExtractedDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE
            categoryId = null
            description = ""
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
            val direction = if (r.directionEnum() == ExtractedDirection.DEPOSIT) TxDirection.DEPOSIT else TxDirection.WITHDRAW
            Text("این تراکنش بابت چه بوده؟",style=MaterialTheme.typography.titleSmall)
            NaturePicker(nature=nature,direction=direction,onNature={nature=it;categoryId=null},onDirection={},showDirection=false)
            if(nature!=TxNature.TRANSFER){
                CategoryPicker(categories=categories,selectedId=categoryId,nature=nature,onCreate={name->scope.launch{categoryId=viewModel.repo.categoryDao.insert(ir.kharjyar.app.data.db.CategoryEntity(name=name,colorArgb=0xFF6C8AE4))}}){categoryId=it}
            }
            OutlinedTextField(description,{description=it},label={Text(if(direction==TxDirection.DEPOSIT)"توضیح واریز (اختیاری)" else "توضیح برداشت (اختیاری)")},supportingText={Text("مثلاً حقوق مهر، خرید سوپرمارکت، دریافت وام یا تسویه طلب")},modifier=Modifier.fillMaxWidth(),minLines=2)
            Text("پیش از ثبت، حساب، نوع جریان مالی و بابت تراکنش را بررسی کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                val id = accountId ?: return@Button
                val amount = r.amountRial ?: return@Button
                scope.launch {
                    viewModel.repo.addManualTransaction(id, amount, direction,nature,
                        if(nature==TxNature.TRANSFER)null else categoryId, description.trim().ifBlank{"ثبت دستی از پیامک بانک"}, r.occurredAtMillis ?: System.currentTimeMillis(), "بانک")
                    nav.popBackStack("home", false)
                }
            }, enabled = accountId != null && r.amountRial != null && r.directionEnum() != ExtractedDirection.UNKNOWN,
                modifier = Modifier.fillMaxWidth()) { Text("تأیید و ثبت تراکنش") }
        }
    }
}
