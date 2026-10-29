package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.ui.components.DateTimeField
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.NumberTextField

/** state فرم تراکنش (ثبت دستی و تکمیل پیش‌نویس). */
class TxFormState(
    var accountId: Long? = null,
    var amountText: String = "",
    var direction: Int = TxDirection.WITHDRAW,
    var nature: Int = TxNature.EXPENSE,
    var categoryId: Long? = null,
    var description: String = "",
    var date: PersianDate = PersianDate.today(),
    var hour: Int = 12,
    var minute: Int = 0
) {
    fun amountRial(unit: MoneyUnit): Long? = Money.inputToRial(amountText, unit)
    fun occurredAtMillis(): Long = PersianDate.toMillis(date, hour, minute)
}

@Composable
fun AccountPicker(
    accounts: List<AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    ComboBox(
        label = "حساب",
        options = accounts.map { it.id },
        selected = selectedId,
        labelOf = { id -> accounts.firstOrNull { it.id == id }?.title ?: "—" },
        placeholder = "انتخاب حساب",
        onSelect = onSelect,
        leadingOf = { id ->
            val color = accounts.firstOrNull { it.id == id }?.colorArgb
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (color != null) Color(color) else Color.Gray,
                        CircleShape
                    )
            )
        }
    )
}

private data class FinancialFlowChoice(val id:Int,val direction:Int,val nature:Int,val title:String)
private val financialFlowChoices=listOf(
    FinancialFlowChoice(1,TxDirection.DEPOSIT,TxNature.INCOME,"درآمد — پول وارد حساب شد"),
    FinancialFlowChoice(2,TxDirection.WITHDRAW,TxNature.EXPENSE,"هزینه — پول از حساب خارج شد"),
    FinancialFlowChoice(3,TxDirection.DEPOSIT,TxNature.TRANSFER,"انتقال ورودی — از حساب دیگر خودم"),
    FinancialFlowChoice(4,TxDirection.WITHDRAW,TxNature.TRANSFER,"انتقال خروجی — به حساب دیگر خودم"),
    FinancialFlowChoice(5,TxDirection.DEPOSIT,TxNature.NEUTRAL,"واریز غیر‌درآمدی — وام، وصول طلب یا برگشت وجه"),
    FinancialFlowChoice(6,TxDirection.WITHDRAW,TxNature.NEUTRAL,"برداشت غیرهزینه‌ای — اصل بدهی، امانت یا تسویه")
)

/** یک انتخاب قابل‌فهم برای کاربر؛ جهت و ماهیت همچنان جدا و دقیق ذخیره می‌شوند. */
@Composable
fun NaturePicker(nature:Int,direction:Int,onNature:(Int)->Unit,onDirection:(Int)->Unit,showDirection:Boolean=true){
    val compatible=if(showDirection)financialFlowChoices else financialFlowChoices.filter{it.direction==direction}
    val selected=compatible.firstOrNull{it.direction==direction&&it.nature==nature}?:compatible.first()
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        ComboBox(label="نوع جریان مالی",options=compatible.map{it.id},selected=selected.id,labelOf={id->compatible.first{it.id==id}.title},onSelect={id->val choice=compatible.first{it.id==id};onDirection(choice.direction);onNature(choice.nature)})
        Text(when(selected.id){1->"موجودی و درآمد هر دو افزایش می‌یابند.";2->"موجودی و هزینه هر دو افزایش می‌یابند.";3,4->"موجودی حساب تغییر می‌کند، اما در جمع درآمد و هزینه محاسبه نمی‌شود.";5->"موجودی حساب بیشتر می‌شود، ولی این مبلغ درآمد نیست.";else->"موجودی حساب کمتر می‌شود، ولی این مبلغ هزینه مصرفی نیست."},style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CategoryPicker(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    nature: Int,
    /** اگر داده شود، گزینه «دسته‌بندی جدید» هم در فهرست می‌آید. */
    onCreate: ((String) -> Unit)? = null,
    onSelect: (Long?) -> Unit
) {
    val visible = categories.filter { !it.archived }
    var showNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // گزینه ۰ به معنی «نامشخص» و گزینه ۱- به معنی «ساختن دسته تازه» است
    ComboBox(
        label = "دسته‌بندی",
        options = listOf(0L) + visible.map { it.id } + if (onCreate != null) listOf(-1L) else emptyList(),
        selected = selectedId ?: 0L,
        labelOf = { id ->
            when (id) {
                0L -> "نامشخص"
                -1L -> "+ دسته‌بندی جدید"
                else -> visible.firstOrNull { it.id == id }?.name ?: "—"
            }
        },
        onSelect = { id ->
            when (id) {
                -1L -> { newName = ""; showNew = true }
                0L -> onSelect(null)
                else -> onSelect(id)
            }
        }
    )

    if (showNew) {
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("دسته‌بندی جدید") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("نام دسته (مثل «نان و خواربار»)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        onCreate?.invoke(newName.trim())
                        showNew = false
                    }
                ) { Text("بساز و انتخاب کن") }
            },
            dismissButton = { TextButton(onClick = { showNew = false }) { Text("انصراف") } }
        )
    }
}

@Composable
fun DatePickerRow(
    date: PersianDate,
    hour: Int,
    minute: Int,
    onDate: (PersianDate) -> Unit,
    onTime: (Int, Int) -> Unit
) {
    // انتخاب تاریخ و ساعت از طریق پنجره تقویم شمسی، نه فیلدهای عددی
    DateTimeField(
        date = date,
        hour = hour,
        minute = minute,
        onDate = onDate,
        onTime = onTime
    )
}
