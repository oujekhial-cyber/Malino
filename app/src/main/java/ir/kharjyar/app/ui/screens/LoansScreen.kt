package ir.kharjyar.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.*
import kotlinx.coroutines.launch

@Composable fun LoansScreen(vm:AppViewModel){val loans by vm.loans.collectAsState();val installments by vm.loanInstallments.collectAsState();val settings by vm.settings.collectAsState();val scope=rememberCoroutineScope();var adding by remember{mutableStateOf(false)};var kind by remember{mutableStateOf(LoanKind.BORROWED)};var title by remember{mutableStateOf("")};var party by remember{mutableStateOf("")};var principal by remember{mutableStateOf("")};var payment by remember{mutableStateOf("")};var count by remember{mutableStateOf("12")};var firstDue by remember{mutableStateOf(PersianDate.today().plusDays(30))}
 Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(if(adding)"افزودن وام" else "مدیریت قسط و وام",style=MaterialTheme.typography.headlineSmall)
 if(adding){TwoWayModeSelector("وام دریافتی","وام پرداختی",kind==LoanKind.BORROWED,MaterialTheme.colorScheme.primary,MaterialTheme.colorScheme.tertiary,{kind=LoanKind.BORROWED},{kind=LoanKind.LENT});OutlinedTextField(title,{title=it},label={Text("عنوان وام")},modifier=Modifier.fillMaxWidth());OutlinedTextField(party,{party=it},label={Text(if(kind==LoanKind.BORROWED)"بانک/وام‌دهنده" else "وام‌گیرنده")},modifier=Modifier.fillMaxWidth());AmountTextField(principal,{principal=it},"اصل مبلغ",Modifier.fillMaxWidth());AmountTextField(payment,{payment=it},"مبلغ هر قسط",Modifier.fillMaxWidth());OutlinedTextField(count,{count=Digits.normalize(it).filter(Char::isDigit)},label={Text("تعداد اقساط")},modifier=Modifier.fillMaxWidth());Text("موعد اولین قسط");ir.kharjyar.app.ui.components.PersianDateField(firstDue,{firstDue=it});Button({scope.launch{val n=count.toIntOrNull()?:return@launch;val amount=Digits.parseAmount(payment)?:return@launch;val loanId=vm.repo.db.loanDao().insertLoan(LoanEntity(title=title,party=party,kind=kind,principalRial=Digits.parseAmount(principal)?:0,installmentAmountRial=amount,installmentCount=n,startAt=System.currentTimeMillis(),nextDueAt=firstDue.startOfDayMillis()));vm.repo.db.loanDao().insertInstallments((1..n).map{i->LoanInstallmentEntity(loanId=loanId,number=i,amountRial=amount,dueAt=firstDue.plusDays((i-1)*30).startOfDayMillis())});adding=false;title="";party="";principal="";payment=""}},enabled=title.isNotBlank()&&party.isNotBlank()&&(count.toIntOrNull()?:0)>0&&(Digits.parseAmount(payment)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("ثبت وام و برنامه اقساط")};TextButton({adding=false},Modifier.fillMaxWidth()){Text("انصراف")}}
 else {if(loans.isEmpty())Text("هنوز وامی ثبت نشده است.");loans.forEach{loan->val rows=installments.filter{it.loanId==loan.id};val paid=rows.count{it.paid};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("${loan.title} — ${loan.party}",style=MaterialTheme.typography.titleMedium);Text("${Digits.toPersian(paid.toString())} از ${Digits.toPersian(loan.installmentCount.toString())} قسط پرداخت شده");Text("هر قسط: ${Money.format(loan.installmentAmountRial,settings.moneyUnit)}");rows.firstOrNull{!it.paid}?.let{next->Text("موعد بعدی: ${next.dueAt.let(PersianDate::fromMillis).format()}");Button({scope.launch{vm.repo.db.loanDao().updateInstallment(next.copy(paid=true,paidAt=System.currentTimeMillis()));val following=rows.firstOrNull{!it.paid&&it.id!=next.id};vm.repo.db.loanDao().updateLoan(loan.copy(nextDueAt=following?.dueAt?:loan.nextDueAt,closed=following==null))}}){Text("ثبت پرداخت قسط ${Digits.toPersian(next.number.toString())}")}}}}}}
 }};if(!adding)FloatingActionButton({adding=true},Modifier.align(Alignment.BottomStart).padding(20.dp)){Text("+")}}
