package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch

@Composable fun VehiclesScreen(vm:AppViewModel){val dao=vm.repo.db.civicDao();val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val messages by dao.observeMessages().collectAsState(initial=emptyList());val services by dao.observeOilServices().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();val context=LocalContext.current
 var selectedId by remember{mutableStateOf<Long?>(null)};val selected=vehicles.firstOrNull{it.id==selectedId}?:vehicles.firstOrNull();var tab by remember{mutableIntStateOf(0)};var addVehicle by remember{mutableStateOf(false)};var title by remember{mutableStateOf("")};var plate by remember{mutableStateOf("")};var owner by remember{mutableStateOf<Long?>(null)};var unreadOnly by remember{mutableStateOf(false)}
 var currentKm by remember{mutableStateOf("")};var intervalKm by remember{mutableStateOf("5000")};var oilType by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today().plusMonths(6))}
 fun normalized(s:String)=Digits.normalize(s).filter(Char::isLetterOrDigit).lowercase()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("وسایل نقلیه",style=MaterialTheme.typography.headlineSmall)
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){vehicles.forEach{v->FilterChip(selected?.id==v.id,{selectedId=v.id},{Text(v.title)})};AssistChip({addVehicle=!addVehicle},{Text("+ خودرو")})}
  if(addVehicle){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(title,{title=it},label={Text("نام خودرو")},modifier=Modifier.fillMaxWidth());OutlinedTextField(plate,{plate=it},label={Text("شماره پلاک")},modifier=Modifier.fillMaxWidth());Text("مالک خودرو");Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){FilterChip(owner==null,{owner=null},{Text("خودم")});people.forEach{p->FilterChip(owner==p.id,{owner=p.id},{Text(p.name)})}};Button({scope.launch{val id=dao.insertVehicle(VehicleEntity(ownerId=owner,title=title,plate=plate));selectedId=id;title="";plate="";addVehicle=false}},enabled=title.isNotBlank()&&plate.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("ثبت خودرو")}}}}
  if(selected==null){Text("برای شروع یک خودرو و شماره پلاک آن را ثبت کنید.");return@Column}
  val ownerName=selected.ownerId?.let{id->people.firstOrNull{it.id==id}?.name}?:"خودم";Text("${selected.title} • پلاک ${selected.plate} • مالک: $ownerName",style=MaterialTheme.typography.titleMedium)
  Row(Modifier.fillMaxWidth()){FilterChip(tab==0,{tab=0},{Text("جرائم راهنمایی و رانندگی")});Spacer(Modifier.width(8.dp));FilterChip(tab==1,{tab=1},{Text("تعویض روغن")})}
  if(tab==0){Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(unreadOnly,{unreadOnly=it});Text("فقط خوانده‌نشده‌ها")};val p=normalized(selected.plate);val fines=messages.filter{it.kind==CivicMessageKind.TRAFFIC_FINE&&(!unreadOnly||!it.read)&&(p.isBlank()||normalized(it.body).contains(p))};if(fines.isEmpty())Text("پیامک جریمه‌ای برای این پلاک پیدا نشد.");fines.forEach{m->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(PersianDate.formatDateTime(m.receivedAt),style=MaterialTheme.typography.labelMedium);Text(m.body);if(!m.read)TextButton({scope.launch{dao.updateMessage(m.copy(read=true,vehicleId=selected.id))}}){Text("خواندم")}}}}}
  else {Text("ثبت سرویس و یادآوری موعد بعدی",style=MaterialTheme.typography.titleMedium);OutlinedTextField(currentKm,{currentKm=Digits.normalize(it).filter(Char::isDigit)},label={Text("کیلومتر فعلی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(intervalKm,{intervalKm=Digits.normalize(it).filter(Char::isDigit)},label={Text("فاصله تعویض بعدی (کیلومتر)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(oilType,{oilType=it},label={Text("نوع/برند روغن")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("توضیحات")},modifier=Modifier.fillMaxWidth());Text("موعد زمانی تعویض بعدی");PersianDateField(due,{due=it});Button({scope.launch{val nowKm=currentKm.toIntOrNull()?:0;val nextKm=nowKm+(intervalKm.toIntOrNull()?:5000);val at=due.startOfDayMillis()+9*60*60*1000L;val reminderId=vm.repo.db.reminderDao().insert(ReminderEntity(title="تعویض روغن ${selected.title}",category="تعویض روغن خودرو",note="پلاک ${selected.plate} — موعد کیلومتر ${Digits.toPersian(nextKm.toString())}",nextAt=at));LifeReminderWorker.schedule(context,reminderId,at);dao.insertOilService(VehicleOilServiceEntity(vehicleId=selected.id,servicedAt=System.currentTimeMillis(),currentKm=nowKm,nextKm=nextKm,nextDueAt=at,oilType=oilType,note=note,reminderId=reminderId));currentKm="";oilType="";note=""}},enabled=currentKm.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("ثبت تعویض روغن و یادآور")};services.filter{it.vehicleId==selected.id}.forEach{s->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text("سرویس در کیلومتر ${Digits.toPersian(s.currentKm.toString())}");Text("تعویض بعدی: ${Digits.toPersian(s.nextKm.toString())} کیلومتر یا ${PersianDate.fromMillis(s.nextDueAt).format()}");if(s.oilType.isNotBlank())Text(s.oilType)}}}}
 }
}
