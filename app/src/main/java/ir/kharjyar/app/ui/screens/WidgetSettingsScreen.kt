package ir.kharjyar.app.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.data.prefs.WidgetLayout
import ir.kharjyar.app.data.prefs.WidgetAlign
import ir.kharjyar.app.data.prefs.WidgetVAlign
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.LabeledSlider
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.WidgetPreview
import ir.kharjyar.app.ui.components.WidgetPreviewLine
import ir.kharjyar.app.widget.KharjYarWidgetReceiver
import ir.kharjyar.app.widget.WidgetUpdater
import kotlinx.coroutines.launch

/**
 * صفحه مستقل «تنظیمات ویجت».
 *
 * همه گزینه‌های ویجت اینجا جمع شده‌اند و هر تغییر، هم در پیش‌نمایش بالای صفحه
 * و هم بلافاصله روی ویجت واقعی صفحه اصلی اعمال می‌شود.
 */
@Composable
fun WidgetSettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    var weatherCity by remember { mutableStateOf(ir.kharjyar.app.weather.WeatherService.city(context)) }

    /** هر تغییر تنظیمات، ویجت‌های روی صفحه اصلی را هم تازه می‌کند. */
    fun applyChange(block: suspend () -> Unit) {
        scope.launch {
            block()
            WidgetUpdater.requestUpdate(context)
        }
    }

    val previewLines: List<WidgetPreviewLine> = when (settings.widgetContent) {
        WidgetContent.TODAY_EXPENSE ->
            listOf(WidgetPreviewLine("برداشت امروز", "۳٬۲۵۰٬۰۰۰", false))
        WidgetContent.MONTH_EXPENSE ->
            listOf(WidgetPreviewLine("برداشت مهر", "۱۸٬۴۰۰٬۰۰۰", false))
        WidgetContent.RECENT -> listOf(
            WidgetPreviewLine("برداشت", "۱٬۲۸۰٬۰۰۰", false),
            WidgetPreviewLine("برداشت", "۴۹۰٬۰۰۰", false)
        )
        WidgetContent.SUMMARY -> listOf(
            WidgetPreviewLine("واریز مهر", "۵٬۸۷۰٬۰۰۰", true),
            WidgetPreviewLine("برداشت مهر", "۳٬۲۵۰٬۰۰۰", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- پیش‌نمایش زنده ----------
        WidgetCard("پیش‌نمایش") {
            Text(
                "هر تغییری که اینجا بدهید، همین‌جا دیده می‌شود و روی ویجت صفحه اصلی هم اعمال می‌گردد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WidgetPreview(
                layout = settings.widgetLayout,
                opacity = settings.widgetOpacity,
                showNumbers = settings.widgetShowNumbers,
                lines = previewLines,
                showTitle = settings.widgetShowTitle,
                showClock = settings.widgetShowClock,
                showDates = settings.widgetShowDates,
                clockSize = settings.widgetClockSize,
                valueSize = settings.widgetValueSize,
                labelSize = settings.widgetLabelSize,
                editable = true,
                titleAlign = settings.widgetTitleAlign,
                titleVAlign = settings.widgetTitleVAlign,
                clockAlign = settings.widgetClockAlign,
                clockVAlign = settings.widgetClockVAlign,
                onTitlePlaced = { h, v -> applyChange { viewModel.settingsRepo.setWidgetTitleAlign(h); viewModel.settingsRepo.setWidgetTitleVAlign(v) } },
                onClockPlaced = { h, v -> applyChange { viewModel.settingsRepo.setWidgetClockAlign(h); viewModel.settingsRepo.setWidgetClockVAlign(v) } }
            )
        }

        WidgetCard("هواشناسی بدون موقعیت مکانی") {
            Text("شهر را دستی انتخاب کنید؛ خرج‌یار هیچ دسترسی مکانی درخواست نمی‌کند.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(weatherCity, { weatherCity = it }, label = { Text("نام شهر") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                ir.kharjyar.app.weather.WeatherService.setCity(context, weatherCity)
                scope.launch { ir.kharjyar.app.weather.WeatherService.refresh(context); WidgetUpdater.requestUpdate(context) }
            }, modifier = Modifier.fillMaxWidth()) { Text("ذخیره شهر و بروزرسانی هوا") }
        }

        // ---------- قالب و محتوا ----------
        WidgetCard("قالب و محتوا") {
            ComboBox(
                label = "قالب ویجت",
                options = WidgetLayout.entries.toList(),
                selected = settings.widgetLayout,
                labelOf = { widgetLayoutLabel(it) },
                onSelect = { v -> applyChange { viewModel.settingsRepo.setWidgetLayout(v) } }
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
                labelOf = { widgetContentLabel(it) },
                onSelect = { v -> applyChange { viewModel.settingsRepo.setWidgetContent(v) } }
            )
            Text(
                "قالب با رنگ تم فعال برنامه ساخته می‌شود؛ ساعت و تاریخ میلادی همیشه زنده‌اند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text("برای جابه‌جایی، برچسب هر بخش را مستقیماً روی پیش‌نمایش بکشید.", style = MaterialTheme.typography.bodySmall)

        // ---------- چه چیزهایی دیده شود ----------
        WidgetCard("چه چیزهایی نمایش داده شود") {
            ToggleRow(
                title = "نام برنامه",
                subtitle = "نوشته «خرج‌یار» و خط زیر آن",
                checked = settings.widgetShowTitle
            ) { v -> applyChange { viewModel.settingsRepo.setWidgetShowTitle(v) } }

            ToggleRow(
                title = "ساعت",
                subtitle = "ساعت زنده گوشی",
                checked = settings.widgetShowClock
            ) { v -> applyChange { viewModel.settingsRepo.setWidgetShowClock(v) } }

            ToggleRow(
                title = "تاریخ شمسی و میلادی",
                subtitle = "زیر ساعت نشان داده می‌شوند",
                checked = settings.widgetShowDates
            ) { v -> applyChange { viewModel.settingsRepo.setWidgetShowDates(v) } }

            ToggleRow(
                title = "نمایش اعداد",
                subtitle = "با خاموش بودن، به‌جای مبلغ نقطه‌چین دیده می‌شود",
                checked = settings.widgetShowNumbers
            ) { v -> applyChange { viewModel.settingsRepo.setWidgetShowNumbers(v) } }

            if (settings.appLockEnabled) {
                ToggleRow(
                    title = "نمایش اعداد با وجود قفل برنامه",
                    subtitle = "پیش‌فرض: با قفل فعال، اعداد ویجت مخفی‌اند",
                    checked = settings.widgetShowNumbersWhenLocked
                ) { v -> applyChange { viewModel.settingsRepo.setWidgetShowNumbersWhenLocked(v) } }
            }
        }

        // ---------- اندازه‌ها و شفافیت ----------
        WidgetCard("اندازه و شفافیت") {
            LabeledSlider(
                label = "میزان شیشه‌ای بودن",
                value = settings.widgetOpacity,
                range = 0..100,
                valueSuffix = "٪",
                onValueChange = { v -> applyChange { viewModel.settingsRepo.setWidgetOpacity(v) } }
            )
            Text(
                "عدد کمتر یعنی شیشه‌ای‌تر (تصویر زمینه گوشی بیشتر دیده می‌شود).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabeledSlider(
                label = "اندازه ساعت",
                value = settings.widgetClockSize,
                range = 18..72,
                onValueChange = { v -> applyChange { viewModel.settingsRepo.setWidgetClockSize(v) } }
            )
            LabeledSlider(
                label = "اندازه تاریخ‌ها",
                value = settings.widgetDateSize,
                range = 8..28,
                onValueChange = { v -> applyChange { viewModel.settingsRepo.setWidgetDateSize(v) } }
            )
            LabeledSlider(
                label = "اندازه مبالغ",
                value = settings.widgetValueSize,
                range = 9..30,
                onValueChange = { v -> applyChange { viewModel.settingsRepo.setWidgetValueSize(v) } }
            )
            LabeledSlider(
                label = "اندازه برچسب‌ها",
                value = settings.widgetLabelSize,
                range = 7..22,
                onValueChange = { v -> applyChange { viewModel.settingsRepo.setWidgetLabelSize(v) } }
            )
        }

        // ---------- افزودن ویجت ----------
        WidgetCard("افزودن ویجت") {
            Text(
                "اگر ویجت روی صفحه اصلی نیست، از این دکمه اضافه‌اش کنید. اندروید برای " +
                    "چسباندن ویجت، یک تأیید کوتاه از لانچر می‌گیرد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = {
                    runCatching {
                        val mgr = AppWidgetManager.getInstance(context)
                        val component = ComponentName(context, KharjYarWidgetReceiver::class.java)
                        if (mgr.isRequestPinAppWidgetSupported) {
                            mgr.requestPinAppWidget(component, null, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("افزودن ویجت به صفحه اصلی") }
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    SkinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** یک ردیف کلید روشن/خاموش با توضیح کوتاه. */
@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** برچسب فارسی قالب‌های ویجت. */
fun widgetLayoutLabel(l: WidgetLayout): String = when (l) {
    WidgetLayout.ROYAL -> "لوکس (قاب طلایی، ساعت خیلی بزرگ)"
    WidgetLayout.MINIMAL -> "مینیمال (عنوان بزرگ، ساعت کنار)"
    WidgetLayout.PANELS -> "نواری (سربرگ بالا، مقادیر در نوار)"
    WidgetLayout.STACKED -> "نواری فشرده"
    WidgetLayout.SPLIT -> "دوبخشی (خط وسط)"
    WidgetLayout.GLASS -> "شیشه‌ای (با تصویر تم)"
}

/** برچسب فارسی محتوای ویجت. */
fun widgetContentLabel(c: WidgetContent): String = when (c) {
    WidgetContent.SUMMARY -> "خلاصه ماه"
    WidgetContent.TODAY_EXPENSE -> "برداشت امروز"
    WidgetContent.MONTH_EXPENSE -> "برداشت ماه"
    WidgetContent.RECENT -> "آخرین تراکنش‌ها"
}
