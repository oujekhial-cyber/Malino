package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ModernChoiceDialog
import ir.kharjyar.app.ui.components.ModernChoiceOption
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.VehicleGraphic
import ir.kharjyar.app.ui.components.normalizeVehicleTitle
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch

private const val VEHICLE_ENTRY=1
private const val SERVICE_ENTRY=2

@Composable fun VehiclesScreen(vm:AppViewModel){val dao=vm.repo.db.civicDao();val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val messages by dao.observeMessages().collectAsState(initial=emptyList());val services by dao.observeOilServices().collectAsState(initial=emptyList());val settings by vm.settings.collectAsState();val scope=rememberCoroutineScope();var entry by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)};var filter by remember{mutableStateOf<Int?>(null)};var selectedVehicleId by remember{mutableStateOf<Long?>(null)};var editingVehicleId by remember{mutableStateOf<Long?>(null)};var pendingDelete by remember{mutableStateOf<VehicleEntity?>(null)};var vehicleOrder by remember{mutableStateOf<List<Long>>(emptyList())};var draggingPlateId by remember{mutableStateOf<Long?>(null)};var plateDragY by remember{mutableFloatStateOf(0f)};var dragStartIndex by remember{mutableIntStateOf(-1)};var settlingVehicleIds by remember{mutableStateOf<Set<Long>>(emptySet())};val displacedCardY=remember{Animatable(0f)};val cardHeights=remember{mutableStateMapOf<Long,Float>()}
 LaunchedEffect(vehicles.map{it.id},settings.vehicleOrder){if(draggingPlateId==null){val valid=settings.vehicleOrder.filter{id->vehicles.any{it.id==id}};vehicleOrder=valid+vehicles.map{it.id}.filterNot{it in valid}}}
 BackHandler(enabled=entry!=null||selectedVehicleId!=null){if(entry!=null){entry=null;editingVehicleId=null}else selectedVehicleId=null}
 if(entry!=null){VehicleEntryPage(vm,entry!!,vehicles.firstOrNull{it.id==editingVehicleId},{entry=null;editingVehicleId=null},{entry=null;editingVehicleId=null});return}
 selectedVehicleId?.let{id->vehicles.firstOrNull{it.id==id}?.let{vehicle->VehiclePlateDetail(vehicle,people,services,messages,settings.moneyUnit,dao,{selectedVehicleId=null});return}}
 fun allFines(v:VehicleEntity):List<CivicMessageEntity> = messages.filter { message ->
  message.kind==CivicMessageKind.TRAFFIC_FINE &&
   (message.vehicleId==v.id || (message.vehicleId==null && ir.kharjyar.app.core.sms.IranianPlateMatcher.matches(message.body,v.plate)))
 }
 fun unreadFines(v:VehicleEntity):List<CivicMessageEntity> = allFines(v).filter{!it.read}
 val orderedVehicles=(vehicleOrder.mapNotNull{id->vehicles.firstOrNull{it.id==id}}+vehicles.filterNot{it.id in vehicleOrder});val shown=orderedVehicles.filter{filter==null||(filter==1&&unreadFines(it).isNotEmpty())||(filter==0&&unreadFines(it).isEmpty())};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){ModernSummaryHero("وسایل نقلیه","وضعیت جرائم پلاک‌های ثبت‌شده",Color(0xFF397BD5),listOf(SummaryMetric("بدون جریمه جدید",Digits.toPersian(vehicles.count{unreadFines(it).isEmpty()}.toString()),Color(0xFF1B9A61),Icons.Filled.VerifiedUser),SummaryMetric("دارای جریمه جدید",Digits.toPersian(vehicles.count{unreadFines(it).isNotEmpty()}.toString()),Color(0xFFE24B57),Icons.Filled.WarningAmber)),Icons.Filled.DirectionsCar);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter==null,{filter=null},{Text("همه")});FilterChip(filter==0,{filter=0},{Text("عادی")});FilterChip(filter==1,{filter=1},{Text("جریمه جدید")})};Text("برای تغییر ترتیب، هر جای کارت را نگه دارید و آزادانه بالا یا پایین بکشید.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text(if(vehicles.isEmpty())"برای شروع، وسیله نقلیه خود را ثبت کنید." else "وسیله نقلیه‌ای در این فیلتر نیست.",Modifier.padding(24.dp))};shown.forEach{vehicle->
 val fines=unreadFines(vehicle);val totalFineRial=allFines(vehicle).mapNotNull{ir.kharjyar.app.core.sms.TrafficFineParser.amountRial(it.body)}.sum();val accent=if(fines.isEmpty())Color(0xFF20A565) else Color(0xFFE14B55);val ownerName=vehicle.ownerName.ifBlank{vehicle.ownerId?.let{id->people.firstOrNull{it.id==id}?.name}?:"خودم"};val vehicleServices=services.filter{it.vehicleId==vehicle.id};val latest=vehicleServices.maxByOrNull{it.servicedAt}
 SwipeActionRow(onDelete={pendingDelete=vehicle},onEdit={editingVehicleId=vehicle.id;entry=VEHICLE_ENTRY},enabled=draggingPlateId==null,modifier=Modifier.onGloballyPositioned{cardHeights[vehicle.id]=it.size.height.toFloat()}.graphicsLayer{translationY=when{draggingPlateId==vehicle.id->plateDragY;vehicle.id in settlingVehicleIds->displacedCardY.value;else->0f};scaleX=if(draggingPlateId==vehicle.id)1.018f else 1f;scaleY=if(draggingPlateId==vehicle.id)1.018f else 1f;shadowElevation=if(draggingPlateId==vehicle.id)18f else 0f}.pointerInput(vehicle.id,shown.map{it.id}){detectDragGesturesAfterLongPress(
  onDragStart={draggingPlateId=vehicle.id;plateDragY=0f;dragStartIndex=shown.indexOfFirst{it.id==vehicle.id}},
  onDragCancel={draggingPlateId=null;plateDragY=0f;dragStartIndex=-1},
  onDragEnd={
   val visibleIds=shown.map{it.id};val startIndex=dragStartIndex.coerceIn(0,(visibleIds.lastIndex).coerceAtLeast(0));val averageHeight=(cardHeights.values.average().takeIf{!it.isNaN()&&it>0}?:220.0).toFloat()+10f;val targetIndex=(startIndex+kotlin.math.round(plateDragY/averageHeight).toInt()).coerceIn(0,visibleIds.lastIndex.coerceAtLeast(0))
   if(visibleIds.isNotEmpty()&&targetIndex!=startIndex){val reordered=visibleIds.toMutableList();reordered.removeAt(startIndex);reordered.add(targetIndex,vehicle.id);val iterator=reordered.iterator();vehicleOrder=vehicleOrder.map{id->if(id in visibleIds)iterator.next()else id};val affected=if(targetIndex>startIndex)visibleIds.subList(startIndex+1,targetIndex+1).toSet()else visibleIds.subList(targetIndex,startIndex).toSet();scope.launch{settlingVehicleIds=affected;displacedCardY.snapTo(if(targetIndex>startIndex)averageHeight else -averageHeight);displacedCardY.animateTo(0f,tween(760));settlingVehicleIds=emptySet()}}
   scope.launch{vm.settingsRepo.setVehicleOrder(vehicleOrder)};draggingPlateId=null;plateDragY=0f;dragStartIndex=-1
  }
 ){change,drag->change.consume();plateDragY+=drag.y}}){Card(colors=CardDefaults.cardColors(containerColor=Color.Transparent),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent.copy(.72f),RoundedCornerShape(22.dp))){
  Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent.copy(.16f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).clickable{selectedVehicleId=vehicle.id}.padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){VehicleGraphic(vehicle.title,vehicle.vehicleType,accent,size=50.dp);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(vehicle.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("${vehicle.vehicleType} • مالک: $ownerName",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Surface(color=accent.copy(.14f),shape=RoundedCornerShape(50)){Text(if(fines.isEmpty())"عادی" else "${Digits.toPersian(fines.size.toString())} جریمه جدید",Modifier.padding(horizontal=10.dp,vertical=5.dp),color=accent,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium)}}
   VehiclePlateBadge(vehicle.plate)
   if(totalFineRial>0)Text("جمع جرائم: ${Money.format(totalFineRial,settings.moneyUnit)}",color=Color(0xFFE14B55),fontWeight=FontWeight.Bold)
   if(latest!=null){HorizontalDivider();Text("آخرین سرویس در کیلومتر ${Digits.toPersian(latest.currentKm.toString())}");Text("${latest.serviceType} بعدی: ${Digits.toPersian(latest.nextKm.toString())} کیلومتر"+(if(latest.nextDueAt!=Long.MAX_VALUE)" • ${PersianDate.fromMillis(latest.nextDueAt).format()}" else ""),color=accent)}else Text("سرویس تعویض روغن ثبت نشده است.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Text("برای ویرایش به راست و برای حذف به چپ بکشید",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
 }
}
 }
 FloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن")}
 if(chooser)ModernChoiceDialog("ثبت در پرونده خودرو","وسیله یا سرویس موردنظر را انتخاب کنید",listOf(ModernChoiceOption("وسیله نقلیه و پلاک","ثبت خودرو، موتور، وانت یا وسیله سنگین",Color(0xFF1B8F52),Icons.Filled.DirectionsCar){chooser=false;editingVehicleId=null;entry=VEHICLE_ENTRY},ModernChoiceOption("سرویس و تعویض روغن",if(vehicles.isEmpty())"ابتدا یک وسیله نقلیه ثبت کنید" else "ثبت کیلومتر، قطعات و موعد سرویس بعدی",Color(0xFFD9822B),Icons.Filled.Build,vehicles.isNotEmpty()){chooser=false;entry=SERVICE_ENTRY}),{chooser=false})
 pendingDelete?.let{vehicle->AlertDialog(onDismissRequest={pendingDelete=null},title={Text("حذف وسیله نقلیه؟")},text={Text("«${vehicle.title}» و سوابق سرویس آن حذف می‌شوند. پیامک‌ها و جرائم اصلی حذف نخواهند شد.")},confirmButton={Button({scope.launch{dao.detachVehicleMessages(vehicle.id);dao.deleteVehicleServices(vehicle.id);dao.deleteVehicle(vehicle.id);pendingDelete=null}}){Text("تأیید")}},dismissButton={TextButton({pendingDelete=null}){Text("انصراف")}})}
}
}

