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
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
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
            // شبکه دو‌ستونه از همه تم‌ها
            AllSkins.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { skin ->
                        ThemeOption(
                            skin = skin,
                            selected = settings.palette == skin.id,
                            modifier = Modifier.weight(1f),
                            onClick = { scope.launch { viewModel.settingsRepo.setPalette(skin.id) } }
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
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
                Column(Modifier.weight(1f)) {
                    Text("ساعت و تاریخ در ویجت", style = MaterialTheme.typography.bodyLarge)
                    Text("ساعت بزرگ همراه تاریخ شمسی و میلادی در سمت راست ویجت", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.widgetShowClock, onCheckedChange = { scope.launch { viewModel.settingsRepo.setWidgetShowClock(it); ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context) } })
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
            // ---------- پیش‌نمایش زنده ویجت ----------
            Text("پیش‌نمایش", style = MaterialTheme.typography.labelLarge)
            WidgetPreview(
                opacity = settings.widgetOpacity,
                clockSize = settings.widgetClockSize,
                dateSize = settings.widgetDateSize,
                valueSize = settings.widgetValueSize,
                labelSize = settings.widgetLabelSize,
                showClock = settings.widgetShowClock,
                showNumbers = settings.widgetShowNumbers,
                lines = when (settings.widgetContent) {
                    WidgetContent.TODAY_EXPENSE -> listOf("هزینه امروز" to "۳,۲۵۰,۰۰۰")
                    WidgetContent.MONTH_EXPENSE -> listOf("هزینه این ماه" to "۱۸,۴۰۰,۰۰۰")
                    WidgetContent.RECENT -> listOf("واریز" to "۵,۸۷۰,۰۰۰", "برداشت" to "۱,۲۸۰,۰۰۰")
                    WidgetContent.SUMMARY -> listOf("درآمد" to "۵,۸۷۰,۰۰۰", "هزینه" to "۳,۲۵۰,۰۰۰")
                }
            )

            // ---------- شیشه‌ای بودن و اندازه‌ها ----------
            LabeledSlider(
                label = "میزان شیشه‌ای بودن پس‌زمینه",
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

            Text("اندازه اجزای ویجت", style = MaterialTheme.typography.labelLarge)
            if (settings.widgetShowClock) {
                LabeledSlider(
                    label = "اندازه ساعت",
                    value = settings.widgetClockSize,
                    range = 18..72,
                    onValueChange = { v ->
                        scope.launch {
                            viewModel.settingsRepo.setWidgetClockSize(v)
                            ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                        }
                    }
                )
                LabeledSlider(
                    label = "اندازه تاریخ‌ها",
                    value = settings.widgetDateSize,
                    range = 8..28,
                    onValueChange = { v ->
                        scope.launch {
                            viewModel.settingsRepo.setWidgetDateSize(v)
                            ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                        }
                    }
                )
            }
            LabeledSlider(
                label = "اندازه اعداد مالی",
                value = settings.widgetValueSize,
                range = 9..30,
                onValueChange = { v ->
                    scope.launch {
                        viewModel.settingsRepo.setWidgetValueSize(v)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )
            LabeledSlider(
                label = "اندازه برچسب‌ها",
                value = settings.widgetLabelSize,
                range = 7..22,
                onValueChange = { v ->
                    scope.launch {
                        viewModel.settingsRepo.setWidgetLabelSize(v)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                }
            )
            OutlinedButton(
                onClick = {
                    scope.launch {
                        viewModel.settingsRepo.setWidgetOpacity(92)
                        viewModel.settingsRepo.setWidgetClockSize(40)
                        viewModel.settingsRepo.setWidgetDateSize(13)
                        viewModel.settingsRepo.setWidgetValueSize(14)
                        viewModel.settingsRepo.setWidgetLabelSize(10)
                        ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("بازگرداندن اندازه‌های پیش‌فرض") }

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

        // ---------- درباره برنامه ----------
        SectionCard("درباره برنامه") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("برنامه‌نویس", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("رحیم کرمی", style = MaterialTheme.typography.bodyLarge)
                }
            }

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

/** کارت پیش‌نمایش یک تم (گرادیان + نام + حالت انتخاب). */
@Composable
private fun ThemeOption(
    skin: AppSkin,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(skin.cardCorner)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(skin.heroGradient))
            .border(
                width = if (selected) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    if (selected) skin.fabGradient else skin.cardBorderColors
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).background(skin.incomeColor, CircleShape))
            Box(Modifier.size(14.dp).background(skin.expenseColor, CircleShape))
            Box(Modifier.size(14.dp).background(skin.accent, CircleShape))
        }
        Spacer(Modifier.height(10.dp))
        Text(skin.title, style = MaterialTheme.typography.titleSmall, color = skin.onHero)
        Text(
            if (selected) "انتخاب‌شده ✓" else skin.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = skin.onHero.copy(alpha = 0.75f)
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
