package ir.kharjyar.app.core.card

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits

/**
 * چیزی که از روی کارت بانکی خوانده شده است.
 * هر فیلد خالی یعنی «پیدا نشد» و نباید چیزی را در فرم خراب کند.
 */
data class CardScan(
    /** ۱۶ رقم، فقط وقتی پر می‌شود که آزمون Luhn را رد کند. */
    val cardNumber: String = "",
    /** به شکل MM/YYYY شمسی، هم‌شکل با فرم معرفی حساب. */
    val expiry: String = "",
    val cvv2: String = "",
    /** ۲۴ رقم، بدون IR. */
    val iban: String = "",
    val accountNumber: String = "",
    /** نام بانک حدس‌زده‌شده از شش رقم اول کارت (BIN). */
    val bankName: String = ""
) {
    val isEmpty: Boolean
        get() = cardNumber.isBlank() && expiry.isBlank() && cvv2.isBlank() &&
            iban.isBlank() && accountNumber.isBlank()

    /** شماره کارت، انقضا و CVV2 روی خود کارت هستند؛ رسیدن به هر سه یعنی کار تمام است. */
    val hasCardBasics: Boolean
        get() = cardNumber.isNotBlank() && expiry.isNotBlank() && cvv2.isNotBlank()

    /** انباشت نتیجه فریم‌های پیاپی دوربین: هر فیلد با اولین مقدار معتبرش پر می‌ماند. */
    fun mergedWith(newer: CardScan): CardScan = CardScan(
        cardNumber = cardNumber.ifBlank { newer.cardNumber },
        expiry = expiry.ifBlank { newer.expiry },
        cvv2 = cvv2.ifBlank { newer.cvv2 },
        iban = iban.ifBlank { newer.iban },
        accountNumber = accountNumber.ifBlank { newer.accountNumber },
        bankName = bankName.ifBlank { newer.bankName }
    )
}

/**
 * استخراج اطلاعات کارت بانکی از متن خام OCR.
 *
 * همه چیز روی خود گوشی انجام می‌شود؛ اینجا فقط متنِ تشخیص‌داده‌شده تحلیل می‌شود
 * و هیچ ارتباط شبکه‌ای در کار نیست.
 *
 * قاعده کلی: «چیزی که مطمئن نیستیم را ننویس». شماره کارت باید Luhn را رد کند و
 * شبا باید checksum استاندارد mod-97 داشته باشد، وگرنه نادیده گرفته می‌شود تا
 * عدد اشتباه در فرم ننشیند.
 */
object CardScanParser {

    /** اجرای کامل روی متن OCR. */
    fun parse(raw: String, currentJalaliYear: Int = PersianDate.today().year): CardScan {
        val text = Digits.normalize(raw).replace('\u200c', ' ').replace('\u066B', '.')
        var scan = extract(text, currentJalaliYear, allowIban = true)
        if (!scan.hasCardBasics) {
            // تلاش دوم با اصلاح حروف شبیه رقم (O→0، I→1). چون Luhn/mod-97 بررسی
            // می‌شود، حدس اشتباه تقریباً همیشه رد می‌شود.
            val fixed = fixDigitLookalikes(text)
            if (fixed != text) scan = scan.mergedWith(extract(fixed, currentJalaliYear, allowIban = false))
        }
        return if (scan.cardNumber.isNotBlank() && scan.bankName.isBlank()) {
            scan.copy(bankName = bankOfCard(scan.cardNumber))
        } else scan
    }

    // ---------------------------------------------------------------- داخلی

    private fun extract(text: String, currentYear: Int, allowIban: Boolean): CardScan {
        var card = ""
        var ibanValid = ""
        var ibanGuess = ""

        for (line in text.split('\n')) {
            val marksIban = line.contains("IR", ignoreCase = true) || line.contains("شبا")
            for (piece in DIGIT_RUN.findAll(line)) {
                val digits = piece.value.filter(Char::isDigit)
                when {
                    digits.length == 24 -> {
                        if (isIbanValid(digits)) {
                            if (ibanValid.isBlank()) ibanValid = digits
                        } else if (marksIban && ibanGuess.isBlank()) {
                            ibanGuess = digits
                        }
                    }
                    digits.length >= 16 -> {
                        if (card.isBlank()) card = firstLuhnWindow(digits)
                    }
                }
            }
        }

        val iban = if (allowIban) ibanValid.ifBlank { ibanGuess } else ""
        return CardScan(
            cardNumber = card,
            expiry = findExpiry(text, currentYear),
            cvv2 = findCvv2(text),
            iban = iban,
            accountNumber = findAccountNumber(text),
            bankName = if (card.isNotBlank()) bankOfCard(card) else ""
        )
    }

