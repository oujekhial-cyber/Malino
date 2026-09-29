package ir.kharjyar.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.prefs.DigitStyle
import ir.kharjyar.app.data.prefs.ThemeMode
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.BankLogo
import ir.kharjyar.app.ui.components.bankCardColor
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Email
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.theme.AllSkins
import ir.kharjyar.app.ui.theme.AppSkin
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController, section: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val blockedSenders by viewModel.blockedSenders.collectAsState()

    var smsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    var showAbout by remember { mutableStateOf(false) }
    var notifGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }
    val notifEnabled = remember { NotificationManagerCompat.from(context).areNotificationsEnabled() }

    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { smsGranted = it }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifGranted = it }
    fun openPermissionSettings() {
        context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        
        if (section == null) {
            SettingsMenuRow("قالب‌ها") { nav.navigate("settings/appearance") }
            SettingsMenuRow("مدیریت حساب") { nav.navigate("settings/account") }
            SettingsMenuRow("نمایش اعداد و واحد پول") { nav.navigate("settings/numbers") }
            SettingsMenuRow("دسته‌بندی‌ها") { nav.navigate("categories") }
            SettingsMenuRow("مجوزها و اعلان‌ها") { nav.navigate("settings/permissions") }
            SettingsMenuRow("امنیت") { nav.navigate("settings/security") }
            SettingsMenuRow("پشتیبان‌گیری و بازیابی") { nav.navigate("backup") }
            SettingsMenuRow("پیامک‌های تبلیغاتی") { nav.navigate("blockedSenders") }
            SettingsMenuRow("حریم خصوصی") { nav.navigate("settings/privacy") }
            SettingsMenuRow("درباره ما") { showAbout = true }
        }

        // ---------- تم ----------
        if (section == "appearance") {
        SectionCard("ظاهر و تم") {
            Text("تم برنامه", style = MaterialTheme.typography.labelLarge)
            Text(
                "تم انتخابی روی پس‌زمینه، کارت‌ها، نمودار، دیالوگ‌ها، نوار پایین و ویجت اعمال می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ComboBox(
                label = "حالت روشنایی",
                options = ThemeMode.entries,
                selected = settings.themeMode,
                labelOf = {
                    when (it) {
                        ThemeMode.SYSTEM -> "پیش‌فرض سیستم (تغییر خودکار روز/شب)"
                        ThemeMode.LIGHT -> "همیشه روشن"
                        ThemeMode.DARK -> "همیشه تیره"
                    }
                },
                onSelect = { mode -> scope.launch { viewModel.settingsRepo.setThemeMode(mode) } }
            )
            // انتخاب تم از کمبوباکس + پیش‌نمایش تم فعلی
            ComboBox(
                label = "تم",
                options = AllSkins.map { it.id },
                selected = settings.palette,
                labelOf = { id -> AllSkins.first { it.id == id }.title },
                onSelect = { id -> scope.launch { viewModel.settingsRepo.setPalette(id) } },
                leadingOf = { id ->
                    val s = AllSkins.first { it.id == id }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(Modifier.size(12.dp).background(s.heroGradient.first(), CircleShape))
                        Box(Modifier.size(12.dp).background(s.accent, CircleShape))
                    }
                }
            )
            var fontScaleDraft by remember(settings.appFontScale) { mutableStateOf(settings.appFontScale.toFloat()) }
            HorizontalDivider()
            Text("اندازه متن و اعداد", style = MaterialTheme.typography.titleMedium)
            Text(
                "اندازه نوشته‌ها و همه اعداد برنامه را متناسب با دید خود تنظیم کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SkinCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("کوچک", style = MaterialTheme.typography.labelSmall)
                        Text("نمونه ۱۲۳٬۴۵۶", style = MaterialTheme.typography.titleMedium)
                        Text("بزرگ", style = MaterialTheme.typography.titleLarge)
                    }
                    Slider(
                        value = fontScaleDraft,
                        onValueChange = { fontScaleDraft = it },
                        valueRange = 85f..130f,
                        steps = 8,
                        onValueChangeFinished = { scope.launch { viewModel.settingsRepo.setAppFontScale(fontScaleDraft.toInt()) } }
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${Digits.toPersian(fontScaleDraft.toInt().toString())}٪", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { fontScaleDraft = 100f; scope.launch { viewModel.settingsRepo.setAppFontScale(100) } }) { Text("اندازه استاندارد") }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("قاب نئونی کارت اصلی", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "نور نئونی دور کارت موجودی در صفحه خانه، با رنگ تم فعال",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.cardShine,
                    onCheckedChange = { scope.launch { viewModel.settingsRepo.setCardShine(it) } }
                )
            }
            HorizontalDivider()
            NavRow("تنظیمات ویجت") { nav.navigate("widgetSettings") }
        }
        }

        // ---------- حساب پیش‌فرض ----------
        if (section == "account") {
        SectionCard("مدیریت حساب") {
            Text(
                "با انتخاب حساب پیش‌فرض، داشبورد و ویجت به‌صورت پیش‌فرض اطلاعات همان حساب را نشان می‌دهند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val accountOptions: List<Long> = listOf(0L) + accounts.filter { !it.archived }.map { it.id }
            ComboBox(
                label = "حساب پیش‌فرض داشبورد و ویجت",
                options = accountOptions,
                selected = settings.defaultAccountId ?: 0L,
                labelOf = { id ->
                    if (id == 0L) "همه حساب‌ها"
                    else accounts.firstOrNull { it.id == id }?.title ?: "—"
                },
                onSelect = { id ->
                    scope.launch {
                        viewModel.settingsRepo.setDefaultAccount(if (id == 0L) null else id)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )
            HorizontalDivider()
            Text("حساب‌های معرفی‌شده",style=MaterialTheme.typography.titleMedium)
            if(accounts.isEmpty()){
                Text("هنوز حسابی معرفی نشده است.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }else accounts.forEach { account ->
                SkinCard(Modifier.fillMaxWidth().clickable{nav.navigate("accountEdit/${account.id}")}){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)){
                        BankLogo(bankName=account.bankName,size=38.dp,ringColor=bankCardColor(account.bankName,account.colorArgb))
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){Text(account.title,style=MaterialTheme.typography.titleSmall);if(account.archived)Text("بایگانی",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                            Text(listOfNotNull(account.bankName.ifBlank{null},account.maskedNumber.ifBlank{null}?.let{Digits.toPersian(it)}).joinToString(" — ").ifBlank{"بدون مشخصات بانکی"},style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("‹",style=MaterialTheme.typography.headlineSmall,color=MaterialTheme.colorScheme.primary)
                    }
                }
            }
            OutlinedButton({nav.navigate("accountEdit/0")},Modifier.fillMaxWidth()){Text("افزودن حساب جدید")}
            Text("برای ویرایش هر حساب، کارت آن را لمس کنید.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        }

        // ---------- پول ----------
        if (section == "numbers") {
        SectionCard("نمایش اعداد و واحد پول") {
            ComboBox(
                label = "واحد نمایش پول",
                options = listOf(MoneyUnit.RIAL, MoneyUnit.TOMAN),
                selected = settings.moneyUnit,
                labelOf = { if (it == MoneyUnit.RIAL) "ریال" else "تومان" },
                onSelect = { scope.launch { viewModel.settingsRepo.setMoneyUnit(it) } }
            )
            ComboBox(
                label = "شکل ارقام",
                options = listOf(DigitStyle.PERSIAN, DigitStyle.LATIN),
                selected = settings.digitStyle,
                labelOf = { if (it == DigitStyle.PERSIAN) "فارسی (۱۲۳)" else "انگلیسی (123)" },
                onSelect = { style ->
                    scope.launch {
                        viewModel.settingsRepo.setDigitStyle(style)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )
            Text(
                "شکل ارقام روی کل برنامه و ویجت اعمال می‌شود. مبالغ همیشه به ریال ذخیره می‌شوند؛ " +
                    "تغییر واحد نمایش، مقادیر ذخیره‌شده را تغییر نمی‌دهد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        var feeInput by remember(settings.bankFeePercent) { mutableStateOf(settings.bankFeePercent.toString().trimEnd('0').trimEnd('.')) }
        var feeSaved by remember { mutableStateOf(false) }
        SectionCard("درصد کارمزد بانکی") {
            Text(
                "کارمزد بانکی مبلغی است که بانک برای انجام خدماتی مانند انتقال وجه، کارت‌به‌کارت یا بعضی پرداخت‌ها از حساب کسر می‌کند. گاهی این مبلغ با تأخیر در پیامک بانکی دیده می‌شود و باعث اختلاف جزئی بین مانده واقعی بانک و مانده محاسبه‌شده برنامه می‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "درصد اعلام‌شده بانک مرکزی یا بانک خود را دستی وارد کنید. خرج‌یار فقط اختلاف‌های کوچک تا این درصد از مبلغ تراکنش را به‌عنوان کارمزد احتمالی ثبت می‌کند. مقدار صفر، تشخیص خودکار کارمزد را غیرفعال می‌کند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = feeInput,
                onValueChange = { value ->
                    feeInput = Digits.normalize(value).filter { it.isDigit() || it == '.' }.take(6)
                    feeSaved = false
                },
                label = { Text("درصد کارمزد") },
                suffix = { Text("٪") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val value = feeInput.toFloatOrNull()?.coerceIn(0f, 100f) ?: 0f
                    feeInput = value.toString().trimEnd('0').trimEnd('.')
                    scope.launch { viewModel.settingsRepo.setBankFeePercent(value.toInt()) }
                    feeSaved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ذخیره درصد کارمزد") }
            if (feeSaved) Text("درصد کارمزد ذخیره شد.", color = MaterialTheme.colorScheme.primary)
        }
        }

        // ---------- مجوزها ----------
        if (section == "permissions") {
        SectionCard("مجوزها و اعلان‌ها") {
            PermissionRow("دریافت پیامک", smsGranted, { smsPermission.launch(Manifest.permission.RECEIVE_SMS) }, ::openPermissionSettings)
            PermissionRow("ارسال اعلان", notifGranted && notifEnabled, { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }, ::openPermissionSettings)
            if (!notifEnabled) {
                Text("اعلان‌های برنامه در تنظیمات دستگاه خاموش است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Text(
                "توجه: اندروید تحویل بی‌استثنای پیامک در پس‌زمینه را تضمین نمی‌کند (مثلاً پس از Force Stop یا در برخی دستگاه‌ها با بهینه‌سازی باتری تهاجمی).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        }

        // ---------- امنیت ----------
        if (section == "security") {
        SectionCard("امنیت") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("جلوگیری از اسکرین‌شات", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "ضبط صفحه و اسکرین‌شات مسدود می‌شود و پیش‌نمایش برنامه در فهرست اخیر خالی می‌ماند",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.secureScreen,
                    onCheckedChange = { scope.launch { viewModel.settingsRepo.setSecureScreen(it) } }
                )
            }

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
        }



        if (section == "senders") {
        SectionCard("پیامک‌ها") { NavRow("فرستنده‌های تبلیغاتی") { nav.navigate("blockedSenders") } }
        }

        if (section == "privacy") {
        SectionCard("حریم خصوصی") {
            Text(
                "خرج‌یار فقط با اجازه شما پیامک‌های مالی را بررسی می‌کند؛ رمزهای یک‌بارمصرف و پیامک‌های شخصی ذخیره نمی‌شوند. اطلاعات مالی، تصاویر چک و دفتر بدهی روی همین گوشی می‌مانند و برای سازنده ارسال نمی‌شوند. اینترنت فقط برای دریافت هواشناسی شهر انتخابی استفاده می‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        }

    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("درباره خرج‌یار") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContactRow(Icons.Filled.Email, "ایمیل", "fasasoftrrr@gmail.com") {
                    runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:fasasoftrrr@gmail.com"))) }
                }
                Text("واتساپ — لینک به‌زودی", style = MaterialTheme.typography.bodyMedium)
                Text("روبیکا — لینک به‌زودی", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text("نسخه ۱.۰.۰")
                Text("حریم خصوصی: اطلاعات مالی، پیامک‌ها، تصاویر چک و دفتر بدهی در فضای خصوصی و رمزنگاری‌شده برنامه نگهداری می‌شوند. هیچ داده مالی برای سازنده ارسال نمی‌شود. اینترنت فقط برای هواشناسی شهر انتخابی استفاده می‌شود.", style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { TextButton({ showAbout = false }) { Text("بستن") } }
        )
    }
}


@Composable
private fun SectionCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    SkinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onEnable: () -> Unit, onDisable: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(label, style = MaterialTheme.typography.bodyLarge); Text(if (granted) "فعال" else "غیرفعال", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked = granted, onCheckedChange = { enabled -> if (enabled) onEnable() else onDisable() })
    }
}

/** یک ردیف تماس قابل لمس (ایمیل/تلفن) با آیکون. */
@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun SettingsMenuRow(label: String, onClick: () -> Unit) {
    SkinCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text("‹", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
