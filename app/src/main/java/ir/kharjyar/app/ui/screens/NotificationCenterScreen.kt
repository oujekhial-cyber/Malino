package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.CivicMessageKind
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable fun NotificationCenterScreen(vm:AppViewModel,nav:NavHostController){
 val reviewCount by vm.reviewCount.collectAsState();val dao=vm.repo.db.civicDao();val civic by dao.observeMessages().collectAsState(initial=emptyList());val stocks by vm.repo.db.stockDao().observePending().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();val unread=civic.filter{!it.read};val total=reviewCount+unread.size+stocks.size
 Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.primary.copy(.05f),MaterialTheme.colorScheme.background))).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
  Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(25.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer.copy(.58f))){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(54.dp).background(MaterialTheme.colorScheme.primary.copy(.14f),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.NotificationsActive,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(30.dp))};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text("مرکز اعلان‌ها",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text(if(total==0)"اعلان جدیدی ندارید" else "${Digits.toPersian(total.toString())} اعلان جدید",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
  NotificationGroup("پیامک‌های بانکی نیازمند بررسی","پیامک‌های استخراج‌شده قبل از ثبت نهایی",reviewCount,Icons.Filled.AccountBalanceWallet,Color(0xFF3E8BEF)){nav.navigate("review")}
  NotificationGroup("معاملات پیامکی بورس","خرید و فروش‌هایی که هنوز تأیید نشده‌اند",stocks.size,Icons.Filled.ShowChart,Color(0xFF6D65E8)){nav.navigate("assets")}
  NotificationGroup("خدمات شهروندی","قبض، جریمه، بیمه و ابلاغیه‌های خوانده‌نشده",unread.size,Icons.Filled.Campaign,Color(0xFFE05A70)){nav.navigate("civicCenter")}
  if(unread.isNotEmpty()){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("تازه‌ترین پیام‌ها",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);TextButton({scope.launch{unread.forEach{dao.updateMessage(it.copy(read=true))}}}){Icon(Icons.Filled.DoneAll,null);Spacer(Modifier.width(4.dp));Text("همه را خواندم")}}
   unread.take(5).forEach{m->Card(Modifier.fillMaxWidth().clickable{nav.navigate("civicCenter")},shape=RoundedCornerShape(17.dp)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(when(m.kind){CivicMessageKind.UTILITY_BILL->"قبض خدماتی";CivicMessageKind.TRAFFIC_FINE->"جریمه رانندگی";CivicMessageKind.INSURANCE->"پیام بیمه";else->"ابلاغیه جدید"},fontWeight=FontWeight.Bold);Text(m.body,maxLines=2,style=MaterialTheme.typography.bodySmall);Text(PersianDate.formatDateTime(m.receivedAt),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  }}
 }
}

@Composable private fun NotificationGroup(title:String,subtitle:String,count:Int,icon:ImageVector,accent:Color,onClick:()->Unit){Card(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=accent.copy(.08f)),border=androidx.compose.foundation.BorderStroke(1.dp,accent.copy(.35f))){Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(45.dp).background(accent.copy(.14f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(icon,null,tint=accent)};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};if(count>0)Badge(containerColor=accent){Text(Digits.toPersian(count.toString()))}else Text("—",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
