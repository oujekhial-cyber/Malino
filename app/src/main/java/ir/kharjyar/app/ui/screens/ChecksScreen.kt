package ir.kharjyar.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager

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
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.TwoWayModeSelector
import kotlinx.coroutines.launch
import java.io.File

@Composable fun ChecksScreen(vm: AppViewModel) {
    val checks by vm.checks.collectAsState(); val settings by vm.settings.collectAsState(); val accounts by vm.accounts.collectAsState(); val scope=rememberCoroutineScope(); val context=LocalContext.current
    var direction by remember{mutableStateOf(CheckDirection.ISSUED)};var issuer by remember{mutableStateOf("")};var receiver by remember{mutableStateOf("")};var nationalId by remember{mutableStateOf("")};var chequeAccount by remember{mutableStateOf("")};var chequeIban by remember{mutableStateOf("")};var amountWords by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var party by remember{mutableStateOf("")};var bank by remember{mutableStateOf("")};var sayad by remember{mutableStateOf("")};var serial by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today())};var imagePath by remember{mutableStateOf("")}
    var cameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    fun analyze(uri: android.net.Uri) {
        val dir=File(context.filesDir,"checks").apply{mkdirs()};val file=File(dir,"${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }; imagePath=file.absolutePath
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromFilePath(context,uri)).addOnSuccessListener { result ->
            val parsed=ir.kharjyar.app.core.check.SayadCheckParser.parse(result.text)
            if(parsed.sayadId.isNotBlank())sayad=parsed.sayadId;if(parsed.serialNumber.isNotBlank())serial=parsed.serialNumber
            parsed.amountRial?.let{amount=it.toString()};parsed.dueDate?.let{due=it};if(parsed.bankName.isNotBlank())bank=parsed.bankName
            if(parsed.issuerName.isNotBlank())issuer=parsed.issuerName;if(parsed.receiverName.isNotBlank()){receiver=parsed.receiverName;party=parsed.receiverName}
            if(parsed.nationalId.isNotBlank())nationalId=parsed.nationalId;if(parsed.accountNumber.isNotBlank())chequeAccount=parsed.accountNumber
            if(parsed.iban.isNotBlank())chequeIban=parsed.iban;if(parsed.amountWords.isNotBlank())amountWords=parsed.amountWords
        }
    }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null)analyze(uri)}
    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ok->if(ok)cameraUri?.let(::analyze)}
    var cameraPermissionError by remember { mutableStateOf(false) }
    fun openCamera() { val f=File(context.cacheDir,"check-camera-${System.currentTimeMillis()}.jpg"); val uri=androidx.core.content.FileProvider.getUriForFile(context,"${context.packageName}.files",f); cameraUri=uri; camera.launch(uri) }
    val cameraPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->cameraPermissionError=!granted;if(granted)openCamera()}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        TwoWayModeSelector("چک صادرشده", "چک دریافت‌شده", direction==CheckDirection.ISSUED, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.primary, {direction=CheckDirection.ISSUED}, {direction=CheckDirection.RECEIVED})
        Text(if(direction==CheckDirection.ISSUED) "ثبت چک صادرشده" else "ثبت چک دریافت‌شده",style=MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) openCamera() else cameraPermission.launch(Manifest.permission.CAMERA) },Modifier.weight(1f)){Text("دوربین")}
            OutlinedButton({picker.launch("image/*")},Modifier.weight(1f)){Text("گالری")}
        }
        if(cameraPermissionError) Text("برای اسکن چک، اجازه دوربین را فعال کنید.",color=MaterialTheme.colorScheme.error)
        if(imagePath.isNotBlank()) Text("✓ تصویر خوانده شد؛ لطفاً اطلاعات تکمیل‌شده را بررسی کنید.",color=MaterialTheme.colorScheme.primary)
        OutlinedTextField(party,{party=it},label={Text("طرف حساب")},modifier=Modifier.fillMaxWidth());OutlinedTextField(issuer,{issuer=it},label={Text("نام صادرکننده")},modifier=Modifier.fillMaxWidth());OutlinedTextField(receiver,{receiver=it;party=it},label={Text("نام دریافت‌کننده")},modifier=Modifier.fillMaxWidth());OutlinedTextField(nationalId,{nationalId=Digits.normalize(it).filter(Char::isDigit)},label={Text("کد/شناسه ملی")},modifier=Modifier.fillMaxWidth());ir.kharjyar.app.ui.components.AmountTextField(amount,{amount=it},"مبلغ چک به عدد (ریال)",Modifier.fillMaxWidth());OutlinedTextField(amountWords,{amountWords=it},label={Text("مبلغ چک به حروف")},modifier=Modifier.fillMaxWidth());OutlinedTextField(bank,{bank=it},label={Text("بانک و شعبه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(sayad,{sayad=Digits.normalize(it).filter(Char::isDigit)},label={Text("شناسه ۱۶ رقمی صیادی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(serial,{serial=Digits.normalize(it).filter(Char::isDigit)},label={Text("شماره سریال/سری چک")},modifier=Modifier.fillMaxWidth());OutlinedTextField(chequeAccount,{chequeAccount=Digits.normalize(it)},label={Text("شماره حساب چک")},modifier=Modifier.fillMaxWidth());OutlinedTextField(chequeIban,{chequeIban=it.uppercase()},label={Text("شماره شبا")},modifier=Modifier.fillMaxWidth());PersianDateField(due,{due=it})
        Button({scope.launch{vm.repo.db.checkDao().insert(CheckEntity(direction=direction,amountRial=Digits.parseAmount(amount)?:0,counterparty=party,bankName=bank,sayadId=sayad,serialNumber=serial,issuedAt=System.currentTimeMillis(),dueAt=due.startOfDayMillis(),reminderAt=due.startOfDayMillis(),imagePath=imagePath,issuerName=issuer,receiverName=receiver,nationalId=nationalId,chequeAccountNumber=chequeAccount,chequeIban=chequeIban,amountInWords=amountWords));amount="";party="";issuer="";receiver="";nationalId="";chequeAccount="";chequeIban="";amountWords="";sayad="";serial="";imagePath=""}},enabled=party.isNotBlank()&&(Digits.parseAmount(amount)?:0)>0,modifier=Modifier.fillMaxWidth()){Text(if(direction==CheckDirection.ISSUED) "ثبت چک صادرشده و یادآور" else "ثبت چک دریافت‌شده و یادآور")}
        HorizontalDivider();checks.forEach{c->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text((if(c.direction==CheckDirection.ISSUED)"صادره برای " else "دریافتی از ")+c.counterparty,style=MaterialTheme.typography.titleMedium);Text(Money.format(c.amountRial,settings.moneyUnit));Text("سررسید: ${PersianDate.fromMillis(c.dueAt).format()}");if(c.sayadId.isNotBlank())Text("شناسه صیادی: ${c.sayadId}");if(c.receiverName.isNotBlank())Text("دریافت‌کننده: ${c.receiverName}");Row{TextButton({scope.launch{vm.repo.db.checkDao().update(c.copy(status=CheckStatus.CLEARED))}}){Text("وصول/پاس شد")};TextButton({scope.launch{vm.repo.db.checkDao().update(c.copy(status=CheckStatus.BOUNCED))}}){Text("برگشت خورد")}}}}}
    }
}
