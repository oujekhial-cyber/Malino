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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.theme.LocalAppSkin

/**
 * پیش‌نمایش زنده ویجت داخل تنظیمات: دقیقاً همان چیدمان ویجت واقعی
 * (مقادیر سمت چپ، ساعت و تاریخ سمت راست) با اندازه‌ها و شفافیت انتخابی کاربر.
 *
 * برای اینکه شیشه‌ای بودن واقعاً دیده شود، پشت آن یک نوار رنگی (شبیه تصویر زمینه گوشی) رسم می‌شود.
 */
@Composable
fun WidgetPreview(
    opacity: Int,
    clockSize: Int,
    dateSize: Int,
    valueSize: Int,
    labelSize: Int,
    showClock: Boolean,
    showNumbers: Boolean,
    lines: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    val today = PersianDate.today()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            // شبیه‌سازی تصویر زمینه گوشی تا اثر شفافیت دیده شود
            .background(
                Brush.linearGradient(
                    listOf(skin.heroGradient.first(), skin.heroGradient.last(), skin.accent)
                )
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(skin.cardColor.copy(alpha = opacity / 100f))
                .border(
                    1.dp,
                    Brush.linearGradient(skin.cardBorderColors),
                    RoundedCornerShape(18.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---------- سمت چپ: مقادیر ----------
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "خرج‌یار",
                    fontSize = (labelSize * 1.3f).sp,
                    fontWeight = FontWeight.Bold,
                    color = skin.accent
                )
                Spacer(Modifier.height(6.dp))
                lines.forEach { (label, value) ->
                    Text(label, fontSize = labelSize.sp, color = skin.onBackdrop)
                    Text(
                        if (showNumbers) value else "••••",
                        fontSize = valueSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = skin.bigNumberColor
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            if (showClock) {
                Spacer(Modifier.width(12.dp))
                // ---------- سمت راست: ساعت و تاریخ ----------
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        Digits.toPersian("09:41"),
                        fontSize = clockSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = skin.bigNumberColor
                    )
                    Text(
                        "${today.dayOfWeekName()} ${Digits.toPersian(today.day.toString())} ${today.monthName()}",
                        fontSize = dateSize.sp,
                        fontWeight = FontWeight.Medium,
                        color = skin.onBackdrop
                    )
                    Text(
                        "22 Sep 2026",
                        fontSize = (dateSize * 0.85f).sp,
                        color = skin.onBackdrop.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/** اسلایدر برچسب‌دار با نمایش مقدار فعلی — برای اندازه اجزای ویجت. */
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = skin.onBackdrop)
            Text(
                Digits.toPersian(value.toString()) + valueSuffix,
                style = MaterialTheme.typography.labelLarge,
                color = skin.accent
            )
        }
        androidx.compose.material3.Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtMost(60)
        )
    }
}
