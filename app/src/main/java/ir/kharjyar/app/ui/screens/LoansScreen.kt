package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CreditCard
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun LoansScreen(vm:AppViewModel) {
 val loans by vm.loans.collectAsState()
 val installments by vm.loanInstallments.collectAsState()
 val settings by vm.settings.collectAsState()
 var entryKind by remember { mutableStateOf<Int?>(null) }
 var selectedLoanId by remember { mutableStateOf<Long?>(null) }
 var showChooser by remember { mutableStateOf(false) }
 var filter by remember { mutableStateOf<Int?>(null) }
 val selectedLoan=loans.firstOrNull { it.id==selectedLoanId }

 BackHandler(enabled=entryKind!=null||selectedLoanId!=null) {
  if(entryKind!=null) entryKind=null else selectedLoanId=null
 }
 if(entryKind!=null) {
  LoanEntryPage(vm,entryKind!!,{entryKind=null},{entryKind=null})
  return
 }
 if(selectedLoan!=null) {
  LoanDetailsPage(
   vm=vm,
   loan=selectedLoan,
   installments=installments.filter { it.loanId==selectedLoan.id },
   onBack={selectedLoanId=null}
  )
  return
 }

 val shown=loans.filter { filter==null||it.kind==filter }
 val unpaidByLoan=installments.filter{!it.paid}.groupBy{it.loanId}.mapValues{(_,rows)->rows.sumOf{it.amountRial}}
 val lentRemaining=loans.filter{it.kind==LoanKind.LENT}.sumOf{unpaidByLoan[it.id]?:0L};val borrowedRemaining=loans.filter{it.kind==LoanKind.BORROWED}.sumOf{unpaidByLoan[it.id]?:0L}
 Box(Modifier.fillMaxSize()) {
  Column(
   Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    .padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),
   verticalArrangement=Arrangement.spacedBy(10.dp)
  ) {
   ModernSummaryHero("اقساط و وام‌ها","مانده اقساط پرداخت‌نشده",MaterialTheme.colorScheme.primary,listOf(SummaryMetric("مطالبات وام",Money.format(lentRemaining,settings.moneyUnit),Color(0xFF1B9A61),Icons.Filled.Savings),SummaryMetric("تعهدات وام",Money.format(borrowedRemaining,settings.moneyUnit),Color(0xFFE24B57),Icons.Filled.CreditCard)),Icons.Filled.Payments)
   Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
    FilterChip(filter==null,{filter=null},{Text("همه")})
    FilterChip(filter==LoanKind.LENT,{filter=LoanKind.LENT},{Text("وام‌های پرداختی")})
    FilterChip(filter==LoanKind.BORROWED,{filter=LoanKind.BORROWED},{Text("وام‌های دریافتی")})
   }
   if(shown.isEmpty()) Text("هنوز وام یا برنامه اقساطی ثبت نشده است.",modifier=Modifier.padding(vertical=24.dp))
   shown.forEach { loan ->
    val rows=installments.filter { it.loanId==loan.id }
    val paid=rows.count { it.paid }
    val paidAmount=rows.filter { it.paid }.sumOf { it.amountRial }
    val remaining=rows.filterNot { it.paid }.sumOf { it.amountRial }
    val nextDue=rows.filterNot { it.paid }.minByOrNull { it.number }?.dueAt
    ModernLoanCard(
     loan=loan,
     paidCount=paid,
     paidAmount=paidAmount,
     remaining=remaining,
     nextDueAt=nextDue,
     moneyUnit=settings.moneyUnit,
     onClick={selectedLoanId=loan.id}
    )
   }
  }
  FloatingActionButton(
   {showChooser=true},
   Modifier.align(Alignment.BottomEnd).padding(20.dp),
   containerColor=MaterialTheme.colorScheme.primary
  ) { Icon(Icons.Filled.Add,"افزودن وام") }
 }
 if(showChooser)ModernChoiceDialog("نوع وام","جهت پرداخت و بازپرداخت وام را انتخاب کنید",listOf(ModernChoiceOption("وام پرداختی","مبلغی که شما به شخص دیگری وام می‌دهید",Color(0xFF1B8F52),Icons.Filled.NorthEast){showChooser=false;entryKind=LoanKind.LENT},ModernChoiceOption("وام دریافتی","مبلغی که از شخص یا مؤسسه دریافت می‌کنید",Color(0xFFD33B45),Icons.Filled.SouthWest){showChooser=false;entryKind=LoanKind.BORROWED}),{showChooser=false})
}

