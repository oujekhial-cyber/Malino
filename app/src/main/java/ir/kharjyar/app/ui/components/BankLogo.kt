package ir.kharjyar.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.R

/**
 * نشان (لوگو) بانک‌ها.
 *
 * تصویر خودِ لوگوی رسمی هر بانک داخل برنامه است (`res/drawable-xxxhdpi/bank_*.png`).
 * لوگوها از مجموعه «Iranian Bank Logos» (amastaneh/IranianBankLogos، پروانه ISC)
 * گرفته شده‌اند که نسخه منتشرشده روی سایت بانک مرکزی است؛ نوشته نام بانک از زیر
 * هر لوگو بریده شده تا فقط خودِ نشان بماند و در اندازه کوچک خوانا باشد. مجموع
 * حجم ۳۲ تصویر حدود ۱۴۰ کیلوبایت است.
 *
 * اگر نام بانکی در فهرست نباشد (مثلاً «سایر» یا نام دست‌نویس کاربر)، به‌جای لوگو
 * یک نشان ساده با رنگ و کوته‌نوشت همان نام ساخته می‌شود.
 */

/** کلید فایل لوگو برای هر بانک؛ ترتیب فهرست مهم است (خاص‌ها اول). */
private val logoKeys: List<Pair<String, String>> = listOf(
    // نام‌های چندبخشی که نباید با نام کوتاه‌تر اشتباه گرفته شوند
    "توسعه تعاون" to "toseetaavon",
    "توسعه صادرات" to "toseesaderat",
    "ایران زمین" to "iranzamin",
    "ایران ونزوئلا" to "iranvenezuela",
    "ایران و ونزوئلا" to "iranvenezuela",
    "مهر اقتصاد" to "mehreghtesad",
    "مهر ایران" to "mehriran",
    "قرض مهر" to "mehriran",
    "صنعت و معدن" to "sanatmadan",
    "صنعت معدن" to "sanatmadan",
    "اقتصاد نوین" to "eghtesadnovin",
    "رفاه کارگران" to "refah",
    "حکمت ایرانیان" to "hekmat",
    "پست بانک" to "post",
    // نام‌های تک‌واژه‌ای
    "ملی" to "melli",
    "ملت" to "mellat",
    "ملل" to "melal",
    "صادرات" to "saderat",
    "تجارت" to "tejarat",
    "سپه" to "sepah",
    "کشاورزی" to "keshavarzi",
    "مسکن" to "maskan",
    "رفاه" to "refah",
    "پاسارگاد" to "pasargad",
    "پارسیان" to "parsian",
    "سامان" to "saman",
    "شهر" to "shahr",
    "دی" to "day",
    "سینا" to "sina",
    "کارآفرین" to "karafarin",
    "آینده" to "ayandeh",
    "گردشگری" to "gardeshgari",
    "خاورمیانه" to "khavarmianeh",
    "رسالت" to "resalat",
    "سرمایه" to "sarmayeh",
    "انصار" to "ansar",
    "قوامین" to "ghavamin",
    "حکمت" to "hekmat",
    "بلو" to "blu",
    "نور" to "noor",
    "مهر" to "mehriran",
    "پست" to "post"
)

private val logoRes: Map<String, Int> = mapOf(
    "toseetaavon" to R.drawable.bank_toseetaavon,
    "toseesaderat" to R.drawable.bank_toseesaderat,
    "iranzamin" to R.drawable.bank_iranzamin,
    "iranvenezuela" to R.drawable.bank_iranvenezuela,
    "mehriran" to R.drawable.bank_mehriran,
    "sanatmadan" to R.drawable.bank_sanatmadan,
    "eghtesadnovin" to R.drawable.bank_eghtesadnovin,
    "refah" to R.drawable.bank_refah,
    "post" to R.drawable.bank_post,
    "melli" to R.drawable.bank_melli,
    "mellat" to R.drawable.bank_mellat,
    "melal" to R.drawable.bank_melal,
    "saderat" to R.drawable.bank_saderat,
    "tejarat" to R.drawable.bank_tejarat,
    "sepah" to R.drawable.bank_sepah,
    "keshavarzi" to R.drawable.bank_keshavarzi,
    "maskan" to R.drawable.bank_maskan,
    "pasargad" to R.drawable.bank_pasargad,
    "parsian" to R.drawable.bank_parsian,
    "saman" to R.drawable.bank_saman,
    "shahr" to R.drawable.bank_shahr,
    "day" to R.drawable.bank_day,
    "sina" to R.drawable.bank_sina,
    "karafarin" to R.drawable.bank_karafarin,
    "ayandeh" to R.drawable.bank_ayandeh,
    "gardeshgari" to R.drawable.bank_gardeshgari,
    "khavarmianeh" to R.drawable.bank_khavarmianeh,
    "resalat" to R.drawable.bank_resalat,
    "ansar" to R.drawable.bank_ansar,
    "sarmayeh" to R.drawable.bank_sarmayeh,
    "hekmat" to R.drawable.bank_hekmat,
    "ghavamin" to R.drawable.bank_ghavamin,
    "blu" to R.drawable.bank_blu,
    "noor" to R.drawable.bank_noor,
    "mehreghtesad" to R.drawable.bank_mehreghtesad
)

/** نام بانک را برای مقایسه ساده می‌کند: بدون «بانک»/«موسسه»، بدون نیم‌فاصله. */
private fun normalizeBank(bankName: String): String =
    bankName
        .replace('\u200c', ' ')
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace("بانک", " ")
        .replace("موسسه", " ")
        .replace("مؤسسه", " ")
        .replace("اعتباری", " ")
        .replace("الحسنه", " ")
        .replace("قرض", " قرض ")
        .split(' ', '\t', '\n')
        .filter { it.isNotBlank() }
        .joinToString(" ")

