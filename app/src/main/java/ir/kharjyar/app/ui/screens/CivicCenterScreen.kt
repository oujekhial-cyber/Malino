package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ModernChoiceDialog
import ir.kharjyar.app.ui.components.ModernChoiceOption
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.DoneAll
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.showSavedMessage
import kotlinx.coroutines.launch

private const val ENTRY_PERSON=10
private const val ENTRY_VEHICLE=11
private const val ENTRY_UTILITY_BILL=12
private const val BILL_ALL="همه"
private val utilityBillTypes=listOf("آب","برق","گاز","تلفن","سایر")

@Composable
fun CivicCenterScreen(vm:AppViewModel){
 val dao=vm.repo.db.civicDao();val messages by dao.observeMessages().collectAsState(initial=emptyList());val utilityBills by dao.observeUtilityBills().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val scope=rememberCoroutineScope()
 var selectedKind by remember{mutableStateOf<Int?>(null)};var entry by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)};var utilityFilter by remember{mutableStateOf(BILL_ALL)}
 BackHandler(enabled=entry!=null||selectedKind!=null){if(entry!=null)entry=null else selectedKind=null}
 if(entry==ENTRY_UTILITY_BILL){CivicBillEntry(vm,{entry=null},{entry=null});return}
 if(entry!=null){CivicProfileEntry(vm,entry!!,{entry=null},{entry=null});return}
 val sections=listOf(CivicMessageKind.TRAFFIC_FINE,CivicMessageKind.UTILITY_BILL,CivicMessageKind.INSURANCE,CivicMessageKind.ADLIRAN)
 Box(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   if(selectedKind==null){
    ModernSummaryHero("قبوض شهروندی","پیامک‌های خدماتی و وضعیت رسیدگی",Color(0xFF397BD5),listOf(SummaryMetric("خوانده‌نشده",Digits.toPersian(messages.count{!it.read}.toString()),Color(0xFFE24B57),Icons.Filled.NotificationsActive),SummaryMetric("خوانده‌شده",Digits.toPersian(messages.count{it.read}.toString()),Color(0xFF1B9A61),Icons.Filled.DoneAll)),Icons.Filled.LocationCity)
    Text("بخش‌ها",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    sections.forEach{kind->val rows=messages.filter{it.kind==kind};val accent=civicAccent(kind);Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().clickable{selectedKind=kind}.border(1.2.dp,accent.copy(.65f),RoundedCornerShape(18.dp))){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(civicSectionTitle(kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("${Digits.toPersian(rows.size.toString())} مورد • ${Digits.toPersian(rows.count{!it.read}.toString())} جدید",style=MaterialTheme.typography.bodySmall)};Text("←",color=accent,style=MaterialTheme.typography.titleLarge)}}
    }
    
   }else{
    val kind=selectedKind!!;val sectionMessages=messages.filter{it.kind==kind};val shown=if(kind==CivicMessageKind.UTILITY_BILL&&utilityFilter!=BILL_ALL)sectionMessages.filter{m->utilityBills.firstOrNull{it.id==m.utilityBillId}?.type==utilityFilter}else sectionMessages;val accent=civicAccent(kind)
    TextButton({selectedKind=null}){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به قبوض شهروندی")}
    Text(civicSectionTitle(kind),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Text(civicSectionSubtitle(kind),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    if(kind==CivicMessageKind.UTILITY_BILL){Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){(listOf(BILL_ALL)+utilityBillTypes).forEach{type->FilterChip(utilityFilter==type,{utilityFilter=type},{Text(type)})}};Text("اشتراک‌های ثبت‌شده",fontWeight=FontWeight.Bold);if(utilityBills.isEmpty())Text("ابتدا شناسه قبض یا شماره اشتراک را ثبت کنید تا پیامک‌ها به محل درست متصل شوند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);utilityBills.filter{utilityFilter==BILL_ALL||it.type==utilityFilter}.forEach{bill->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=accent.copy(.06f))){Column(Modifier.padding(12.dp)){Text("${bill.title} • ${bill.type}",fontWeight=FontWeight.Bold);Text("شناسه/اشتراک: ${Digits.toPersian(bill.identifier)}",style=MaterialTheme.typography.bodySmall);if(bill.address.isNotBlank())Text(bill.address,style=MaterialTheme.typography.bodySmall)}}};Text("پیامک‌های تطبیق‌یافته",fontWeight=FontWeight.Bold)}
    if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text("هنوز موردی در این بخش ثبت نشده است.",Modifier.padding(24.dp))}
    shown.forEach{m->Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(m.kind==CivicMessageKind.UTILITY_BILL)utilityBills.firstOrNull{it.id==m.utilityBillId}?.let{"${it.title} • ${it.type}"}?:civicTitle(m.kind) else civicTitle(m.kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(m.read)"خوانده‌شده" else "جدید",color=if(m.read)Color(0xFF1B8F52) else Color(0xFFD33B45),fontWeight=FontWeight.Bold)};Text("${m.sender} • ${PersianDate.formatDateTime(m.receivedAt)}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(m.body);Row{if(!m.read)TextButton({scope.launch{dao.updateMessage(m.copy(read=true))}}){Text("خواندم")};TextButton({scope.launch{dao.deleteMessage(m.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}}}}}
   }
  }
  if(selectedKind==null)FloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن پروفایل")}
  if(selectedKind==CivicMessageKind.UTILITY_BILL)ExtendedFloatingActionButton(text={Text("ثبت اشتراک قبض")},onClick={entry=ENTRY_UTILITY_BILL},modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp),icon={Icon(Icons.Filled.Add,null)})
  if(chooser)ModernChoiceDialog("افزودن پرونده","اطلاعاتی که می‌خواهید مدیریت کنید را انتخاب کنید",listOf(ModernChoiceOption("فرد تحت پوشش","ثبت مشخصات اعضای خانواده یا افراد مرتبط",Color(0xFF1B8F52),Icons.Filled.PersonAdd){chooser=false;entry=ENTRY_PERSON},ModernChoiceOption("وسیله نقلیه و پلاک","ثبت وسیله، مالک و شماره پلاک",Color(0xFF397BD5),Icons.Filled.DirectionsCar){chooser=false;entry=ENTRY_VEHICLE}),{chooser=false})
 }
}

private fun civicSectionTitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جرائم راهنمایی و رانندگی";CivicMessageKind.UTILITY_BILL->"قبوض خدماتی";CivicMessageKind.INSURANCE->"بیمه و خدمات پوشش";else->"ابلاغیه‌ها و عدل‌ایران"}
private fun civicSectionSubtitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جریمه‌ها و پیامک‌های مرتبط با پلاک‌های ثبت‌شده";CivicMessageKind.UTILITY_BILL->"قبوض آب، برق، گاز و سایر خدمات";CivicMessageKind.INSURANCE->"اطلاعیه‌های بیمه افراد تحت پوشش";else->"متن کامل پیامک‌های قضایی و ابلاغ الکترونیک"}

@Composable private fun CivicSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=color);Text(label)}}}
private fun civicAccent(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->Color(0xFFD33B45);CivicMessageKind.UTILITY_BILL->Color(0xFF1B8F52);CivicMessageKind.INSURANCE->Color(0xFF367BD6);else->Color(0xFF8254B8)}
private fun civicTitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جریمه راهنمایی و رانندگی";CivicMessageKind.UTILITY_BILL->"قبض خدماتی";CivicMessageKind.INSURANCE->"بیمه";else->"اطلاع‌رسانی عدل‌ایران"}
private fun utilityBillType(message:CivicMessageEntity):String{val text="${message.sender} ${message.body}";return when{listOf("آب","آبفا").any{text.contains(it,true)}->"آب";listOf("برق","توانیر").any{text.contains(it,true)}->"برق";listOf("گاز").any{text.contains(it,true)}->"گاز";listOf("تلفن","مخابرات").any{text.contains(it,true)}->"تلفن";else->"سایر"}}

