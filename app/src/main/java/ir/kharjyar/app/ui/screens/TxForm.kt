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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import ir.kharjyar.app.ui.components.ComboBox

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
            // نقطه رنگی حساب
            val color = accounts.firstOrNull { it.id == id }?.colorArgb ?: 0xFF888888
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(color))
            )
        }
    )
}

/** گزینه‌های «جهت بانکی» و «ماهیت» به‌صورت کمبوباکس. */
@Composable
fun NaturePicker(nature: Int, direction: Int, onNature: (Int) -> Unit, onDirection: (Int) -> Unit) {
    val directions = listOf(TxDirection.DEPOSIT, TxDirection.WITHDRAW)
    val natures = listOf(TxNature.INCOME, TxNature.EXPENSE, TxNature.TRANSFER)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ComboBox(
            label = "جهت بانکی",
            options = directions,
            selected = direction,
            labelOf = { if (it == TxDirection.DEPOSIT) "واریز" else "برداشت" },
            onSelect = onDirection
        )
        ComboBox(
            label = "ماهیت",
            options = natures,
            selected = nature,
            labelOf = {
                when (it) {
                    TxNature.INCOME -> "درآمد"
                    TxNature.EXPENSE -> "هزینه/خرید"
                    else -> "انتقال"
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
    // ۰ به معنی «نامشخص» است چون ComboBox مقدار غیرnull می‌خواهد
    val options = listOf(0L) + visible.map { it.id }
    ComboBox(
        label = "دسته‌بندی",
        options = options,
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