/**
 * کلید فایل لوگوی بانک (مثلاً `mellat`) یا null اگر بانک شناخته نشود.
 * بیرون از Compose هم قابل استفاده است، پس تست‌پذیر می‌ماند.
 */
fun bankAssetKey(bankName: String): String? {
    val n = normalizeBank(bankName)
    if (n.isBlank()) return null
    for ((needle, key) in logoKeys) {
        val nn = normalizeBank(needle)
        if (n == nn || n.contains(nn)) return key
    }
    return null
}

/** شناسه تصویر لوگوی بانک، یا null اگر بانک شناخته نشود. */
@DrawableRes
fun bankLogoRes(bankName: String): Int? = bankAssetKey(bankName)?.let { logoRes[it] }

/** رنگ شناخته‌شده بانک؛ برای حلقه دور نشان و نشان جایگزین. */
private val bankColors: Map<String, Color> = mapOf(
    "toseetaavon" to Color(0xFF00A79D),
    "melli" to Color(0xFF1B4F9C),
    "mellat" to Color(0xFFD12236),
    "saderat" to Color(0xFF2B1A6B),
    "tejarat" to Color(0xFF1B3D8F),
    "sepah" to Color(0xFF1F3C88),
    "keshavarzi" to Color(0xFF0F8A3D),
    "maskan" to Color(0xFFF4623A),
    "refah" to Color(0xFF7A1F7A),
    "pasargad" to Color(0xFFE8B10D),
    "parsian" to Color(0xFF8E1B3A),
    "saman" to Color(0xFF29ABE2),
    "eghtesadnovin" to Color(0xFF7B1FA2),
    "shahr" to Color(0xFFE4002B),
    "day" to Color(0xFF00A3A1),
    "sina" to Color(0xFF1B3F94),
    "karafarin" to Color(0xFF1B6B3A),
    "ayandeh" to Color(0xFF6B4226),
    "gardeshgari" to Color(0xFFB2182B),
    "iranzamin" to Color(0xFF8E44AD),
    "khavarmianeh" to Color(0xFFF39200),
    "resalat" to Color(0xFF00A0A0),
    "mehriran" to Color(0xFF7CBB3F),
    "post" to Color(0xFF0F8A3D),
    "ansar" to Color(0xFFD9262E),
    "sarmayeh" to Color(0xFF1B3F60),
    "melal" to Color(0xFFF07E26),
    "hekmat" to Color(0xFF1B6BA8),
    "ghavamin" to Color(0xFF00A651),
    "sanatmadan" to Color(0xFF1B3D8F),
    "toseesaderat" to Color(0xFF0F7A3D),
    "iranvenezuela" to Color(0xFF283593),
    "blu" to Color(0xFF4E91E6),
    "noor" to Color(0xFF0BBBB9),
    "mehreghtesad" to Color(0xFF029A4C)
)

/** رنگ شناخته‌شده بانک؛ اگر بانک ناشناس باشد null. */
fun bankColorOf(bankName: String): Color? = bankAssetKey(bankName)?.let { bankColors[it] }

/**
 * رنگ کارت یک حساب: رنگ لوگوی بانک. اگر بانک ناشناس باشد، رنگ ذخیره‌شده خود
 * حساب می‌ماند. با این کار دیگر لازم نیست کاربر رنگ کارت را دستی انتخاب کند.
 */
fun bankCardColor(bankName: String, fallbackArgb: Long): Color =
    bankColorOf(bankName) ?: Color(fallbackArgb)

/** همان `bankCardColor` ولی به شکل ARGB برای ذخیره در پایگاه داده. */
fun bankCardColorArgb(bankName: String, fallbackArgb: Long): Long {
    val c = bankColorOf(bankName) ?: return fallbackArgb
    val a = (c.alpha * 255f).toInt().toLong() and 0xFF
    val r = (c.red * 255f).toInt().toLong() and 0xFF
    val g = (c.green * 255f).toInt().toLong() and 0xFF
    val b = (c.blue * 255f).toInt().toLong() and 0xFF
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

/** کوته‌نوشت نام بانک، برای وقتی لوگویی در دست نیست. */
fun bankShortOf(bankName: String): String {
    val n = normalizeBank(bankName)
    if (n.isBlank()) return ""
    val first = n.split(' ').first()
    return if (first.length <= 5) first else first.take(4)
}

/**
 * نشان بانک: لوگوی رسمی داخل یک دایره سفید.
 *
 * @param ringColor رنگ حلقه دور نشان (مثلاً رنگ خود حساب یا رنگ متن کارت).
 */
@Composable
fun BankLogo(
    bankName: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    ringColor: Color? = null
) {
    val res = bankLogoRes(bankName)
    val ring = (ringColor ?: Color(0xFF9E9E9E)).copy(alpha = if (ringColor != null) 0.75f else 0.35f)

    if (res != null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White)
                .border(if (ringColor != null) 1.5.dp else 1.dp, ring, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(res),
                contentDescription = bankName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // بانک ناشناس: نشان ساده با کوته‌نوشت نام
    val label = bankShortOf(bankName)
    val base = bankColorOf(bankName)
    val onBadge = if (base != null && base.luminance() > 0.6f) Color(0xFF14121A) else Color.White
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        (base ?: Color(0xFF6E6E78)).lighten(0.22f),
                        (base ?: Color(0xFF3C3C45)).darken(0.18f)
                    )
                )
            )
            .border(if (ringColor != null) 1.5.dp else 1.dp, ring, CircleShape),
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
