package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ai.AiTransactionDraft
import ir.kharjyar.app.ai.ExperimentalPersianAi
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

/** رابط مستقل آزمایش Groq؛ مدل فقط پیش‌نویس می‌سازد و ثبت پس از تأیید صریح کاربر انجام می‌شود. */
@Composable
fun ExperimentalAiSettingsSection(viewModel: AppViewModel, enabled: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeAccounts = accounts.filterNot { it.archived }
    val activeCategories = categories.filterNot { it.archived }
    var tokenDraft by remember { mutableStateOf("") }
    var tokenSaved by remember { mutableStateOf(ExperimentalPersianAi.hasToken(context)) }
    var request by remember { mutableStateOf("امروز ۲۵۰ هزار تومان برای خرید مواد غذایی پرداخت کردم") }
    var draft by remember { mutableStateOf<AiTransactionDraft?>(null) }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var transactionDate by remember { mutableStateOf(PersianDate.today()) }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    SectionCard("دستیار هوشمند آزمایشی") {
        Text("این نسخه برای آزمایش از Qwen روی سهمیه رایگان Groq استفاده می‌کند. فقط متن درخواست و نام دسته‌بندی‌ها ارسال می‌شود؛ شماره حساب، موجودی، پیامک و تاریخچه مالی ارسال نمی‌شود.")
        Text("کلید رایگان را در console.groq.com بسازید. کلید روی گوشی با Android Keystore رمزگذاری و از بکاپ حذف می‌شود.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SkinCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("فعال‌سازی اختیاری")
                    Text(if(enabled)"دستیار فعال است" else "خاموش است و هیچ درخواستی ارسال نمی‌کند", style=MaterialTheme.typography.labelSmall)
                }
                Switch(checked=enabled, enabled=tokenSaved, onCheckedChange={v->scope.launch{viewModel.settingsRepo.setExperimentalAiEnabled(v)}})
            }
        }
        OutlinedTextField(tokenDraft,{tokenDraft=it},Modifier.fillMaxWidth(),label={Text(if(tokenSaved)"جایگزینی کلید Groq" else "کلید API رایگان Groq")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button({ExperimentalPersianAi.saveToken(context,tokenDraft);tokenDraft="";tokenSaved=ExperimentalPersianAi.hasToken(context);error=""},enabled=tokenDraft.isNotBlank(),modifier=Modifier.weight(1f)){Text("ذخیره امن کلید")}
            OutlinedButton({ExperimentalPersianAi.clearToken(context);tokenSaved=false;draft=null;scope.launch{viewModel.settingsRepo.setExperimentalAiEnabled(false)}},enabled=tokenSaved,modifier=Modifier.weight(1f)){Text("حذف کلید")}
        }
        OutlinedTextField(request,{request=it;draft=null},Modifier.fillMaxWidth(),label={Text("درخواست ثبت تراکنش")},minLines=3,enabled=enabled&&!loading)
        Button(onClick={
            loading=true;error="";message="";draft=null
            scope.launch {
                ExperimentalPersianAi.interpretTransaction(context,request,activeCategories.map{it.name})
                    .onSuccess { value ->
                        draft=value
                        selectedAccount=selectedAccount ?: activeAccounts.firstOrNull()
                        val target=Digits.normalizeForMatch(value.category)
                        selectedCategory=activeCategories.firstOrNull{Digits.normalizeForMatch(it.name)==target}
                            ?: activeCategories.firstOrNull{Digits.normalizeForMatch(it.name).contains(target)||target.contains(Digits.normalizeForMatch(it.name))}
                    }.onFailure{error=it.message?:"خطای ناشناخته"}
                loading=false
            }
        },enabled=enabled&&tokenSaved&&request.isNotBlank()&&!loading,modifier=Modifier.fillMaxWidth()){
            if(loading) CircularProgressIndicator(Modifier.padding(2.dp)) else Text("تحلیل با هوش مصنوعی")
        }

        draft?.let { value ->
            Text("پیش‌نمایش؛ هنوز چیزی ثبت نشده است",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)
            SkinCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text(if(value.direction=="deposit")"واریز" else "برداشت",style=MaterialTheme.typography.titleMedium)
                    Text(Money.format(value.amountRial,settings.moneyUnit),style=MaterialTheme.typography.titleLarge)
                    Text("توضیح: ${value.description.ifBlank{"ثبت با دستیار آزمایشی"}}")
                    if(value.explanation.isNotBlank()) Text(value.explanation,style=MaterialTheme.typography.bodySmall)
                }
            }
            ComboBox("حساب",activeAccounts,selectedAccount,{selectedAccount=it},labelOf={it.title})
            ComboBox("دسته‌بندی",activeCategories,selectedCategory,{selectedCategory=it},labelOf={it.name})
            Text("تاریخ تراکنش",style=MaterialTheme.typography.labelLarge)
            PersianDateField(transactionDate,{transactionDate=it})
            Text("مدل فقط پیشنهاد داده است. حساب، دسته، مبلغ و جهت را بررسی کنید؛ ثبت فقط با دکمه زیر انجام می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
            Button(onClick={
                val account=selectedAccount;val category=selectedCategory
                if(account==null||category==null){error="حساب و دسته‌بندی را انتخاب کنید.";return@Button}
                scope.launch {
                    viewModel.repo.addManualTransaction(
                        accountId=account.id,
                        amountRial=value.amountRial,
                        direction=if(value.direction=="deposit")TxDirection.DEPOSIT else TxDirection.WITHDRAW,
                        nature=if(value.direction=="deposit")TxNature.INCOME else TxNature.EXPENSE,
                        categoryId=category.id,
                        description=value.description.ifBlank{"ثبت با دستیار آزمایشی"},
                        occurredAt=transactionDate.startOfDayMillis()
                    )
                    draft=null;message="تراکنش با تأیید شما ثبت شد.";error=""
                }
            },enabled=selectedAccount!=null&&selectedCategory!=null,modifier=Modifier.fillMaxWidth()){Text("تأیید و ثبت تراکنش")}
        }
        if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.primary)
        if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error)
        Text("اگر Groq از اینترنت شما در دسترس نباشد یا محدودیت منطقه‌ای بدهد، با خاموش‌کردن این گزینه هیچ بخشی از خرج‌یار مختل نمی‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
