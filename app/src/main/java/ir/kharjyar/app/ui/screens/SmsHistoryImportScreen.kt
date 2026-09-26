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

data class PhoneSms(val id:Long,val sender:String,val body:String,val at:Long,val accountId:Long?,val extraction:ExtractionResult)

@Composable fun SmsHistoryImportScreen(vm:AppViewModel,nav:NavHostController){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val accounts by vm.accounts.collectAsState();val settings by vm.settings.collectAsState();val banks=accounts.filter{!it.archived}.map{it.bankName}.distinct();var bank by remember{mutableStateOf<String?>(null)};var accountFilter by remember{mutableStateOf<Long?>(null)};var fromDate by remember{mutableStateOf(PersianDate.today().plusDays(-30))};var toDate by remember{mutableStateOf(PersianDate.today())};var fromH by remember{mutableStateOf(0)};var fromM by remember{mutableStateOf(0)};var toH by remember{mutableStateOf(23)};var toM by remember{mutableStateOf(59)};var items by remember{mutableStateOf<List<PhoneSms>>(emptyList())};var skipped by remember{mutableStateOf(setOf<Long>())}
 suspend fun read(){val selectedBank=bank?:return;val bankAccounts=accounts.filter{!it.archived&&it.bankName==selectedBank};val mappings=vm.repo.accountDao.allSenders().filter{m->bankAccounts.any{it.id==m.accountId}};val senderSet=mappings.map{AccountMatcher.normalizeSender(it.sender)}.toSet();val matchables=bankAccounts.map{MatchableAccount(it.id,it.maskedNumber,it.accountNumber,it.iban,it.cardNumber)};val from=PersianDate.toMillis(fromDate,fromH,fromM);val to=PersianDate.toMillis(toDate,toH,toM)+59999
  items=withContext(Dispatchers.IO){val out=mutableListOf<PhoneSms>();context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,arrayOf("_id","address","body","date"),"date BETWEEN ? AND ?",arrayOf(from.toString(),to.toString()),"date ASC")?.use{c->while(c.moveToNext()){val id=c.getLong(0);val sender=c.getString(1)?:"";val body=c.getString(2)?:"";val at=c.getLong(3);if(AccountMatcher.normalizeSender(sender) !in senderSet && !body.contains(selectedBank))continue;if(SmsClassifier.classify(body)!=SmsKind.FINANCIAL_LIKELY)continue;val extraction=Extractor.autoExtract(body);if(extraction.amountRial==null||extraction.directionEnum()==ExtractedDirection.UNKNOWN)continue;val aid=(AccountNumberMatcher.match(body,matchables) as? AccountMatch.Single)?.accountId;out+=PhoneSms(id,sender,body,at,aid,extraction)}};out.sortedBy{it.extraction.occurredAtMillis?:it.at}}
  items.groupBy{it.accountId}.forEach{(aid,list)->if(aid!=null){list.filter{it.extraction.balanceRial!=null}.maxByOrNull{it.extraction.occurredAtMillis?:it.at}?.let{sms->vm.repo.db.bankBalanceSnapshotDao().upsert(BankBalanceSnapshotEntity(aid,sms.extraction.balanceRial!!,sms.extraction.occurredAtMillis?:sms.at,sms.id))}}}
 }
 fun load(){scope.launch{read()}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)load()}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("پیامک‌های مالی بانک",style=MaterialTheme.typography.headlineSmall);ComboBox(label="بانک",options=banks,selected=bank,onSelect={bank=it;accountFilter=null},labelOf={it});bank?.let{b->val related=accounts.filter{!it.archived&&it.bankName==b};Text("فیلتر حساب",style=MaterialTheme.typography.labelLarge);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(accountFilter==null,{accountFilter=null},{Text("همه حساب‌های بانک")});related.forEach{a->FilterChip(accountFilter==a.id,{accountFilter=a.id},{Text(a.title)})}}};Text("شروع");DateTimeField(fromDate,fromH,fromM,{fromDate=it},{h,m->fromH=h;fromM=m});Text("پایان");DateTimeField(toDate,toH,toM,{toDate=it},{h,m->toH=h;toM=m});Button({if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED)load()else permission.launch(Manifest.permission.READ_SMS)},enabled=bank!=null,modifier=Modifier.fillMaxWidth()){Text("خواندن پیامک‌های مالی")}
 items.filter{it.id !in skipped&&(accountFilter==null||it.accountId==accountFilter)}.forEach{sms->val r=sms.extraction;Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(PersianDate.formatDateTime(r.occurredAtMillis?:sms.at));Text(accounts.firstOrNull{it.id==sms.accountId}?.title?:"حساب نامشخص");Text(sms.body,maxLines=5);Text(Money.format(r.amountRial!!,settings.moneyUnit));Row{Button({val aid=sms.accountId?:accountFilter?:return@Button;scope.launch{val dir=if(r.directionEnum()==ExtractedDirection.DEPOSIT)TxDirection.DEPOSIT else TxDirection.WITHDRAW;val id=vm.repo.txDao.insert(TransactionEntity(accountId=aid,amountRial=r.amountRial!!,direction=dir,nature=if(dir==TxDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE,occurredAt=r.occurredAtMillis?:sms.at,recordedAt=System.currentTimeMillis(),source=TxSource.SMS,status=TxStatus.PENDING,balanceAfterRial=r.balanceRial,refNumber=r.refNumber?:""));nav.navigate("tx/$id")}},enabled=sms.accountId!=null||accountFilter!=null){Text("ثبت و تکمیل")};TextButton({skipped=skipped+sms.id}){Text("صرف‌نظر")}}}}}
 }
}
