package ir.kharjyar.app.core.money

/** تبدیل مبلغ صحیح ریالی به حروف فارسی برای درج روی چک. */
object PersianNumberWords {
    private val ones = listOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = listOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val tens = listOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = listOf("", "یکصد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
    private val scales = listOf("", "هزار", "میلیون", "میلیارد", "تریلیون", "کوادریلیون")

    fun rial(value: Long): String = "${toWords(value.coerceAtLeast(0))} ریال"

    fun toWords(value: Long): String {
        if (value == 0L) return "صفر"
        var number = value
        var group = 0
        val parts = mutableListOf<String>()
        while (number > 0 && group < scales.size) {
            val chunk = (number % 1000).toInt()
            if (chunk != 0) {
                val scale = scales[group]
                parts += listOf(underThousand(chunk), scale).filter { it.isNotBlank() }.joinToString(" ")
            }
            number /= 1000
            group++
        }
        return parts.asReversed().joinToString(" و ")
    }

    private fun underThousand(value: Int): String {
        val result = mutableListOf<String>()
        val hundred = value / 100
        val rest = value % 100
        if (hundred > 0) result += hundreds[hundred]
        when {
            rest == 0 -> Unit
            rest < 10 -> result += ones[rest]
            rest < 20 -> result += teens[rest - 10]
            else -> {
                result += tens[rest / 10]
                if (rest % 10 > 0) result += ones[rest % 10]
            }
        }
        return result.joinToString(" و ")
    }
}