@Composable
private fun ModernLoanCard(loan:LoanEntity,paidCount:Int,paidAmount:Long,remaining:Long,nextDueAt:Long?,moneyUnit:ir.kharjyar.app.core.money.MoneyUnit,onClick:()->Unit){
 val lent=loan.kind==LoanKind.LENT
 val accent=if(lent)Color(0xFF159B73) else Color(0xFFE0525E)
 val progress=(paidCount.toFloat()/loan.installmentCount.coerceAtLeast(1)).coerceIn(0f,1f)
 Card(modifier=Modifier.fillMaxWidth().border(1.dp,accent.copy(.32f),RoundedCornerShape(24.dp)).clickable(onClick=onClick),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color.Transparent),elevation=CardDefaults.cardElevation(3.dp)){
  Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent.copy(.17f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Box(Modifier.size(50.dp).background(accent.copy(.16f),RoundedCornerShape(16.dp)).border(1.dp,accent.copy(.34f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Icon(if(lent)Icons.Filled.NorthEast else Icons.Filled.SouthWest,null,tint=accent,modifier=Modifier.size(27.dp))}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(loan.title,style=MaterialTheme.typography.titleMedium);Text((if(lent)"وام‌گیرنده: " else "وام‌دهنده: ")+loan.party,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    Surface(color=accent.copy(.13f),shape=RoundedCornerShape(50),border=BorderStroke(1.dp,accent.copy(.25f))){Text(if(lent)"وام پرداختی" else "وام دریافتی",Modifier.padding(horizontal=10.dp,vertical=5.dp),color=accent,style=MaterialTheme.typography.labelMedium)}
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
    LoanMetricTile("پرداخت‌شده",Money.format(paidAmount,moneyUnit),Color(0xFF159B73),Icons.Filled.CheckCircle,Modifier.weight(1f))
    LoanMetricTile("مانده اقساط",Money.format(remaining,moneyUnit),if(remaining>0)accent else Color(0xFF159B73),Icons.Filled.Payments,Modifier.weight(1f))
   }
   Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("پیشرفت بازپرداخت",style=MaterialTheme.typography.labelMedium);Text("${Digits.toPersian(paidCount.toString())} از ${Digits.toPersian(loan.installmentCount.toString())} قسط",style=MaterialTheme.typography.labelMedium,color=accent)};Box(Modifier.fillMaxWidth().height(7.dp).background(accent.copy(.12f),RoundedCornerShape(50))){Box(Modifier.fillMaxWidth(progress).height(7.dp).background(accent,RoundedCornerShape(50)))}}
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.EventAvailable,null,tint=accent,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(if(nextDueAt!=null)"قسط بعدی: ${PersianDate.fromMillis(nextDueAt).format()}" else "همه اقساط پرداخت شده",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("مشاهده جزئیات",style=MaterialTheme.typography.labelSmall,color=accent);Icon(Icons.Filled.ChevronLeft,null,tint=accent)}
  }
 }
}

@Composable private fun LoanMetricTile(title:String,value:String,accent:Color,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier){Surface(modifier=modifier,color=accent.copy(.08f),shape=RoundedCornerShape(15.dp),border=BorderStroke(1.dp,accent.copy(.18f))){Column(Modifier.padding(11.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){Icon(icon,null,tint=accent,modifier=Modifier.size(17.dp));Text(title,style=MaterialTheme.typography.labelSmall,color=accent)};Text(value,style=MaterialTheme.typography.titleSmall)}}}

