package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * نشان بانک.
 *
 * این‌ها آرم رسمی بانک‌ها نیستند (استفاده از آرم ثبت‌شده بانک‌ها داخل برنامه
 * مجوز می‌خواهد و فایل تصویری هم حجم اضافه می‌آورد). به‌جایش برای هر بانک یک
 * «نشان» ساخته می‌شود: دایره‌ای با رنگ شناخته‌شده همان بانک و کوته‌نوشت نامش.
 * نتیجه در عمل همان کارکرد آرم را دارد: هر بانک در کارت و در فهرست بانک‌ها
 * با رنگ و نشان خودش شناخته می‌شود.
 */
private val bankColors: Map<String, Color> = mapOf(
    "توسعه تعاون" to Color(0xFF00A79D),
    "ملی" to Color(0xFF1B4F9C),
    "ملت" to Color(0xFFE5006D),
    "صادرات" to Color(0xFF0B4EA2),
    "تجارت" to Color(0xFF00539F),
    "سپه" to Color(0xFF1F3C88),
    "کشاورزی" to Color(0xFF0F8A3D),
    "مسکن" to Color(0xFF0B3D91),
    "رفاه" to Color(0xFF00795B),
    "پاسارگاد" to Color(0xFFC9A227),
    "پارسیان" to Color(0xFFD71920),
    "سامان" to Color(0xFF0072BC),
    "اقتصاد نوین" to Color(0xFF00A0AF),
    "شهر" to Color(0xFFE4002B),
    "دی" to Color(0xFF16A085),
    "سینا" to Color(0xFF005BAA),
    "کارآفرین" to Color(0xFF0F6B3B),
    "آینده" to Color(0xFF6A1B9A),
    "گردشگری" to Color(0xFF00B0B9),
    "ایران زمین" to Color(0xFF2E7D32),
    "خاورمیانه" to Color(0xFF17375E),
    "رسالت" to Color(0xFF1E8449),
    "قرض‌الحسنه مهر" to Color(0xFF0D6E9E),
    "پست بانک" to Color(0xFFF2A900)
)

/** کوته‌نوشت نام بانک برای نشستن داخل دایره نشان. */
private val bankShort: Map<String, String> = mapOf(
    "توسعه تعاون" to "تعاون",
    "ملی" to "ملی",
    "ملت" to "ملت",
    "صادرات" to "صاد",
    "تجارت" to "تج",
    "سپه" to "سپه",
    "کشاورزی" to "کشا",
    "مسکن" to "مسکن",
    "رفاه" to "رفاه",
    "پاسارگاد" to "پاس",
    "پارسیان" to "پار",
    "سامان" to "سام",
    "اقتصاد نوین" to "نوین",
    "شهر" to "شهر",
    "دی" to "دی",
    "سینا" to "سینا",
    "کارآفرین" to "کار",
    "آینده" to "آینده",
    "گردشگری" to "گرد",
    "ایران زمین" to "ایران",
    "خاورمیانه" to "خاور",
    "رسالت" to "رسالت",
    "قرض‌الحسنه مهر" to "مهر",
    "پست بانک" to "پست"
)

/** رنگ شناخته‌شده بانک؛ اگر بانک ناشناس باشد null. */
fun bankColorOf(bankName: String): Color? {
    val key = bankName.trim().removePrefix("بانک ").trim()
    return bankColors[key] ?: bankColors.entries.firstOrNull { key.contains(it.key) }?.value
}

/** کوته‌نوشت نام بانک؛ برای بانک ناشناس، دو حرف اول نام. */
fun bankShortOf(bankName: String): String {
    val key = bankName.trim().removePrefix("بانک ").trim()
    bankShort[key]?.let { return it }
    bankShort.entries.firstOrNull { key.contains(it.key) }?.let { return it.value }
    return key.take(3)
}

/**
 * نشان دایره‌ای بانک.
 *
 * @param bankName نام بانک همان‌طور که کاربر در حساب ثبت کرده است.
 * @param ringColor اگر نشان روی کارت رنگی بنشیند، رنگ حلقه دور آن.
 */
@Composable
fun BankLogo(
    bankName: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    ringColor: Color? = null
) {
    val base = bankColorOf(bankName)
    val label = bankShortOf(bankName)
    val onBadge = if (base != null && base.luminance() > 0.6f) Color(0xFF14121A) else Color.White

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (base != null) {
                    Brush.linearGradient(listOf(base.lighten(0.22f), base.darken(0.18f)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFF6E6E78), Color(0xFF3C3C45)))
                }
            )
            .border(
                if (ringColor != null) 1.5.dp else 1.dp,
                (ringColor ?: onBadge).copy(alpha = if (ringColor != null) 0.75f else 0.4f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (label.isBlank()) {
            Icon(
                Icons.Filled.AccountBalance,
                contentDescription = null,
                tint = onBadge,
                modifier = Modifier.size(size * 0.5f)
            )
        } else {
            Text(
                label,
                color = onBadge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                fontSize = (size.value * 0.30f).sp,
                lineHeight = (size.value * 0.34f).sp
            )
        }
    }
}

private fun Color.lighten(f: Float) = Color(
    red + (1f - red) * f,
    green + (1f - green) * f,
    blue + (1f - blue) * f,
    alpha
)

private fun Color.darken(f: Float) = Color(red * (1f - f), green * (1f - f), blue * (1f - f), alpha)
