package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** تشخیص محافظه‌کارانه و محلی پیامک واریز سود واقعی سپرده. */
object InterestSmsDetector {
    private val phrases = listOf(
        "سود سپرده", "سود ماهانه", "سود علی الحساب", "سود علی‌الحساب",
        "واریز سود", "سود واریزی", "سود متعلقه", "سود تعلق گرفته",
        "سود دوره", "سود دوره ای", "سود دوره‌ای", "سود حساب پس انداز", "سود حساب پس‌انداز"
    ).map(Digits::normalizeForMatch)

    fun isInterest(body: String): Boolean {
        val text = Digits.normalizeForMatch(body)
        return phrases.any(text::contains)
    }
}
