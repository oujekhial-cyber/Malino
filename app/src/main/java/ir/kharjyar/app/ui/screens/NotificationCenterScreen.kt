package ir.kharjyar.app.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch
@Composable fun NotificationCenterScreen(vm:AppViewModel,nav:NavHostController){
 val statuses=remember{listOf(SmsStatus.RAW,SmsStatus.NEEDS_ACCOUNT,SmsStatus.NEEDS_TEMPLATE,SmsStatus.DRAFT_READY)};val sms by vm.repo.smsDao.observeByStatus(statuses).collectAsState(initial=emptyList());val civic by vm.repo.db.civicDao().observeMessages().collectAsState(initial=emptyList());val stocks by vm.repo.db.stockDao().observePending().collectAsState(initial=emptyList());val txs by vm.allTransactions.collectAsState();val scope=rememberCoroutineScope();val unread=civic.filter{!it.read};val total=sms.size+stocks.size+unread.size
 Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.primary.copy(.07f),MaterialTheme.colorScheme.background))).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer.copy(.72f))){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.NotificationsActive,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(38.dp));Spacer(Modifier.width(12.dp));Column{Text("مرکز اعلان‌ها",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text(if(total==0)"همه‌چیز مرتب است" else "${Digits.toPersian(total.toString())} کار منتظر اقدام شماست")}}}
  if(total>0)Text("اقدام‌های آماده",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black) else Text("اعلان رسیدگی‌نشده‌ای ندارید",Modifier.padding(24.dp))
  sms.forEach{m->val tx=txs.firstOrNull{it.smsId==m.id};val data=when(m.status){SmsStatus.NEEDS_ACCOUNT->Triple("معرفی حساب پیامک","حساب این پیامک مشخص نیست","accountFromSms/${m.id}");SmsStatus.NEEDS_TEMPLATE->Triple("تکمیل الگوی پیامک","اطلاعات پیامک مبهم است","template/${m.id}");SmsStatus.DRAFT_READY->Triple("تأیید تراکنش بانکی","پیش‌نویس آماده ثبت است",tx?.let{"tx/${it.id}"}?:"review");else->Triple("بررسی پیامک بانکی","نیازمند بررسی نهایی","review")};ActionCard(data.first,"${m.sender} • ${m.body.take(100)}",data.second,Icons.Filled.AccountBalanceWallet,Color(0xFF2979D6)){nav.navigate(data.third)}}
  stocks.forEach{s->ActionCard("${if(s.side==StockSide.BUY)"خرید" else "فروش"} ${s.symbol}","${Digits.toPersian(s.quantity.toString())} سهم • ${s.broker}","تأیید یا رد مستقیم معامله",Icons.Filled.ShowChart,Color(0xFF6658D3)){nav.navigate("assets")}}
  unread.forEach{m->ActionCard(when(m.kind){CivicMessageKind.UTILITY_BILL->"قبض خدماتی";CivicMessageKind.TRAFFIC_FINE->"جریمه رانندگی";CivicMessageKind.INSURANCE->"پیام بیمه";else->"ابلاغیه"},m.body.take(120),PersianDate.formatDateTime(m.receivedAt),Icons.Filled.Campaign,Color(0xFFE04A63)){scope.launch{vm.repo.db.civicDao().updateMessage(m.copy(read=true))};nav.navigate("civicCenter")}}
 }
}
@Composable private fun ActionCard(title:String,body:String,hint:String,icon:ImageVector,accent:Color,onClick:()->Unit){Card(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,accent.copy(.35f))){Row(Modifier.background(Brush.horizontalGradient(listOf(accent.copy(.1f),Color.Transparent))).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(accent.copy(.14f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(icon,null,tint=accent)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(body,maxLines=2,style=MaterialTheme.typography.bodySmall);Text(hint,color=accent,style=MaterialTheme.typography.labelSmall)};Icon(Icons.Filled.ArrowBack,"بازکردن",tint=accent)}}}
