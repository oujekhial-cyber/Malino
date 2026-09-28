package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.PersianDateField
import kotlinx.coroutines.launch

@Composable
fun DebtsScreen(vm: AppViewModel) {
    val people by vm.debtPeople.collectAsState(); val debts by vm.debts.collectAsState(); val payments by vm.debtPayments.collectAsState(); val settings by vm.settings.collectAsState(); val scope=rememberCoroutineScope()
    var entryKind by remember { mutableStateOf<Int?>(null) };var showChooser by remember { mutableStateOf(false) }
    var filterKind by remember{mutableStateOf<Int?>(null)};var personQuery by remember{mutableStateOf("")};var filterFrom by remember{mutableStateOf(PersianDate.today().plusDays(-365))};var filterTo by remember{mutableStateOf(PersianDate.today().plusDays(365))}

    BackHandler(enabled=entryKind != null){entryKind = null}
    if (entryKind != null) {
        DebtEntryPage(vm, entryKind!!, people, onDone = { entryKind=null }, onCancel = { entryKind=null })
        return
    }

    val shown=debts.filter { d -> (filterKind==null||d.kind==filterKind)&&d.createdAt>=filterFrom.startOfDayMillis()&&d.createdAt<filterTo.endOfDayMillisExclusive()&&(personQuery.isBlank()||people.firstOrNull{it.id==d.personId}?.name?.contains(personQuery,true)==true) }
    fun remaining(d:DebtEntity)=(d.amountRial-payments.filter{it.debtId==d.id}.sumOf{it.amountRial}).coerceAtLeast(0)
    val totalReceivable=shown.filter{it.kind==DebtKind.RECEIVABLE}.sumOf(::remaining);val totalPayable=shown.filter{it.kind==DebtKind.PAYABLE}.sumOf(::remaining)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("طلب‌ها و بدهی‌ها",style=MaterialTheme.typography.headlineSmall)
            Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(13.dp),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("مجموع طلب",color=Color(0xFF1B8F52));Text(Money.format(totalReceivable,settings.moneyUnit),color=Color(0xFF1B8F52),style=MaterialTheme.typography.titleMedium)};Column{Text("مجموع بدهی",color=Color(0xFFD33B45));Text(Money.format(totalPayable,settings.moneyUnit),color=Color(0xFFD33B45),style=MaterialTheme.typography.titleMedium)}}}
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filterKind==null,{filterKind=null},{Text("همه")});FilterChip(filterKind==DebtKind.RECEIVABLE,{filterKind=DebtKind.RECEIVABLE},{Text("طلب‌ها")});FilterChip(filterKind==DebtKind.PAYABLE,{filterKind=DebtKind.PAYABLE},{Text("بدهی‌ها")})}
            OutlinedTextField(personQuery,{personQuery=it},label={Text("جست‌وجوی نام شخص")},singleLine=true,modifier=Modifier.fillMaxWidth())
            var showDateFilters by remember{mutableStateOf(false)};TextButton({showDateFilters=!showDateFilters}){Text(if(showDateFilters)"بستن فیلتر تاریخ" else "فیلتر بر اساس تاریخ")};if(showDateFilters){Text("از تاریخ");PersianDateField(filterFrom,{filterFrom=it});Text("تا تاریخ");PersianDateField(filterTo,{filterTo=it})}
            if(shown.isEmpty())Text("هنوز طلب یا بدهی ثبت نشده است.",modifier=Modifier.padding(vertical=24.dp))
            shown.forEach { debt -> val remain=remaining(debt);val personName=people.firstOrNull{it.id==debt.personId}?.name?:"؟";val receivable=debt.kind==DebtKind.RECEIVABLE;val accent=if(receivable)Color(0xFF20A565) else Color(0xFFE14B55);val bg=accent.copy(alpha=.10f)
                Card(colors=CardDefaults.cardColors(containerColor=bg),modifier=Modifier.fillMaxWidth().border(1.4.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(personName,style=MaterialTheme.typography.titleMedium);Text(if(receivable)"طلب" else "بدهی",color=accent,style=MaterialTheme.typography.labelLarge)};if(debt.title.isNotBlank())Text(debt.title);Text("مبلغ اولیه: ${Money.format(debt.amountRial,settings.moneyUnit)}");Text("مانده: ${Money.format(remain,settings.moneyUnit)}",color=accent,style=MaterialTheme.typography.titleSmall);Text("سررسید: ${debt.dueAt?.let{PersianDate.fromMillis(it).format()}?:"—"}");var pay by remember(debt.id){mutableStateOf("")};if(remain>0){AmountTextField(pay,{pay=it},"مبلغ پرداخت مرحله‌ای");Button({scope.launch{val value=Digits.parseAmount(pay)?:return@launch;vm.repo.db.debtDao().insertPayment(DebtPaymentEntity(debtId=debt.id,amountRial=value,paidAt=System.currentTimeMillis()));if(value>=remain)vm.repo.db.debtDao().updateDebt(debt.copy(settled=true));pay=""}},enabled=(Digits.parseAmount(pay)?:0)>0){Text("ثبت پرداخت")}}else Text("تسویه‌شده",color=accent)}}
            }
        }
        FloatingActionButton(onClick={showChooser=true},modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp),containerColor=MaterialTheme.colorScheme.primary){Icon(Icons.Filled.Add,"افزودن طلب یا بدهی")}
    }
    if(showChooser) AlertDialog(onDismissRequest={showChooser=false},title={Text("چه موردی ثبت می‌کنید؟")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Button({showChooser=false;entryKind=DebtKind.RECEIVABLE},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFF1B8F52))){Text("ثبت طلب")};Button({showChooser=false;entryKind=DebtKind.PAYABLE},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFFD33B45))){Text("ثبت بدهی")}}},confirmButton={})
}

@Composable
private fun DebtEntryPage(vm:AppViewModel,kind:Int,people:List<DebtPersonEntity>,onDone:()->Unit,onCancel:()->Unit){val scope=rememberCoroutineScope();val context=LocalContext.current;var person by remember{mutableStateOf("")};var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var due by remember{mutableStateOf(PersianDate.today())};val receivable=kind==DebtKind.RECEIVABLE;Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};Text(if(receivable)"ثبت طلب" else "ثبت بدهی",style=MaterialTheme.typography.headlineSmall,color=if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45));OutlinedTextField(person,{person=it},label={Text("نام شخص")},modifier=Modifier.fillMaxWidth());OutlinedTextField(title,{title=it},label={Text("عنوان یا توضیح")},modifier=Modifier.fillMaxWidth());AmountTextField(amount,{amount=it},"مبلغ به ریال",Modifier.fillMaxWidth());Text("تاریخ سررسید");PersianDateField(due,{due=it});Button({scope.launch{val old=people.firstOrNull{it.name.trim()==person.trim()};val personId=old?.id?:vm.repo.db.debtDao().insertPerson(DebtPersonEntity(name=person.trim(),createdAt=System.currentTimeMillis()));vm.repo.db.debtDao().insertDebt(DebtEntity(personId=personId,kind=kind,amountRial=Digits.parseAmount(amount)?:0,title=title.trim(),createdAt=System.currentTimeMillis(),dueAt=due.startOfDayMillis(),reminderAt=due.startOfDayMillis()));showSavedMessage(context,"طلب یا بدهی");onDone()}},enabled=person.isNotBlank()&&(Digits.parseAmount(amount)?:0)>0,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,if(receivable)Color(0xFF1B8F52) else Color(0xFFD33B45))){Text(if(receivable)"ثبت طلب و یادآور" else "ثبت بدهی و یادآور")}}
}
