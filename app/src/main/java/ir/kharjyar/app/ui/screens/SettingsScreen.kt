package ir.kharjyar.app.ui.screens

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.data.prefs.ThemeMode
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.widget.KharjYarWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()

    var smsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    var notifGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
    val notifEnabled = remember { NotificationManagerCompat.from(context).areNotificationsEnabled() }

    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { smsGranted = it }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifGranted = it }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall)

        // ---------- تم ----------
        SectionCard("ظاهر و تم") {
            Text("حالت", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.themeMode == ThemeMode.SYSTEM, onClick = { scope.launch { viewModel.settingsRepo.setThemeMode(ThemeMode.SYSTEM) } }, label = { Text("سیستم") })
                FilterChip(selected = settings.themeMode == ThemeMode.LIGHT, onClick = { scope.launch { viewModel.settingsRepo.setThemeMode(ThemeMode.LIGHT) } }, label = { Text("روشن") })
                FilterChip(selected = settings.themeMode == ThemeMode.DARK, onClick = { scope.launch { viewModel.settingsRepo.setThemeMode(ThemeMode.DARK) } }, label = { Text("تاریک") })
            }
            Text("پالت رنگی", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.palette == Palette.DYNAMIC, onClick = { scope.launch { viewModel.settingsRepo.setPalette(Palette.DYNAMIC) } }, label = { Text("پویا (Material You)") })
                FilterChip(selected = settings.palette == Palette.OCEAN, onClick = { scope.launch { viewModel.settingsRepo.setPalette(Palette.OCEAN) } }, label = { Text("اقیانوس") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.palette == Palette.FOREST, onClick = { scope.launch { viewModel.settingsRepo.setPalette(Palette.FOREST) } }, label = { Text("جنگل") })
                FilterChip(selected = settings.palette == Palette.SUNSET, onClick = { scope.launch { viewModel.settingsRepo.setPalette(Palette.SUNSET) } }, label = { Text("غروب") })
                FilterChip(selected = settings.palette == Palette.MIDNIGHT, onClick = { scope.launch { viewModel.settingsRepo.setPalette(Palette.MIDNIGHT) } }, label = { Text("نیمه‌شب") })
            }
        }

        // ---------- پول ----------
        SectionCard("واحد نمایش پول") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.moneyUnit == MoneyUnit.TOMAN, onClick = { scope.launch { viewModel.settingsRepo.setMoneyUnit(MoneyUnit.TOMAN) } }, label = { Text("تومان") })
                FilterChip(selected = settings.moneyUnit == MoneyUnit.RIAL, onClick = { scope.launch { viewModel.settingsRepo.setMoneyUnit(MoneyUnit.RIAL) } }, label = { Text("ریال") })
            }
            Text(
                "مبالغ همیشه به ریال ذخیره می‌شوند؛ تغییر واحد نمایش مقادیر ذخیره‌شده را تغییر نمی‌دهد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- مجوزها ----------
        SectionCard("مجوزها و اعلان") {
            PermissionRow("دریافت پیامک", smsGranted) { smsPermission.launch(Manifest.permission.RECEIVE_SMS) }
            PermissionRow("ارسال اعلان", notifGranted) { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
            if (!notifEnabled) {
                Text("اعلان‌های برنامه در تنظیمات دستگاه خاموش است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Text(
                "توجه: اندروید تحویل بی‌استثنای پیامک در پس‌زمینه را تضمین نمی‌کند (مثلاً پس از Force Stop یا در برخی دستگاه‌ها با بهینه‌سازی باتری تهاجمی).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- امنیت ----------
        SectionCard("امنیت") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("قفل برنامه", style = MaterialTheme.typography.bodyLarge)
                    Text("اثر انگشت، چهره یا رمز دستگاه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.appLockEnabled,
                    onCheckedChange = { on ->
                        val activity = context as? MainActivity
                        if (on) {
                            // فعال‌سازی قفل نیازمند تأیید هویت است
                            if (activity != null && activity.canUseBiometric()) {
                                activity.authenticate {
                                    scope.launch { viewModel.settingsRepo.setAppLock(true) }
                                }
                            }
                        } else {
                            scope.launch { viewModel.settingsRepo.setAppLock(false) }
                        }
                    }
                )
            }
            if (settings.appLockEnabled) {
                Text("زمان قفل پس از خروج", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "فوری", 60 to "۱ دقیقه", 300 to "۵ دقیقه").forEach { (sec, label) ->
                        FilterChip(
                            selected = settings.lockTimeoutSeconds == sec,
                            onClick = { scope.launch { viewModel.settingsRepo.setLockTimeout(sec) } },
                            label = { Text(label) }
                        )
                    }
                }
            }
            Text(
                "قفل برنامه فقط رابط کاربری را می‌بندد و جایگزین رمزنگاری دیتابیس نیست.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- ویجت ----------
        SectionCard("ویجت صفحه اصلی") {
            Text("محتوای ویجت", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.widgetContent == WidgetContent.SUMMARY, onClick = { scope.launch { viewModel.settingsRepo.setWidgetContent(WidgetContent.SUMMARY); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } }, label = { Text("خلاصه ماه") })
                FilterChip(selected = settings.widgetContent == WidgetContent.TODAY_EXPENSE, onClick = { scope.launch { viewModel.settingsRepo.setWidgetContent(WidgetContent.TODAY_EXPENSE); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } }, label = { Text("هزینه امروز") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.widgetContent == WidgetContent.MONTH_EXPENSE, onClick = { scope.launch { viewModel.settingsRepo.setWidgetContent(WidgetContent.MONTH_EXPENSE); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } }, label = { Text("هزینه ماه") })
                FilterChip(selected = settings.widgetContent == WidgetContent.RECENT, onClick = { scope.launch { viewModel.settingsRepo.setWidgetContent(WidgetContent.RECENT); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } }, label = { Text("آخرین تراکنش‌ها") })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("نمایش اعداد در ویجت", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = settings.widgetShowNumbers, onCheckedChange = { scope.launch { viewModel.settingsRepo.setWidgetShowNumbers(it); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } })
            }
            if (settings.appLockEnabled) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("نمایش اعداد با وجود قفل برنامه", style = MaterialTheme.typography.bodyLarge)
                        Text("پیش‌فرض: با قفل فعال، اعداد ویجت مخفی‌اند", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.widgetShowNumbersWhenLocked, onCheckedChange = { scope.launch { viewModel.settingsRepo.setWidgetShowNumbersWhenLocked(it); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } })
                }
            }
            OutlinedButton(onClick = {
                val mgr = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, KharjYarWidgetReceiver::class.java)
                if (mgr.isRequestPinAppWidgetSupported) {
                    mgr.requestPinAppWidget(component, null, null)
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("افزودن ویجت به صفحه اصلی (با تأیید لانچر)") }
        }

        // ---------- سایر ----------
        SectionCard("داده و ابزار") {
            NavRow("مدیریت حساب‌ها") { nav.navigate("accounts") }
            NavRow("دسته‌بندی‌ها و قوانین") { nav.navigate("categories") }
            NavRow("موارد نیازمند بررسی") { nav.navigate("review") }
            NavRow("بکاپ و بازیابی رمزنگاری‌شده") { nav.navigate("backup") }
        }

        SectionCard("حریم خصوصی") {
            Text(
                "خرج‌یار فقط پیامک‌های دریافتی جدید را (با اجازه شما) بررسی می‌کند و فقط پیامک‌های مالی را نگه می‌دارد. رمزهای یک‌بارمصرف و پیامک‌های شخصی ذخیره نمی‌شوند. همه داده‌ها فقط روی همین گوشی هستند؛ برنامه اینترنت ندارد و هیچ اطلاعاتی به جایی ارسال نمی‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onRequest: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        if (granted) {
            Text("فعال", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        } else {
            OutlinedButton(onClick = onRequest) { Text("درخواست") }
        }
    }
}

@Composable
private fun NavRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
