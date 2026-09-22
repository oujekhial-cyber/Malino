package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun NaturePicker(nature: Int, direction: Int, onNature: (Int) -> Unit, onDirection: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ComboBox(
            label = "جهت بانکی",
            options = listOf(TxDirection.DEPOSIT, TxDirection.WITHDRAW),
            selected = direction,
            labelOf = { if (it == TxDirection.DEPOSIT) "واریز (پول وارد حساب شد)" else "برداشت (پول از حساب خارج شد)" },
            onSelect = onDirection
        )
        ComboBox(
            label = "ماهیت",
            options = listOf(TxNature.INCOME, TxNature.EXPENSE, TxNature.TRANSFER),
            selected = nature,
            labelOf = {
                when (it) {
                    TxNature.INCOME -> "درآمد"
                    TxNature.TRANSFER -> "انتقال بین حساب‌ها"
                    else -> "هزینه/خرید"
                }
            },
            onSelect = onNature
        )
        Text(
            "واریز الزاماً درآمد نیست و برداشت الزاماً هزینه نیست؛ ماهیت را خودتان مشخص کنید.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryPicker(categories: List<CategoryEntity>, selectedId: Long?, nature: Int, onSelect: (Long?) -> Unit) {
    val visible = categories.filter { !it.archived }
    // گزینه ۰ به معنی «نامشخص» است
    ComboBox(
        label = "دسته‌بندی",
        options = listOf(0L) + visible.map { it.id },
        selected = selectedId ?: 0L,
        labelOf = { id ->
            if (id == 0L) "نامشخص" else visible.firstOrNull { it.id == id }?.name ?: "—"
        },
        onSelect = { id -> onSelect(if (id == 0L) null else id) }
    )
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
