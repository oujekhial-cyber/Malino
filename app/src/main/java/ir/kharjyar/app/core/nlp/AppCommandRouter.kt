package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.text.Digits

/** مسیریاب قطعی و آفلاین فرمان‌ها؛ عبارت‌های مسیریابی هیچ‌گاه به سرویس آنلاین فرستاده نمی‌شوند. */
object AppCommandRouter {
    private val commands = listOf(
        listOf("تراکنش", "برداشت ها", "واریز ها", "لیست کن", "نمایش بده") to "transactions",
        listOf("گزارش", "آمار", "تحلیل مالی") to "reports",
        listOf("حساب ها", "حساب بانکی", "کیف پول") to "accounts",
        listOf("دسته بندی") to "categories",
        listOf("بدهی", "طلب") to "debts",
        listOf("وام", "قسط", "اقساط") to "loans",
        listOf("چک") to "checks",
        listOf("دارایی", "طلا", "ارز", "سکه") to "assets",
        listOf("خودرو", "ماشین") to "vehicles",
        listOf("یادآوری") to "reminders",
        listOf("یادداشت") to "notes",
        listOf("پیامک بانکی", "پیامک ها") to "review",
        listOf("بازار", "قیمت طلا", "قیمت دلار") to "marketPulse",
        listOf("پشتیبان", "بکاپ") to "backup",
        listOf("تنظیمات") to "settings"
    )

    fun route(text: String): String? {
        val normalized = Digits.normalizeForMatch(text).replace('‌',' ')
        val navigationIntent = listOf("باز کن", "برو", "نشان بده", "نمایش بده", "لیست کن", "فهرست").any(normalized::contains)
        if (!navigationIntent) return null
        return commands.firstOrNull { (words, _) -> words.any(normalized::contains) }?.second
    }
}
