package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** نتیجه معنایی موتور پیامک؛ تصمیم و دلیل آن برای عیب‌یابی از هم جدا نگه داشته می‌شوند. */
enum class BankSmsSemanticType {
    TRANSACTION, OTP, SECURITY_NOTICE, PROMOTION, FAILED_OR_PENDING,
    BALANCE_ONLY, SERVICE_NOTICE, UNKNOWN
}

data class BankSmsDecision(
    val type: BankSmsSemanticType,
    val verdict: BankSmsVerdict,
    val confidence: Int,
    val bankName: String?,
    val extraction: ExtractionResult?,
    val reasons: List<String>
) {
    val shouldIngest: Boolean get() = verdict == BankSmsVerdict.TRANSACTION
}

/**
 * دروازه واحد و چندلایه پیامک بانکی.
 *
 * ترتیب عمداً ثابت است: رمز/امنیت و عملیات ناموفق پیش از واژه‌های مالی بررسی
 * می‌شوند؛ سپس تبلیغات، ساختار تراکنش و در آخر استخراج مبلغ/جهت اعتبارسنجی می‌شود.
 * متن یا اطلاعات حساس هیچ‌گاه لاگ یا به شبکه ارسال نمی‌شود.
 */
object BankSmsDecisionEngine {
    private val failedOrPending = listOf(
        "ناموفق", "انجام نشد", "لغو شد", "رد شد", "عدم موفقیت", "خطا در تراکنش",
        "موجودی کافی نیست", "کسری موجودی", "در انتظار تایید", "در انتظار تأیید",
        "در حال بررسی", "در دست اقدام"
    )
    private val security = listOf(
        "ورود به همراه بانک", "ورود به اینترنت بانک", "ورود از دستگاه", "دستگاه جدید",
        "تلاش ناموفق ورود", "رمز ورود", "نام کاربری", "تغییر رمز", "مسدود شدن کارت",
        "کارت شما مسدود", "فعال سازی", "فعال‌سازی", "ثبت دستگاه"
    )
    private val promotional = listOf(
        "جشنواره", "قرعه کشی", "قرعه‌کشی", "تخفیف", "پیشنهاد ویژه", "فروش ویژه",
        "باشگاه مشتریان", "امتیاز", "هدیه", "جایزه", "ثبت نام کنید", "ثبت‌نام کنید",
        "کلیک کنید", "مراجعه کنید", "همین حالا", "لغو 11", "لغو11"
    )
    private val transactionTerms = listOf(
        "برداشت", "واریز", "واریزی", "خرید", "پرداخت", "انتقال", "حواله", "کسر",
        "بدهکار", "بستانکار", "کارمزد", "سود", "وصول", "عودت", "برگشت وجه"
    )
    private val balanceTerms = listOf("مانده", "موجودی", "باقیمانده")
    private val url = Regex("https?://|www\\.|\\.(?:ir|com)(?:/|\\s|$)")
    private val moneyLike = Regex("(?:[+-]\\s*)?\\d{1,3}(?:[,،٬.]\\d{3})+(?:\\s*[+-])?|(?:مبلغ\\s*[:：]?\\s*)\\d{4,}")

    fun analyze(sender: String, body: String, senderKnownByAccount: Boolean = false): BankSmsDecision {
        val text = Digits.normalizeForMatch(body).lowercase()
        val bank = BankSenderResolver.bankName(sender)
        val knownSender = senderKnownByAccount || bank != null
        val reasons = mutableListOf<String>()

        // OTP حتی اگر مبلغ، کارت و واژه «خرید» داشته باشد تراکنش انجام‌شده نیست.
        if (SmsClassifier.isOtp(body)) return decision(
            BankSmsSemanticType.OTP, BankSmsVerdict.OTP_OR_SECURITY, 100, bank,
            null, listOf("رمز یک‌بارمصرف یا کد تأیید")
        )
        if (security.any(text::contains)) return decision(
            BankSmsSemanticType.SECURITY_NOTICE, BankSmsVerdict.OTP_OR_SECURITY, 98, bank,
            null, listOf("اعلان ورود، کارت یا تنظیم امنیتی")
        )
        if (failedOrPending.any(text::contains)) return decision(
            BankSmsSemanticType.FAILED_OR_PENDING, BankSmsVerdict.NON_FINANCIAL, 98, bank,
            null, listOf("عملیات ناموفق، لغوشده یا هنوز نهایی‌نشده")
        )

        val hasTransactionTerm = transactionTerms.any(text::contains)
        val hasBalance = balanceTerms.any(text::contains)
        val hasMoney = moneyLike.containsMatchIn(text)
        val hasPromotion = promotional.any(text::contains)
        val hasUrl = url.containsMatchIn(text)
        if ((hasPromotion || hasUrl) && !(hasTransactionTerm && hasBalance && hasMoney)) {
            return decision(
                BankSmsSemanticType.PROMOTION, BankSmsVerdict.PROMOTION,
                if (hasPromotion && hasUrl) 99 else 95, bank, null,
                listOfNotNull(if (hasPromotion) "واژگان تبلیغاتی" else null, if (hasUrl) "پیوند یا دعوت بیرونی" else null)
            )
        }

        val evidence = BankSmsEvidenceClassifier.classify(sender, body, knownSender)
        reasons += evidence.reasons
        if (evidence.verdict == BankSmsVerdict.TRANSACTION) {
            val extraction = Extractor.autoExtract(body)
            val validAmount = extraction.amountRial != null && extraction.amountRial > 0
            val validDirection = extraction.directionEnum() != ExtractedDirection.UNKNOWN
            if (validAmount && validDirection) {
                reasons += "مبلغ و جهت قابل استخراج"
                if (extraction.balanceRial != null) reasons += "مانده قابل استخراج"
                if (extraction.accountIdHint != null) reasons += "شناسه حساب قابل استخراج"
                val confidence = (78 + evidence.score.coerceIn(0, 14) +
                    (if (extraction.confidenceEnum() == Confidence.HIGH) 7 else 2)).coerceAtMost(99)
                return decision(BankSmsSemanticType.TRANSACTION, evidence.verdict, confidence, bank, extraction, reasons)
            }
            // پیام مشکوک حذف نمی‌شود؛ برای آموزش قالب باقی می‌ماند ولی ثبت خودکار ندارد.
            reasons += if (!validAmount) "مبلغ قطعی استخراج نشد" else "جهت قطعی استخراج نشد"
            return decision(BankSmsSemanticType.UNKNOWN, BankSmsVerdict.REVIEW, 55, bank, extraction, reasons)
        }

        if (hasBalance && !hasTransactionTerm) return decision(
            BankSmsSemanticType.BALANCE_ONLY, BankSmsVerdict.REVIEW, 85, bank,
            Extractor.autoExtract(body), listOf("اعلام مانده بدون وقوع تراکنش")
        )
        return decision(
            if (knownSender) BankSmsSemanticType.SERVICE_NOTICE else BankSmsSemanticType.UNKNOWN,
            evidence.verdict, evidence.score.coerceIn(0, 70), bank, null,
            reasons.ifEmpty { listOf("شواهد کافی برای تراکنش وجود ندارد") }
        )
    }

    private fun decision(
        type: BankSmsSemanticType,
        verdict: BankSmsVerdict,
        confidence: Int,
        bank: String?,
        extraction: ExtractionResult?,
        reasons: List<String>
    ) = BankSmsDecision(type, verdict, confidence.coerceIn(0, 100), bank, extraction, reasons.distinct())
}
