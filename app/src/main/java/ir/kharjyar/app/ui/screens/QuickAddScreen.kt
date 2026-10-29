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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
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
import ir.kharjyar.app.core.nlp.AssetCommandTaxonomy
import ir.kharjyar.app.core.nlp.ParseConfidence
import ir.kharjyar.app.core.nlp.ParsedTransaction
import ir.kharjyar.app.core.nlp.ParserAccount
import ir.kharjyar.app.core.nlp.ParserCategory
import ir.kharjyar.app.core.nlp.TransactionParser
import ir.kharjyar.app.core.nlp.SmartMutationTarget
import ir.kharjyar.app.core.nlp.SmartRecordMutationResolver
import ir.kharjyar.app.core.nlp.SettingsCommand
import ir.kharjyar.app.core.nlp.SettingsCommandParser
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.PersianDateField
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
    val transactions by viewModel.allTransactions.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val debtPeople by viewModel.debtPeople.collectAsState()
    val assets by viewModel.assets.collectAsState()

    var input by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<ParsedTransaction?>(null) }
    var saved by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var analyzeSpokenCommand by remember { mutableStateOf(false) }
    var missingAmountInput by remember { mutableStateOf("") }
    var showAccountPicker by remember { mutableStateOf(false) }
    var autoRegisterPending by remember { mutableStateOf(false) }
    var mutationTarget by remember { mutableStateOf<SmartMutationTarget?>(null) }
    var mutationAction by remember { mutableStateOf<String?>(null) }
    var mutationError by remember { mutableStateOf<String?>(null) }
    var settingsCommand by remember { mutableStateOf<SettingsCommand?>(null) }
    var settingsResult by remember { mutableStateOf<String?>(null) }

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
            // به کاربر اجازه مکث طبیعی میان بخش‌های یک فرمان طولانی داده می‌شود.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3_500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15_000L)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "فرمان کامل را بگویید؛ هنگام مکث منتظر می‌مانم")
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

    fun handleSettingsCommand(command:SettingsCommand):Boolean {
        settingsResult=null
        when(command) {
            SettingsCommand.Backup->{nav.navigate("backup");return true}
            SettingsCommand.ManageCategories->{nav.navigate("categories");return true}
            is SettingsCommand.AddCategory->{viewModel.prepareFeatureCommand(AppViewModel.PendingFeatureCommand("category_create",null,command.name,input));nav.navigate("categories");return true}
            else->settingsCommand=command
        }
        return true
    }

    fun analyze() {
        // نتیجه بلافاصله دیده شود، نه اینکه پشت صفحه‌کلید بماند
        keyboard?.hide()
        focusManager.clearFocus(force = true)
        SettingsCommandParser.parse(input)?.let { handleSettingsCommand(it);return }
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
        AppCommandRouter.featureAction(input)?.let { (action,route) ->
            val featureParsed=TransactionParser.parse(input,activeAccounts.map{ParserAccount(it.id,it.title,it.bankName)},categories.map{ParserCategory(it.id,it.name)},settings.moneyUnit)
            viewModel.prepareFeatureCommand(AppViewModel.PendingFeatureCommand(action,featureParsed.amountRial,input,input,accountHint=input,quantity=AssetCommandTaxonomy.quantity(input),occurredAt=featureParsed.takeIf{it.dateExplicit}?.let{p->PersianDate.toMillis(p.date,p.hour,p.minute)}))
            nav.navigate(route)
            return
        }
        AppCommandRouter.route(input)?.let { route -> nav.navigate(route); return }
        val result = TransactionParser.parse(
            text = input,
            accounts = activeAccounts.map { ParserAccount(it.id, it.title, it.bankName) },
            categories = categories.map { ParserCategory(it.id, it.name) },
            defaultUnit = settings.moneyUnit
        )
        parsed = result
        showAccountPicker = result.accountId == null && activeAccounts.isNotEmpty()
        saved = false
    }

    fun analyzeWithAi() {
        keyboard?.hide(); focusManager.clearFocus(force = true)
        SettingsCommandParser.parse(input)?.let { handleSettingsCommand(it);return }
        aiLoading = true; aiError = null; mutationError=null; saved = false; parsed=null
        scope.launch {
            ExperimentalPersianAi.interpretCommand(context, input, categories.filterNot { it.archived }.map { it.name })
                .onSuccess { command ->
                    // واژه‌های قطعی نوع دارایی بر تصمیم مدل اولویت دارند تا خانه/زمین به ملک
                    // و هر نوع وسیله نقلیه به بخش مستقل خودروها هدایت شود.
                    val deterministicFeature=AppCommandRouter.featureAction(input)
                    val resolvedAction=deterministicFeature?.first?:command.action
                    val resolvedRoute=deterministicFeature?.second?:command.route
                    if(command.action=="record_delete"||command.action=="record_edit") {
                        val target=SmartRecordMutationResolver.resolve(command.entityType,input,command.amountRial,command.quantity,command.accountHint,command.title,command.party,transactions,activeAccounts,loans,debts,debtPeople,assets)
                        if(target==null) mutationError="مورد یکتایی با این مشخصات پیدا نشد؛ نام، مبلغ، حساب یا تاریخ را دقیق‌تر بگویید."
                        else { mutationTarget=target;mutationAction=command.action;mutationError=null }
                        aiLoading=false
                        return@onSuccess
                    }
                    if (command.action in setOf("loan_borrowed","loan_lent","debt_receivable","debt_payable") && command.amountRial != null && command.route != null) {
                        viewModel.prepareObligationDraft(
                            AppViewModel.PendingObligationDraft(command.action,command.amountRial,command.party,command.accountHint,command.title)
                        )
                        nav.navigate(command.route)
                        aiLoading=false
                        return@onSuccess
                    }
                    if (resolvedAction in setOf("asset_record","reminder_create","check_issued","check_received","note_create","vehicle_record","installment_payment","civic_record") && resolvedRoute != null) {
                        val specializedDate=TransactionParser.parse(input,activeAccounts.map{ParserAccount(it.id,it.title,it.bankName)},categories.map{ParserCategory(it.id,it.name)},settings.moneyUnit)
                        viewModel.prepareFeatureCommand(
                            AppViewModel.PendingFeatureCommand(
                                resolvedAction,command.amountRial,command.title,input,command.party,command.accountHint,command.quantity?:AssetCommandTaxonomy.quantity(input),
                                command.dueDate,command.sayadId,command.serialNumber,command.bankName,command.nationalId,command.chequeAccountNumber,command.chequeIban,
                                specializedDate.takeIf{it.dateExplicit}?.let{p->PersianDate.toMillis(p.date,p.hour,p.minute)}
                            )
                        )
                        nav.navigate(resolvedRoute)
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
                    val completed = local.copy(
                        amountRial=draft.amountRial,
                        direction=if(draft.direction=="deposit") TxDirection.DEPOSIT else TxDirection.WITHDRAW,
                        nature=if(draft.direction=="deposit") ir.kharjyar.app.data.db.TxNature.INCOME else ir.kharjyar.app.data.db.TxNature.EXPENSE,
                        categoryId=category?.id ?: local.categoryId,
                        categoryName=category?.name ?: local.categoryName,
                        description=draft.description.ifBlank { local.description },
                        warnings=local.warnings.filterNot { it.contains("مبلغ") || it.contains("نوع تراکنش") }
                    )
                    parsed=completed
                    showAccountPicker=completed.accountId==null&&activeAccounts.isNotEmpty()
                }.onFailure { aiError=it.message ?: "فرمان توسط سرویس هوش مصنوعی تشخیص داده نشد.";autoRegisterPending=false }
            aiLoading=false
        }
    }

    fun persistCompletedTransaction(p:ParsedTransaction) {
        val accId=p.accountId?:return
        val amount=p.amountRial?:return
        scope.launch {
            val sourceAccount=accounts.firstOrNull{it.id==accId}
            val isWithdrawal=p.direction==TxDirection.WITHDRAW||p.nature==ir.kharjyar.app.data.db.TxNature.TRANSFER
            val available=sourceAccount?.let{ir.kharjyar.app.core.balance.AccountBalance.estimate(it,transactions).rial}
            if(isWithdrawal&&available!=null&&amount>available){
                aiError="مانده حساب «${sourceAccount.title}» کمتر از مبلغ برداشت است؛ مانده فعلی ${Money.format(available,settings.moneyUnit)} است"
                autoRegisterPending=false
                return@launch
            }
            val occurredAt=PersianDate.toMillis(p.date,p.hour,p.minute)
            if(p.nature==ir.kharjyar.app.data.db.TxNature.TRANSFER&&p.transferToOwn&&p.targetAccountId!=null) {
                viewModel.repo.addInternalTransfer(accId,p.targetAccountId,amount,p.description,occurredAt)
            } else {
                viewModel.repo.addManualTransaction(accId,amount,p.direction,p.nature,if(p.nature==ir.kharjyar.app.data.db.TxNature.TRANSFER)null else p.categoryId,p.description,occurredAt,if(p.nature==ir.kharjyar.app.data.db.TxNature.TRANSFER)"حساب شخص دیگر" else "")
            }
            saved=true;input="";parsed=null;autoRegisterPending=false
        }
    }

    LaunchedEffect(parsed,autoRegisterPending) {
        val p=parsed
        val ready=p!=null&&p.isComplete&&p.dateExplicit&&
            (p.nature==ir.kharjyar.app.data.db.TxNature.TRANSFER||p.categoryId!=null)&&
            p.warnings.none{it.contains("نوع تراکنش")}
        if(autoRegisterPending&&ready) persistCompletedTransaction(p!!)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // هدر کوتاه و تصویری؛ جزئیات فنی از مسیر اصلی کاربر حذف شده‌اند.
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(skin.accent, skin.accent.copy(alpha=.62f))))
                .padding(20.dp)
        ) {
            Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color.White.copy(alpha=.18f)),contentAlignment=Alignment.Center) {
                        Icon(Icons.Filled.AutoAwesome,null,tint=androidx.compose.ui.graphics.Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("مرکز فرمان هوشمند",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=androidx.compose.ui.graphics.Color.White)
                        Text("بگویید چه کاری برایتان انجام دهم",style=MaterialTheme.typography.bodyMedium,color=androidx.compose.ui.graphics.Color.White.copy(alpha=.86f))
                    }
                }
            }
        }

        SkinCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("فرمان شما",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold,color=skin.onBackdrop)
                OutlinedTextField(
                    value=input,
                    onValueChange={input=it;saved=false},
                    placeholder={Text("مثلاً: ۱۰ گرم طلا به قیمت ۵۰ میلیون خریدم")},
                    trailingIcon={
                        Box(Modifier.padding(end=4.dp).size(44.dp).clip(CircleShape)
                            .background(if(voiceAvailable)skin.accent.copy(alpha=.15f)else skin.onBackdrop.copy(alpha=.08f))
                            .clickable{if(voiceAvailable)startVoice()else voiceError="موتور تبدیل گفتار به متن روی این گوشی در دسترس نیست؛ لطفاً تایپ کنید."},contentAlignment=Alignment.Center){
                            Icon(Icons.Filled.Mic,"گفتن با صدا",tint=if(voiceAvailable)skin.accent else skin.onBackdrop.copy(alpha=.4f))
                        }
                    },
                    modifier=Modifier.fillMaxWidth(),minLines=3
                )
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if(voiceAvailable)skin.incomeColor else skin.expenseColor))
                    Spacer(Modifier.width(6.dp))
                    Text(if(voiceAvailable)"برای گفتن فرمان، میکروفون را لمس کنید" else "ورودی صوتی در دسترس نیست؛ فرمان را تایپ کنید",style=MaterialTheme.typography.labelSmall,color=skin.onBackdrop.copy(alpha=.62f))
                }
                voiceError?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=skin.expenseColor)}
            }
        }

        // فقط یک تصمیم اصلی در هر ردیف: تحلیل هوشمند، سپس گزینه‌های جایگزین.
        Button(
            onClick={analyzeWithAi()},
            enabled=settings.experimentalAiEnabled&&ExperimentalPersianAi.hasToken(context)&&input.isNotBlank()&&!aiLoading,
            modifier=Modifier.fillMaxWidth().height(52.dp),
            shape=RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.AutoAwesome,null,Modifier.size(19.dp));Spacer(Modifier.width(8.dp))
            Text(if(aiLoading)"در حال بررسی فرمان…" else "تشخیص هوش مصنوعی",fontWeight=FontWeight.Bold)
        }
        if(!settings.experimentalAiEnabled||!ExperimentalPersianAi.hasToken(context)) {
            Text("برای تشخیص هوش مصنوعی، سرویس را از تنظیمات هوش مصنوعی فعال کنید.",style=MaterialTheme.typography.labelSmall,color=skin.onBackdrop.copy(alpha=.62f))
        }
        if(settings.aiAutoRegisterEnabled) {
            OutlinedButton(
                onClick={autoRegisterPending=true;analyzeWithAi()},
                enabled=settings.experimentalAiEnabled&&ExperimentalPersianAi.hasToken(context)&&input.isNotBlank()&&!aiLoading,
                modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)
            ){Icon(Icons.Filled.Check,null,Modifier.size(18.dp));Spacer(Modifier.width(7.dp));Text("ثبت خودکار")}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton({analyze()},enabled=input.isNotBlank(),modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Text("تشخیص محلی ( آفلاین )") }
            OutlinedButton({nav.navigate("smsPaste")},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Text("تحلیل پیامک بانکی") }
        }
        aiError?.let{Text(it,color=skin.expenseColor,style=MaterialTheme.typography.bodySmall)}
        mutationError?.let{Text(it,color=skin.expenseColor,style=MaterialTheme.typography.bodySmall)}
        settingsResult?.let{Text(it,color=skin.incomeColor,style=MaterialTheme.typography.bodyMedium,fontWeight=FontWeight.Bold)}

        SkinCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Text("فرمان‌های تنظیمات",fontWeight=FontWeight.Bold,color=skin.onBackdrop)
                Text("• تم روشن، تیره یا خودکار؛ انتخاب تم ساکورا، بنفش، نیلوفر، اقیانوس، طلایی و مینیمال",style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop.copy(.72f))
                Text("• «تم را عوض کن» برای انتخاب تصادفی یک تم متفاوت",style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop.copy(.72f))
                Text("• تغییر واحد پول به ریال یا تومان و بزرگ/کوچک/استاندارد کردن فونت",style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop.copy(.72f))
                Text("• گرفتن بکاپ، مدیریت دسته‌بندی‌ها یا ساخت دسته جدید با نام",style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop.copy(.72f))
            }
        }

        if(input.isBlank()&&parsed==null) {
            Text("برای شروع، یکی از نمونه‌ها را انتخاب کنید",style=MaterialTheme.typography.labelMedium,color=skin.onBackdrop.copy(alpha=.68f))
            listOf(
                "امروز ۲۵۰ هزار تومن کیک از سوپرمارکت با حساب بانک ملی خریدم",
                "۱۰ گرم طلا به قیمت ۵۰ میلیون خریدم"
            ).forEach { sample ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(skin.accent.copy(alpha=.08f))
                    .clickable{input=sample}.padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome,null,tint=skin.accent,modifier=Modifier.size(17.dp));Spacer(Modifier.width(8.dp))
                    Text(sample,style=MaterialTheme.typography.bodySmall,color=skin.onBackdrop)
                }
            }
        }

        // ---------- نتیجه ----------
        parsed?.let { p ->
            val hasMissing = p.amountRial==null || p.accountId==null || !p.dateExplicit ||
                (p.nature!=ir.kharjyar.app.data.db.TxNature.TRANSFER && p.categoryId==null) ||
                p.warnings.any{it.contains("نوع تراکنش")}
            if(hasMissing) Text("برای تکمیل ثبت به چند پاسخ نیاز دارم",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,color=skin.accent)
            if (p.amountRial == null) {
                Text("مبلغ تراکنش چقدر بود؟",style=MaterialTheme.typography.titleSmall)
                OutlinedTextField(missingAmountInput,{missingAmountInput=it},Modifier.fillMaxWidth(),label={Text("مبلغ با واحد ${if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)"تومان" else "ریال"}")},singleLine=true)
                OutlinedButton({
                    Money.inputToRial(missingAmountInput,settings.moneyUnit)?.takeIf{it>0}?.let{amount->parsed=p.copy(amountRial=amount,warnings=p.warnings.filterNot{it.contains("مبلغ")});missingAmountInput=""}
                },enabled=(Money.inputToRial(missingAmountInput,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("ثبت مبلغ در پیش‌نویس")}
            }
            if (p.warnings.any { it.contains("نوع تراکنش مشخص نبود") }) {
                Text("این مبلغ واریز بوده یا برداشت؟",style=MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({parsed=p.copy(direction=TxDirection.DEPOSIT,nature=ir.kharjyar.app.data.db.TxNature.INCOME,warnings=p.warnings.filterNot{it.contains("نوع تراکنش")})},Modifier.weight(1f)){Text("واریز")}
                    OutlinedButton({parsed=p.copy(direction=TxDirection.WITHDRAW,nature=ir.kharjyar.app.data.db.TxNature.EXPENSE,warnings=p.warnings.filterNot{it.contains("نوع تراکنش")})},Modifier.weight(1f)){Text("برداشت")}
                }
            }
            if (p.accountId == null) {
                Text(
                    if (p.direction == TxDirection.DEPOSIT) "مبلغ به کدام حساب واریز شده؟" else "از کدام حساب پرداخت کردید؟",
                    style = MaterialTheme.typography.titleSmall,
                    color = skin.accent
                )
                OutlinedButton(
                    onClick={showAccountPicker=true},
                    enabled=activeAccounts.isNotEmpty(),
                    modifier=Modifier.fillMaxWidth()
                ){Text(if(activeAccounts.isEmpty())"ابتدا یک حساب بسازید" else "انتخاب حساب از فهرست") }
            }
            if (p.nature != ir.kharjyar.app.data.db.TxNature.TRANSFER && p.categoryId == null) {
                Text("این تراکنش مربوط به کدام دسته‌بندی است؟",style=MaterialTheme.typography.titleSmall,color=skin.accent)
                ComboBox("دسته‌بندی",categories.filterNot{it.archived},null,{category->parsed=p.copy(categoryId=category.id,categoryName=category.name,warnings=p.warnings.filterNot{it.contains("دسته")})},labelOf={it.name})
            }
            if (!p.dateExplicit) {
                Text("تراکنش در چه تاریخی انجام شد؟",style=MaterialTheme.typography.titleSmall,color=skin.accent)
                PersianDateField(p.date,{date->parsed=p.copy(date=date,dateExplicit=true)})
                TextButton({parsed=p.copy(date=PersianDate.today(),dateExplicit=true)}){Text("امروز بود")}
            }
            ResultCard(
                parsed = p,
                unit = settings.moneyUnit,
                readyToConfirm = p.isComplete && p.dateExplicit &&
                    (p.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER || p.categoryId != null) &&
                    p.warnings.none { it.contains("نوع تراکنش") },
                onEdit = {
                    // انتقال به فرم کامل برای اصلاح دستی
                    nav.navigate("manual")
                },
                onConfirm = { persistCompletedTransaction(p) }
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

    mutationTarget?.let { target ->
        val deleting=mutationAction=="record_delete"
        AlertDialog(
            onDismissRequest={mutationTarget=null;mutationAction=null},
            title={Text(if(deleting)"تأیید حذف" else "تأیید ویرایش")},
            text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(if(deleting)"آیا مورد زیر حذف شود؟" else "آیا فرم ویرایش مورد زیر باز شود؟");Text(target.label,fontWeight=FontWeight.Bold);Text(target.detail,style=MaterialTheme.typography.bodySmall);if(deleting)Text("این عملیات فقط پس از تأیید شما اجرا می‌شود.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}},
            confirmButton={TextButton({
                mutationTarget=null;mutationAction=null
                if(deleting) scope.launch {
                    when(target.type){
                        "transaction"->viewModel.repo.txDao.delete(target.id)
                        "asset"->{viewModel.repo.db.assetDao().deleteTrades(target.id);viewModel.repo.db.assetDao().delete(target.id)}
                        "debt"->{viewModel.repo.db.debtDao().deletePaymentsOf(target.id);viewModel.debts.value.firstOrNull{it.id==target.id}?.let{viewModel.repo.db.debtDao().deleteDebt(it)}}
                        "loan"->{viewModel.repo.db.loanDao().deleteInstallments(target.id);viewModel.repo.db.loanDao().deleteLoan(target.id)}
                    }
                    saved=true;input=""
                } else {
                    when(target.type){
                        "transaction"->nav.navigate("tx/${target.id}")
                        "asset"->{viewModel.prepareRecordEdit("asset",target.id);nav.navigate("assets")}
                        "debt"->{viewModel.prepareRecordEdit("debt",target.id);nav.navigate("debts")}
                        "loan"->{viewModel.prepareRecordEdit("loan",target.id);nav.navigate("loans")}
                    }
                }
            }){Text(if(deleting)"بله، حذف شود" else "بازکردن فرم ویرایش",color=if(deleting)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)}},
            dismissButton={TextButton({mutationTarget=null;mutationAction=null}){Text("انصراف")}}
        )
    }

    settingsCommand?.let { command ->
        val description=when(command){
            is SettingsCommand.Brightness->"حالت نمایش برنامه به ${when(command.mode){ir.kharjyar.app.data.prefs.ThemeMode.DARK->"تیره";ir.kharjyar.app.data.prefs.ThemeMode.LIGHT->"روشن";else->"خودکار مطابق سیستم"}} تغییر کند؟"
            is SettingsCommand.Theme->if(command.random)"یک تم متفاوت به‌صورت تصادفی انتخاب و اعمال شود؟" else "تم انتخاب‌شده روی کل برنامه اعمال شود؟"
            is SettingsCommand.Currency->"واحد نمایش مبلغ‌ها به ${if(command.unit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)"تومان" else "ریال"} تغییر کند؟"
            is SettingsCommand.Font->"اندازه فونت برنامه ${if(command.exact==100)"به حالت استاندارد برگردد" else if(command.delta>0)"بزرگ‌تر شود" else "کوچک‌تر شود"}؟"
            is SettingsCommand.RenameCategory->"نام دسته‌بندی «${command.oldName}» به «${command.newName}» تغییر کند؟"
            is SettingsCommand.ToggleCategory->"دسته‌بندی «${command.name}» ${if(command.enabled)"فعال" else "غیرفعال"} شود؟"
            is SettingsCommand.DeleteCategory->"دسته‌بندی «${command.name}» حذف شود؟ تراکنش‌ها حذف نمی‌شوند و فقط اتصال دسته از آن‌ها برداشته می‌شود."
            else->"این تغییر اعمال شود؟"
        }
        AlertDialog(onDismissRequest={settingsCommand=null},title={Text("تأیید تغییر تنظیمات")},text={Text(description)},confirmButton={TextButton({
            settingsCommand=null
            scope.launch {
                when(command){
                    is SettingsCommand.Brightness->viewModel.settingsRepo.setThemeMode(command.mode)
                    is SettingsCommand.Theme->{val selected=command.palette?:Palette.entries.filter{it!=settings.palette}.random();viewModel.settingsRepo.setPalette(selected)}
                    is SettingsCommand.Currency->viewModel.settingsRepo.setMoneyUnit(command.unit)
                    is SettingsCommand.Font->viewModel.settingsRepo.setAppFontScale(command.exact?:settings.appFontScale.plus(command.delta))
                    is SettingsCommand.RenameCategory->{
                        val target=categories.firstOrNull{Digits.normalizeForMatch(it.name)==Digits.normalizeForMatch(command.oldName)}
                        if(target==null)settingsResult="دسته‌بندی «${command.oldName}» پیدا نشد" else if(categories.any{it.id!=target.id&&Digits.normalizeForMatch(it.name)==Digits.normalizeForMatch(command.newName)})settingsResult="دسته‌بندی «${command.newName}» از قبل وجود دارد" else {viewModel.repo.categoryDao.update(target.copy(name=command.newName));settingsResult="نام دسته‌بندی تغییر کرد"}
                    }
                    is SettingsCommand.ToggleCategory->{val target=categories.firstOrNull{Digits.normalizeForMatch(it.name)==Digits.normalizeForMatch(command.name)};if(target==null)settingsResult="دسته‌بندی «${command.name}» پیدا نشد" else {viewModel.repo.categoryDao.update(target.copy(archived=!command.enabled));settingsResult="وضعیت دسته‌بندی تغییر کرد"}}
                    is SettingsCommand.DeleteCategory->{val target=categories.firstOrNull{Digits.normalizeForMatch(it.name)==Digits.normalizeForMatch(command.name)};if(target==null)settingsResult="دسته‌بندی «${command.name}» پیدا نشد" else {viewModel.repo.categoryDao.detachTransactions(target.id);viewModel.repo.categoryDao.deleteRulesOfCategory(target.id);viewModel.repo.categoryDao.delete(target.id);settingsResult="دسته‌بندی حذف شد"}}
                    else->Unit
                }
                if(settingsResult==null)settingsResult="تنظیم درخواستی با موفقیت اعمال شد";input=""
            }
        }){Text("اعمال تغییر")}},dismissButton={TextButton({settingsCommand=null}){Text("انصراف")}})
    }

    if(showAccountPicker && parsed?.accountId==null) {
        val current=parsed
        AlertDialog(
            onDismissRequest={showAccountPicker=false},
            title={Text(if(current?.direction==TxDirection.DEPOSIT)"واریز به کدام حساب؟" else "برداشت از کدام حساب؟")},
            text={
                Column(Modifier.fillMaxWidth().heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text("حساب را انتخاب کنید تا خودکار در پیش‌نویس تراکنش قرار بگیرد.",style=MaterialTheme.typography.bodySmall)
                    activeAccounts.forEach { account ->
                        OutlinedButton(
                            onClick={
                                current?.let { p -> parsed=p.copy(accountId=account.id,accountTitle=account.title,warnings=p.warnings.filterNot{it.contains("حساب")}) }
                                showAccountPicker=false
                            },
                            modifier=Modifier.fillMaxWidth()
                        ) { Column(Modifier.fillMaxWidth()) { Text(account.title);if(account.bankName.isNotBlank())Text(account.bankName,style=MaterialTheme.typography.labelSmall) } }
                    }
                }
            },
            confirmButton={},
            dismissButton={TextButton({showAccountPicker=false}){Text("بعداً انتخاب می‌کنم")}}
        )
    }
}

/** کارت پیش‌نمایش نتیجه تحلیل، پیش از ثبت. */
@Composable
private fun ResultCard(
    parsed: ParsedTransaction,
    unit: ir.kharjyar.app.core.money.MoneyUnit,
    readyToConfirm: Boolean,
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
                    enabled = readyToConfirm,
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
