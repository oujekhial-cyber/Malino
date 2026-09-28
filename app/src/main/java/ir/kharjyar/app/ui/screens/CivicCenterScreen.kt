package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import kotlinx.coroutines.launch

private const val ENTRY_PERSON=10
private const val ENTRY_VEHICLE=11

@Composable fun CivicCenterScreen(vm:AppViewModel){val dao=vm.repo.db.civicDao();val context=LocalContext.current;val messages by dao.observeMessages().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var filter by remember{mutableIntStateOf(-1)};var entry by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)}
 BackHandler(enabled=entry!=null){entry=null}
 if(entry!=null){CivicProfileEntry(vm,entry!!,{entry=null},{entry=null});return}
 val shown=messages.filter{filter<0||it.kind==filter};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("قبوض و جرائم مالی",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("اطلاعات این بخش فقط از پیامک‌های دریافتی روی گوشی استخراج می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){CivicSummary("خوانده‌نشده",messages.count{!it.read},Color(0xFFD33B45),Modifier.weight(1f));CivicSummary("خوانده‌شده",messages.count{it.read},Color(0xFF1B8F52),Modifier.weight(1f))};Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(-1 to "همه",CivicMessageKind.TRAFFIC_FINE to "جرائم",CivicMessageKind.UTILITY_BILL to "قبوض",CivicMessageKind.INSURANCE to "بیمه",CivicMessageKind.ADLIRAN to "عدل‌ایران").forEach{(id,label)->FilterChip(filter==id,{filter=id},{Text(label)})}};if(people.isNotEmpty()||vehicles.isNotEmpty()){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text("پروفایل‌های مرتبط",fontWeight=FontWeight.Bold);if(people.isNotEmpty())Text("افراد: ${people.joinToString("، "){it.name}}");if(vehicles.isNotEmpty())Text("وسایل نقلیه: ${vehicles.joinToString("، "){"${it.title} (${it.plate})"}}")}}};if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text("هنوز پیامک مرتبطی دریافت نشده است.",Modifier.padding(24.dp))};shown.forEach{m->val accent=civicAccent(m.kind);Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(civicTitle(m.kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(m.read)"خوانده‌شده" else "جدید",color=if(m.read)Color(0xFF1B8F52) else Color(0xFFD33B45),fontWeight=FontWeight.Bold)};Text("${m.sender} • ${PersianDate.formatDateTime(m.receivedAt)}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(m.body);Row{if(!m.read)TextButton({scope.launch{dao.updateMessage(m.copy(read=true))}}){Text("خواندم")};TextButton({scope.launch{dao.deleteMessage(m.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}}}}}
 }
 FloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن پروفایل")}
 if(chooser)AlertDialog(onDismissRequest={chooser=false},title={Text("چه موردی ثبت شود؟")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Button({chooser=false;entry=ENTRY_PERSON},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFF1B8F52))){Text("فرد تحت پوشش")};Button({chooser=false;entry=ENTRY_VEHICLE},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFFD33B45))){Text("وسیله نقلیه و پلاک")}}},confirmButton={})
}
}

@Composable private fun CivicSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=color);Text(label)}}}
private fun civicAccent(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->Color(0xFFD33B45);CivicMessageKind.UTILITY_BILL->Color(0xFF1B8F52);CivicMessageKind.INSURANCE->Color(0xFF367BD6);else->Color(0xFF8254B8)}
private fun civicTitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جریمه راهنمایی و رانندگی";CivicMessageKind.UTILITY_BILL->"قبض خدماتی";CivicMessageKind.INSURANCE->"بیمه";else->"اطلاع‌رسانی عدل‌ایران"}

@Composable private fun CivicProfileEntry(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit){val context=LocalContext.current;val dao=vm.repo.db.civicDao();val people by dao.observePeople().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var name by remember{mutableStateOf("")};var relation by remember{mutableStateOf("")};var insurer by remember{mutableStateOf("")};var policy by remember{mutableStateOf("")};var vehicleTitle by remember{mutableStateOf("")};var plate by remember{mutableStateOf("")};var owner by remember{mutableStateOf<Long?>(null)};val person=kind==ENTRY_PERSON;val accent=if(person)Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};Text(if(person)"ثبت فرد تحت پوشش" else "ثبت وسیله نقلیه و پلاک",style=MaterialTheme.typography.headlineSmall,color=accent);Text(if(person)"برای اتصال پیامک‌های بیمه و خدمات به اعضای خانواده" else "برای دسته‌بندی جرائم و پیامک‌های مرتبط با خودرو",color=MaterialTheme.colorScheme.onSurfaceVariant);if(person){OutlinedTextField(name,{name=it},label={Text("نام")},modifier=Modifier.fillMaxWidth());OutlinedTextField(relation,{relation=it},label={Text("نسبت")},modifier=Modifier.fillMaxWidth());OutlinedTextField(insurer,{insurer=it},label={Text("شرکت یا سازمان بیمه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(policy,{policy=it},label={Text("شماره بیمه‌نامه")},modifier=Modifier.fillMaxWidth())}else{OutlinedTextField(vehicleTitle,{vehicleTitle=it},label={Text("عنوان خودرو")},modifier=Modifier.fillMaxWidth());OutlinedTextField(plate,{plate=it},label={Text("شماره پلاک")},modifier=Modifier.fillMaxWidth());if(people.isNotEmpty()){Text("مالک");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){people.forEach{p->FilterChip(owner==p.id,{owner=p.id},{Text(p.name)})}}}else Text("در صورت نیاز، ابتدا یک فرد تحت پوشش ثبت کنید.",style=MaterialTheme.typography.bodySmall)};Button({scope.launch{if(person)dao.insertPerson(CoveredPersonEntity(name=name.trim(),relation=relation.trim(),insuranceProvider=insurer.trim(),policyNumber=policy.trim()))else dao.insertVehicle(VehicleEntity(ownerId=owner,title=vehicleTitle.trim(),plate=plate.trim()));showSavedMessage(context,"اطلاعات");onDone()}},enabled=if(person)name.isNotBlank()else vehicleTitle.isNotBlank()&&plate.isNotBlank(),modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text(if(person)"ثبت فرد" else "ثبت وسیله نقلیه")}}
}
