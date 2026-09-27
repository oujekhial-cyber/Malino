package ir.kharjyar.app.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch

private const val VEHICLE_ENTRY=1
private const val SERVICE_ENTRY=2

@Composable fun VehiclesScreen(vm:AppViewModel){val dao=vm.repo.db.civicDao();val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val messages by dao.observeMessages().collectAsState(initial=emptyList());val services by dao.observeOilServices().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var entry by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)};var filter by remember{mutableStateOf<Int?>(null)}
 if(entry!=null){VehicleEntryPage(vm,entry!!,{entry=null},{entry=null});return}
 fun normalized(s:String):String {
  return Digits.normalize(s).filter(Char::isLetterOrDigit).lowercase()
 }
 fun unreadFines(v:VehicleEntity):List<CivicMessageEntity> {
  val plateKey=normalized(v.plate)
  return messages.filter { it.kind==CivicMessageKind.TRAFFIC_FINE && !it.read && (plateKey.isBlank() || normalized(it.body).contains(plateKey)) }
 }
 val shown=vehicles.filter{filter==null||(filter==1&&unreadFines(it).isNotEmpty())||(filter==0&&unreadFines(it).isEmpty())};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("وسایل نقلیه",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){VehicleSummary("بدون جریمه جدید",vehicles.count{unreadFines(it).isEmpty()},Color(0xFF1B8F52),Modifier.weight(1f));VehicleSummary("دارای جریمه جدید",vehicles.count{unreadFines(it).isNotEmpty()},Color(0xFFD33B45),Modifier.weight(1f))};Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter==null,{filter=null},{Text("همه")});FilterChip(filter==0,{filter=0},{Text("عادی")});FilterChip(filter==1,{filter=1},{Text("جریمه جدید")})};if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text(if(vehicles.isEmpty())"برای شروع، وسیله نقلیه خود را ثبت کنید." else "وسیله نقلیه‌ای در این فیلتر نیست.",Modifier.padding(24.dp))};shown.forEach{vehicle->val fines=unreadFines(vehicle);val accent=if(fines.isEmpty())Color(0xFF20A565) else Color(0xFFE14B55);val ownerName=vehicle.ownerId?.let{id->people.firstOrNull{it.id==id}?.name}?:"خودم";val vehicleServices=services.filter{it.vehicleId==vehicle.id};val latest=vehicleServices.maxByOrNull{it.servicedAt};Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(vehicle.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(fines.isEmpty())"وضعیت عادی" else "${Digits.toPersian(fines.size.toString())} جریمه جدید",color=accent,fontWeight=FontWeight.Bold)};Text("پلاک ${vehicle.plate} • مالک: $ownerName");if(latest!=null){HorizontalDivider();Text("آخرین سرویس: کیلومتر ${Digits.toPersian(latest.currentKm.toString())}");Text("تعویض بعدی: ${Digits.toPersian(latest.nextKm.toString())} کیلومتر یا ${PersianDate.fromMillis(latest.nextDueAt).format()}",color=accent);if(latest.oilType.isNotBlank())Text("روغن: ${latest.oilType}")}else Text("هنوز سرویس تعویض روغن ثبت نشده است.",style=MaterialTheme.typography.bodySmall);fines.take(2).forEach{fine->HorizontalDivider();Text(PersianDate.formatDateTime(fine.receivedAt),style=MaterialTheme.typography.labelSmall);Text(fine.body,maxLines=3);TextButton({scope.launch{dao.updateMessage(fine.copy(read=true,vehicleId=vehicle.id))}}){Text("خواندم")}}}}}
 }
 FloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن")}
 if(chooser)AlertDialog(onDismissRequest={chooser=false},title={Text("چه موردی ثبت شود؟")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Button({chooser=false;entry=VEHICLE_ENTRY},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF1B8F52))){Text("وسیله نقلیه جدید")};Button({chooser=false;entry=SERVICE_ENTRY},Modifier.fillMaxWidth(),enabled=vehicles.isNotEmpty(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFD33B45))){Text("سرویس و تعویض روغن")};if(vehicles.isEmpty())Text("برای ثبت سرویس، ابتدا یک وسیله نقلیه اضافه کنید.",style=MaterialTheme.typography.bodySmall)}},confirmButton={})
}
}

@Composable private fun VehicleSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=color);Text(label,style=MaterialTheme.typography.labelMedium)}}}

@Composable private fun VehicleEntryPage(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit){val dao=vm.repo.db.civicDao();val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val context=LocalContext.current;val scope=rememberCoroutineScope();var title by remember{mutableStateOf("")};var plate by remember{mutableStateOf("")};var owner by remember{mutableStateOf<Long?>(null)};var vehicleId by remember{mutableStateOf(vehicles.firstOrNull()?.id)};var currentKm by remember{mutableStateOf("")};var intervalKm by remember{mutableStateOf("5000")};var oilType by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today().plusMonths(6))};val isVehicle=kind==VEHICLE_ENTRY;val accent=if(isVehicle)Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};Text(if(isVehicle)"ثبت وسیله نقلیه" else "ثبت سرویس و تعویض روغن",style=MaterialTheme.typography.headlineSmall,color=accent);if(isVehicle){OutlinedTextField(title,{title=it},label={Text("نام وسیله نقلیه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(plate,{plate=it},label={Text("شماره پلاک")},modifier=Modifier.fillMaxWidth());Text("مالک");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(owner==null,{owner=null},{Text("خودم")});people.forEach{p->FilterChip(owner==p.id,{owner=p.id},{Text(p.name)})}}}else{Text("انتخاب وسیله نقلیه");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){vehicles.forEach{v->FilterChip(vehicleId==v.id,{vehicleId=v.id},{Text(v.title)})}};OutlinedTextField(currentKm,{currentKm=Digits.normalize(it).filter(Char::isDigit)},label={Text("کیلومتر فعلی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(intervalKm,{intervalKm=Digits.normalize(it).filter(Char::isDigit)},label={Text("فاصله تعویض بعدی (کیلومتر)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(oilType,{oilType=it},label={Text("نوع یا برند روغن")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("توضیحات")},modifier=Modifier.fillMaxWidth());Text("موعد زمانی تعویض بعدی");PersianDateField(due,{due=it})};Button({scope.launch{if(isVehicle)dao.insertVehicle(VehicleEntity(ownerId=owner,title=title.trim(),plate=plate.trim()))else{val selected=vehicles.firstOrNull{it.id==vehicleId}?:return@launch;val nowKm=currentKm.toIntOrNull()?:0;val nextKm=nowKm+(intervalKm.toIntOrNull()?:5000);val at=due.startOfDayMillis()+9*60*60*1000L;val reminderId=vm.repo.db.reminderDao().insert(ReminderEntity(title="تعویض روغن ${selected.title}",category="تعویض روغن خودرو",note="پلاک ${selected.plate} — موعد کیلومتر ${Digits.toPersian(nextKm.toString())}",nextAt=at));LifeReminderWorker.schedule(context,reminderId,at);dao.insertOilService(VehicleOilServiceEntity(vehicleId=selected.id,servicedAt=System.currentTimeMillis(),currentKm=nowKm,nextKm=nextKm,nextDueAt=at,oilType=oilType.trim(),note=note.trim(),reminderId=reminderId))};onDone()}},enabled=if(isVehicle)title.isNotBlank()&&plate.isNotBlank()else vehicleId!=null&&currentKm.isNotBlank(),modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=accent)){Text(if(isVehicle)"ثبت وسیله نقلیه" else "ثبت سرویس و فعال‌سازی یادآور")}}
}
