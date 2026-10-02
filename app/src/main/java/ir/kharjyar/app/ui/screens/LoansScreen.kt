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
    val lent=loan.kind==LoanKind.LENT
    val accent=if(lent) Color(0xFF20A565) else Color(0xFFE14B55)
    Card(
     colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),
     modifier=Modifier.fillMaxWidth().border(1.4.dp,accent,RoundedCornerShape(16.dp)).clickable { selectedLoanId=loan.id }
    ) {
     Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
       Text(loan.title,style=MaterialTheme.typography.titleMedium)
       Text(if(lent) "وام پرداختی" else "وام دریافتی",color=accent,style=MaterialTheme.typography.labelLarge)
      }
      Text(loan.party)
      Text("${Digits.toPersian(paid.toString())} از ${Digits.toPersian(loan.installmentCount.toString())} قسط پرداخت شده",style=MaterialTheme.typography.bodySmall)
     }
    }
   }
  }
  FloatingActionButton(
   {showChooser=true},
   Modifier.align(Alignment.BottomEnd).padding(20.dp),
   containerColor=MaterialTheme.colorScheme.primary
  ) { Icon(Icons.Filled.Add,"افزودن وام") }
 }
 if(showChooser) AlertDialog(
  onDismissRequest={showChooser=false},
  title={Text("نوع وام را انتخاب کنید")},
  text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Button({showChooser=false;entryKind=LoanKind.LENT},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFF1B8F52))) { Text("ثبت وام پرداختی") }
   Button({showChooser=false;entryKind=LoanKind.BORROWED},Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,Color(0xFFD33B45))) { Text("ثبت وام دریافتی") }
  }},
  confirmButton={}
 )
}

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
  Text(loan.title,style=MaterialTheme.typography.headlineSmall)
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
  Text("اقساط پرداخت‌شده",style=MaterialTheme.typography.titleMedium)
  if(paidRows.isEmpty()) {
   Text("هنوز قسطی پرداخت نشده است.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  } else paidRows.forEach { installment ->
   Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.08f)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().border(1.dp,accent.copy(alpha=.32f),RoundedCornerShape(18.dp))) {
    Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     Column(verticalArrangement=Arrangement.spacedBy(3.dp)) {
      Text("قسط ${Digits.toPersian(installment.number.toString())}",style=MaterialTheme.typography.titleSmall)
      Text("تاریخ پرداخت: ${PersianDate.fromMillis(installment.paidAt?:installment.dueAt).format()}",style=MaterialTheme.typography.bodySmall)
     }
     Text(Money.format(installment.amountRial,settings.moneyUnit),color=accent)
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
  Text(if(lent) "ثبت وام پرداختی" else "ثبت وام دریافتی",style=MaterialTheme.typography.headlineSmall,color=accent)
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
