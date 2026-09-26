package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DateTimeField
import kotlinx.coroutines.launch

@Composable fun DebtsScreen(vm: AppViewModel) {
    val people by vm.debtPeople.collectAsState(); val debts by vm.debts.collectAsState(); val payments by vm.debtPayments.collectAsState(); val settings by vm.settings.collectAsState(); val scope=rememberCoroutineScope()
    var person by remember{ mutableStateOf("")}; var title by remember{ mutableStateOf("")}; var amount by remember{ mutableStateOf("")}; var kind by remember{ mutableStateOf(DebtKind.RECEIVABLE)}; var due by remember{ mutableStateOf(PersianDate.today())}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("طلب‌ها و بدهی‌ها", style=MaterialTheme.typography.headlineSmall)
        OutlinedTextField(person,{person=it},label={Text("نام شخص")},modifier=Modifier.fillMaxWidth()); OutlinedTextField(title,{title=it},label={Text("عنوان/توضیح")},modifier=Modifier.fillMaxWidth()); ir.kharjyar.app.ui.components.AmountTextField(amount,{amount=it},"مبلغ به ریال",Modifier.fillMaxWidth())
        Row { FilterChip(kind==DebtKind.RECEIVABLE,{kind=DebtKind.RECEIVABLE},{Text("طلب من")}); Spacer(Modifier.width(8.dp)); FilterChip(kind==DebtKind.PAYABLE,{kind=DebtKind.PAYABLE},{Text("بدهی من")}) }
        DateTimeField(due,9,0,{due=it},{_,_->})
        Button({ scope.launch { val p=people.firstOrNull{it.name==person}; val pid=p?.id?:vm.repo.db.debtDao().insertPerson(DebtPersonEntity(name=person,createdAt=System.currentTimeMillis())); vm.repo.db.debtDao().insertDebt(DebtEntity(personId=pid,kind=kind,amountRial=Digits.parseAmount(amount)?:0,title=title,createdAt=System.currentTimeMillis(),dueAt=due.startOfDayMillis(),reminderAt=due.startOfDayMillis())); person="";title="";amount="" } }, enabled=person.isNotBlank()&&(Digits.parseAmount(amount)?:0)>0, modifier=Modifier.fillMaxWidth()){Text(if(kind==DebtKind.RECEIVABLE) "ثبت طلب و یادآور" else "ثبت بدهی و یادآور")}
        HorizontalDivider()
        debts.forEach { debt -> val paid=payments.filter{it.debtId==debt.id}.sumOf{it.amountRial}; val remain=(debt.amountRial-paid).coerceAtLeast(0); val p=people.firstOrNull{it.id==debt.personId}
            Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("${p?.name?:"؟"} — ${debt.title}",style=MaterialTheme.typography.titleMedium);Text((if(debt.kind==DebtKind.RECEIVABLE)"طلب: " else "بدهی: ")+Money.format(debt.amountRial,settings.moneyUnit));Text("مانده: ${Money.format(remain,settings.moneyUnit)}");Text("سررسید: ${debt.dueAt?.let{PersianDate.formatDateTime(it)}?:"—"}"); var pay by remember(debt.id){mutableStateOf("")};ir.kharjyar.app.ui.components.AmountTextField(pay,{pay=it},"مبلغ بازپرداخت مرحله‌ای");Button({scope.launch{val v=Digits.parseAmount(pay)?:return@launch;vm.repo.db.debtDao().insertPayment(DebtPaymentEntity(debtId=debt.id,amountRial=v,paidAt=System.currentTimeMillis()));if(v>=remain)vm.repo.db.debtDao().updateDebt(debt.copy(settled=true))}},enabled=remain>0){Text("ثبت پرداخت")}}}
        }
    }
}
