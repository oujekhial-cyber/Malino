package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** نقش هر فیلد قابل استخراج از پیامک. */
enum class FieldRole { AMOUNT, BALANCE, ACCOUNT_ID, DATE, TIME, REF_NUMBER, COUNTERPARTY, DIRECTION }

/** جهت استخراج‌شده. */
enum class ExtractedDirection { DEPOSIT, WITHDRAW, UNKNOWN }

/** سطح اطمینان استخراج. */
enum class Confidence { HIGH, MEDIUM, LOW }

/** نتیجه استخراج از یک پیامک. */
@Serializable
data class ExtractionResult(
    val amountRial: Long? = null,
    val amountUnit: String = "RIAL",
    val direction: String = "UNKNOWN",
    val balanceRial: Long? = null,
    val accountIdHint: String? = null,
    val dateText: String? = null,
    val timeText: String? = null,
    val refNumber: String? = null,
    val counterparty: String? = null,
    val confidence: String = "LOW",
    /** epoch millis زمان تراکنش، اگر تاریخ/ساعت قابل استخراج بود. */
    val occurredAtMillis: Long? = null
) {
    fun directionEnum(): ExtractedDirection = runCatching { ExtractedDirection.valueOf(direction) }.getOrDefault(ExtractedDirection.UNKNOWN)
    fun confidenceEnum(): Confidence = runCatching { Confidence.valueOf(confidence) }.getOrDefault(Confidence.LOW)

    fun toJson(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun fromJson(s: String): ExtractionResult? = runCatching { json.decodeFromString(serializer(), s) }.getOrNull()
    }
}

/**
 * قاعده استخراج مبتنی بر برچسب (anchor): متنِ قبل از مقدار + نوع مقدار.
 * از موقعیت ثابت کاراکترها استفاده نمی‌شود.
 */
@Serializable
data class FieldRule(
    val role: String,
    /** برچسب/لنگری که قبل از مقدار می‌آید، مثل «مبلغ:» یا «برداشت» */
    val anchor: String,
    /** نوع مقدار: NUMBER, DATE, TIME, TEXT_LINE */
    val valueType: String = "NUMBER",
    /** حداکثر فاصله مجاز بین لنگر و مقدار (کاراکتر). */
    val maxGap: Int = 12
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun listToJson(rules: List<FieldRule>): String =
            json.encodeToString(kotlinx.serialization.builtins.ListSerializer(serializer()), rules)

        fun listFromJson(s: String): List<FieldRule> =
            runCatching {
                json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(serializer()), s)
            }.getOrDefault(emptyList())
    }
}

object Extractor {

    private val depositWords = listOf("واریز", "افزایش", "انتقال به حساب شما", "دریافت")
    private val withdrawWords = listOf("برداشت", "خرید", "کسر", "کاهش", "پرداخت", "انتقال از")

    private val amountAnchors = listOf("مبلغ:", "مبلغ", "به مبلغ", "برداشت:", "واریز:", "خرید:", "کسر:", "پرداخت:")
    private val balanceAnchors = listOf("مانده:", "مانده", "موجودی:", "موجودی")
    private val accountAnchors = listOf("حساب", "کارت", "سپرده", "حساب:", "کارت:")
    private val refAnchors = listOf("پیگیری:", "پیگیری", "مرجع:", "شماره پیگیری", "کد رهگیری")

    private val numberPattern = Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d+")
    private val datePattern = Regex("(\\d{2,4})[/\\-.](\\d{1,2})[/\\-.](\\d{1,2})")
    private val timePattern = Regex("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?")

    /** استخراج خودکار (heuristic) بدون قالب. */
    fun autoExtract(body: String): ExtractionResult {
        val text = Digits.normalize(body)
        val lines = text.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }

        var direction = ExtractedDirection.UNKNOWN
        for (w in depositWords) if (text.contains(w)) { direction = ExtractedDirection.DEPOSIT; break }
        if (direction == ExtractedDirection.UNKNOWN) {
            for (w in withdrawWords) if (text.contains(w)) { direction = ExtractedDirection.WITHDRAW; break }
        }

        // مانده: نزدیک‌ترین عدد بعد از لنگر مانده
        val balance = findNumberAfterAnchors(text, balanceAnchors)

        // مبلغ: نزدیک‌ترین عدد بعد از لنگر مبلغ/عمل، که با مانده یکی نباشد
        var amount = findNumberAfterAnchors(text, amountAnchors, exclude = balance?.second)
        if (amount == null) {
            // fallback: عدد بزرگ در سطری که کلمه عمل دارد
            for (line in lines) {
                if ((depositWords + withdrawWords).any { line.contains(it) }) {
                    val nums = numberPattern.findAll(line)
                        .mapNotNull { m -> Digits.parseAmount(m.value)?.let { it to m.range } }
                        .filter { it.first >= 1000 }
                        .toList()
                    val notBalance = nums.filter { it.first != balance?.first }
                    if (notBalance.size == 1) { amount = notBalance[0].first to notBalance[0].second; break }
                }
            }
        }

