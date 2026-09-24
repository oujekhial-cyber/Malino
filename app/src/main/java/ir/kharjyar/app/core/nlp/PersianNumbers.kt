package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.text.Digits

/**
 * تبدیل عددهای فارسیِ گفتاری به مقدار عددی.
 *
 * نمونه‌ها:
 *   «۲۵۰ هزار»        → 250000
 *   «دویست و پنجاه هزار» → 250000
 *   «دو میلیون و نیم»  → 2500000
 *   «سیصد و بیست»      → 320
 *
 * هم رقم و هم حرف را می‌فهمد و ترکیبشان را هم (مثل «۲ میلیون و ۵۰۰ هزار»).
 */
object PersianNumbers {

    /** واحدهای یکان تا نودونه. */
    private val units: Map<String, Long> = mapOf(
        "صفر" to 0L, "یک" to 1L, "دو" to 2L, "سه" to 3L, "چهار" to 4L,
        "پنج" to 5L, "شش" to 6L, "شیش" to 6L, "هفت" to 7L, "هشت" to 8L, "نه" to 9L,
        "ده" to 10L, "یازده" to 11L, "دوازده" to 12L, "سیزده" to 13L,
        "چهارده" to 14L, "پانزده" to 15L, "پونزده" to 15L, "شانزده" to 16L, "شونزده" to 16L,
        "هفده" to 17L, "هیفده" to 17L, "هجده" to 18L, "هیجده" to 18L, "نوزده" to 19L,
        "بیست" to 20L, "سی" to 30L, "چهل" to 40L, "پنجاه" to 50L,
        "شصت" to 60L, "هفتاد" to 70L, "هشتاد" to 80L, "نود" to 90L,
        "صد" to 100L, "یکصد" to 100L, "دویست" to 200L, "سیصد" to 300L, "سی‌صد" to 300L,
        "چهارصد" to 400L, "پانصد" to 500L, "پونصد" to 500L, "ششصد" to 600L, "شیشصد" to 600L,
        "هفتصد" to 700L, "هشتصد" to 800L, "نهصد" to 900L
    )

    /** ضریب‌های بزرگ. ترتیب از بزرگ به کوچک مهم است. */
    private val scales: List<Pair<String, Long>> = listOf(
        "میلیارد" to 1_000_000_000L,
        "ملیارد" to 1_000_000_000L,
        "میلیون" to 1_000_000L,
        "ملیون" to 1_000_000L,
        "هزار" to 1_000L
    )

    /**
     * نخستین عدد موجود در متن را برمی‌گرداند.
     * اگر عددی پیدا نشود null است.
     */
    fun parseFirst(text: String): Long? {
        val tokens = tokenize(text)
        // پنجره‌ای از توکن‌ها که پشت سر هم عدد می‌سازند
        var i = 0
        while (i < tokens.size) {
            if (!isNumeric(tokens[i])) { i++; continue }
            var j = i
            while (j < tokens.size && (isNumeric(tokens[j]) || tokens[j] == "و")) j++
            // «و» انتهایی جزو عدد نیست
            var end = j
            while (end > i && tokens[end - 1] == "و") end--
            val value = evaluate(tokens.subList(i, end))
            if (value != null) return value
            i = end.coerceAtLeast(i + 1)
        }
        return null
    }

    /** آیا این توکن جزئی از یک عدد است؟ */
    private fun isNumeric(t: String): Boolean =
        t.toLongOrNull() != null ||
            units.containsKey(t) ||
            scales.any { it.first == t } ||
            t == "نیم" || t == "ونیم" || t == "و‌نیم"

    private fun tokenize(text: String): List<String> =
        Digits.normalize(text)
            .replace('\u200c', ' ')          // نیم‌فاصله
            .replace(Regex("[,،٬]"), "")      // جداکننده هزارگان
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

    /**
     * ارزیابی دنباله توکن‌های عددی.
     *
     * الگوریتم استاندارد عدد-به-متن: مقدارهای کوچک جمع می‌شوند تا به یک ضریب
     * برسیم؛ آن‌گاه جمع جاری در ضریب ضرب و به کل اضافه می‌شود.
     */
    private fun evaluate(tokens: List<String>): Long? {
        var total = 0L
        var current = 0L
        var sawAny = false
        var pendingHalf = false      // «نیم» که هنوز ضریبش نیامده: «نیم میلیون»
        var lastScale = 0L           // آخرین ضریب مصرف‌شده: برای «دو میلیون و نیم»

        for (t in tokens) {
            when {
                t == "و" -> Unit

                t == "نیم" || t == "ونیم" || t == "و‌نیم" -> {
                    if (lastScale > 0L && current == 0L) {
                        // «دو میلیون و نیم» → نیمِ همان ضریب قبلی
                        total += lastScale / 2
                    } else {
                        // «نیم میلیون» → منتظر ضریب بعدی می‌مانیم
                        pendingHalf = true
                    }
                    sawAny = true
                }

                t.toLongOrNull() != null -> {
                    current += t.toLong()
                    sawAny = true
                }

                units.containsKey(t) -> {
                    current += units.getValue(t)
                    sawAny = true
                }

                else -> {
                    val scale = scales.firstOrNull { it.first == t }?.second
                        ?: return null
                    // «هزار» بدون عدد قبلی یعنی یک‌هزار
                    val base = if (current == 0L && !pendingHalf) 1L else current
                    total += base * scale
                    if (pendingHalf) {
                        total += scale / 2
                        pendingHalf = false
                    }
                    lastScale = scale
                    current = 0L
                    sawAny = true
                }
            }
        }
        if (!sawAny) return null
        return total + current
    }
}
