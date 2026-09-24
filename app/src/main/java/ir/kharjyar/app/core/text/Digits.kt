package ir.kharjyar.app.core.text

/**
 * ابزار نرمال‌سازی ارقام فارسی/عربی/لاتین و جداکننده‌های عددی.
 * تمام پردازش محلی است.
 */
object Digits {

    /**
     * سبک ارقام انتخابی کاربر. چون نمایش ارقام در ده‌ها نقطه (شامل ویجت که
     * خارج از درخت Compose است) لازم می‌شود، به‌جای پاس دادن دستی از یک
     * پرچم سراسری استفاده می‌شود که هنگام خواندن تنظیمات ست می‌شود.
     */
    @Volatile
    var usePersianDigits: Boolean = true

    private val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /** تبدیل همه ارقام فارسی و عربی به لاتین. */
    fun normalize(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val p = persian.indexOf(ch)
            val a = arabic.indexOf(ch)
            sb.append(
                when {
                    p >= 0 -> ('0' + p)
                    a >= 0 -> ('0' + a)
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    /**
     * تبدیل ارقام لاتین به فارسی برای نمایش.
     * اگر کاربر ارقام لاتین را انتخاب کرده باشد، رشته دست‌نخورده برمی‌گردد.
     */
    fun toPersian(input: String): String {
        if (!usePersianDigits) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') sb.append(persian[ch - '0']) else sb.append(ch)
        }
        return sb.toString()
    }

    /** آغازگر ایزوله چپ‌به‌راست (نامرئی). */
    const val LRI = '\u2066'

    /** پایان ایزوله جهت‌دار (نامرئی). */
    const val PDI = '\u2069'

    /**
     * رشته را داخل «ایزوله چپ‌به‌راست» می‌گذارد.
     *
     * در متن راست‌به‌چپ، گروه‌های عددی که با فاصله از هم جدا شده‌اند برعکس چیده
     * می‌شوند (مثلاً شماره کارت «۵۰۲۹ ۰۸۱۰ ۸۳۲۴ ۸۶۰۵» به شکل «۸۶۰۵ ۸۳۲۴ ۰۸۱۰ ۵۰۲۹»
     * دیده می‌شود). این دو نویسه نامرئی، ترتیب داخلی را چپ‌به‌راست نگه می‌دارند
     * بدون اینکه چینش راست‌چین بقیه متن به‌هم بخورد.
     */
    fun ltr(input: String): String =
        if (input.isEmpty()) input else "$LRI$input$PDI"

    /** حذف نویسه‌های جهت‌دهی از یک رشته (برای مقایسه و کپی). */
    fun stripBidi(input: String): String = input.filter { it != LRI && it != PDI }

    /**
     * گروه‌بندی چهارتایی شماره کارت با ترتیب درست در محیط راست‌به‌چپ.
     * ورودی فقط ارقام لاتین است؛ خروجی آماده نمایش (با ارقام انتخابی کاربر).
     */
    fun cardGroups(digits: String, separator: String = "  "): String =
        ltr(toPersian(digits.chunked(4).joinToString(separator)))

    /** تبدیل اجباری به ارقام فارسی، بدون توجه به تنظیم کاربر. */
    fun toPersianAlways(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') sb.append(persian[ch - '0']) else sb.append(ch)
        }
        return sb.toString()
    }

    /**
     * پارس یک رشته عددی (مبلغ) با جداکننده‌های مختلف: , ، ٬ . /
     * نقطه/اسلش فقط وقتی جداکننده هزارگان حساب می‌شود که الگو با گروه‌های سه‌رقمی سازگار باشد.
     * خروجی null یعنی رشته یک عدد معتبر نیست.
     */
    fun parseAmount(raw: String): Long? {
        val s = normalize(raw.trim())
            .replace("\u200c", "")
            .replace(" ", "")
        if (s.isEmpty()) return null
        // جداکننده‌های رایج هزارگان
        val cleaned = s.replace(",", "").replace("،", "").replace("٬", "").replace("'", "")
        // نقطه یا اسلش: اگر همه گروه‌های بعدی سه‌رقمی باشند جداکننده هزارگان است
        val candidate = if (cleaned.contains('.') || cleaned.contains('/')) {
            val parts = cleaned.split('.', '/')
            if (parts.size > 1 && parts.drop(1).all { it.length == 3 && it.all(Char::isDigit) } &&
                parts.first().isNotEmpty() && parts.first().all(Char::isDigit)
            ) {
                parts.joinToString("")
            } else {
                return null
            }
        } else cleaned
        if (candidate.isEmpty() || !candidate.all(Char::isDigit)) return null
        if (candidate.length > 17) return null
        return candidate.toLongOrNull()
    }

    /** جداکننده سه‌رقمی برای نمایش، با ارقام فارسی. */
    fun group(value: Long, persianDigits: Boolean = true): String {
        val negative = value < 0
        val abs = if (negative) -value else value
        val grouped = abs.toString().reversed().chunked(3).joinToString("،").reversed()
        val res = if (negative) "-$grouped" else grouped
        return if (persianDigits) toPersian(res) else res
    }
}
