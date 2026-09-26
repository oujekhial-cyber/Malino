package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** اطلاعات عددی حساب که برای تطبیق پیامک لازم است. */
data class MatchableAccount(
    val id: Long,
    val maskedNumber: String,
    val accountNumber: String,
    val iban: String,
    val cardNumber: String
)

/** تطبیق حساب از خود شماره کارت/حساب/شبا در متن، مستقل از قالب بانک. */
object AccountNumberMatcher {
    fun match(body: String, accounts: List<MatchableAccount>): AccountMatch {
        val bodyDigits = numericTokens(body)
        val matched = accounts.filter { account ->
            identifiers(account).any { identifier ->
                bodyDigits.any { token -> identifiersAgree(token, identifier) }
            }
        }.map { it.id }.distinct()
        return when (matched.size) {
            0 -> AccountMatch.Unknown
            1 -> AccountMatch.Single(matched.single())
            else -> AccountMatch.Ambiguous(matched)
        }
    }

    private fun identifiers(account: MatchableAccount): Set<String> =
        listOf(account.maskedNumber, account.accountNumber, account.iban, account.cardNumber)
            .map { Digits.normalize(it).filter(Char::isDigit) }
            .filter { it.length >= 4 }
            .toSet()

    private fun numericTokens(text: String): List<String> =
        Regex("[\\d۰-۹٠-٩*٭][\\d۰-۹٠-٩*٭.\\-]{2,}[\\d۰-۹٠-٩]").findAll(text)
            .map { Digits.normalize(it.value).filter(Char::isDigit) }
            .filter { it.length >= 4 }
            .toList()

    private fun identifiersAgree(inMessage: String, saved: String): Boolean {
        if (inMessage == saved) return true
        // پیامک‌ها معمولاً فقط ۴ تا ۶ رقم آخر را نشان می‌دهند. برای جلوگیری از
        // تطبیق مبلغ، فقط پسوند ذخیره‌شده پذیرفته می‌شود و حداقل چهار رقم لازم است.
        val shorter = minOf(inMessage.length, saved.length)
        val suffixLength = when {
            shorter >= 6 -> 6
            shorter >= 4 -> 4
            else -> return false
        }
        return inMessage.takeLast(suffixLength) == saved.takeLast(suffixLength)
    }
}
