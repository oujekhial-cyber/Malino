package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** نگاشت محافظه‌کارانه سرشماره‌های نام‌دار بانک‌ها؛ فقط در صورت وجود دقیقاً یک حساب همان بانک استفاده می‌شود. */
object BankSenderResolver {
    private val aliases = mapOf(
        "TTBANK" to "توسعه تعاون",
        "TOSEETAAVON" to "توسعه تعاون",
        "TEJARATBANK" to "تجارت",
        "MELLIBANK" to "ملی",
        "BANKMELLI" to "ملی",
        "MELLAT" to "ملت",
        "BANSADERAT" to "صادرات",
        "BANKSADERAT" to "صادرات",
        "BSEP" to "سپه",
        "BANKSEPAH" to "سپه",
        "PASARGAD" to "پاسارگاد",
        "SAMANBANK" to "سامان",
        "PARSIANBANK" to "پارسیان",
        "BLUBANK" to "بلو بانک"
    )
    fun bankName(sender:String):String? { val key=Digits.normalize(sender).uppercase().filter(Char::isLetterOrDigit);return aliases.entries.firstOrNull{key==it.key||key.contains(it.key)}?.value }
    fun sameBank(saved:String,inferred:String):Boolean {
        fun normalized(value:String):String = Digits.normalizeForMatch(value)
            .replace("بانک", "").replace("موسسه", "").replace("مؤسسه", "")
            .filter(Char::isLetterOrDigit)
        return normalized(saved) == normalized(inferred)
    }
}