        val accountId = findAccountId(text)
        val dateMatch = datePattern.find(text)
        val timeMatch = timePattern.find(text)
        val ref = findNumberAfterAnchors(text, refAnchors)

        val confidence = when {
            amount != null && direction != ExtractedDirection.UNKNOWN && balance != null -> Confidence.HIGH
            amount != null && direction != ExtractedDirection.UNKNOWN -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return ExtractionResult(
            amountRial = amount?.first,
            direction = direction.name,
            balanceRial = balance?.first,
            accountIdHint = accountId,
            dateText = dateMatch?.value,
            timeText = timeMatch?.value,
            refNumber = ref?.first?.toString(),
            confidence = confidence.name,
            occurredAtMillis = parseOccurredAt(dateMatch, timeMatch)
        )
    }

    /** اعمال قواعد یک قالب آموزش‌دیده روی متن. */
    fun applyRules(body: String, rules: List<FieldRule>, amountUnit: String = "RIAL"): ExtractionResult {
        val text = Digits.normalize(body)
        var amount: Long? = null
        var balance: Long? = null
        var accountId: String? = null
        var ref: String? = null
        var counterparty: String? = null
        var dateText: String? = null
        var timeText: String? = null
        var direction = ExtractedDirection.UNKNOWN

        for (rule in rules) {
            val role = runCatching { FieldRole.valueOf(rule.role) }.getOrNull() ?: continue
            when (role) {
                FieldRole.DIRECTION -> {
                    if (text.contains(rule.anchor)) {
                        direction = if (rule.valueType == "DEPOSIT") ExtractedDirection.DEPOSIT else ExtractedDirection.WITHDRAW
                    }
                }
                else -> {
                    val value = extractAfterAnchor(text, rule) ?: continue
                    when (role) {
                        FieldRole.AMOUNT -> amount = Digits.parseAmount(value)
                        FieldRole.BALANCE -> balance = Digits.parseAmount(value)
                        FieldRole.ACCOUNT_ID -> accountId = value.trim()
                        FieldRole.REF_NUMBER -> ref = value.trim()
                        FieldRole.COUNTERPARTY -> counterparty = value.trim()
                        FieldRole.DATE -> dateText = value.trim()
                        FieldRole.TIME -> timeText = value.trim()
                        FieldRole.DIRECTION -> {}
                    }
                }
            }
        }

        // مبلغ به ریال تبدیل شود اگر واحد قالب تومان است
        val amountRial = amount?.let { if (amountUnit == "TOMAN") Math.multiplyExact(it, 10L) else it }
        val balanceRial = balance?.let { if (amountUnit == "TOMAN") Math.multiplyExact(it, 10L) else it }

        val dateMatch = dateText?.let { datePattern.find(it) } ?: datePattern.find(text)
        val timeMatch = timeText?.let { timePattern.find(it) } ?: timePattern.find(text)

        val confidence = when {
            amountRial != null && direction != ExtractedDirection.UNKNOWN -> Confidence.HIGH
            amountRial != null -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return ExtractionResult(
            amountRial = amountRial,
            amountUnit = amountUnit,
            direction = direction.name,
            balanceRial = balanceRial,
            accountIdHint = accountId,
            dateText = dateMatch?.value,
            timeText = timeMatch?.value,
            refNumber = ref,
            counterparty = counterparty,
            confidence = confidence.name,
            occurredAtMillis = parseOccurredAt(dateMatch, timeMatch)
        )
    }

    /** اعتبارسنجی قواعد قبل از ذخیره قالب: لنگر خالی/خیلی کوتاه یا نقش تکراری ممنوع. */
    fun validateRules(rules: List<FieldRule>): List<String> {
        val errors = mutableListOf<String>()
        if (rules.none { it.role == FieldRole.AMOUNT.name }) errors.add("قاعده مبلغ الزامی است")
        val seen = mutableSetOf<String>()
        for (r in rules) {
            if (r.role != FieldRole.DIRECTION.name && !seen.add(r.role)) errors.add("نقش تکراری: ${r.role}")
            if (r.anchor.isBlank()) errors.add("لنگر خالی برای ${r.role}")
            if (r.anchor.length > 60) errors.add("لنگر بیش از حد بلند برای ${r.role}")
            if (r.maxGap !in 0..60) errors.add("فاصله مجاز نامعتبر برای ${r.role}")
        }
        return errors
    }

    // ---------- internals ----------

    private fun extractAfterAnchor(text: String, rule: FieldRule): String? {
        val idx = text.indexOf(rule.anchor)
        if (idx < 0) return null
        val after = text.substring(idx + rule.anchor.length)
        val window = after.take(rule.maxGap + 40)
        return when (rule.valueType) {
            "NUMBER" -> {
                val m = numberPattern.find(window) ?: return null
                if (m.range.first > rule.maxGap) null else m.value
            }
            "DATE" -> {
                val m = datePattern.find(window) ?: return null
                if (m.range.first > rule.maxGap) null else m.value
            }
            "TIME" -> {
                val m = timePattern.find(window) ?: return null
                if (m.range.first > rule.maxGap) null else m.value
            }
            "TEXT_LINE" -> {
                val line = window.lineSequence().firstOrNull()?.trim()?.trimStart(':', '：', ' ')
                line?.take(60)?.ifBlank { null }
            }
            else -> null
        }
    }

    private fun findNumberAfterAnchors(
        text: String,
        anchors: List<String>,
        exclude: IntRange? = null
    ): Pair<Long, IntRange>? {
        for (anchor in anchors) {
            var start = 0
            while (true) {
                val idx = text.indexOf(anchor, start)
                if (idx < 0) break
                val afterStart = idx + anchor.length
                val window = text.substring(afterStart).take(40)
                val m = numberPattern.find(window)
                if (m != null && m.range.first <= 12) {
                    val absolute = (m.range.first + afterStart)..(m.range.last + afterStart)
                    if (exclude == null || absolute != exclude) {
                        val v = Digits.parseAmount(m.value)
                        if (v != null) return v to absolute
                    }
                }
                start = idx + anchor.length
            }
        }
        return null
    }

    private fun findAccountId(text: String): String? {
        for (anchor in accountAnchors) {
            val idx = text.indexOf(anchor)
            if (idx < 0) continue
            val window = text.substring(idx + anchor.length).take(30)
            // شناسه حساب: عددی که ممکن است با * یا . شروع شود مثل *1234 یا 6037...1234
            val m = Regex("[*٭.]{0,4}\\d{2,}[*٭.\\d-]*").find(window) ?: continue
            if (m.range.first <= 8) return m.value.trim()
        }
        return null
    }

    private fun parseOccurredAt(dateMatch: MatchResult?, timeMatch: MatchResult?): Long? {
        if (dateMatch == null) return null
        return runCatching {
            var y = dateMatch.groupValues[1].toInt()
            val mo = dateMatch.groupValues[2].toInt()
            val d = dateMatch.groupValues[3].toInt()
            if (y < 100) y += 1400 // 03/05/12 -> 1403
            if (mo !in 1..12 || d !in 1..31) return null
            val pd = ir.kharjyar.app.core.date.PersianDate(y, mo, d)
            if (d > pd.monthLength()) return null
            val hour = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val minute = timeMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0
            if (hour !in 0..23 || minute !in 0..59) return null
            ir.kharjyar.app.core.date.PersianDate.toMillis(pd, hour, minute)
        }.getOrNull()
    }

    /**
     * پیشنهاد خودکار قواعد قالب از یک نمونه پیامک و مقادیر تأییدشده کاربر.
     * برای هر فیلد، متن بلافاصله قبل از مقدار به عنوان لنگر انتخاب می‌شود.
     */
    fun suggestRules(body: String, fieldValues: Map<FieldRole, String>): List<FieldRule> {
        val text = Digits.normalize(body)
        val rules = mutableListOf<FieldRule>()
        for ((role, rawValue) in fieldValues) {
            if (role == FieldRole.DIRECTION) {
                rules.add(FieldRule(role.name, anchor = rawValue.substringBefore('|'), valueType = rawValue.substringAfter('|', "WITHDRAW")))
                continue
            }
            val value = Digits.normalize(rawValue.trim())
            if (value.isEmpty()) continue
            val idx = text.indexOf(value)
            if (idx <= 0) continue
            // لنگر: تا ۱۵ کاراکتر قبل از مقدار، بریده در مرز خط
            val before = text.substring(0, idx)
            val lineStart = maxOf(before.lastIndexOf('\n') + 1, before.length - 15)
            var anchor = before.substring(lineStart).trimStart()
            // لنگر نباید خودش عدد باشد
            anchor = anchor.dropWhile { it.isDigit() || it == ',' || it == '،' }.trimStart()
            if (anchor.isBlank()) continue
            val valueType = when (role) {
                FieldRole.DATE -> "DATE"
                FieldRole.TIME -> "TIME"
                FieldRole.COUNTERPARTY -> "TEXT_LINE"
                FieldRole.ACCOUNT_ID -> "NUMBER"
                else -> "NUMBER"
            }
            rules.add(FieldRule(role.name, anchor = anchor, valueType = valueType))
        }
        return rules
    }
}
