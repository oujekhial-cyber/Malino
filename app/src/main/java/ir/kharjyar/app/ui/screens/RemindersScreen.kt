package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DateTimeField
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch

@Composable
fun RemindersScreen(vm: AppViewModel) {
    val reminders by vm.repo.db.reminderDao().observeAll().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categories = listOf("تعویض روغن خودرو", "قبض برق", "قبض آب", "قبض گاز", "قبض تلفن و اینترنت", "بیمه", "دارو و سلامت", "سرویس و نگهداری", "سایر")
    val repeats = listOf(ReminderRepeat.ONCE, ReminderRepeat.DAILY, ReminderRepeat.WEEKLY, ReminderRepeat.MONTHLY, ReminderRepeat.YEARLY, ReminderRepeat.CUSTOM_DAYS)
    var category by remember { mutableStateOf(categories.first()) }
    var title by remember { mutableStateOf(categories.first()) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(PersianDate.today()) }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var repeat by remember { mutableIntStateOf(ReminderRepeat.ONCE) }
    var interval by remember { mutableStateOf("1") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("یادآورهای روزمره", style = MaterialTheme.typography.headlineSmall)
        Text("برای قبض‌ها، سرویس خودرو، بیمه و کارهای تکرارشونده یادآور بسازید.")
        ComboBox("نوع یادآور", categories, category, { category = it; if (title.isBlank() || title in categories) title = it }, labelOf = { it })
        OutlinedTextField(title, { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(note, { note = it }, label = { Text("توضیحات (اختیاری)") }, modifier = Modifier.fillMaxWidth())
        DateTimeField(date, hour, minute, { date = it }, { h, m -> hour = h; minute = m })
        ComboBox("تکرار", repeats, repeat, { repeat = it }, labelOf = { when(it) { ReminderRepeat.ONCE -> "بدون تکرار"; ReminderRepeat.DAILY -> "روزانه"; ReminderRepeat.WEEKLY -> "هفتگی"; ReminderRepeat.MONTHLY -> "ماهانه"; ReminderRepeat.YEARLY -> "سالانه"; else -> "هر چند روز یک‌بار" } })
        if (repeat == ReminderRepeat.CUSTOM_DAYS) OutlinedTextField(interval, { interval = Digits.normalize(it).filter(Char::isDigit) }, label = { Text("فاصله تکرار (روز)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { scope.launch {
            val at = date.startOfDayMillis() + (hour * 60L + minute) * 60_000L
            val id = vm.repo.db.reminderDao().insert(ReminderEntity(title = title.trim(), category = category, note = note.trim(), nextAt = at, repeatType = repeat, repeatInterval = interval.toIntOrNull()?.coerceAtLeast(1) ?: 1))
            LifeReminderWorker.schedule(context, id, at)
            title = category; note = ""
        } }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("ثبت و فعال‌سازی یادآور") }
        HorizontalDivider()
        if (reminders.isEmpty()) Text("هنوز یادآوری ثبت نشده است.")
        reminders.forEach { r ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Text("${r.category} • ${PersianDate.formatDateTime(r.nextAt)}")
                if (r.note.isNotBlank()) Text(r.note)
                Text(if (r.enabled) "فعال" else "انجام‌شده", color = if (r.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                Row { TextButton({ scope.launch { val changed = r.copy(enabled = !r.enabled); vm.repo.db.reminderDao().update(changed); if (changed.enabled) LifeReminderWorker.schedule(context, r.id, r.nextAt) else LifeReminderWorker.cancel(context, r.id) } }) { Text(if (r.enabled) "غیرفعال کردن" else "فعال کردن") }; TextButton({ scope.launch { LifeReminderWorker.cancel(context, r.id); vm.repo.db.reminderDao().delete(r.id) } }) { Text("حذف", color = MaterialTheme.colorScheme.error) } }
            } }
        }
    }
}
