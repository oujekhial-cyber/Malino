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
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.core.sms.*
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DateTimeField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhoneSms(val id:Long,val sender:String,val body:String,val at:Long,val accountId:Long?,val extraction:ExtractionResult)

@Composable fun SmsHistoryImportScreen(vm:AppViewModel,nav:NavHostController,initialAccountId:Long?=null){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val accounts by vm.accounts.collectAsState();val categories by vm.categories.collectAsState();val settings by vm.settings.collectAsState();val activeAccounts=accounts.filter{!it.archived};val banks=activeAccounts.filter{it.accountType!=AccountType.CASH&&it.bankName.isNotBlank()}.map{it.bankName}.distinct();var bank by remember{mutableStateOf<String?>(null)};var accountFilter by remember{mutableStateOf<Long?>(initialAccountId)};var findings by remember{mutableStateOf<Map<Long,ReconcileFinding>>(emptyMap())};
 LaunchedEffect(initialAccountId,accounts){initialAccountId?.let{id->accounts.firstOrNull{it.id==id}?.let{bank=it.bankName;accountFilter=id}}}
var fromDate by remember{mutableStateOf(PersianDate.today().plusDays(-30))};var toDate by remember{mutableStateOf(PersianDate.today())};var fromH by remember{mutableStateOf(0)};var fromM by remember{mutableStateOf(0)};var toH by remember{mutableStateOf(23)};var toM by remember{mutableStateOf(59)};var items by remember{mutableStateOf<List<PhoneSms>>(emptyList())};var skipped by remember{mutableStateOf(setOf<Long>())};var pendingDeleteSms by remember{mutableStateOf<PhoneSms?>(null)}
 suspend fun read(){val selectedBank=bank?:return;val bankAccounts=accounts.filter{!it.archived&&it.bankName==selectedBank};val mappings=vm.repo.accountDao.allSenders().filter{m->bankAccounts.any{it.id==m.accountId}};val senderSet=mappings.map{AccountMatcher.normalizeSender(it.sender)}.toSet();val matchables=bankAccounts.map{MatchableAccount(it.id,it.maskedNumber,it.accountNumber,it.iban,it.cardNumber)};val from=PersianDate.toMillis(fromDate,fromH,fromM);val to=PersianDate.toMillis(toDate,toH,toM)+59999
  items=withContext(Dispatchers.IO){val out=mutableListOf<PhoneSms>();context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,arrayOf("_id","address","body","date"),"date BETWEEN ? AND ?",arrayOf(from.toString(),to.toString()),"date ASC")?.use{c->while(c.moveToNext()){val id=c.getLong(0);val sender=c.getString(1)?:"";val body=c.getString(2)?:"";val at=c.getLong(3);if(SmsClassifier.isOtp(body)||SmsClassifier.classify(body)==SmsKind.NON_FINANCIAL)continue;val extraction=Extractor.autoExtract(body);if(extraction.amountRial==null||extraction.directionEnum()==ExtractedDirection.UNKNOWN)continue;val senderNormalized=AccountMatcher.normalizeSender(sender);val senderAccountIds=mappings.filter{AccountMatcher.normalizeSender(it.sender)==senderNormalized}.map{it.accountId}.distinct();val numberAccountId=(AccountNumberMatcher.match(body,matchables) as? AccountMatch.Single)?.accountId;val aid=numberAccountId?:senderAccountIds.singleOrNull();if(accountFilter!=null&&aid!=accountFilter)continue;val mappedSender=senderNormalized in senderSet;val inferredBank=BankSenderResolver.bankName(sender);val bankSender=inferredBank!=null&&BankSenderResolver.sameBank(selectedBank,inferredBank);val bankInBody=selectedBank.isNotBlank()&&Digits.normalizeForMatch(body).contains(Digits.normalizeForMatch(selectedBank));if(!mappedSender&&!bankSender&&!bankInBody)continue;out+=PhoneSms(id,sender,body,at,aid,extraction)}};out.sortedBy{it.extraction.occurredAtMillis?:it.at}}
  val feePercent=vm.settingsRepo.current().bankFeePercent
  items.groupBy{it.accountId}.forEach { (aid,list) -> if(aid!=null) {
   list.filter{it.extraction.balanceRial!=null}.maxByOrNull{it.extraction.occurredAtMillis?:it.at}?.let { sms ->
    val bankBalance=sms.extraction.balanceRial!!;val at=sms.extraction.occurredAtMillis?:sms.at
    vm.repo.db.bankBalanceSnapshotDao().upsert(BankBalanceSnapshotEntity(aid,bankBalance,at,sms.id))
    val account=accounts.firstOrNull{it.id==aid};val txs=vm.repo.txDao.allOnce();val estimated=account?.let{ir.kharjyar.app.core.balance.AccountBalance.estimate(it,txs).rial}
    val diff=if(estimated!=null)estimated-bankBalance else 0L;val base=list.maxOfOrNull{it.extraction.amountRial?:0L}?:0L;val maxFee=(base.toDouble()*feePercent/100.0).toLong()
    val duplicate=txs.any{it.accountId==aid&&it.description=="کارمزد بانکی خودکار"&&it.amountRial==diff&&kotlin.math.abs(it.occurredAt-at)<86_400_000L}
    if(diff>0&&diff<=maxFee&&!duplicate)vm.repo.txDao.insert(TransactionEntity(accountId=aid,amountRial=diff,direction=TxDirection.WITHDRAW,nature=TxNature.EXPENSE,description="کارمزد بانکی خودکار",occurredAt=at,recordedAt=System.currentTimeMillis(),source=TxSource.SMS,status=TxStatus.CONFIRMED))
   }
  }}
  accountFilter?.let { targetId ->
   val txs=vm.repo.txDao.allOnce()
   findings=AccountSmsReconciler.reconcile(items.filter{it.accountId==targetId}.map{sms->ReconcileSms(sms.id,sms.extraction.amountRial!!,if(sms.extraction.directionEnum()==ExtractedDirection.DEPOSIT)TxDirection.DEPOSIT else TxDirection.WITHDRAW,sms.extraction.occurredAtMillis?:sms.at,sms.extraction.refNumber.orEmpty())},txs,targetId).associateBy{it.sms.id}
  }
 }
 fun load(){scope.launch{read()}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)load()}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(if(initialAccountId!=null)"بررسی هوشمند مغایرت حساب" else "فراخوانی پیامک‌های مالی بانک",style=MaterialTheme.typography.headlineSmall);if(initialAccountId!=null)Text("فقط پیامک‌هایی بررسی می‌شوند که شماره کارت، حساب یا شبای همین حساب را داشته باشند. مبلغ، جهت، زمان و شماره پیگیری با تراکنش‌های ثبت‌شده مقایسه می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);ComboBox(label="بانک",options=banks,selected=bank,onSelect={bank=it;accountFilter=null},labelOf={it});ComboBox(label="حساب",options=activeAccounts,selected=activeAccounts.firstOrNull{it.id==accountFilter},onSelect={accountFilter=it.id},labelOf={account->if(account.accountType==AccountType.CASH)"${account.title} • حساب نقدی" else "${account.title} • ${account.bankName}"});if(accountFilter!=null){TextButton(onClick={accountFilter=null}){Text("همه حساب‌های بانک")}};Text("شروع");DateTimeField(fromDate,fromH,fromM,{fromDate=it},{h,m->fromH=h;fromM=m});Text("پایان");DateTimeField(toDate,toH,toM,{toDate=it},{h,m->toH=h;toM=m});Button({if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED)load()else permission.launch(Manifest.permission.READ_SMS)},enabled=bank!=null,modifier=Modifier.fillMaxWidth()){Text("خواندن پیامک‌های مالی")}
 items.filter{it.id !in skipped&&(accountFilter==null||it.accountId==accountFilter)&&(initialAccountId==null||findings[it.id]?.kind!=ReconcileKind.MATCHED)}.forEach{sms->val r=sms.extraction;val finding=findings[sms.id];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){finding?.let{f->Text(when(f.kind){ReconcileKind.MISSING_TRANSACTION->"احتمالاً ثبت نشده";ReconcileKind.AMOUNT_MISMATCH->"مبلغ ثبت‌شده احتمالاً اشتباه است: ${Money.format(f.transaction!!.amountRial,settings.moneyUnit)} به‌جای ${Money.format(f.sms.amountRial,settings.moneyUnit)}";else->"قبلاً ثبت شده"+(f.transaction?.let{tx->val reason=tx.description.ifBlank{categories.firstOrNull{it.id==tx.categoryId}?.name.orEmpty()};if(reason.isNotBlank())"؛ بابت: $reason" else ""}?:"")},color=if(f.kind==ReconcileKind.MATCHED)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,style=MaterialTheme.typography.labelLarge)};Text(PersianDate.formatDateTime(r.occurredAtMillis?:sms.at));Text(accounts.firstOrNull{it.id==sms.accountId}?.title?:"حساب نامشخص");Text(sms.body,maxLines=5);Text(Money.format(r.amountRial!!,settings.moneyUnit));Row{Button({val aid=sms.accountId?:accountFilter?:return@Button;scope.launch{val dir=if(r.directionEnum()==ExtractedDirection.DEPOSIT)TxDirection.DEPOSIT else TxDirection.WITHDRAW;val id=vm.repo.txDao.insert(TransactionEntity(accountId=aid,amountRial=r.amountRial!!,direction=dir,nature=if(dir==TxDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE,occurredAt=r.occurredAtMillis?:sms.at,recordedAt=System.currentTimeMillis(),source=TxSource.SMS,status=TxStatus.PENDING,balanceAfterRial=r.balanceRial,refNumber=r.refNumber?:""));nav.navigate("tx/$id")}},enabled=(sms.accountId!=null||accountFilter!=null)&&finding?.kind!=ReconcileKind.AMOUNT_MISMATCH&&finding?.kind!=ReconcileKind.MATCHED){Text("ثبت و تکمیل")};finding?.transaction?.takeIf{finding.kind==ReconcileKind.AMOUNT_MISMATCH}?.let{tx->TextButton({nav.navigate("tx/${tx.id}")}){Text("اصلاح تراکنش")}};TextButton({skipped=skipped+sms.id}){Text("صرف‌نظر")};TextButton({pendingDeleteSms=sms}){Text("حذف از لیست",color=MaterialTheme.colorScheme.error)}}}}}
 }
 pendingDeleteSms?.let { sms ->
  AlertDialog(
   onDismissRequest={pendingDeleteSms=null},
   title={Text("حذف پیامک فراخوانی‌شده")},
   text={Text("این پیامک از فهرست فراخوانی‌شده حذف شود؟ پیامک اصلی داخل برنامه پیامک‌های گوشی حذف نخواهد شد.")},
   confirmButton={TextButton({skipped=skipped+sms.id;pendingDeleteSms=null}){Text("حذف از لیست",color=MaterialTheme.colorScheme.error)}},
   dismissButton={TextButton({pendingDeleteSms=null}){Text("انصراف")}}
  )
 }
}
