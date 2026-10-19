package ir.kharjyar.app.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.ai.ExperimentalPersianAi
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.nlp.AppCommandRouter
import ir.kharjyar.app.core.nlp.ParseConfidence
import ir.kharjyar.app.core.nlp.ParsedTransaction
import ir.kharjyar.app.core.nlp.ParserAccount
import ir.kharjyar.app.core.nlp.ParserCategory
import ir.kharjyar.app.core.nlp.TransactionParser
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

/**
 * ثبت سریع تراکنش با یک جمله فارسی.
 *
 * کاربر می‌نویسد «۲۵۰ هزار تومن کیک از سوپرمارکت خریدم با حساب روزمره» و
 * برنامه مبلغ، حساب، دسته و تاریخ را درمی‌آورد. نتیجه همیشه اول نمایش داده
 * می‌شود و بدون تأیید کاربر چیزی ثبت نمی‌شود.
 *
 * تمام تحلیل روی خود گوشی انجام می‌شود؛ هیچ داده‌ای به اینترنت نمی‌رود.
 */
@Composable
fun QuickAddScreen(viewModel: AppViewModel, nav: NavHostController) {
    val skin = LocalAppSkin.current
    val scope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var input by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<ParsedTransaction?>(null) }
    var saved by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var analyzeSpokenCommand by remember { mutableStateOf(false) }

    val activeAccounts = accounts.filter { !it.archived }
    val context = LocalContext.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // آیا این گوشی اصلاً برنامه تبدیل گفتار به متن دارد؟
    val voiceAvailable = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .resolveActivity(context.packageManager) != null
    }

    /**
     * تبدیل گفتار به متن با برنامه سیستمی اندروید.
     * چون ضبط صدا را همان برنامه انجام می‌دهد، خرج‌یار به مجوز میکروفون نیاز ندارد.
     */
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
        if (!spoken.isNullOrBlank()) {
            // برخی نسخه‌های موتور گفتار گوگل واژه رایج «سوپرمارکت» را به‌اشتباه
            // سانسور می‌کنند (س***مارکت). فقط همین الگوی شناخته‌شده را محلی اصلاح می‌کنیم.
            input = sanitizeSpeechText(spoken)
            voiceError = null
            analyzeSpokenCommand = true
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "بگویید چه خرید یا واریزی داشتید")
        }
        val ok = runCatching { speechLauncher.launch(intent); true }.getOrDefault(false)
        if (!ok) {
            voiceError = "تبدیل گفتار به متن در دسترس نیست؛ لطفاً تایپ کنید."
        }
    }

    // ورود به صفحه «ثبت سریع» بلافاصله موتور گفتار سیستم/گوگل را باز می‌کند؛
    // خود صفحه نیز پشت آن آماده است و در صورت لغو، تایپ دستی در دسترس می‌ماند.
    LaunchedEffect(voiceAvailable) {
        if (voiceAvailable) startVoice()
    }

    fun analyze() {
        // نتیجه بلافاصله دیده شود، نه اینکه پشت صفحه‌کلید بماند
        keyboard?.hide()
        focusManager.clearFocus(force = true)
        AppCommandRouter.obligationAction(input)?.let { action ->
            val obligation = TransactionParser.parse(
                text=input,
                accounts=activeAccounts.map { ParserAccount(it.id,it.title,it.bankName) },
                categories=categories.map { ParserCategory(it.id,it.name) },
                defaultUnit=settings.moneyUnit
            )
            obligation.amountRial?.let { amount ->
                viewModel.prepareObligationDraft(AppViewModel.PendingObligationDraft(action,amount,"",input,input))
                nav.navigate(if(action.startsWith("loan_"))"loans" else "debts")
                return
            }
        }
        AppCommandRouter.route(input)?.let { route -> nav.navigate(route); return }
        parsed = TransactionParser.parse(
            text = input,
            accounts = activeAccounts.map { ParserAccount(it.id, it.title, it.bankName) },
            categories = categories.map { ParserCategory(it.id, it.name) },
            defaultUnit = settings.moneyUnit
        )
        saved = false
    }

    fun analyzeWithAi() {
        keyboard?.hide(); focusManager.clearFocus(force = true)
        aiLoading = true; aiError = null; saved = false
        scope.launch {
            ExperimentalPersianAi.interpretCommand(context, input, categories.filterNot { it.archived }.map { it.name })
                .onSuccess { command ->
                    if (command.action in setOf("loan_borrowed","loan_lent","debt_receivable","debt_payable") && command.amountRial != null && command.route != null) {
                        viewModel.prepareObligationDraft(
                            AppViewModel.PendingObligationDraft(command.action,command.amountRial,command.party,command.accountHint,command.title)
                        )
                        nav.navigate(command.route)
                        aiLoading=false
                        return@onSuccess
                    }
                    if (command.action == "navigate" && command.route != null) {
                        nav.navigate(command.route)
                        aiLoading=false
                        return@onSuccess
                    }
                    val draft = command.transaction ?: return@onSuccess
                    val local = TransactionParser.parse(
                        text=input,
                        accounts=activeAccounts.map { ParserAccount(it.id,it.title,it.bankName) },
                        categories=categories.map { ParserCategory(it.id,it.name) },
                        defaultUnit=settings.moneyUnit
                    )
                    val target = Digits.normalizeForMatch(draft.category)
                    val category = categories.firstOrNull { Digits.normalizeForMatch(it.name) == target }
                    parsed = local.copy(
                        amountRial=draft.amountRial,
                        direction=if(draft.direction=="deposit") TxDirection.DEPOSIT else TxDirection.WITHDRAW,
                        nature=if(draft.direction=="deposit") ir.kharjyar.app.data.db.TxNature.INCOME else ir.kharjyar.app.data.db.TxNature.EXPENSE,
                        categoryId=category?.id ?: local.categoryId,
                        categoryName=category?.name ?: local.categoryName,
                        description=draft.description.ifBlank { local.description },
                        warnings=local.warnings.filterNot { it.contains("مبلغ") || it.contains("نوع تراکنش") }
                    )
                }.onFailure { aiError=it.message ?: "فرمان توسط سرویس هوش مصنوعی تشخیص داده نشد." }
            aiLoading=false
        }
    }

    LaunchedEffect(analyzeSpokenCommand) {
        if (analyzeSpokenCommand) {
            analyzeSpokenCommand = false
            // وقتی دستیار آنلاین عمداً فعال شده و کلید سرویس منتخب وجود دارد،
            // متن حاصل از گفتار مستقیماً برای تبدیل به فرمان ساختاریافته فرستاده می‌شود.
            // در حالت خاموش، همان موتور محلی و آفلاین اجرا می‌شود.
            if (settings.experimentalAiEnabled && ExperimentalPersianAi.hasToken(context)) {
                analyzeWithAi()
            } else {
                analyze()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedButton(onClick = { nav.navigate("smsPaste") }, modifier = Modifier.fillMaxWidth()) {
            Text("جایگذاری و تحلیل پیامک بانکی")
        }

        // ---------- معرفی ----------
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = skin.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "مرکز فرمان هوشمند",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = skin.onBackdrop
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "فرمان را بنویسید یا بگویید. ابتدا موتور محلی و کلیدواژه‌ها استفاده می‌شوند و برای تشخیص پیچیده‌تر می‌توانید سرویس انتخاب‌شده در تنظیمات را به‌کار ببرید. هیچ ثبت مالی بدون پیش‌نمایش و تأیید شما انجام نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = skin.onBackdrop.copy(alpha = 0.75f)
                )
            }
        }

        // ---------- ورودی ----------
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; saved = false },
            label = { Text("چه اتفاقی افتاد؟") },
            placeholder = { Text("امروز ۲۵۰ هزار تومن کیک از سوپرمارکت با حساب بانک ملی خریدم") },
            trailingIcon = {
                // گفتن به‌جای تایپ کردن
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceAvailable) skin.accent.copy(alpha = 0.16f)
                            else skin.onBackdrop.copy(alpha = 0.10f)
                        )
                        .clickable {
                            if (voiceAvailable) startVoice()
                            else voiceError = "موتور تبدیل گفتار به متن روی این گوشی پیدا نشد. " +
                                "برنامه Google (یا Speech Services) را نصب/فعال کنید و " +
                                "زبان فارسی را در تنظیمات آن اضافه کنید."
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "گفتن با صدا",
                        tint = if (voiceAvailable) skin.accent else skin.onBackdrop.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // وضعیت موتور گفتار تا کاربر بداند چرا میکروفون کار می‌کند یا نه
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (voiceAvailable) skin.incomeColor else skin.expenseColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (voiceAvailable && settings.experimentalAiEnabled)
                    "ورودی صوتی فعال است؛ متن گفتار برای سرویس هوش مصنوعی انتخاب‌شده فرستاده می‌شود"
                else if (voiceAvailable)
                    "ورودی صوتی فعال است؛ فرمان فقط با موتور محلی برنامه تحلیل می‌شود"
                else "تبدیل گفتار به متن در دسترس نیست — می‌توانید تایپ کنید",
                style = MaterialTheme.typography.bodySmall,
                color = skin.onBackdrop.copy(alpha = 0.7f)
            )
        }

        voiceError?.let { msg ->
            Text(
                msg,
                style = MaterialTheme.typography.bodySmall,
                color = skin.expenseColor
            )
        }

        // نمونه‌های آماده
        Text("نمونه‌ها:", style = MaterialTheme.typography.labelMedium, color = skin.onBackdrop.copy(alpha = 0.7f))
        listOf(
            "امروز ۲۵۰ هزار تومن کیک از سوپرمارکت با حساب بانک ملی خریدم"
        ).forEach { sample ->
            Text(
                sample,
                style = MaterialTheme.typography.bodySmall,
                color = skin.accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(skin.accent.copy(alpha = 0.10f))
                    .clickable { input = sample; analyze() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = { analyze() },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("تشخیص محلی فرمان") }

        OutlinedButton(
            onClick = { analyzeWithAi() },
            enabled = settings.experimentalAiEnabled && ExperimentalPersianAi.hasToken(context) && input.isNotBlank() && !aiLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if(aiLoading) "در حال تشخیص…" else "تشخیص با هوش مصنوعی ${ExperimentalPersianAi.activeProvider(context).title}") }
        aiError?.let { Text(it,color=skin.expenseColor,style=MaterialTheme.typography.bodySmall) }

        SkinCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Text("فرمان‌های قابل انجام",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
                Text("• ثبت برداشت، واریز و انتقال بین حساب‌های خودم")
                Text("• تشخیص مبلغ، حساب، دسته، تاریخ و توضیح")
                Text("• ورود به تراکنش‌ها، گزارش‌ها، حساب‌ها و دسته‌بندی‌ها")
                Text("• ورود به بدهی‌ها، وام و اقساط، چک‌ها، دارایی‌ها و خودروها")
                Text("• ورود به یادآوری‌ها، یادداشت‌ها، پیامک بانکی، بازار و پشتیبان‌گیری")
                Text("ثبت، ویرایش، حذف و عملیات گروهی همیشه به تأیید نهایی کاربر نیاز دارند.",style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop.copy(alpha=.7f))
            }
        }

        // ---------- نتیجه ----------
        parsed?.let { p ->
            if (p.accountId == null) {
                Text(
                    if (p.direction == TxDirection.DEPOSIT) "مبلغ به کدام حساب واریز شده؟" else "از کدام حساب پرداخت کردید؟",
                    style = MaterialTheme.typography.titleSmall,
                    color = skin.accent
                )
                ComboBox(
                    label = if (p.direction == TxDirection.DEPOSIT) "حساب مقصد واریز" else "حساب پرداخت‌کننده",
                    options = activeAccounts,
                    selected = null,
                    onSelect = { account ->
                        parsed = p.copy(
                            accountId = account.id,
                            accountTitle = account.title,
                            warnings = p.warnings.filterNot { it.contains("حساب") }
                        )
                    },
                    labelOf = { it.title }
                )
            }
            ResultCard(
                parsed = p,
                unit = settings.moneyUnit,
                onEdit = {
                    // انتقال به فرم کامل برای اصلاح دستی
                    nav.navigate("manual")
                },
                onConfirm = {
                    val accId = p.accountId
                    val amount = p.amountRial
                    if (accId != null && amount != null) {
                        scope.launch {
                            val occurredAt = PersianDate.toMillis(p.date, p.hour, p.minute)
                            if (p.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER &&
                                p.transferToOwn && p.targetAccountId != null
                            ) {
                                viewModel.repo.addInternalTransfer(
                                    fromAccountId = accId,
                                    toAccountId = p.targetAccountId,
                                    amountRial = amount,
                                    description = p.description,
                                    occurredAt = occurredAt
                                )
                            } else {
                                viewModel.repo.addManualTransaction(
                                    accountId = accId,
                                    amountRial = amount,
                                    direction = p.direction,
                                    nature = p.nature,
                                    categoryId = if (p.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER) null else p.categoryId,
                                    description = p.description,
                                    occurredAt = occurredAt,
                                    counterparty = if (p.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER) "حساب شخص دیگر" else ""
                                )
                            }
                            saved = true
                            input = ""
                            parsed = null
                        }
                    }
                }
            )
        }

        if (saved) {
            Text(
                "✓ تراکنش ثبت شد",
                style = MaterialTheme.typography.bodyLarge,
                color = skin.incomeColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(90.dp))
    }
}

