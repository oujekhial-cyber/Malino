package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.prefs.WidgetLayout
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlin.math.roundToInt

/** یک ردیف پیش‌نمایش ویجت؛ `income` جهت آیکون را مشخص می‌کند (null یعنی بدون جهت). */
data class WidgetPreviewLine(val label: String, val value: String, val income: Boolean? = null)

/**
 * پیش‌نمایش ویجت داخل «تنظیمات ویجت».
 * پشت کارت یک نوار رنگی کشیده می‌شود تا اثر شیشه‌ای بودن واقعاً دیده شود.
 * همه گزینه‌های اختیاری (عنوان، ساعت، تاریخ‌ها و اندازه فونت‌ها) در پیش‌نمایش
 * هم اعمال می‌شوند تا کاربر نتیجه را پیش از اعمال روی ویجت واقعی ببیند.
 */
@Composable
fun WidgetPreview(
    layout: WidgetLayout,
    opacity: Int,
    showNumbers: Boolean,
    lines: List<WidgetPreviewLine>,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showClock: Boolean = true,
    showDates: Boolean = true,
    clockSize: Int = 30,
    valueSize: Int = 12,
    labelSize: Int = 10
) {
    val skin = LocalAppSkin.current
    val today = PersianDate.today()
    val jalali = "${today.dayOfWeekName()} ${Digits.toPersian(today.day.toString())} ${today.monthName()}"
    val opts = PreviewOptions(showTitle, showClock, showDates, clockSize, valueSize, labelSize)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            // شبیه‌سازی تصویر زمینه گوشی
            .background(
                Brush.linearGradient(
                    listOf(skin.heroGradient.first(), skin.heroGradient.last(), skin.accent)
                )
            )
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(skin.cardColor.copy(alpha = opacity / 100f))
                .border(1.dp, skin.accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            when (layout) {
                WidgetLayout.PANELS, WidgetLayout.STACKED ->
                    PanelsPreview(jalali, lines, showNumbers, opts)
                WidgetLayout.ROYAL ->
                    RoyalPreview(jalali, lines, showNumbers, opts)
                else ->
                    SplitPreview(layout, jalali, lines, showNumbers, opts)
            }
        }
    }
}

/** گزینه‌های نمایشی مشترک بین قالب‌های پیش‌نمایش. */
private data class PreviewOptions(
    val showTitle: Boolean,
    val showClock: Boolean,
    val showDates: Boolean,
    val clockSize: Int,
    val valueSize: Int,
    val labelSize: Int
)

/** دوبخشی و مینیمال: مقادیر راست، ساعت چپ. */
@Composable
private fun SplitPreview(
    layout: WidgetLayout,
    jalali: String,
    lines: List<WidgetPreviewLine>,
    showNumbers: Boolean,
    opts: PreviewOptions
) {
    val skin = LocalAppSkin.current
    val big = layout == WidgetLayout.MINIMAL
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            if (opts.showTitle) {
                Text(
                    "خرج‌یار",
                    fontSize = if (big) 20.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = skin.accent
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier
                        .width(44.dp)
                        .height(1.dp)
                        .background(skin.onBackdrop.copy(alpha = 0.25f))
                )
                Spacer(Modifier.height(8.dp))
            }
            lines.forEach { ValueRow(it, showNumbers, opts) }
        }
        if (layout != WidgetLayout.MINIMAL) {
            Box(
                Modifier
                    .width(1.dp)
                    .height(64.dp)
                    .background(skin.onBackdrop.copy(alpha = 0.2f))
            )
        }
        Spacer(Modifier.width(12.dp))
        ClockBlock(jalali, opts, big)
    }
}

/** نواری: سربرگ بالا، مقادیر در نوارهای جدا. */
@Composable
private fun PanelsPreview(
    jalali: String,
    lines: List<WidgetPreviewLine>,
    showNumbers: Boolean,
    opts: PreviewOptions
) {
    val skin = LocalAppSkin.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (opts.showTitle) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = skin.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "خرج‌یار",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = skin.accent,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            ClockBlock(jalali, opts, false)
        }
        Spacer(Modifier.height(8.dp))
        lines.forEach { line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(skin.onBackdrop.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValueRow(line, showNumbers, opts)
            }
        }
    }
}

/** لوکس: ساعت خیلی بزرگ بالا، مقادیر با خط جداکننده. */
@Composable
private fun RoyalPreview(
    jalali: String,
    lines: List<WidgetPreviewLine>,
    showNumbers: Boolean,
    opts: PreviewOptions
) {
    val skin = LocalAppSkin.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (opts.showTitle) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = skin.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("خرج‌یار", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = skin.accent)
                }
            }
            ClockBlock(jalali, opts, true)
        }
        Spacer(Modifier.height(9.dp))
        lines.forEachIndexed { i, line ->
            if (i > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(skin.accent.copy(alpha = 0.18f))
                )
                Spacer(Modifier.height(6.dp))
            }
            ValueRow(line, showNumbers, opts)
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** بلوک ساعت و تاریخ‌ها. */
@Composable
private fun ClockBlock(jalali: String, opts: PreviewOptions, big: Boolean) {
    val skin = LocalAppSkin.current
    if (!opts.showClock && !opts.showDates) return
    // اندازه ساعت پیش‌نمایش نسبت به ویجت واقعی کوچک‌تر است تا در کادر جا شود
    val size = (opts.clockSize * if (big) 0.8f else 0.7f).toInt().coerceIn(12, 46)
    Column(horizontalAlignment = Alignment.End) {
        if (opts.showClock) {
            Text(
                Digits.toPersian("20:56"),
                fontSize = size.sp,
                fontWeight = FontWeight.Bold,
                color = skin.bigNumberColor
            )
        }
        if (opts.showDates) {
            Text(jalali, fontSize = 10.sp, color = skin.onBackdrop)
            Text(
                "22 Sept 2026",
                fontSize = 9.sp,
                color = skin.onBackdrop.copy(alpha = 0.7f)
            )
        }
    }
}

/** یک ردیف برچسب/مقدار با آیکون جهت همان ردیف. */
@Composable
private fun ValueRow(line: WidgetPreviewLine, showNumbers: Boolean, opts: PreviewOptions) {
    val skin = LocalAppSkin.current
    val income = line.income
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(skin.onBackdrop.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (income) {
                    true -> Icons.AutoMirrored.Filled.TrendingUp
                    false -> Icons.AutoMirrored.Filled.TrendingDown
                    null -> Icons.Filled.AccountBalanceWallet
                },
                contentDescription = null,
                tint = when (income) {
                    true -> skin.incomeColor
                    false -> skin.expenseColor
                    null -> skin.accent
                },
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(7.dp))
        Text(
            line.label,
            fontSize = opts.labelSize.sp,
            color = skin.onBackdrop,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            if (showNumbers) line.value else "••••",
            fontSize = opts.valueSize.sp,
            fontWeight = FontWeight.Bold,
            color = skin.bigNumberColor,
            maxLines = 1
        )
    }
}

/** اسلایدر باریک با نمایش مقدار در کپسول رنگ تم. */
@Composable
fun LabeledSlider(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    valueSuffix: String = "",
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = skin.onBackdrop)
            Text(
                Digits.toPersian(value.toString()) + valueSuffix,
                style = MaterialTheme.typography.labelSmall,
                color = skin.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(skin.accent.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = skin.accent,
                activeTrackColor = skin.accent,
                inactiveTrackColor = skin.onBackdrop.copy(alpha = 0.16f)
            )
        )
    }
}
