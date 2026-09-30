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
 val dao=vm.repo.db.civicDao();val messages by dao.observeMessages().collectAsState(initial=emptyList());val people by dao.observePeople().collectAsState(initial=emptyList());val vehicles by dao.observeVehicles().collectAsState(initial=emptyList());val scope=rememberCoroutineScope()
 var selectedKind by remember{mutableStateOf<Int?>(null)};var entry by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)};var utilityFilter by remember{mutableStateOf(BILL_ALL)}
 BackHandler(enabled=entry!=null||selectedKind!=null){if(entry!=null)entry=null else selectedKind=null}
 if(entry==ENTRY_UTILITY_BILL){CivicBillEntry(vm,{entry=null},{entry=null});return}
 if(entry!=null){CivicProfileEntry(vm,entry!!,{entry=null},{entry=null});return}
 val sections=listOf(CivicMessageKind.TRAFFIC_FINE,CivicMessageKind.UTILITY_BILL,CivicMessageKind.INSURANCE,CivicMessageKind.ADLIRAN)
 Box(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   if(selectedKind==null){
    Text("قبوض شهروندی",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Text("پیامک‌های خدماتی به‌صورت خودکار در بخش مرتبط دسته‌بندی می‌شوند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){CivicSummary("خوانده‌نشده",messages.count{!it.read},Color(0xFFD33B45),Modifier.weight(1f));CivicSummary("خوانده‌شده",messages.count{it.read},Color(0xFF1B8F52),Modifier.weight(1f))}
    Text("بخش‌ها",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    sections.forEach{kind->val rows=messages.filter{it.kind==kind};val accent=civicAccent(kind);Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().clickable{selectedKind=kind}.border(1.2.dp,accent.copy(.65f),RoundedCornerShape(18.dp))){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(civicSectionTitle(kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("${Digits.toPersian(rows.size.toString())} مورد • ${Digits.toPersian(rows.count{!it.read}.toString())} جدید",style=MaterialTheme.typography.bodySmall)};Text("←",color=accent,style=MaterialTheme.typography.titleLarge)}}
    }
    
   }else{
    val kind=selectedKind!!;val sectionMessages=messages.filter{it.kind==kind};val shown=if(kind==CivicMessageKind.UTILITY_BILL&&utilityFilter!=BILL_ALL)sectionMessages.filter{utilityBillType(it)==utilityFilter}else sectionMessages;val accent=civicAccent(kind)
    TextButton({selectedKind=null}){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به قبوض شهروندی")}
    Text(civicSectionTitle(kind),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Text(civicSectionSubtitle(kind),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    if(kind==CivicMessageKind.UTILITY_BILL){Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){(listOf(BILL_ALL)+utilityBillTypes).forEach{type->FilterChip(utilityFilter==type,{utilityFilter=type},{Text(type)})}}}
    if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text("هنوز موردی در این بخش ثبت نشده است.",Modifier.padding(24.dp))}
    shown.forEach{m->Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(civicTitle(m.kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(m.read)"خوانده‌شده" else "جدید",color=if(m.read)Color(0xFF1B8F52) else Color(0xFFD33B45),fontWeight=FontWeight.Bold)};Text("${m.sender} • ${PersianDate.formatDateTime(m.receivedAt)}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(m.body);Row{if(!m.read)TextButton({scope.launch{dao.updateMessage(m.copy(read=true))}}){Text("خواندم")};TextButton({scope.launch{dao.deleteMessage(m.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}}}}}
   }
  }
  if(selectedKind==null)FloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن پروفایل")}
  if(selectedKind==CivicMessageKind.UTILITY_BILL)ExtendedFloatingActionButton(text={Text("افزودن قبض")},onClick={entry=ENTRY_UTILITY_BILL},modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp),icon={Icon(Icons.Filled.Add,null)})
  if(chooser)AlertDialog(onDismissRequest={chooser=false},title={Text("چه موردی ثبت شود؟")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Button({chooser=false;entry=ENTRY_PERSON},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFF1B8F52))){Text("فرد تحت پوشش")};Button({chooser=false;entry=ENTRY_VEHICLE},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFFD33B45))){Text("وسیله نقلیه و پلاک")}}},confirmButton={})
 }
}

private fun civicSectionTitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جرائم راهنمایی و رانندگی";CivicMessageKind.UTILITY_BILL->"قبوض خدماتی";CivicMessageKind.INSURANCE->"بیمه و خدمات پوشش";else->"ابلاغیه‌ها و عدل‌ایران"}
private fun civicSectionSubtitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جریمه‌ها و پیامک‌های مرتبط با پلاک‌های ثبت‌شده";CivicMessageKind.UTILITY_BILL->"قبوض آب، برق، گاز و سایر خدمات";CivicMessageKind.INSURANCE->"اطلاعیه‌های بیمه افراد تحت پوشش";else->"متن کامل پیامک‌های قضایی و ابلاغ الکترونیک"}

