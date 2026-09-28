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
        "BLUBANK" to "بلو بانک",
        "ENBANK" to "اقتصاد نوین",
        "EGHTESADNOVIN" to "اقتصاد نوین",
        "REFAHBANK" to "رفاه",
        "BANKREFAH" to "رفاه",
        "KESHAVARZI" to "کشاورزی",
        "BKI" to "کشاورزی",
        "MASKANBANK" to "مسکن",
        "BANKMASKAN" to "مسکن",
        "SHAHRBANK" to "شهر",
        "BANKSHAHR" to "شهر",
        "SARMAYEHBANK" to "سرمایه",
        "SINABANK" to "سینا",
        "KARAFARIN" to "کارآفرین",
        "AYANDEHBANK" to "آینده",
        "IRANZAMIN" to "ایران زمین",
        "DAYBANK" to "دی",
        "TOURISMBANK" to "گردشگری",
        "RESALATBANK" to "رسالت",
        "MEHRIRAN" to "قرض‌الحسنه مهر"
    )
    fun bankName(sender:String):String? { val key=Digits.normalize(sender).uppercase().filter(Char::isLetterOrDigit);return aliases.entries.firstOrNull{key==it.key||key.contains(it.key)}?.value }
    fun sameBank(saved:String,inferred:String):Boolean {
        fun normalized(value:String):String = Digits.normalizeForMatch(value)
            .replace("بانک", "").replace("موسسه", "").replace("مؤسسه", "")
            .filter(Char::isLetterOrDigit)
        return normalized(saved) == normalized(inferred)
    }
}
