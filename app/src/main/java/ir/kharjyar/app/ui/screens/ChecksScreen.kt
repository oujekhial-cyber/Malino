package ir.kharjyar.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DateTimeField
import kotlinx.coroutines.launch
import java.io.File

@Composable fun ChecksScreen(vm: AppViewModel) {
    val checks by vm.checks.collectAsState(); val settings by vm.settings.collectAsState(); val accounts by vm.accounts.collectAsState(); val scope=rememberCoroutineScope(); val context=LocalContext.current
    var direction by remember{mutableStateOf(CheckDirection.ISSUED)};var amount by remember{mutableStateOf("")};var party by remember{mutableStateOf("")};var bank by remember{mutableStateOf("")};var sayad by remember{mutableStateOf("")};var serial by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today())};var imagePath by remember{mutableStateOf("")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null){val dir=File(context.filesDir,"checks").apply{mkdirs()};val file=File(dir,"${System.currentTimeMillis()}.jpg");context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { output -> input.copyTo(output) } };imagePath=file.absolutePath;TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromFilePath(context,uri)).addOnSuccessListener{t->val text=Digits.normalize(t.text);Regex("\\d{16}").find(text)?.value?.let{sayad=it};Regex("\\d{6,12}").findAll(text).firstOrNull()?.value?.let{serial=it}}}}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text("چک‌ها",style=MaterialTheme.typography.headlineSmall);Row{FilterChip(direction==CheckDirection.ISSUED,{direction=CheckDirection.ISSUED},{Text("صادرشده")});Spacer(Modifier.width(8.dp));FilterChip(direction==CheckDirection.RECEIVED,{direction=CheckDirection.RECEIVED},{Text("دریافت‌شده")})}
        OutlinedButton({picker.launch("image/*")},Modifier.fillMaxWidth()){Text(if(imagePath.isBlank())"اسکن/انتخاب تصویر چک" else "✓ تصویر چک ذخیره شد")}
        OutlinedTextField(party,{party=it},label={Text("طرف حساب")},modifier=Modifier.fillMaxWidth());OutlinedTextField(amount,{amount=it},label={Text("مبلغ ریال")},modifier=Modifier.fillMaxWidth());OutlinedTextField(bank,{bank=it},label={Text("بانک")},modifier=Modifier.fillMaxWidth());OutlinedTextField(sayad,{sayad=it},label={Text("شناسه صیادی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(serial,{serial=it},label={Text("سریال چک")},modifier=Modifier.fillMaxWidth());DateTimeField(due,9,0,{due=it},{_,_->})
        Button({scope.launch{vm.repo.db.checkDao().insert(CheckEntity(direction=direction,amountRial=Digits.parseAmount(amount)?:0,counterparty=party,bankName=bank,sayadId=sayad,serialNumber=serial,issuedAt=System.currentTimeMillis(),dueAt=due.startOfDayMillis(),reminderAt=due.startOfDayMillis(),imagePath=imagePath));amount="";party="";sayad="";serial="";imagePath=""}},enabled=party.isNotBlank()&&(Digits.parseAmount(amount)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("ثبت چک و یادآور")}
        HorizontalDivider();checks.forEach{c->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text((if(c.direction==CheckDirection.ISSUED)"صادره برای " else "دریافتی از ")+c.counterparty,style=MaterialTheme.typography.titleMedium);Text(Money.format(c.amountRial,settings.moneyUnit));Text("سررسید: ${PersianDate.formatDateTime(c.dueAt)}");Row{TextButton({scope.launch{vm.repo.db.checkDao().update(c.copy(status=CheckStatus.CLEARED))}}){Text("وصول/پاس شد")};TextButton({scope.launch{vm.repo.db.checkDao().update(c.copy(status=CheckStatus.BOUNCED))}}){Text("برگشت خورد")}}}}}
    }
}
