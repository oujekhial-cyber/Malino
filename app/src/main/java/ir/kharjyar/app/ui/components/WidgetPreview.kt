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
    showDates: Boolean = true,
    showImage: Boolean = true,
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
        // تصویر تم پشت ویجت (اگر فعال باشد) تا پیش‌نمایش با ویجت واقعی یکی باشد
        if (showImage) {
            skin.backdropImage?.let { res ->
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(res),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }
        }
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
                    if (showDates) {
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
}

/**
 * اسلایدر باریک و جمع‌وجور برای اندازه اجزای ویجت:
 * ریل نازک، دستگیره کوچک، و مقدار داخل یک کپسول کنار برچسب.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = skin.onBackdrop.copy(alpha = 0.9f)
            )
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
        androidx.compose.material3.Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            modifier = Modifier.height(24.dp),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = skin.accent,
                activeTrackColor = skin.accent,
                inactiveTrackColor = skin.onBackdrop.copy(alpha = 0.18f)
            ),
            // دستگیره کوچک و گرد به‌جای دستگیره بزرگ پیش‌فرض
            thumb = {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(skin.accent)
                )
            },
            // ریل نازک
            track = { state ->
                val fraction = if (range.last > range.first) {
                    (value - range.first).toFloat() / (range.last - range.first).toFloat()
                } else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(skin.onBackdrop.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(skin.accent)
                    )
                }
            }
        )
    }
}