/** کارت پیش‌نمایش نتیجه تحلیل، پیش از ثبت. */
@Composable
private fun ResultCard(
    parsed: ParsedTransaction,
    unit: ir.kharjyar.app.core.money.MoneyUnit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit
) {
    val skin = LocalAppSkin.current
    val isDeposit = parsed.direction == TxDirection.DEPOSIT
    val tint = if (isDeposit) skin.incomeColor else skin.expenseColor

    SkinCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when (parsed.confidence) {
                                ParseConfidence.HIGH -> skin.incomeColor
                                ParseConfidence.MEDIUM -> skin.accent
                                ParseConfidence.LOW -> skin.expenseColor
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "برداشت من از جمله شما",
                    style = MaterialTheme.typography.titleSmall,
                    color = skin.onBackdrop
                )
            }
            Spacer(Modifier.height(12.dp))

            InfoRow("مبلغ", parsed.amountRial?.let { Money.format(it, unit) } ?: "— مشخص نشد", tint)
            InfoRow(
                "نوع",
                when (parsed.nature) {
                    ir.kharjyar.app.data.db.TxNature.TRANSFER -> "انتقال وجه"
                    else -> if (isDeposit) "واریز" else "برداشت"
                },
                skin.onBackdrop
            )
            InfoRow("از حساب", parsed.accountTitle ?: "— انتخاب نشده", skin.onBackdrop)
            if (parsed.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER) {
                InfoRow(
                    "به حساب",
                    if (parsed.transferToOwn) parsed.targetAccountTitle ?: "— انتخاب نشده" else "حساب شخص دیگر",
                    skin.onBackdrop
                )
            }
            InfoRow("دسته", parsed.categoryName ?: "بدون دسته", skin.onBackdrop)
            InfoRow(
                "تاریخ",
                "${parsed.date.dayOfWeekName()} ${Digits.toPersian(parsed.date.day.toString())} ${parsed.date.monthName()}" +
                    if (!parsed.dateExplicit) " (امروز)" else "",
                skin.onBackdrop
            )
            if (parsed.description.isNotBlank()) {
                InfoRow("شرح", parsed.description, skin.onBackdrop)
            }

            // هشدارها
            if (parsed.warnings.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                parsed.warnings.forEach { w ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = skin.expenseColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            w,
                            style = MaterialTheme.typography.bodySmall,
                            color = skin.expenseColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onConfirm,
                    enabled = parsed.isComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ثبت کن")
                }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("فرم کامل")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    val skin = LocalAppSkin.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = skin.onBackdrop.copy(alpha = 0.65f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}


/** اصلاح خروجی‌های سانسورشده/اشتباه رایج موتور گفتار، بدون تغییر متن‌های دیگر. */
internal fun sanitizeSpeechText(text: String): String = text
    .replace(Regex("س\\s*[\\*＊٭•·_\\-]{2,}\\s*مارکت", RegexOption.IGNORE_CASE), "سوپرمارکت")
    .replace(Regex("سوپر\\s+مارکت"), "سوپرمارکت")
