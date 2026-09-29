package ir.kharjyar.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch
@Composable fun BlockedSendersScreen(vm:AppViewModel){val rows by vm.blockedSenders.collectAsState();val scope=rememberCoroutineScope();LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("فرستنده‌های تبلیغاتی",style=MaterialTheme.typography.headlineSmall);Text("پیام‌های این فرستنده‌ها نادیده گرفته می‌شوند.")};items(rows,key={it.id}){r->Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(r.sender);TextButton({scope.launch{vm.repo.unblockSender(r.sender)}}){Text("برگرداندن")}}}}}}
