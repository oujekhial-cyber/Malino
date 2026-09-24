package ir.kharjyar.app.ui.screens

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.prefs.DigitStyle
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.data.prefs.WidgetLayout
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.LabeledSlider
import ir.kharjyar.app.ui.components.WidgetPreview
import ir.kharjyar.app.ui.theme.AllSkins
import ir.kharjyar.app.ui.theme.AppSkin
import ir.kharjyar.app.widget.KharjYarWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val blockedSenders by viewModel.blockedSenders.collectAsState()

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
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        // ---------- تم ----------
        SectionCard("ظاهر و تم") {
            Text("تم برنامه", style = MaterialTheme.typography.labelLarge)
            Text(
                "تم انتخابی روی پس‌زمینه، کارت‌ها، نمودار، دیالوگ‌ها، نوار پایین و ویجت اعمال می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        }

        // ---------- حساب پیش‌فرض ----------
        SectionCard("حساب پیش‌فرض") {
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
        }

        // ---------- پول ----------
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

        // ---------- ویجت ----------
        SectionCard("ویجت صفحه اصلی") {
            Text(
                "قالب ویجت را انتخاب کنید. هر قالب با رنگ تم فعال برنامه ساخته می‌شود؛ " +
                    "ساعت و تاریخ‌ها همیشه زنده‌اند و با ساعت گوشی هماهنگ می‌مانند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ComboBox(
                label = "قالب ویجت",
                options = WidgetLayout.entries.toList(),
                selected = settings.widgetLayout,
                labelOf = { layoutLabel(it) },
                onSelect = { v ->
                    scope.launch {
                        viewModel.settingsRepo.setWidgetLayout(v)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )

            ComboBox(
                label = "محتوای ویجت",
                options = listOf(
                    WidgetContent.SUMMARY,
                    WidgetContent.TODAY_EXPENSE,
                    WidgetContent.MONTH_EXPENSE,
                    WidgetContent.RECENT
                ),
                selected = settings.widgetContent,
                labelOf = { contentLabel(it) },
                onSelect = { v ->
                    scope.launch {
                        viewModel.settingsRepo.setWidgetContent(v)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )

            // ---------- پیش‌نمایش ----------
            Text("پیش‌نمایش", style = MaterialTheme.typography.labelLarge)
            WidgetPreview(
                layout = settings.widgetLayout,
                opacity = settings.widgetOpacity,
                showNumbers = settings.widgetShowNumbers,
                lines = when (settings.widgetContent) {
                    WidgetContent.TODAY_EXPENSE -> listOf("برداشت امروز" to "۳,۲۵۰,۰۰۰")
                    WidgetContent.MONTH_EXPENSE -> listOf("برداشت شهریور" to "۱۸,۴۰۰,۰۰۰")
                    WidgetContent.RECENT -> listOf("واریز" to "۵,۸۷۰,۰۰۰", "برداشت" to "۱,۲۸۰,۰۰۰")
                    WidgetContent.SUMMARY -> listOf("واریز شهریور" to "۵,۸۷۰,۰۰۰", "برداشت شهریور" to "۳,۲۵۰,۰۰۰")
                }
            )

            // ---------- شیشه‌ای بودن ----------
            LabeledSlider(
                label = "میزان شیشه‌ای بودن",
                value = settings.widgetOpacity,
                range = 0..100,
                valueSuffix = "٪",
                onValueChange = { v ->
                    scope.launch {
                        viewModel.settingsRepo.setWidgetOpacity(v)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )
            Text(
                "عدد کمتر یعنی شیشه‌ای‌تر (تصویر زمینه گوشی بیشتر دیده می‌شود).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("نمایش اعداد در ویجت", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = settings.widgetShowNumbers,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.settingsRepo.setWidgetShowNumbers(it)
                            ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                        }
                    }
                )
            }
            if (settings.appLockEnabled) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("نمایش اعداد با وجود قفل برنامه", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "پیش‌فرض: با قفل فعال، اعداد ویجت مخفی‌اند",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.widgetShowNumbersWhenLocked,
                        onCheckedChange = {
                            scope.launch {
                                viewModel.settingsRepo.setWidgetShowNumbersWhenLocked(it)
                                ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                            }
                        }
                    )
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

        // ---------- فرستنده‌های تبلیغاتی ----------
        if (blockedSenders.isNotEmpty()) {
            SectionCard("فرستنده‌های تبلیغاتی") {
                Text(
                    "پیام‌های این فرستنده‌ها نادیده گرفته می‌شوند. برای برگرداندن، حذفشان کنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                blockedSenders.forEach { blocked ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(blocked.sender, style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = {
                            scope.launch { viewModel.repo.unblockSender(blocked.sender) }
                        }) { Text("برگرداندن") }
                    }
                }
            }
        }

        SectionCard("حریم خصوصی") {
            Text(
                "خرج‌یار فقط پیامک‌های دریافتی جدید را (با اجازه شما) بررسی می‌کند و فقط پیامک‌های مالی را نگه می‌دارد. رمزهای یک‌بارمصرف و پیامک‌های شخصی ذخیره نمی‌شوند. همه داده‌ها فقط روی همین گوشی هستند؛ برنامه اینترنت ندارد و هیچ اطلاعاتی به جایی ارسال نمی‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- پشتیبانی ----------
        SectionCard("پشتیبانی و ارتباط با ما") {
            Text(
                "برای گزارش مشکل، پیشنهاد یا راهنمایی با ما در تماس باشید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ایمیل: با زدن، برنامه ایمیل باز می‌شود
            ContactRow(
                icon = Icons.Filled.Email,
                label = "ایمیل",
                value = "fasasoftrrr@gmail.com",
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:fasasoftrrr@gmail.com"))
                                .putExtra(Intent.EXTRA_SUBJECT, "خرج‌یار")
                        )
                    }
                }
            )

            // شماره تماس: با زدن، شماره‌گیر باز می‌شود
            ContactRow(
                icon = Icons.Filled.Phone,
                label = "شماره تماس",
                value = Digits.toPersian("09399874951"),
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:09399874951")))
                    }
                }
            )

            Text(
                "نسخه ۱.۰.۰",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

/** برچسب فارسی تراز افقی. */
private fun layoutLabel(l: WidgetLayout): String = when (l) {
    WidgetLayout.ROYAL -> "لوکس (قاب طلایی، ساعت خیلی بزرگ)"
    WidgetLayout.MINIMAL -> "مینیمال (عنوان بزرگ، ساعت کنار)"
    WidgetLayout.PANELS -> "نواری (سربرگ بالا، مقادیر در نوار)"
    WidgetLayout.STACKED -> "نواری فشرده"
    WidgetLayout.SPLIT -> "دوبخشی (خط وسط)"
    WidgetLayout.GLASS -> "شیشه‌ای (با تصویر تم)"
}

private fun contentLabel(c: WidgetContent): String = when (c) {
    WidgetContent.SUMMARY -> "خلاصه ماه"
    WidgetContent.TODAY_EXPENSE -> "برداشت امروز"
    WidgetContent.MONTH_EXPENSE -> "برداشت ماه"
    WidgetContent.RECENT -> "آخرین تراکنش‌ها"
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
