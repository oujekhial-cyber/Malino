package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.UtilityBillProfileEntity

/** اتصال پیامک قبض فقط به شناسه/اشتراکی که کاربر از قبل ثبت کرده است. */
object UtilityBillMatcher {
    fun looksLikeBillMessage(body:String):Boolean {
        val text=Digits.normalizeForMatch(body)
        return listOf("قبض","شناسه قبض","شناسه پرداخت","مهلت پرداخت","بدهی دوره","صورتحساب").any(text::contains)
    }

    fun normalizeIdentifier(value:String):String = Digits.normalize(value).filter(Char::isDigit)

    fun uniqueProfileId(body:String, profiles:List<UtilityBillProfileEntity>):Long? {
        val textDigits = Digits.normalize(body)
        val matched = profiles.asSequence().filter { it.active }.filter { profile ->
            val id = normalizeIdentifier(profile.identifier)
            id.length >= 4 && Regex("(?<!\\d)${Regex.escape(id)}(?!\\d)").containsMatchIn(textDigits.replace(Regex("[\\s.ـ_/-]"),""))
        }.map { it.id }.distinct().toList()
        return matched.singleOrNull()
    }
}
