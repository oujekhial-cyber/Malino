package ir.kharjyar.app.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.PersianDateField
import kotlinx.coroutines.launch

@Composable fun LoansScreen(vm:AppViewModel){val loans by vm.loans.collectAsState();val installments by vm.loanInstallments.collectAsState();val settings by vm.settings.collectAsState();val scope=rememberCoroutineScope();var entryKind by remember{mutableStateOf<Int?>(null)};var showChooser by remember{mutableStateOf(false)};var filter by remember{mutableStateOf<Int?>(null)}
 if(entryKind!=null){LoanEntryPage(vm,entryKind!!,{entryKind=null},{entryKind=null});return}
 val shown=loans.filter{filter==null||it.kind==filter};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("اقساط و وام‌ها",style=MaterialTheme.typography.headlineSmall);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter==null,{filter=null},{Text("همه")});FilterChip(filter==LoanKind.LENT,{filter=LoanKind.LENT},{Text("وام‌های پرداختی")});FilterChip(filter==LoanKind.BORROWED,{filter=LoanKind.BORROWED},{Text("وام‌های دریافتی")})};if(shown.isEmpty())Text("هنوز وام یا برنامه اقساطی ثبت نشده است.",modifier=Modifier.padding(vertical=24.dp));shown.forEach{loan->val rows=installments.filter{it.loanId==loan.id};val paid=rows.count{it.paid};val paidAmount=rows.filter{it.paid}.sumOf{it.amountRial};val remaining=(loan.principalRial-paidAmount).coerceAtLeast(0);val lent=loan.kind==LoanKind.LENT;val accent=if(lent)Color(0xFF20A565) else Color(0xFFE14B55);Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),modifier=Modifier.fillMaxWidth().border(1.4.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(loan.title,style=MaterialTheme.typography.titleMedium);Text(if(lent)"وام پرداختی" else "وام دریافتی",color=accent,style=MaterialTheme.typography.labelLarge)};Text(loan.party);Text("اصل مبلغ: ${Money.format(loan.principalRial,settings.moneyUnit)}");Text("مانده تقریبی: ${Money.format(remaining,settings.moneyUnit)}",color=accent);Text("${Digits.toPersian(paid.toString())} از ${Digits.toPersian(loan.installmentCount.toString())} قسط پرداخت شده");Text("مبلغ هر قسط: ${Money.format(loan.installmentAmountRial,settings.moneyUnit)}");rows.firstOrNull{!it.paid}?.let{next->Text("موعد بعدی: ${PersianDate.fromMillis(next.dueAt).format()}");Button({scope.launch{vm.repo.db.loanDao().updateInstallment(next.copy(paid=true,paidAt=System.currentTimeMillis()));val following=rows.firstOrNull{!it.paid&&it.id!=next.id};vm.repo.db.loanDao().updateLoan(loan.copy(nextDueAt=following?.dueAt?:loan.nextDueAt,closed=following==null))}},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("ثبت پرداخت قسط ${Digits.toPersian(next.number.toString())}")}}?:Text("تمام اقساط پرداخت شده است",color=accent)}}}}
  FloatingActionButton({showChooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp),containerColor=MaterialTheme.colorScheme.primary){Icon(Icons.Filled.Add,"افزودن وام")}}
 if(showChooser)AlertDialog(onDismissRequest={showChooser=false},title={Text("نوع وام را انتخاب کنید")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Button({showChooser=false;entryKind=LoanKind.LENT},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF1B8F52))){Text("ثبت وام پرداختی")};Button({showChooser=false;entryKind=LoanKind.BORROWED},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFD33B45))){Text("ثبت وام دریافتی")}}},confirmButton={})
}

@Composable private fun LoanEntryPage(vm:AppViewModel,kind:Int,onDone:()->Unit,onCancel:()->Unit){val scope=rememberCoroutineScope();var title by remember{mutableStateOf("")};var party by remember{mutableStateOf("")};var principal by remember{mutableStateOf("")};var payment by remember{mutableStateOf("")};var count by remember{mutableStateOf("12")};var firstDue by remember{mutableStateOf(PersianDate.today().plusDays(30))};val lent=kind==LoanKind.LENT;val accent=if(lent)Color(0xFF1B8F52) else Color(0xFFD33B45)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};Text(if(lent)"ثبت وام پرداختی" else "ثبت وام دریافتی",style=MaterialTheme.typography.headlineSmall,color=accent);OutlinedTextField(title,{title=it},label={Text("عنوان وام")},modifier=Modifier.fillMaxWidth());OutlinedTextField(party,{party=it},label={Text(if(lent)"نام وام‌گیرنده" else "بانک یا وام‌دهنده")},modifier=Modifier.fillMaxWidth());AmountTextField(principal,{principal=it},"اصل مبلغ",Modifier.fillMaxWidth());AmountTextField(payment,{payment=it},"مبلغ هر قسط",Modifier.fillMaxWidth());OutlinedTextField(count,{count=Digits.normalize(it).filter(Char::isDigit)},label={Text("تعداد اقساط")},modifier=Modifier.fillMaxWidth());Text("موعد اولین قسط");PersianDateField(firstDue,{firstDue=it});Button({scope.launch{val number=count.toIntOrNull()?:return@launch;val installmentAmount=Digits.parseAmount(payment)?:return@launch;val loanId=vm.repo.db.loanDao().insertLoan(LoanEntity(title=title.trim(),party=party.trim(),kind=kind,principalRial=Digits.parseAmount(principal)?:0,installmentAmountRial=installmentAmount,installmentCount=number,startAt=System.currentTimeMillis(),nextDueAt=firstDue.startOfDayMillis()));vm.repo.db.loanDao().insertInstallments((1..number).map{index->LoanInstallmentEntity(loanId=loanId,number=index,amountRial=installmentAmount,dueAt=firstDue.plusMonths(index-1).startOfDayMillis())});onDone()}},enabled=title.isNotBlank()&&party.isNotBlank()&&(count.toIntOrNull()?:0)>0&&(Digits.parseAmount(payment)?:0)>0,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("ثبت وام و برنامه اقساط")}}
}