    /** یک بلوک عدد با جداکننده‌های رایج چاپ روی کارت. */
    private val DIGIT_RUN = Regex("[0-9](?:[0-9 \\-]*[0-9])?")

    /** اولین پنجره ۱۶ رقمی که Luhn را رد کند (بلوک بلندتر می‌تواند CVV2 چسبیده داشته باشد). */
    private fun firstLuhnWindow(digits: String): String {
        for (start in 0..(digits.length - 16)) {
            val candidate = digits.substring(start, start + 16)
            if (isLuhnValid(candidate)) return candidate
        }
        return ""
    }

    /** آزمون Luhn استاندارد کارت‌های شتاب. */
    fun isLuhnValid(number: String): Boolean {
        if (number.length != 16 || !number.all(Char::isDigit)) return false
        var sum = 0
        for (i in number.indices) {
            var d = number[number.length - 1 - i] - '0'
            if (i % 2 == 1) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return sum % 10 == 0
    }

    /**
     * اعتبارسنجی شبا: «IR» + ۲۴ رقم، با جابه‌جایی چهار نویسه اول به انتها و
     * تبدیل I=18 و R=27، باقیمانده بر ۹۷ باید ۱ شود.
     */
    fun isIbanValid(digits24: String): Boolean {
        if (digits24.length != 24 || !digits24.all(Char::isDigit)) return false
        val rearranged = digits24.substring(2) + "1827" + digits24.substring(0, 2)
        var remainder = 0
        for (ch in rearranged) {
            remainder = (remainder * 10 + (ch - '0')) % 97
        }
        return remainder == 1
    }

    // ---------------------------------------------------------------- انقضا

    private val EXPIRY_PAIR = Regex("(?<![0-9])(\\d{1,4})\\s*[/\\-]\\s*(\\d{2,4})(?![0-9])")
    private val EXPIRY_LABEL = Regex(
        "(?:انقضا|انقضاء|اعتبار|EXP(?:IRES|IRY)?|VALID\\s*THRU)[^0-9]{0,12}(\\d{4})(?![0-9])",
        RegexOption.IGNORE_CASE
    )

    /** خروجی MM/YYYY شمسی، یا رشته خالی اگر چیز قابل اعتمادی پیدا نشد. */
    private fun findExpiry(text: String, currentYear: Int): String {
        for (m in EXPIRY_PAIR.findAll(text)) {
            interpretations(m.groupValues[1], m.groupValues[2])
                .firstOrNull { (month, year) -> isPlausibleExpiry(month, year, currentYear) }
                ?.let { (month, year) -> return format(month, year) }
        }
        // شکل بدون جداکننده روی بعضی کارت‌ها (مثل «انقضا ۰۲۰۸» یعنی آبان ۱۴۰۲)
        EXPIRY_LABEL.find(text)?.let { m ->
            val four = m.groupValues[1]
            interpretations(four.substring(0, 2), four.substring(2))
                .firstOrNull { (month, year) -> isPlausibleExpiry(month, year, currentYear) }
                ?.let { (month, year) -> return format(month, year) }
        }
        return ""
    }

    /**
     * همه خوانش‌های ممکن یک جفت عدد، به ترتیب اولویت.
     * اولین موردی که تاریخ معقولی بدهد برنده است.
     */
    private fun interpretations(a: String, b: String): List<Pair<Int, Int>> {
        val x = a.toIntOrNull() ?: return emptyList()
        val y = b.toIntOrNull() ?: return emptyList()
        val out = mutableListOf<Pair<Int, Int>>()
        interpretPair(a, b)?.let { out += it }
        // کارت‌های با تاریخ میلادی دو رقمی (مثل 07/27)
        if (b.length == 2 && x in 1..12 && y in 20..99) out += x to (1379 + y)
        if (a.length == 2 && y in 1..12 && x in 20..99) out += y to (1379 + x)
        return out
    }

    /**
     * تشخیص اینکه کدام بخش ماه است و کدام سال.
     * کارت‌های ایرانی معمولاً «سال/ماه» دو رقمی چاپ می‌کنند (۰۵/۰۹ یعنی آذر ۱۴۰۵)
     * ولی وقتی سال چهار رقمی باشد شکل «ماه/سال» است (۰۶/۱۴۰۵).
     */
    private fun interpretPair(a: String, b: String): Pair<Int, Int>? {
        val x = a.toIntOrNull() ?: return null
        val y = b.toIntOrNull() ?: return null
        return when {
            b.length == 4 -> x to y            // MM/YYYY
            a.length == 4 -> y to x            // YYYY/MM
            y > 12 && x in 1..12 -> x to fullYear(y)   // ماه/سال
            x > 12 && y in 1..12 -> y to fullYear(x)   // سال/ماه
            x in 1..12 && y in 1..12 -> y to fullYear(x) // مبهم: عرف ایران سال/ماه
            else -> null
        }
    }

    /** ۰۵ → ۱۴۰۵ و ۹۹ → ۱۳۹۹. */
    private fun fullYear(yy: Int): Int = when {
        yy >= 1300 -> yy
        yy >= 1900 -> yy - 621          // اگر کارت میلادی چاپ کرده بود
        yy <= 50 -> 1400 + yy
        else -> 1300 + yy
    }

    private fun isPlausibleExpiry(month: Int, year: Int, currentYear: Int): Boolean =
        month in 1..12 && year in (currentYear - 8)..(currentYear + 15)

    /** بدون String.format تا در زبان فارسی دستگاه، ارقام لاتین بماند. */
    private fun format(month: Int, year: Int): String =
        month.toString().padStart(2, '0') + "/" + year.toString().padStart(4, '0')

    // ---------------------------------------------------------------- CVV2

    private val CVV_RE = Regex(
        "(?:CVV2|CVV|CVC2|CVC)[^0-9]{0,10}(\\d{3,4})(?![0-9])",
        RegexOption.IGNORE_CASE
    )

    /** فقط با برچسب پذیرفته می‌شود؛ عدد سه رقمی بی‌برچسب می‌تواند هر چیزی باشد. */
    private fun findCvv2(text: String): String = CVV_RE.find(text)?.groupValues?.get(1) ?: ""

    // ------------------------------------------------------- شماره حساب

    private val ACCOUNT_RE = Regex("شماره\\s*حساب[^0-9]{0,10}([0-9][0-9.\\-]{3,29})")

    private fun findAccountNumber(text: String): String =
        ACCOUNT_RE.find(text)?.groupValues?.get(1)?.trimEnd('.', '-') ?: ""

    // ------------------------------------------------------------- بانک

    /**
     * شش رقم اول کارت (BIN) بانک صادرکننده را مشخص می‌کند.
     * فهرست با نام‌های همان فهرست بانک‌های صفحه معرفی حساب هماهنگ است.
     */
    private val BINS: Map<String, String> = mapOf(
        "502908" to "توسعه تعاون",
        "603799" to "ملی",
        "610433" to "ملت", "991975" to "ملت",
        "603769" to "صادرات",
        "627353" to "تجارت", "585983" to "تجارت",
        "589210" to "سپه",
        "603770" to "کشاورزی", "639217" to "کشاورزی",
        "628023" to "مسکن",
        "589463" to "رفاه",
        "502229" to "پاسارگاد", "639347" to "پاسارگاد",
        "622106" to "پارسیان", "627884" to "پارسیان", "639194" to "پارسیان",
        "621986" to "سامان",
        "627412" to "اقتصاد نوین",
        "502806" to "شهر", "504706" to "شهر",
        "502938" to "دی",
        "639346" to "سینا",
        "627488" to "کارآفرین", "502910" to "کارآفرین",
        "636214" to "آینده",
        "505416" to "گردشگری",
        "505785" to "ایران زمین",
        "585947" to "خاورمیانه",
        "504172" to "رسالت",
        "606373" to "قرض‌الحسنه مهر",
        "627760" to "پست بانک"
    )

    fun bankOfCard(cardNumber: String): String =
        if (cardNumber.length >= 6) BINS[cardNumber.substring(0, 6)] ?: "" else ""

    // ------------------------------------------------- اصلاح حروف شبیه رقم

    private fun fixDigitLookalikes(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            sb.append(
                when (ch) {
                    'O', 'o', 'Q', 'D' -> '0'
                    'l', '|' -> '1'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }
}