@Composable
private fun LoanDetailsPage(vm:AppViewModel,loan:LoanEntity,installments:List<LoanInstallmentEntity>,onBack:()->Unit) {
 val settings by vm.settings.collectAsState()
 val scope=rememberCoroutineScope()
 val paidRows=installments.filter { it.paid }.sortedByDescending { it.paidAt?:it.dueAt }
 val next=installments.filterNot { it.paid }.minByOrNull { it.number }
 val paidAmount=paidRows.sumOf { it.amountRial }
 val remaining=(loan.principalRial-paidAmount).coerceAtLeast(0)
 val lent=loan.kind==LoanKind.LENT
 val accent=if(lent) Color(0xFF20A565) else Color(0xFFE14B55)
 Column(
  Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
  verticalArrangement=Arrangement.spacedBy(12.dp)
 ) {
  TextButton(onBack) { Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست وام‌ها") }
  ModernSummaryHero(loan.title,if(lent)"وام پرداختی به ${loan.party}" else "وام دریافتی از ${loan.party}",accent,listOf(SummaryMetric("پرداخت‌شده",Money.format(paidAmount,settings.moneyUnit),Color(0xFF159B73),Icons.Filled.CheckCircle),SummaryMetric("مانده",Money.format(remaining,settings.moneyUnit),accent,Icons.Filled.Payments)),if(lent)Icons.Filled.NorthEast else Icons.Filled.SouthWest)
  Card(
   colors=CardDefaults.cardColors(containerColor=Color.Transparent),
   shape=RoundedCornerShape(24.dp),
   elevation=CardDefaults.cardElevation(5.dp),
   modifier=Modifier.fillMaxWidth().border(1.2.dp,accent.copy(alpha=.55f),RoundedCornerShape(24.dp))
  ) {
   Column(Modifier.background(Brush.linearGradient(listOf(accent.copy(alpha=.20f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
    Text("مشخصات وام",style=MaterialTheme.typography.titleMedium,color=accent)
    Text("نوع: ${if(lent)"وام پرداختی" else "وام دریافتی"}")
    Text("${if(lent)"وام‌گیرنده" else "بانک یا وام‌دهنده"}: ${loan.party}")
    Text("اصل مبلغ: ${Money.format(loan.principalRial,settings.moneyUnit)}")
    Text("مبلغ هر قسط: ${Money.format(loan.installmentAmountRial,settings.moneyUnit)}")
    Text("تعداد اقساط: ${Digits.toPersian(loan.installmentCount.toString())}")
    Text("جمع پرداخت‌شده: ${Money.format(paidAmount,settings.moneyUnit)}")
    Text("مانده تقریبی: ${Money.format(remaining,settings.moneyUnit)}",color=accent)
    next?.let { Text("موعد قسط بعدی: ${PersianDate.fromMillis(it.dueAt).format()}") }
   }
  }
  next?.let { installment ->
   Button(
    {scope.launch {
     vm.repo.db.loanDao().updateInstallment(installment.copy(paid=true,paidAt=System.currentTimeMillis()))
     val following=installments.filterNot { it.paid||it.id==installment.id }.minByOrNull { it.number }
     vm.repo.db.loanDao().updateLoan(loan.copy(nextDueAt=following?.dueAt?:loan.nextDueAt,closed=following==null))
    }},
    modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)
   ) { Text("ثبت پرداخت قسط ${Digits.toPersian(installment.number.toString())}") }
  } ?: Text("تمام اقساط پرداخت شده است",color=accent)

  HorizontalDivider()
  Text("برنامه اقساط",style=MaterialTheme.typography.titleMedium)
  installments.sortedBy { it.number }.forEach { installment ->
   val rowAccent=if(installment.paid)Color(0xFF159B73) else accent
   Card(colors=CardDefaults.cardColors(containerColor=Color.Transparent),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().border(1.dp,rowAccent.copy(alpha=.28f),RoundedCornerShape(18.dp))) {
    Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(rowAccent.copy(.11f),MaterialTheme.colorScheme.surface))).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
     Box(Modifier.size(40.dp).background(rowAccent.copy(.14f),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Icon(if(installment.paid)Icons.Filled.CheckCircle else Icons.Filled.Schedule,null,tint=rowAccent)}
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
      Text("قسط ${Digits.toPersian(installment.number.toString())}",style=MaterialTheme.typography.titleSmall)
      Text(if(installment.paid)"پرداخت‌شده در ${PersianDate.fromMillis(installment.paidAt?:installment.dueAt).format()}" else "سررسید ${PersianDate.fromMillis(installment.dueAt).format()}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     }
     Column(horizontalAlignment=Alignment.End,verticalArrangement=Arrangement.spacedBy(4.dp)){Text(Money.format(installment.amountRial,settings.moneyUnit),color=rowAccent);Surface(color=rowAccent.copy(.13f),shape=RoundedCornerShape(50)){Text(if(installment.paid)"پرداخت‌شده" else "در انتظار",Modifier.padding(horizontal=8.dp,vertical=3.dp),style=MaterialTheme.typography.labelSmall,color=rowAccent)}}
    }
   }
  }
 }
}

@Composable
private fun LoanEntryPage(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit) {
 val context=LocalContext.current
 val settings by vm.settings.collectAsState()
 val scope=rememberCoroutineScope()
 var title by remember { mutableStateOf("") }
 var party by remember { mutableStateOf("") }
 var principal by remember { mutableStateOf("") }
 var payment by remember { mutableStateOf("") }
 var count by remember { mutableStateOf("12") }
 var firstDue by remember { mutableStateOf(PersianDate.today().plusDays(30)) }
 val lent=kind==LoanKind.LENT
 val accent=if(lent) Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  TextButton(onCancel) { Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست") }
  ModernSummaryHero(if(lent)"ثبت وام پرداختی" else "ثبت وام دریافتی","مشخصات و برنامه اقساط",accent,listOf(SummaryMetric("اصل مبلغ",Money.inputToRial(principal,settings.moneyUnit)?.let{Money.format(it,settings.moneyUnit)}?:"—",accent,Icons.Filled.Payments),SummaryMetric("تعداد اقساط",Digits.toPersian(count.ifBlank{"—"}),MaterialTheme.colorScheme.primary,Icons.Filled.CalendarMonth)),Icons.Filled.Savings)
  OutlinedTextField(title,{title=it},label={Text("عنوان وام")},modifier=Modifier.fillMaxWidth())
  OutlinedTextField(party,{party=it},label={Text(if(lent)"نام وام‌گیرنده" else "بانک یا وام‌دهنده")},modifier=Modifier.fillMaxWidth())
  AmountTextField(principal,{principal=it},"اصل مبلغ",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
  AmountTextField(payment,{payment=it},"مبلغ هر قسط",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
  OutlinedTextField(count,{count=Digits.normalize(it).filter(Char::isDigit)},label={Text("تعداد اقساط")},modifier=Modifier.fillMaxWidth())
  Text("موعد اولین قسط")
  PersianDateField(firstDue,{firstDue=it})
  Button({scope.launch {
   val number=count.toIntOrNull()?:return@launch
   val installmentAmount=Money.inputToRial(payment,settings.moneyUnit)?:return@launch
   val loanId=vm.repo.db.loanDao().insertLoan(LoanEntity(title=title.trim(),party=party.trim(),kind=kind,principalRial=Money.inputToRial(principal,settings.moneyUnit)?:0,installmentAmountRial=installmentAmount,installmentCount=number,startAt=System.currentTimeMillis(),nextDueAt=firstDue.startOfDayMillis()))
   vm.repo.db.loanDao().insertInstallments((1..number).map { index -> LoanInstallmentEntity(loanId=loanId,number=index,amountRial=installmentAmount,dueAt=firstDue.plusMonths(index-1).startOfDayMillis()) })
   showSavedMessage(context,"وام و برنامه اقساط")
   onDone()
  }},enabled=title.isNotBlank()&&party.isNotBlank()&&(count.toIntOrNull()?:0)>0&&(Money.inputToRial(payment,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)) {
   Text("ثبت وام و برنامه اقساط")
  }
 }
}