@Composable private fun CivicSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=color);Text(label)}}}
private fun civicAccent(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->Color(0xFFD33B45);CivicMessageKind.UTILITY_BILL->Color(0xFF1B8F52);CivicMessageKind.INSURANCE->Color(0xFF367BD6);else->Color(0xFF8254B8)}
private fun civicTitle(kind:Int)=when(kind){CivicMessageKind.TRAFFIC_FINE->"جریمه راهنمایی و رانندگی";CivicMessageKind.UTILITY_BILL->"قبض خدماتی";CivicMessageKind.INSURANCE->"بیمه";else->"اطلاع‌رسانی عدل‌ایران"}
private fun utilityBillType(message:CivicMessageEntity):String{val text="${message.sender} ${message.body}";return when{listOf("آب","آبفا").any{text.contains(it,true)}->"آب";listOf("برق","توانیر").any{text.contains(it,true)}->"برق";listOf("گاز").any{text.contains(it,true)}->"گاز";listOf("تلفن","مخابرات").any{text.contains(it,true)}->"تلفن";else->"سایر"}}

@Composable private fun CivicBillEntry(vm:AppViewModel,onDone:()->Unit,onCancel:()->Unit){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val settings by vm.settings.collectAsState();var type by remember{mutableStateOf("آب")};var billId by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var date by remember{mutableStateOf(PersianDate.today())};var note by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به قبوض خدماتی")}
  Text("افزودن قبض",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text("نوع قبض");Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){utilityBillTypes.forEach{item->FilterChip(type==item,{type=item},{Text(item)})}}
  OutlinedTextField(billId,{billId=Digits.normalize(it)},label={Text("شناسه قبض یا اشتراک")},modifier=Modifier.fillMaxWidth())
  AmountTextField(amount,{amount=it},"مبلغ قبض",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
  Text("تاریخ قبض");PersianDateField(date,{date=it})
  OutlinedTextField(note,{note=it},label={Text("توضیحات (اختیاری)")},modifier=Modifier.fillMaxWidth(),minLines=2)
  Button({scope.launch{val amountRial=Money.inputToRial(amount,settings.moneyUnit)?:0;val body=buildString{append("قبض $type");if(billId.isNotBlank())append(" • شناسه: ${billId.trim()}");append(" • مبلغ: ${Money.format(amountRial,settings.moneyUnit)}");if(note.isNotBlank())append(" • ${note.trim()}")};vm.repo.db.civicDao().insertMessage(CivicMessageEntity(kind=CivicMessageKind.UTILITY_BILL,sender=type,body=body,receivedAt=date.startOfDayMillis(),fingerprint="manual-utility-${System.currentTimeMillis()}",read=false));showSavedMessage(context,"قبض $type");onDone()}},enabled=(Money.inputToRial(amount,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("ثبت قبض")}
 }
}

@Composable private fun CivicProfileEntry(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit){val context=LocalContext.current;val dao=vm.repo.db.civicDao();val people by dao.observePeople().collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var name by remember{mutableStateOf("")};var relation by remember{mutableStateOf("")};var insurer by remember{mutableStateOf("")};var policy by remember{mutableStateOf("")};var vehicleTitle by remember{mutableStateOf("")};var plate by remember{mutableStateOf("")};var owner by remember{mutableStateOf<Long?>(null)};val person=kind==ENTRY_PERSON;val accent=if(person)Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};Text(if(person)"ثبت فرد تحت پوشش" else "ثبت وسیله نقلیه و پلاک",style=MaterialTheme.typography.headlineSmall,color=accent);Text(if(person)"برای اتصال پیامک‌های بیمه و خدمات به اعضای خانواده" else "برای دسته‌بندی جرائم و پیامک‌های مرتبط با خودرو",color=MaterialTheme.colorScheme.onSurfaceVariant);if(person){OutlinedTextField(name,{name=it},label={Text("نام")},modifier=Modifier.fillMaxWidth());OutlinedTextField(relation,{relation=it},label={Text("نسبت")},modifier=Modifier.fillMaxWidth());OutlinedTextField(insurer,{insurer=it},label={Text("شرکت یا سازمان بیمه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(policy,{policy=it},label={Text("شماره بیمه‌نامه")},modifier=Modifier.fillMaxWidth())}else{OutlinedTextField(vehicleTitle,{vehicleTitle=it},label={Text("عنوان خودرو")},modifier=Modifier.fillMaxWidth());OutlinedTextField(plate,{plate=it},label={Text("شماره پلاک")},modifier=Modifier.fillMaxWidth());if(people.isNotEmpty()){Text("مالک");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){people.forEach{p->FilterChip(owner==p.id,{owner=p.id},{Text(p.name)})}}}else Text("در صورت نیاز، ابتدا یک فرد تحت پوشش ثبت کنید.",style=MaterialTheme.typography.bodySmall)};Button({scope.launch{if(person)dao.insertPerson(CoveredPersonEntity(name=name.trim(),relation=relation.trim(),insuranceProvider=insurer.trim(),policyNumber=policy.trim()))else dao.insertVehicle(VehicleEntity(ownerId=owner,title=vehicleTitle.trim(),plate=plate.trim()));showSavedMessage(context,"اطلاعات");onDone()}},enabled=if(person)name.isNotBlank()else vehicleTitle.isNotBlank()&&plate.isNotBlank(),modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text(if(person)"ثبت فرد" else "ثبت وسیله نقلیه")}}
}
