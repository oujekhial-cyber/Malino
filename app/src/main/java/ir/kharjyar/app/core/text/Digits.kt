package ir.kharjyar.app.core.text

/**
 * ابزار نرمال‌سازی ارقام فارسی/عربی/لاتین و جداکننده‌های عددی.
 * تمام پردازش محلی است.
 */
object Digits {

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

    /** تبدیل ارقام لاتین به فارسی برای نمایش. */
    fun toPersian(input: String): String {
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
