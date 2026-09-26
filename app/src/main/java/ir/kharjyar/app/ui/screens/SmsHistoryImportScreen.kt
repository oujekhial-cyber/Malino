package ir.kharjyar.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Telephony
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
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.sms.*
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DateTimeField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhoneSms(val id: Long,val sender:String,val body:String,val at:Long)
@Composable fun SmsHistoryImportScreen(vm:AppViewModel,nav:NavHostController){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val accounts by vm.accounts.collectAsState();val settings by vm.settings.collectAsState();var accountId by remember{mutableStateOf<Long?>(null)};var fromDate by remember{mutableStateOf(PersianDate.today().plusDays(-30))};var toDate by remember{mutableStateOf(PersianDate.today())};var fromH by remember{mutableStateOf(0)};var fromM by remember{mutableStateOf(0)};var toH by remember{mutableStateOf(23)};var toM by remember{mutableStateOf(59)};var items by remember{mutableStateOf<List<PhoneSms>>(emptyList())};var skipped by remember{mutableStateOf(setOf<Long>())}
 fun load(){val aid=accountId?:return;scope.launch{val account=accounts.firstOrNull{it.id==aid}?:return@launch;val senders=vm.repo.accountDao.sendersOf(aid).map{AccountMatcher.normalizeSender(it.sender)};val from=PersianDate.toMillis(fromDate,fromH,fromM);val to=PersianDate.toMillis(toDate,toH,toM)+59999;items=withContext(Dispatchers.IO){val out=mutableListOf<PhoneSms>();context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,arrayOf("_id","address","body","date"),"date BETWEEN ? AND ?",arrayOf(from.toString(),to.toString()),"date ASC")?.use{c->while(c.moveToNext()){val sms=PhoneSms(c.getLong(0),c.getString(1)?:"",c.getString(2)?:"",c.getLong(3));if(senders.any{it==AccountMatcher.normalizeSender(sms.sender)}||sms.body.contains(account.bankName)||AccountNumberMatcher.match(sms.body,listOf(MatchableAccount(account.id,account.maskedNumber,account.accountNumber,account.iban,account.cardNumber))) is AccountMatch.Single)out+=sms}};out}}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)load()}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("خواندن پیامک‌های قبلی بانک",style=MaterialTheme.typography.headlineSmall);ComboBox(label="حساب/بانک",options=accounts.filter{!it.archived}.map{it.id},selected=accountId,onSelect={accountId=it},labelOf={id->accounts.firstOrNull{it.id==id}?.let{"${it.title} — ${it.bankName}"}?:"—"});Text("شروع");DateTimeField(fromDate,fromH,fromM,{fromDate=it},{h,m->fromH=h;fromM=m});Text("پایان");DateTimeField(toDate,toH,toM,{toDate=it},{h,m->toH=h;toM=m});Button({if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED)load()else permission.launch(Manifest.permission.READ_SMS)},enabled=accountId!=null,modifier=Modifier.fillMaxWidth()){Text("خواندن و مرتب‌سازی پیامک‌ها")}
 items.filter{it.id !in skipped}.forEach{sms->val r=remember(sms.id){Extractor.autoExtract(sms.body)};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(PersianDate.formatDateTime(r.occurredAtMillis?:sms.at));Text(sms.body,maxLines=5);Text(r.amountRial?.let{Money.format(it,settings.moneyUnit)}?:"مبلغ نامشخص");Row{Button({val aid=accountId?:return@Button;val amount=r.amountRial?:return@Button;scope.launch{val dir=if(r.directionEnum()==ExtractedDirection.DEPOSIT)TxDirection.DEPOSIT else TxDirection.WITHDRAW;val id=vm.repo.txDao.insert(TransactionEntity(accountId=aid,amountRial=amount,direction=dir,nature=if(dir==TxDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE,occurredAt=r.occurredAtMillis?:sms.at,recordedAt=System.currentTimeMillis(),source=TxSource.SMS,status=TxStatus.PENDING,balanceAfterRial=r.balanceRial,refNumber=r.refNumber?:""));nav.navigate("tx/$id")}},enabled=r.amountRial!=null&&r.directionEnum()!=ExtractedDirection.UNKNOWN){Text("ثبت و تکمیل")};TextButton({skipped=skipped+sms.id}){Text("صرف‌نظر")}}}}}
 }
}