@Composable private fun VehiclePlateDetail(vehicle:VehicleEntity,people:List<CoveredPersonEntity>,services:List<VehicleOilServiceEntity>,messages:List<CivicMessageEntity>,unit:ir.kharjyar.app.core.money.MoneyUnit,dao:CivicDao,onBack:()->Unit){
 val scope=rememberCoroutineScope();val ownerName=vehicle.ownerName.ifBlank{vehicle.ownerId?.let{id->people.firstOrNull{it.id==id}?.name}?:"خودم"};val vehicleMessages=messages.filter{it.vehicleId==vehicle.id||(it.vehicleId==null&&ir.kharjyar.app.core.sms.IranianPlateMatcher.matches(it.body,vehicle.plate))};val fines=vehicleMessages.filter{it.kind==CivicMessageKind.TRAFFIC_FINE};val totalFine=fines.mapNotNull{ir.kharjyar.app.core.sms.TrafficFineParser.amountRial(it.body)}.sum();val vehicleServices=services.filter{it.vehicleId==vehicle.id}.sortedByDescending{it.servicedAt};val accent=Color(0xFF367BD6);val serviceAccent=Color(0xFF20A565)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  TextButton(onBack){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست پلاک‌ها")}
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){VehicleGraphic(vehicle.title,vehicle.vehicleType,accent,size=56.dp);Spacer(Modifier.width(12.dp));Column{Text(vehicle.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("پرونده وسیله نقلیه",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)}}
  Card(colors=CardDefaults.cardColors(containerColor=Color.Transparent),shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().border(1.2.dp,accent.copy(.55f),RoundedCornerShape(24.dp)),elevation=CardDefaults.cardElevation(5.dp)){Column(Modifier.background(Brush.linearGradient(listOf(accent.copy(.22f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){VehiclePlateBadge(vehicle.plate);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){VehicleInfoPill("نوع",vehicle.vehicleType,accent,Modifier.weight(1f));VehicleInfoPill("مالک",ownerName,accent,Modifier.weight(1f))};HorizontalDivider(color=accent.copy(.22f));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("تعداد جرائم",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(Digits.toPersian(fines.size.toString()),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)};Column(horizontalAlignment=Alignment.End){Text("مجموع جرائم",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(Money.format(totalFine,unit),color=Color(0xFFE14B55),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)}}}}
  DetailSectionHeader("جرائم و پیامک‌های این پلاک","✉️",Color(0xFFE14B55))
  if(vehicleMessages.isEmpty())EmptyDetailCard("هنوز پیامکی برای این پلاک ثبت نشده است.")
  vehicleMessages.sortedByDescending{it.receivedAt}.forEach{m->val messageAccent=if(m.kind==CivicMessageKind.TRAFFIC_FINE)Color(0xFFE14B55)else accent;Card(colors=CardDefaults.cardColors(containerColor=messageAccent.copy(.08f)),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth().border(1.dp,messageAccent.copy(.38f),RoundedCornerShape(20.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(PersianDate.formatDateTime(m.receivedAt),style=MaterialTheme.typography.labelMedium,color=messageAccent);if(!m.read)Surface(color=messageAccent.copy(.15f),shape=RoundedCornerShape(50)){Text("جدید",Modifier.padding(horizontal=9.dp,vertical=3.dp),color=messageAccent,style=MaterialTheme.typography.labelSmall)}};Text(m.body,style=MaterialTheme.typography.bodyMedium);if(!m.read)TextButton({scope.launch{dao.updateMessage(m.copy(read=true,vehicleId=vehicle.id))}}){Text("علامت‌گذاری به‌عنوان خوانده‌شده")}}}}
  DetailSectionHeader("سوابق سرویس","🔧",serviceAccent)
  if(vehicleServices.isEmpty())EmptyDetailCard("هنوز سابقه سرویسی ثبت نشده است.")
  vehicleServices.forEach{s->Card(colors=CardDefaults.cardColors(containerColor=Color.Transparent),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().border(1.dp,serviceAccent.copy(.42f),RoundedCornerShape(22.dp))){Column(Modifier.background(Brush.linearGradient(listOf(serviceAccent.copy(.14f),MaterialTheme.colorScheme.surface))).padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(s.serviceType,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium);Surface(color=serviceAccent.copy(.15f),shape=RoundedCornerShape(50)){Text(PersianDate.fromMillis(s.servicedAt).format(),Modifier.padding(horizontal=10.dp,vertical=5.dp),color=serviceAccent,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium)}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){VehicleInfoPill("کیلومتر ثبت",Digits.toPersian(s.currentKm.toString()),serviceAccent,Modifier.weight(1f));VehicleInfoPill("سرویس بعدی",Digits.toPersian(s.nextKm.toString()),serviceAccent,Modifier.weight(1f))};if(s.oilType.isNotBlank())Text("روغن مصرفی: ${s.oilType}",fontWeight=FontWeight.Medium);if(s.partsStatus.isNotBlank()){HorizontalDivider(color=serviceAccent.copy(.2f));Text(s.partsStatus,style=MaterialTheme.typography.bodySmall,lineHeight=22.sp)};if(s.note.isNotBlank())Text(s.note,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
 }
}
@Composable private fun VehicleInfoPill(label:String,value:String,color:Color,modifier:Modifier=Modifier){Surface(modifier,shape=RoundedCornerShape(14.dp),color=color.copy(.10f),border=BorderStroke(1.dp,color.copy(.25f))){Column(Modifier.padding(horizontal=11.dp,vertical=8.dp)){Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,fontWeight=FontWeight.Bold,maxLines=1)}}}
@Composable private fun DetailSectionHeader(title:String,icon:String,color:Color){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(color.copy(.15f),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Text(icon)};Spacer(Modifier.width(9.dp));Text(title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));HorizontalDivider(Modifier.weight(1f),color=color.copy(.3f))}}
@Composable private fun EmptyDetailCard(text:String){Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(.45f),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline.copy(.2f))){Text(text,Modifier.padding(18.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)}}

private enum class PlateKind(val label:String,val prefix:String,val letter:String,val background:Color,val foreground:Color=Color.Black){
 PERSONAL("شخصی","", "",Color(0xFFF8F8F8)),TAXI("تاکسی","تاکسی|","ت",Color(0xFFFFC928)),PUBLIC("عمومی/باربری","عمومی|","ع",Color(0xFFFFC928)),AGRICULTURAL("کشاورزی","کشاورزی|","ک",Color(0xFFFFC928)),TEMPORARY("گذر موقت","گذر موقت|","گ",Color.White),FREE_ZONE("منطقه آزاد","آزاد-","",Color.White),DISABLED("معلولین","معلولین|","♿",Color.White),GOVERNMENT("دولتی","دولتی|","الف",Color(0xFFD12E35),Color.White),HISTORIC("تاریخی","تاریخی|","",Color(0xFFC7A77A))
}
private fun plateKindOf(plate:String)=when{plate.startsWith("تاکسی|")->PlateKind.TAXI;plate.startsWith("عمومی|")->PlateKind.PUBLIC;plate.startsWith("کشاورزی|")->PlateKind.AGRICULTURAL;plate.startsWith("گذر موقت|")->PlateKind.TEMPORARY;plate.startsWith("آزاد-")->PlateKind.FREE_ZONE;plate.startsWith("معلولین|")->PlateKind.DISABLED;plate.startsWith("دولتی|")->PlateKind.GOVERNMENT;plate.startsWith("تاریخی|")->PlateKind.HISTORIC;else->PlateKind.PERSONAL}
private fun platePayload(plate:String)=when(plateKindOf(plate)){PlateKind.FREE_ZONE->plate.substringAfter('|',"");PlateKind.PERSONAL->plate;else->plate.substringAfter('|',plate)}

@Composable private fun VehiclePlateBadge(plate:String,modifier:Modifier=Modifier){
 val kind=plateKindOf(plate);val payload=platePayload(plate);val car=remember(payload){ir.kharjyar.app.core.sms.IranianPlateMatcher.extract(payload)}
 val motorcycle=remember(plate){Regex("موتور(\\d{3})/(\\d{5})").find(Digits.normalize(plate))};val region=if(kind==PlateKind.FREE_ZONE)plate.substringAfter("آزاد-").substringBefore('|') else ""
 CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){Surface(color=kind.background,shape=RoundedCornerShape(10.dp),border=BorderStroke(1.dp,Color(0xFF8A8A8A)),modifier=modifier.fillMaxWidth()){Row(Modifier.height(if(motorcycle!=null)76.dp else 58.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.width(if(kind==PlateKind.FREE_ZONE)58.dp else 38.dp).fillMaxHeight().background(Color(0xFF17458A),RoundedCornerShape(topStart=10.dp,bottomStart=10.dp)),contentAlignment=Alignment.Center){Text(if(region.isNotBlank())region else "IR",color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelSmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)};if(car!=null&&kind==PlateKind.PERSONAL){Row(Modifier.weight(1f).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly){Text(Digits.toPersian(car.leftTwo),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(car.letter,color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(Digits.toPersian(car.serialThree),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("ایران",color=kind.foreground,style=MaterialTheme.typography.labelSmall);Text(Digits.toPersian(car.iranCode),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)}}}else if(motorcycle!=null){Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(motorcycle.groupValues[1]),color=kind.foreground,fontWeight=FontWeight.Bold);HorizontalDivider(Modifier.width(110.dp),color=Color.Gray);Text(Digits.toPersian(motorcycle.groupValues[2]),color=kind.foreground,fontWeight=FontWeight.Bold)}}else Row(Modifier.weight(1f).padding(horizontal=10.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){if(kind==PlateKind.FREE_ZONE||kind==PlateKind.HISTORIC)Text(Digits.toPersian(payload),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)else{Text(Digits.toPersian(payload.take(2)),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Column(horizontalAlignment=Alignment.CenterHorizontally){if(kind==PlateKind.TAXI)Text("TAXI",color=kind.foreground,style=MaterialTheme.typography.labelSmall);Text(kind.letter,color=kind.foreground,fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleLarge)};Text(Digits.toPersian(payload.drop(2).take(3)),color=kind.foreground,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Column(horizontalAlignment=Alignment.CenterHorizontally){Text(if(kind==PlateKind.TEMPORARY)"خروج" else "ایران",color=kind.foreground,style=MaterialTheme.typography.labelSmall);Text(Digits.toPersian(payload.drop(5).take(2)),color=kind.foreground,fontWeight=FontWeight.Bold)}}}}}}
}
@Composable private fun VehicleSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=color);Text(label,style=MaterialTheme.typography.labelMedium)}}}

