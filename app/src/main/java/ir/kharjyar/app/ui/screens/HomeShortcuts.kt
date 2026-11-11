package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.data.prefs.HOME_SHORTCUT_OPTIONS
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

private fun shortcutTitle(route: String): String = when (route) {
    "manual" -> "ثبت تراکنش"; "manual/transfer" -> "انتقال وجه"; "checks" -> "چک‌ها"; "review" -> "بررسی پیامک‌ها"
    "recurringTransactions" -> "تراکنش دوره‌ای"; "financialCalendar" -> "تقویم مالی"; "globalSearch" -> "جست‌وجو"; "financialHealth" -> "سلامت داده‌ها"; "reports" -> "گزارش‌ها"; else -> route
}

@Composable
fun HomeShortcuts(vm: AppViewModel, nav: NavHostController) {
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    SkinCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("میان‌برهای من", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { selected.clear(); selected.addAll(settings.homeShortcuts); editing = true }) { Text("تنظیم") }
        }
        if (settings.homeShortcuts.isEmpty()) Text("از تنظیم، میان‌برهای دلخواه را انتخاب کنید.", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            settings.homeShortcuts.forEach { route -> OutlinedButton(onClick = { nav.navigate(route) }) { Text(shortcutTitle(route)) } }
        }
    } }
    if (editing) AlertDialog(onDismissRequest = { editing = false }, title = { Text("تنظیم میان‌برها") }, text = {
        Column(Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState())) {
            Text("انتخاب کنید؛ دکمه‌های بالا و پایین ترتیب نمایش را تغییر می‌دهند.", style = MaterialTheme.typography.bodySmall)
            (selected.toList() + HOME_SHORTCUT_OPTIONS.filterNot { it in selected }).forEach { route ->
                Row(Modifier.fillMaxWidth()) {
                    Checkbox(checked = route in selected, onCheckedChange = { checked -> if (checked) selected.add(route) else selected.remove(route) })
                    Text(shortcutTitle(route), Modifier.weight(1f))
                    if (route in selected) {
                        TextButton(onClick = { val i = selected.indexOf(route); if (i > 0) { selected.removeAt(i); selected.add(i - 1, route) } }, contentPadding = PaddingValues(2.dp)) { Text("↑") }
                        TextButton(onClick = { val i = selected.indexOf(route); if (i < selected.lastIndex) { selected.removeAt(i); selected.add(i + 1, route) } }, contentPadding = PaddingValues(2.dp)) { Text("↓") }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = { val value = selected.toList(); scope.launch { vm.settingsRepo.setHomeShortcuts(value) }; editing = false }) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = { editing = false }) { Text("انصراف") } })
}
