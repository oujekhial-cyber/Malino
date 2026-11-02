package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** استخراج محافظه‌کارانه مبلغ جریمه از متن پیامک راهور؛ خروجی همیشه ریال است. */
object TrafficFineParser {
    fun amountRial(body: String): Long? {
        val text = Digits.normalizeForMatch(body)
        val patterns = listOf(
            Regex("(?:مبلغ(?: جریمه)?|جریمه(?: به مبلغ)?)\\s*[:：]?\\s*([0-9][0-9,،٬. ]{2,})\\s*(ریال|تومان)"),
            Regex("([0-9][0-9,،٬. ]{2,})\\s*(ریال|تومان)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues[1].filter(Char::isDigit).toLongOrNull() ?: continue
            if (value <= 0) continue
            return if (match.groupValues[2] == "تومان") runCatching { Math.multiplyExact(value, 10L) }.getOrNull() else value
        }
        return null
    }
}
