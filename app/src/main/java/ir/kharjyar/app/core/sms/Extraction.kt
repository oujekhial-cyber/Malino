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

    private val depositWords = listOf(
        "واریز", "واریزی", "وار.یز", "بستانکار", "افزایش", "دریافت", "عودت", "شارژ",
        "انتقال به حساب شما", "به حساب شما", "انتقال به", "حواله وارده", "وصول"
    )
    private val withdrawWords = listOf(
        "برداشت", "خرید", "کسر", "کاهش", "پرداخت", "انتقال از", "بدهکار", "حواله صادره",
        "کارمزد", "قبض", "انتقال وجه از", "خرید اینترنتی", "خرید شارژ"
    )

    private val amountAnchors = listOf(
        "مبلغ:", "به مبلغ", "مبلغ", "برداشت:", "واریز:", "خرید:", "کسر:", "پرداخت:",
        "بدهکار:", "بستانکار:", "بد:", "بس:"
    )
    private val balanceAnchors = listOf("مانده:", "مانده", "موجودی:", "موجودی", "باقیمانده")
    private val accountAnchors = listOf("حساب", "کارت", "سپرده", "حساب:", "کارت:")
    private val refAnchors = listOf("پیگیری:", "پیگیری", "مرجع:", "شماره پیگیری", "کد رهگیری")

    /** واژه‌هایی که یعنی مبلغ به تومان نوشته شده است. */
    private val tomanWords = listOf("تومان", "تومن", "هزارتومان")

    /** شناسه حساب/کارت به شکل ماسک‌شده یا شماره سپرده چندبخشی، هر جای متن. */
    private val maskedIdPattern = Regex(
        // 6037****1234 یا ****1234 یا 2404.306.5918267.1
        "\\d{2,6}[*٭]{1,8}\\d{2,6}|[*٭]{2,}\\d{3,6}|\\d{2,5}(?:[.\\-]\\d{1,7}){2,4}"
    )

    private val numberPattern = Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d+")
    /** مبلغی که + یا -، قبل یا بعدش آمده؛ الگوی اصلی نمونه‌های واقعی بانک‌ها. */
    private val signedAmountPattern = Regex(
        "(?<![\\d.,])([+-]?)\\s*(\\d{1,3}(?:[,،٬]\\d{3})+|\\d{4,})\\s*([+-]?)(?![\\d.,])"
    )
    private val datePattern = Regex("(\\d{2,4})[/\\-.](\\d{1,2})[/\\-.](\\d{1,2})")
    /** تاریخ بدون سال: 07/03_01:53 یعنی ماه ۷، روز ۳ سال جاری. */
    private val shortDatePattern = Regex("(?<![\\d/])(\\d{1,2})[/\\-.](\\d{1,2})(?=\\s*[_-]\\s*\\d{1,2}:)")
    /** تاریخ فشرده 0127-22:00 یعنی ۲۷ فروردین سال جاری. */
    private val compactDatePattern = Regex("(?<!\\d)(0[1-9]|1[0-2])([0-3]\\d)(?=\\s*[-_]\\s*\\d{1,2}:)")
    private val timePattern = Regex("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?")

    /** استخراج خودکار (heuristic) بدون قالب. */
    fun autoExtract(body: String): ExtractionResult {
        // حروف عربی و نیم‌فاصله یکدست می‌شوند تا کلیدواژه‌ها واقعاً پیدا شوند
        val text = Digits.normalizeForMatch(body)
        val lines = text.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }

        // جهت: نزدیک‌ترین کلیدواژه به ابتدای متن برنده است؛ اگر پیامکی هم
        // «برداشت» و هم «واریز» داشته باشد (مثل «برداشت و واریز به حساب…»)،
        // ترتیب ظاهر شدن تصمیم می‌گیرد، نه ترتیب فهرست ما.
        var direction = ExtractedDirection.UNKNOWN
        val depositAt = depositWords.mapNotNull { w -> text.indexOf(w).takeIf { it >= 0 } }.minOrNull()
        val withdrawAt = withdrawWords.mapNotNull { w -> text.indexOf(w).takeIf { it >= 0 } }.minOrNull()
        direction = when {
            depositAt != null && (withdrawAt == null || depositAt < withdrawAt) -> ExtractedDirection.DEPOSIT
            withdrawAt != null -> ExtractedDirection.WITHDRAW
            else -> ExtractedDirection.UNKNOWN
        }

        // مانده: نزدیک‌ترین عدد بعد از لنگر مانده
        val balance = findNumberAfterAnchors(text, balanceAnchors)

        // مبلغ علامت‌دار (+15,000,000 یا 1,000,000-) قطعی‌ترین نشانه است و
        // در پیامک‌های بدون هیچ عنوانی هم مبلغ و جهت را هم‌زمان مشخص می‌کند.
        val signed = findSignedAmount(text, balance?.second)
        var amount = signed?.let { it.amount to it.range }
        if (signed != null) direction = signed.direction

        // در نبود علامت: نزدیک‌ترین عدد بعد از لنگر مبلغ/عمل
        if (amount == null) amount = findNumberAfterAnchors(text, amountAnchors, exclude = balance?.second)
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

        // اگر هیچ کلیدواژه‌ای نبود، علامت جلوی مبلغ جهت را مشخص می‌کند: 500,000- یا +500,000
        if (direction == ExtractedDirection.UNKNOWN && amount != null) {
            direction = signDirection(text, amount!!.second)
        }

        val accountId = findAccountId(text)
        val dateMatch = datePattern.find(text)
        val shortDate = findFlexibleDate(text)
        val timeMatch = timePattern.find(text)
        val ref = findNumberAfterAnchors(text, refAnchors)

        // بعضی بانک‌ها مبلغ را به تومان می‌نویسند
        val toman = tomanWords.any { text.contains(it) }
        val factor = if (toman) 10L else 1L

        val confidence = when {
            amount != null && direction != ExtractedDirection.UNKNOWN && balance != null -> Confidence.HIGH
            amount != null && direction != ExtractedDirection.UNKNOWN -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return ExtractionResult(
            amountRial = amount?.first?.times(factor),
            amountUnit = if (toman) "TOMAN" else "RIAL",
            direction = direction.name,
            balanceRial = balance?.first?.times(factor),
            accountIdHint = accountId,
            dateText = dateMatch?.value ?: shortDate?.text,
            timeText = timeMatch?.value,
            refNumber = ref?.first?.toString(),
            confidence = confidence.name,
            occurredAtMillis = shortDate?.let { parseOccurredAt(it, timeMatch) }
                ?: parseOccurredAt(dateMatch, timeMatch)
        )
    }

    /** اعمال قواعد یک قالب آموزش‌دیده روی متن. */
    fun applyRules(body: String, rules: List<FieldRule>, amountUnit: String = "RIAL"): ExtractionResult {
        val text = Digits.normalizeForMatch(body)
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
        val idx = text.indexOf(Digits.normalizeForMatch(rule.anchor))
        if (idx < 0) return null
        val after = text.substring(idx + Digits.normalizeForMatch(rule.anchor).length)
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

    private data class SignedAmount(
        val amount: Long,
        val range: IntRange,
        val direction: ExtractedDirection
    )

    private fun findSignedAmount(text: String, exclude: IntRange?): SignedAmount? {
        for (m in signedAmountPattern.findAll(text)) {
            val sign = m.groupValues[1].ifBlank { m.groupValues[3] }
            if (sign != "+" && sign != "-") continue
            val numberGroup = m.groups[2] ?: continue
            val range = numberGroup.range
            if (exclude != null && rangesOverlap(range, exclude)) continue
            val amount = Digits.parseAmount(numberGroup.value) ?: continue
            if (amount < 1_000L) continue
            return SignedAmount(
                amount,
                range,
                if (sign == "+") ExtractedDirection.DEPOSIT else ExtractedDirection.WITHDRAW
            )
        }
        return null
    }

    private fun rangesOverlap(a: IntRange, b: IntRange): Boolean =
        a.first <= b.last && b.first <= a.last

    private data class FlexibleDate(val year: Int, val month: Int, val day: Int, val text: String)

    /** تاریخ کامل، ماه/روز بدون سال، یا MMDD فشرده. */
    private fun findFlexibleDate(text: String): FlexibleDate? {
        datePattern.find(text)?.let { m ->
            var a = m.groupValues[1].toInt()
            val b = m.groupValues[2].toInt()
            var c = m.groupValues[3].toInt()
            if (a <= 31 && m.groupValues[3].length == 4) {
                val year = c; c = a; a = year
            }
            if (a in 1900..2100) {
                val gregorian = java.time.LocalDate.of(a, b, c)
                val jalali = ir.kharjyar.app.core.date.PersianDate.fromLocalDate(gregorian)
                return FlexibleDate(jalali.year, jalali.month, jalali.day, m.value)
            }
            if (a < 100) a += 1400
            return FlexibleDate(a, b, c, m.value)
        }
        val currentYear = ir.kharjyar.app.core.date.PersianDate.today().year
        shortDatePattern.find(text)?.let { m ->
            return FlexibleDate(currentYear, m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.value)
        }
        compactDatePattern.find(text)?.let { m ->
            return FlexibleDate(currentYear, m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.value)
        }
        return null
    }

    private fun parseOccurredAt(date: FlexibleDate, timeMatch: MatchResult?): Long? = runCatching {
        if (date.month !in 1..12 || date.day !in 1..31) return null
        val pd = ir.kharjyar.app.core.date.PersianDate(date.year, date.month, date.day)
        if (date.day > pd.monthLength()) return null
        val hour = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minute = timeMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null
        ir.kharjyar.app.core.date.PersianDate.toMillis(pd, hour, minute)
    }.getOrNull()

    /** واژه‌ای که جهت پیامک را مشخص کرده است (برای پیش‌پرکردن آموزش قالب). */
    fun directionWordIn(body: String): String? {
        val text = Digits.normalizeForMatch(body)
        return (depositWords + withdrawWords)
            .filter { text.contains(it) }
            .minByOrNull { text.indexOf(it) }
    }

    /** جهت از روی علامت مثبت/منفی چسبیده به مبلغ. */
    private fun signDirection(text: String, amountRange: IntRange): ExtractedDirection {
        val before = text.substring(maxOf(0, amountRange.first - 2), amountRange.first)
        val after = text.substring(
            minOf(text.length, amountRange.last + 1),
            minOf(text.length, amountRange.last + 3)
        )
        return when {
            before.contains('-') || after.trimStart().startsWith("-") -> ExtractedDirection.WITHDRAW
            before.contains('+') || after.trimStart().startsWith("+") -> ExtractedDirection.DEPOSIT
            else -> ExtractedDirection.UNKNOWN
        }
    }

    private fun findAccountId(text: String): String? {
        // شکل ماسک‌شده مثل 6037****1234 یا *1234 یا 1234.56.789 هر جای متن
        maskedIdPattern.findAll(text).firstOrNull { m ->
            // تاریخ کوتاه مثل 03-10-06 نباید شماره حساب فرض شود
            m.value.count(Char::isDigit) >= 8
        }?.let { return it.value.trim() }
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
            var mo = dateMatch.groupValues[2].toInt()
            var d = dateMatch.groupValues[3].toInt()
            // شکل روز/ماه/سال هم روی بعضی پیامک‌ها دیده می‌شود (12/05/1403)
            if (y <= 31 && dateMatch.groupValues[3].length == 4) {
                val realYear = d
                d = y
                y = realYear
            }
            // تاریخ میلادی روی پیامک‌های بعضی درگاه‌ها
            if (y in 1900..2100) {
                val pdg = ir.kharjyar.app.core.date.PersianDate.fromLocalDate(
                    java.time.LocalDate.of(y, mo, d)
                )
                y = pdg.year; mo = pdg.month; d = pdg.day
            }
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
        val text = Digits.normalizeForMatch(body)
        val rules = mutableListOf<FieldRule>()
        for ((role, rawValue) in fieldValues) {
            if (role == FieldRole.DIRECTION) {
                rules.add(FieldRule(role.name, anchor = rawValue.substringBefore('|'), valueType = rawValue.substringAfter('|', "WITHDRAW")))
                continue
            }
            val value = Digits.normalizeForMatch(rawValue.trim())
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
