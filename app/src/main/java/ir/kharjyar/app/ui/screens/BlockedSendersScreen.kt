package ir.kharjyar.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable fun BlockedSendersScreen(vm:AppViewModel){
 val rows by vm.blockedSenders.collectAsState();val spam by vm.repo.db.spamSmsDao().observeAll().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var tab by remember{mutableStateOf(0)}
 Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("پیامک‌های تبلیغاتی",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text("تشخیص خودکار محافظه‌کارانه است. پیام‌های کنارگذاشته‌شده را می‌توانید بازگردانید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  TabRow(tab){Tab(tab==0,{tab=0},text={Text("پیام‌ها (${spam.size})")});Tab(tab==1,{tab=1},text={Text("فرستنده‌های مسدود (${rows.size})")})}
  if(tab==0) LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)){
   if(spam.isEmpty())item{Card(Modifier.fillMaxWidth()){Text("پیام تبلیغاتی قرنطینه‌شده‌ای وجود ندارد.",Modifier.padding(18.dp))}}
   items(spam,key={it.id}){m->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(m.sender,fontWeight=FontWeight.Bold);Text(PersianDate.formatDateTime(m.receivedAt),style=MaterialTheme.typography.labelSmall)};Text(m.body,maxLines=4);Text("دلیل: ${m.reason}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Row{Button({scope.launch{vm.repo.restoreSpamMessage(m.id)}},contentPadding=PaddingValues(horizontal=12.dp)){Text("تبلیغاتی نیست؛ بازگرداندن")};Spacer(Modifier.width(8.dp));TextButton({scope.launch{vm.repo.spamSmsDao.delete(m.id)}}){Text("حذف")}}}}}
  }else LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)){
   if(rows.isEmpty())item{Text("فرستنده‌ای به‌صورت دائمی مسدود نشده است.")}
   items(rows,key={it.id}){r->Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(r.sender);TextButton({scope.launch{vm.repo.unblockSender(r.sender)}}){Text("آزاد کردن فرستنده")}}}}
  }
 }
}
