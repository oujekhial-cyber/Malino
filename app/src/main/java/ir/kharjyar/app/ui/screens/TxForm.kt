package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
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
    Column {
        Text("حساب", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(accounts.size) { i ->
                val a = accounts[i]
                FilterChip(selected = selectedId == a.id, onClick = { onSelect(a.id) }, label = { Text(a.title) })
            }
        }
    }
}

@Composable
fun NaturePicker(nature: Int, direction: Int, onNature: (Int) -> Unit, onDirection: (Int) -> Unit) {
    Column {
        Text("جهت بانکی", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            FilterChip(selected = direction == TxDirection.DEPOSIT, onClick = { onDirection(TxDirection.DEPOSIT) }, label = { Text("واریز") })
            FilterChip(selected = direction == TxDirection.WITHDRAW, onClick = { onDirection(TxDirection.WITHDRAW) }, label = { Text("برداشت") })
        }
        Text("ماهیت", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            FilterChip(selected = nature == TxNature.INCOME, onClick = { onNature(TxNature.INCOME) }, label = { Text("درآمد") })
            FilterChip(selected = nature == TxNature.EXPENSE, onClick = { onNature(TxNature.EXPENSE) }, label = { Text("هزینه/خرید") })
            FilterChip(selected = nature == TxNature.TRANSFER, onClick = { onNature(TxNature.TRANSFER) }, label = { Text("انتقال") })
        }
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
    Column {
        Text("دسته‌بندی", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            item {
                FilterChip(selected = selectedId == null, onClick = { onSelect(null) }, label = { Text("نامشخص") })
            }
            items(visible.size) { i ->
                val c = visible[i]
                FilterChip(selected = selectedId == c.id, onClick = { onSelect(c.id) }, label = { Text(c.name) })
            }
        }
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
    Column {
        Text("تاریخ و ساعت (شمسی)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            NumberTextField(
                value = date.year.toString(),
                onValueChange = { v ->
                    v.toIntOrNull()?.let { y ->
                        if (y in 1300..1500) {
                            onDate(PersianDate(y, date.month, date.day.coerceAtMost(PersianDate.monthLength(y, date.month))))
                        }
                    }
                },
                label = "سال",
                maxDigits = 4,
                modifier = Modifier.weight(1.2f)
            )
            NumberTextField(
                value = date.month.toString(),
                onValueChange = { v ->
                    v.toIntOrNull()?.let { m ->
                        if (m in 1..12) {
                            onDate(PersianDate(date.year, m, date.day.coerceAtMost(PersianDate.monthLength(date.year, m))))
                        }
                    }
                },
                label = "ماه",
                maxDigits = 2,
                modifier = Modifier.weight(1f)
            )
            NumberTextField(
                value = date.day.toString(),
                onValueChange = { v ->
                    v.toIntOrNull()?.let { d -> if (d in 1..date.monthLength()) onDate(PersianDate(date.year, date.month, d)) }
                },
                label = "روز",
                maxDigits = 2,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberTextField(
                value = hour.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { h -> if (h in 0..23) onTime(h, minute) } },
                label = "ساعت",
                maxDigits = 2,
                modifier = Modifier.weight(1f)
            )
            NumberTextField(
                value = minute.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { m -> if (m in 0..59) onTime(hour, m) } },
                label = "دقیقه",
                maxDigits = 2,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
