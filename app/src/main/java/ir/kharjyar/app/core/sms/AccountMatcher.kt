package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/**
 * تشخیص حساب مرتبط با پیامک از روی ترکیب «فرستنده + شناسه داخل متن».
 * یک فرستنده الزاماً یک حساب نیست؛ چند حساب می‌توانند از یک سرشماره پیامک بگیرند.
 */
data class SenderMapping(
    val mappingId: Long,
    val accountId: Long,
    val sender: String,
    val identifierHint: String
)

sealed class AccountMatch {
    /** دقیقاً یک حساب مطابقت دارد. */
    data class Single(val accountId: Long) : AccountMatch()

    /** چند حساب محتمل‌اند؛ باید از کاربر پرسیده شود. */
    data class Ambiguous(val accountIds: List<Long>) : AccountMatch()

    /** هیچ حسابی شناخته نشد. */
    object Unknown : AccountMatch()
}

object AccountMatcher {

    fun match(sender: String, body: String, mappings: List<SenderMapping>): AccountMatch {
        val senderNorm = normalizeSender(sender)
        val bodyNorm = Digits.normalizeForMatch(body)
        val forSender = mappings.filter { normalizeSender(it.sender) == senderNorm }
        if (forSender.isEmpty()) return AccountMatch.Unknown

        // اول: تطبیق با شناسه داخل متن
        val withHint = forSender.filter { it.identifierHint.isNotBlank() }
        val hintMatches = withHint.filter { m ->
            val hint = Digits.normalize(m.identifierHint).filter(Char::isDigit)
            hint.isNotEmpty() && bodyDigitsContains(bodyNorm, hint)
        }
        val hintAccountIds = hintMatches.map { it.accountId }.distinct()
        if (hintAccountIds.size == 1) return AccountMatch.Single(hintAccountIds[0])
        if (hintAccountIds.size > 1) return AccountMatch.Ambiguous(hintAccountIds)

        // بدون تطبیق شناسه: اگر فقط یک حساب کلی (بدون hint) برای فرستنده تعریف شده
        val generic = forSender.filter { it.identifierHint.isBlank() }.map { it.accountId }.distinct()
        val allAccounts = forSender.map { it.accountId }.distinct()
        return when {
            generic.size == 1 && allAccounts.size == 1 -> AccountMatch.Single(generic[0])
            allAccounts.size == 1 && withHint.isNotEmpty() ->
                // یک حساب با hint تعریف شده ولی hint در متن پیدا نشد؛ محتمل ولی نامطمئن
                AccountMatch.Ambiguous(allAccounts)
            allAccounts.size > 1 -> AccountMatch.Ambiguous(allAccounts)
            generic.size == 1 -> AccountMatch.Single(generic[0])
            else -> AccountMatch.Unknown
        }
    }

    /** آیا ارقام hint به صورت پیوسته در بخش‌های عددی متن آمده؟ */
    private fun bodyDigitsContains(body: String, hintDigits: String): Boolean {
        // بخش‌های شبه‌شناسه: دنباله‌های عددی همراه ستاره/نقطه
        val tokens = Regex("[*٭.\\d-]{3,}").findAll(body).map { it.value.filter(Char::isDigit) }
        return tokens.any { it.isNotEmpty() && it.contains(hintDigits) }
    }

    /** شش رقم پایانی شناسه با حفظ جداکننده‌های بین آن‌ها و بدون ستاره. */
    fun shortIdentifier(raw: String, digitCount: Int = 6): String {
        val normalized = Digits.normalize(raw)
        val reversed = StringBuilder()
        var digits = 0
        for (ch in normalized.reversed()) {
            when {
                ch.isDigit() && digits < digitCount -> { reversed.append(ch); digits++ }
                digits in 1 until digitCount && ch in ".-/\\" -> reversed.append(ch)
                digits >= digitCount -> break
            }
        }
        return reversed.reverse().toString().trim('.', '-', '/', '\\')
    }

    fun normalizeSender(sender: String): String =
        Digits.normalize(sender.trim()).removePrefix("+98").removePrefix("0098").trimStart('0')
}