@Composable
private fun PlateNumberBox(value:String,placeholder:String,maxLength:Int,width:androidx.compose.ui.unit.Dp,modifier:Modifier=Modifier,onValueChange:(String)->Unit,onComplete:()->Unit={}) {
 BasicTextField(
  value=value,
  onValueChange={raw->val clean=Digits.normalize(raw).filter(Char::isDigit).take(maxLength);onValueChange(clean);if(clean.length==maxLength)onComplete()},
  modifier=modifier.width(width).height(58.dp).background(Color.White,RoundedCornerShape(7.dp)).border(1.dp,Color(0xFF777777),RoundedCornerShape(7.dp)).padding(horizontal=5.dp),
  singleLine=true,
  keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
  textStyle=LocalTextStyle.current.copy(color=Color.Black,fontWeight=FontWeight.Bold,textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontSize=18.sp),
  cursorBrush=androidx.compose.ui.graphics.SolidColor(Color.Black),
  decorationBox={inner->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){if(value.isEmpty())Text(placeholder,color=Color.Gray,style=MaterialTheme.typography.titleMedium);inner()}}
 )
}

@Composable private fun IranianPlateInput(value:String,onValueChange:(String)->Unit){
 val initial=remember{ir.kharjyar.app.core.sms.IranianPlateMatcher.extract(value)}
 var left by remember{mutableStateOf(initial?.leftTwo.orEmpty())};var letter by remember{mutableStateOf(initial?.letter.orEmpty())};var serial by remember{mutableStateOf(initial?.serialThree.orEmpty())};var iran by remember{mutableStateOf(initial?.iranCode.orEmpty())};var showLetters by remember{mutableStateOf(false)}
 val leftFocus=remember{FocusRequester()};val serialFocus=remember{FocusRequester()};val iranFocus=remember{FocusRequester()};val defaultPlate=remember{value=="12س345ایران11"};var leftTouched by remember{mutableStateOf(false)};var serialTouched by remember{mutableStateOf(false)};var iranTouched by remember{mutableStateOf(false)}
 val letters=listOf("الف","ب","پ","ت","ث","ج","د","س","ص","ط","ق","ل","م","ن","و","ه","ی","♿")
 fun emit(){onValueChange("$left$letter${serial}ایران$iran")}
 Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
  Text("شماره پلاک ایران",style=MaterialTheme.typography.labelLarge)
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
   Card(shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFFF1F1F1))){
    Row(Modifier.fillMaxWidth().padding(7.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){
     Box(Modifier.width(28.dp).height(58.dp).background(Color(0xFF17458A),RoundedCornerShape(6.dp)),contentAlignment=Alignment.Center){Text("IR",color=Color.White,fontWeight=FontWeight.Bold)}
     PlateNumberBox(left,"۱۲",2,58.dp,Modifier.focusRequester(leftFocus).onFocusChanged{if(it.isFocused&&!leftTouched){leftTouched=true;if(defaultPlate){left="";emit()}}},{left=it;emit()},{showLetters=true})
     Surface(Modifier.width(52.dp).height(58.dp).clickable{showLetters=true},shape=RoundedCornerShape(7.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFF777777))){Box(contentAlignment=Alignment.Center){Text(letter.ifBlank{"س"},color=Color.Black,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)}}
     PlateNumberBox(serial,"۳۴۵",3,78.dp,Modifier.focusRequester(serialFocus).onFocusChanged{if(it.isFocused&&!serialTouched){serialTouched=true;if(defaultPlate){serial="";emit()}}},{serial=it;emit()},{iranFocus.requestFocus()})
     Box(Modifier.width(78.dp).height(58.dp)){PlateNumberBox(iran,"۱۱",2,78.dp,Modifier.focusRequester(iranFocus).onFocusChanged{if(it.isFocused&&!iranTouched){iranTouched=true;if(defaultPlate){iran="";emit()}}},{iran=it;emit()});Text("ایران",Modifier.align(Alignment.TopCenter).padding(top=2.dp),color=Color.Black,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelSmall)}
    }
   }
  }
 }
 if(showLetters){val listState=rememberLazyListState(initialFirstVisibleItemIndex=(letters.indexOf(letter).coerceAtLeast(0)-2).coerceAtLeast(0));AlertDialog(onDismissRequest={showLetters=false},title={Text("حرف پلاک را بچرخانید")},text={Box(Modifier.fillMaxWidth().height(220.dp),contentAlignment=Alignment.Center){LazyColumn(state=listState,flingBehavior=rememberSnapFlingBehavior(listState),horizontalAlignment=Alignment.CenterHorizontally,contentPadding=PaddingValues(vertical=82.dp)){items(letters){item->val selected=item==letter;Text(item,Modifier.fillMaxWidth().clickable{letter=item;emit();showLetters=false;serialFocus.requestFocus()}.padding(vertical=10.dp),textAlign=androidx.compose.ui.text.style.TextAlign.Center,color=if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(.55f),fontWeight=if(selected)FontWeight.Bold else FontWeight.Normal,style=if(selected)MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium)}}}},confirmButton={TextButton({val index=(listState.firstVisibleItemIndex+2).coerceAtMost(letters.lastIndex);letter=letters[index];emit();showLetters=false;serialFocus.requestFocus()}){Text("انتخاب")}},dismissButton={TextButton({showLetters=false}){Text("انصراف")}})}
}