@Composable private fun CivicBillEntry(vm:AppViewModel,onDone:()->Unit,onCancel:()->Unit){
 val context=LocalContext.current;val scope=rememberCoroutineScope();var type by remember{mutableStateOf("آب")};var title by remember{mutableStateOf("")};var billId by remember{mutableStateOf("")};var address by remember{mutableStateOf("")};var note by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به قبوض خدماتی")}
  Text("ثبت اشتراک قبض",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text("شناسه قبض یا شماره اشتراک را یک‌بار ثبت کنید. از این پس فقط پیامکی که همین شناسه را داشته باشد به این قبض متصل می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Text("نوع خدمت");Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){utilityBillTypes.forEach{item->FilterChip(type==item,{type=item},{Text(item)})}}
  OutlinedTextField(title,{title=it},label={Text("عنوان محل (مثلاً برق خانه)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
  OutlinedTextField(billId,{billId=Digits.normalize(it).filter(Char::isDigit)},label={Text("شناسه قبض یا شماره اشتراک")},modifier=Modifier.fillMaxWidth(),singleLine=true)
  OutlinedTextField(address,{address=it},label={Text("آدرس یا محل مصرف (اختیاری)")},modifier=Modifier.fillMaxWidth())
  OutlinedTextField(note,{note=it},label={Text("توضیحات (اختیاری)")},modifier=Modifier.fillMaxWidth(),minLines=2)
  Button({scope.launch{val id=ir.kharjyar.app.core.sms.UtilityBillMatcher.normalizeIdentifier(billId);val result=vm.repo.db.civicDao().insertUtilityBill(UtilityBillProfileEntity(title=title.trim(),type=type,identifier=id,address=address.trim(),note=note.trim()));if(result>0){showSavedMessage(context,"اشتراک قبض");onDone()}}},enabled=title.isNotBlank()&&ir.kharjyar.app.core.sms.UtilityBillMatcher.normalizeIdentifier(billId).length>=4,modifier=Modifier.fillMaxWidth()){Text("ثبت شناسه و فعال‌کردن تشخیص پیامک")}
 }
}

@Composable private fun CivicProfileEntry(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit){val context=LocalContext.current;val dao=vm.repo.db.civicDao();val people by dao.observePeople().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var name by remember{mutableStateOf("")};var relation by remember{mutableStateOf("")};var insurer by remember{mutableStateOf("")};var policy by remember{mutableStateOf("")};var vehicleTitle by remember{mutableStateOf("")};var plate by remember{mutableStateOf("")};var owner by remember{mutableStateOf<Long?>(null)};val person=kind==ENTRY_PERSON;val accent=if(person)Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};ModernSummaryHero(if(person)"ثبت فرد تحت پوشش" else "ثبت وسیله نقلیه و پلاک",if(person)"اتصال پیامک‌های بیمه و خدمات به اعضای خانواده" else "دسته‌بندی جرائم و پیامک‌های مرتبط با خودرو",accent,listOf(SummaryMetric(if(person)"نام فرد" else "عنوان وسیله",if(person)name.ifBlank{"—"} else vehicleTitle.ifBlank{"—"},accent,if(person)Icons.Filled.Person else Icons.Filled.DirectionsCar),SummaryMetric(if(person)"نسبت" else "پلاک",if(person)relation.ifBlank{"—"} else plate.ifBlank{"—"},MaterialTheme.colorScheme.primary,if(person)Icons.Filled.Groups else Icons.Filled.FactCheck)),if(person)Icons.Filled.PersonAdd else Icons.Filled.DirectionsCar);if(person){OutlinedTextField(name,{name=it},label={Text("نام")},modifier=Modifier.fillMaxWidth());OutlinedTextField(relation,{relation=it},label={Text("نسبت")},modifier=Modifier.fillMaxWidth());OutlinedTextField(insurer,{insurer=it},label={Text("شرکت یا سازمان بیمه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(policy,{policy=it},label={Text("شماره بیمه‌نامه")},modifier=Modifier.fillMaxWidth())}else{OutlinedTextField(vehicleTitle,{vehicleTitle=it},label={Text("عنوان خودرو")},modifier=Modifier.fillMaxWidth());OutlinedTextField(plate,{plate=it},label={Text("شماره پلاک")},modifier=Modifier.fillMaxWidth());if(people.isNotEmpty()){Text("مالک");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){people.forEach{p->FilterChip(owner==p.id,{owner=p.id},{Text(p.name)})}}}else Text("در صورت نیاز، ابتدا یک فرد تحت پوشش ثبت کنید.",style=MaterialTheme.typography.bodySmall)};Button({scope.launch{if(person)dao.insertPerson(CoveredPersonEntity(name=name.trim(),relation=relation.trim(),insuranceProvider=insurer.trim(),policyNumber=policy.trim()))else dao.insertVehicle(VehicleEntity(ownerId=owner,title=vehicleTitle.trim(),plate=plate.trim()));showSavedMessage(context,"اطلاعات");onDone()}},enabled=if(person)name.isNotBlank()else vehicleTitle.isNotBlank()&&plate.isNotBlank(),modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text(if(person)"ثبت فرد" else "ثبت وسیله نقلیه")}}
}