@Composable private fun SpecialPlateInput(kind:PlateKind,value:String,region:String,onRegionChange:(String)->Unit,onValueChange:(String)->Unit){
 val payload=platePayload(value);val regions=listOf("کیش","قشم","اروند","انزلی","ارس","ماکو","چابهار")
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  if(kind==PlateKind.FREE_ZONE)ComboBox("منطقه آزاد",regions,region,onRegionChange)
  OutlinedTextField(payload,{raw->val clean=Digits.normalize(raw).filter(Char::isDigit).take(if(kind==PlateKind.FREE_ZONE)5 else 7);onValueChange(if(kind==PlateKind.FREE_ZONE)"آزاد-$region|$clean" else kind.prefix+clean)},label={Text(when(kind){PlateKind.FREE_ZONE->"شماره پنج‌رقمی پلاک";PlateKind.TEMPORARY->"شماره پلاک گذر موقت";else->"اعداد پلاک ${kind.label}"})},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,modifier=Modifier.fillMaxWidth())
  VehiclePlateBadge(if(kind==PlateKind.FREE_ZONE)"آزاد-$region|$payload" else kind.prefix+payload)
 }
}

@Composable private fun MotorcyclePlateInput(value:String,onValueChange:(String)->Unit){
 val normalized=Digits.normalize(value).filter(Char::isDigit).take(8);var top by remember{mutableStateOf(normalized.take(3))};var bottom by remember{mutableStateOf(normalized.drop(3).take(5))}
 fun emit(t:String=top,b:String=bottom){top=t;bottom=b;onValueChange("موتور$t/$b")}
 val colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.Black,unfocusedTextColor=Color.Black,focusedContainerColor=Color.White,unfocusedContainerColor=Color.White,cursorColor=Color.Black,focusedBorderColor=Color(0xFF17458A),unfocusedBorderColor=Color(0xFF777777))
 Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text("پلاک موتورسیکلت",style=MaterialTheme.typography.labelLarge);CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){Card(shape=RoundedCornerShape(12.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFF4F4F4))){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){Box(Modifier.width(30.dp).height(112.dp).background(Color(0xFF17458A),RoundedCornerShape(6.dp)),contentAlignment=Alignment.Center){Text("IR",color=Color.White,fontWeight=FontWeight.Bold)};Column(Modifier.width(160.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){OutlinedTextField(top,{emit(t=Digits.normalize(it).filter(Char::isDigit).take(3))},placeholder={Text("۱۲۳",color=Color.Gray)},singleLine=true,textStyle=LocalTextStyle.current.copy(color=Color.Black,textAlign=androidx.compose.ui.text.style.TextAlign.Center),colors=colors,modifier=Modifier.fillMaxWidth().height(52.dp));OutlinedTextField(bottom,{emit(b=Digits.normalize(it).filter(Char::isDigit).take(5))},placeholder={Text("۱۲۳۴۵",color=Color.Gray)},singleLine=true,textStyle=LocalTextStyle.current.copy(color=Color.Black,textAlign=androidx.compose.ui.text.style.TextAlign.Center),colors=colors,modifier=Modifier.fillMaxWidth().height(52.dp))}}}};Text("سه رقم در ردیف بالا و پنج رقم در ردیف پایین",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
}
@Composable private fun VehicleEntryPage(vm:AppViewModel,kind:Int,existing:VehicleEntity?=null,onDone:()->Unit,onCancel:()->Unit){val dao=vm.repo.db.civicDao();val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val context=LocalContext.current;val scope=rememberCoroutineScope();var title by remember(existing?.id){mutableStateOf(existing?.title.orEmpty())};var plate by remember(existing?.id){mutableStateOf(existing?.plate?:"12س345ایران11")};var owner by remember(existing?.id){mutableStateOf(existing?.ownerId)};var ownerName by remember(existing?.id){mutableStateOf(existing?.ownerName.orEmpty())};var customOwner by remember(existing?.id){mutableStateOf(existing?.ownerName?.isNotBlank()==true)};var vehicleType by remember(existing?.id){mutableStateOf(existing?.vehicleType?:"خودرو سواری")};var plateKind by remember(existing?.id){mutableStateOf(existing?.plate?.let(::plateKindOf)?:PlateKind.PERSONAL)};var freeZone by remember(existing?.id){mutableStateOf(existing?.plate?.takeIf{it.startsWith("آزاد-")}?.substringAfter("آزاد-")?.substringBefore('|')?:"کیش")};var vehicleId by remember{mutableStateOf(vehicles.firstOrNull()?.id)};var currentKm by remember{mutableStateOf("")};var intervalKm by remember{mutableStateOf("5000")};var oilType by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today().plusMonths(6))};var serviceType by remember{mutableStateOf("تعویض روغن")};val serviceParts=listOf("فیلتر روغن","فیلتر اتاق","فیلتر هوا","صافی بنزین","روغن هیدرولیک","روغن ترمز","روغن گیربکس");val partStates=remember{mutableStateMapOf<String,String>()};val isVehicle=kind==VEHICLE_ENTRY;val accent=if(isVehicle)Color(0xFF1B8F52) else Color(0xFFD33B45);val vehicleTypes=listOf("خودرو سواری","تاکسی","موتورسیکلت","وانت","کامیون","تریلر","اتوبوس","مینی‌بوس","ماشین‌آلات","سایر");val isMotorcycle=vehicleType=="موتورسیکلت";val motorcycleDigits=Digits.normalize(plate).filter(Char::isDigit);val specialPayload=platePayload(plate);val canonicalPlate=when{isMotorcycle->motorcycleDigits.takeIf{it.length==8}?.let{"موتور${it.take(3)}/${it.drop(3)}"};plateKind==PlateKind.PERSONAL->ir.kharjyar.app.core.sms.IranianPlateMatcher.canonical(plate);plateKind==PlateKind.FREE_ZONE&&specialPayload.length==5->"آزاد-$freeZone|$specialPayload";specialPayload.length==7->plateKind.prefix+specialPayload;else->null}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};ModernSummaryHero(if(isVehicle&&existing!=null)"ویرایش وسیله نقلیه" else if(isVehicle)"ثبت وسیله نقلیه و پلاک" else "ثبت سرویس و تعویض روغن",if(isVehicle)"مشخصات وسیله، مالک و قالب پلاک" else "کیلومتر، قطعات و موعد سرویس بعدی",accent,listOf(SummaryMetric(if(isVehicle)"نوع وسیله" else "کیلومتر فعلی",if(isVehicle)vehicleType else currentKm.ifBlank{"—"},accent,if(isVehicle)Icons.Filled.DirectionsCar else Icons.Filled.Build),SummaryMetric(if(isVehicle)"نوع پلاک" else "سرویس",if(isVehicle)plateKind.label else serviceType,MaterialTheme.colorScheme.primary,Icons.Filled.FactCheck)),if(isVehicle)Icons.Filled.DirectionsCar else Icons.Filled.Build);if(isVehicle){
 Text("نوع وسیله نقلیه",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){vehicleTypes.forEach{type->FilterChip(vehicleType==type,{if(vehicleType!=type){vehicleType=type;plate="";plateKind=when(type){"تاکسی"->PlateKind.TAXI;"کامیون","تریلر","اتوبوس","مینی‌بوس"->PlateKind.PUBLIC;"ماشین‌آلات"->PlateKind.AGRICULTURAL;else->PlateKind.PERSONAL}}},{Text(type)})}}
 OutlinedTextField(title,{title=it},label={Text(if(isMotorcycle)"نام یا مدل موتورسیکلت" else "نام یا مدل وسیله نقلیه")},modifier=Modifier.fillMaxWidth())
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=accent.copy(.08f),border=BorderStroke(1.dp,accent.copy(.25f))){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){VehicleGraphic(title,vehicleType,accent,size=68.dp);Spacer(Modifier.width(12.dp));Column{Text("نمای گرافیکی وسیله",fontWeight=FontWeight.Bold);Text(if(title.isBlank())"با نوشتن مدل، تصویر متناسب انتخاب می‌شود" else title,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
 if(isMotorcycle)MotorcyclePlateInput(plate,{plate=it})else{Text("نوع پلاک",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){PlateKind.entries.forEach{k->FilterChip(plateKind==k,{plateKind=k;plate=if(k==PlateKind.PERSONAL)"" else k.prefix},{Text(k.label)})}};if(plateKind==PlateKind.PERSONAL)IranianPlateInput(plate,{plate=it})else SpecialPlateInput(plateKind,plate,freeZone,{freeZone=it;plate="آزاد-$it|${platePayload(plate)}"},{plate=it})}
 Text(canonicalPlate?.let{"پلاک استاندارد: ${Digits.toPersian(it)}"}?:if(isMotorcycle)"سه رقم بالا و پنج رقم پایین را کامل کنید." else "قالب پلاک انتخاب‌شده را کامل کنید.",color=if(plate.isNotBlank()&&canonicalPlate==null)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
 Text("مالک",style=MaterialTheme.typography.titleMedium);Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(owner==null&&!customOwner,{owner=null;customOwner=false;ownerName=""},{Text("خودم")});people.forEach{person->FilterChip(owner==person.id&&!customOwner,{owner=person.id;customOwner=false;ownerName=""},{Text(person.name)})};FilterChip(customOwner,{owner=null;customOwner=true},{Text("شخص دیگر")})}
 if(customOwner)OutlinedTextField(ownerName,{ownerName=it},label={Text("نام مالک (همسر، فرزند یا شخص دیگر)")},singleLine=true,modifier=Modifier.fillMaxWidth())
}else{
 Text("نوع سرویس",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(serviceType=="تعویض روغن",{serviceType="تعویض روغن";intervalKm="5000"},{Text("تعویض روغن")});FilterChip(serviceType=="تعویض تسمه تایم",{serviceType="تعویض تسمه تایم";intervalKm="60000"},{Text("تسمه تایم")})}
 ComboBox("انتخاب وسیله نقلیه",vehicles.map{it.id},vehicleId?:vehicles.firstOrNull()?.id,{vehicleId=it},labelOf={id->vehicles.firstOrNull{it.id==id}?.let{"${it.title} • ${Digits.toPersian(it.plate)}"}?:"انتخاب کنید"})
 OutlinedTextField(currentKm,{currentKm=Digits.normalize(it).filter(Char::isDigit)},label={Text("کیلومتر فعلی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(intervalKm,{intervalKm=Digits.normalize(it).filter(Char::isDigit)},label={Text(if(serviceType=="تعویض تسمه تایم")"تعویض بعدی پس از چند کیلومتر" else "فاصله تعویض بعدی (کیلومتر)")},modifier=Modifier.fillMaxWidth())
 if(serviceType=="تعویض روغن"){
  OutlinedTextField(oilType,{oilType=it},label={Text("نوع یا برند روغن")},modifier=Modifier.fillMaxWidth())
  Text("وضعیت قطعات مصرفی",style=MaterialTheme.typography.titleMedium)
  Text("برای هر قطعه یکی از حالت‌های بررسی شد یا تعویض شد را انتخاب کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  serviceParts.forEach{part->
   val state=partStates[part].orEmpty()
   Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){Text(part,Modifier.weight(1f));FilterChip(state=="بررسی شد",{partStates[part]=if(state=="بررسی شد")"" else "بررسی شد"},{Text("بررسی شد")});Spacer(Modifier.width(5.dp));FilterChip(state=="تعویض شد",{partStates[part]=if(state=="تعویض شد")"" else "تعویض شد"},{Text("تعویض شد")})}}
  }
  Text("موعد زمانی پیشنهادی");PersianDateField(due,{due=it})
  Text("پیشنهاد: سرویس روغن را بر اساس هرکدام که زودتر فرا برسد—کیلومتر تعیین‌شده یا حدود ۶ ماه—انجام دهید؛ برای استفاده سنگین می‌توانید ۳ ماه را انتخاب کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)
 }else{
  Text("تسمه تایم معمولاً هر ۶۰٬۰۰۰ کیلومتر تعویض می‌شود. یادآوری این سرویس کیلومتری است و تاریخ اجباری ندارد.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)
 }
 OutlinedTextField(note,{note=it},label={Text("توضیحات")},modifier=Modifier.fillMaxWidth())
};Button({scope.launch{if(isVehicle){val value=VehicleEntity(id=existing?.id?:0,ownerId=if(customOwner)null else owner,title=normalizeVehicleTitle(title),plate=canonicalPlate?:return@launch,vehicleType=vehicleType,ownerName=if(customOwner)ownerName.trim()else "");if(existing==null)dao.insertVehicle(value)else dao.updateVehicle(value)}else{val selected=vehicles.firstOrNull{it.id==vehicleId}?:return@launch;val nowKm=currentKm.toIntOrNull()?:0;val defaultInterval=if(serviceType=="تعویض تسمه تایم")60000 else 5000;val nextKm=nowKm+(intervalKm.toIntOrNull()?:defaultInterval);val hasDate=serviceType=="تعویض روغن";val at=if(hasDate)due.startOfDayMillis()+9*60*60*1000L else Long.MAX_VALUE;val reminderId=if(hasDate){val id=vm.repo.db.reminderDao().insert(ReminderEntity(title="$serviceType ${selected.title}",category="سرویس خودرو",note="پلاک ${selected.plate} — موعد کیلومتر ${Digits.toPersian(nextKm.toString())}",nextAt=at));LifeReminderWorker.schedule(context,id,at);id}else null;val statuses=partStates.filterValues{it.isNotBlank()}.entries.joinToString("؛ "){"${it.key}: ${it.value}"};dao.insertOilService(VehicleOilServiceEntity(vehicleId=selected.id,servicedAt=System.currentTimeMillis(),currentKm=nowKm,nextKm=nextKm,nextDueAt=at,oilType=oilType.trim(),note=note.trim(),reminderId=reminderId,serviceType=serviceType,partsStatus=statuses))};showSavedMessage(context,"اطلاعات وسیله نقلیه");onDone()}},enabled=if(isVehicle)title.isNotBlank()&&canonicalPlate!=null&&(!customOwner||ownerName.isNotBlank()) else vehicleId!=null&&currentKm.isNotBlank(),modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text(if(isVehicle&&existing!=null)"ذخیره تغییرات" else if(isVehicle)"ثبت وسیله نقلیه" else "ثبت سرویس و فعال‌سازی یادآور")}}
}
