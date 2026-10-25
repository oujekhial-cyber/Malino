package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

enum class BankSmsVerdict { TRANSACTION, REVIEW, PROMOTION, OTP_OR_SECURITY, NON_FINANCIAL }

data class BankSmsEvidence(
    val verdict: BankSmsVerdict,
    val score: Int,
    val reasons: List<String>
) {
    val isHighConfidenceTransaction: Boolean get() = verdict == BankSmsVerdict.TRANSACTION
}

/**
 * تشخیص چندنشانه‌ای پیامک بانکی. برخلاف تطبیق یک واژه، نتیجه فقط زمانی مالی است
 * که «رویداد انجام‌شده + مبلغ پولی + نشانه حساب/بانک» با هم دیده شوند.
 */
object BankSmsEvidenceClassifier {
    private val completedEvent = Regex(
        "(?:برداشت|واریز|خرید|پرداخت|انتقال|حواله|کسر|بدهکار|بستانکار|وصول)(?:\\s+(?:شد|گردید|انجام شد|موفق|ثبت شد))?|(?:از حساب|به حساب|از کارت|به کارت).{0,35}(?:کسر|واریز|برداشت)"
    )
    private val currencyAmount = Regex(
        "(?:مبلغ\\s*[:：]?\\s*)?\\d{1,3}(?:[,،٬.]\\d{3})+\\s*(?:ریال|تومان)|(?:مبلغ\\s*[:：]?\\s*)\\d{4,}\\s*(?:ریال|تومان)?|\\d{4,}\\s*(?:ریال|تومان)"
    )
    private val accountEvidence = Regex(
        "(?:حساب|کارت|سپرده|شبا)\\s*(?:شماره|به شماره|منتهی به|[xX*•-])?\\s*(?:[xX*•-]*\\d){3,}"
    )
    private val balanceEvidence = Regex("(?:مانده|موجودی)\\s*[:：]?\\s*\\d")
    private val referenceEvidence = Regex("(?:پیگیری|مرجع|تراکنش)\\s*[:：]?\\s*\\d{3,}")
    private val promo = listOf("تخفیف","جشنواره","قرعه کشی","قرعه کشی","فروش ویژه","پیشنهاد ویژه","حراج","کد تخفیف","هدیه","امتیاز","باشگاه مشتریان")
    private val callToAction = listOf("خرید کنید","ثبت نام کنید","ثبت نام","کلیک کنید","مراجعه کنید","همین حالا","فرصت","اطلاعات بیشتر","لغو 11","لغو11")
    private val futureOrOffer = listOf("می توانید","میتوانید","بهره مند شوید","خواهد شد","تا پایان","ویژه شما","برای شما")

    fun classify(sender: String, body: String, knownSender: Boolean = false): BankSmsEvidence {
        val text = Digits.normalizeForMatch(body).lowercase()
        if (SmsClassifier.isOtp(body) || listOf("ورود به همراه بانک","ورود به اینترنت بانک","رمز ورود","دستگاه جدید").any(text::contains)) {
            return BankSmsEvidence(BankSmsVerdict.OTP_OR_SECURITY, -20, listOf("رمز یا اعلان امنیتی"))
        }
        var score = 0
        val reasons = mutableListOf<String>()
        val event = completedEvent.containsMatchIn(text)
        val amount = currencyAmount.containsMatchIn(text)
        val account = accountEvidence.containsMatchIn(text)
        val balance = balanceEvidence.containsMatchIn(text)
        val reference = referenceEvidence.containsMatchIn(text)
        val promoCount = promo.count(text::contains)
        val action = callToAction.any(text::contains)
        val offer = futureOrOffer.any(text::contains)
        val url = Regex("https?://|www\\.|\\.ir(?:/|\\s|$)|\\.com(?:/|\\s|$)").containsMatchIn(text)

        if (knownSender) { score += 3; reasons += "فرستنده بانکی شناخته‌شده" }
        if (event) { score += 4; reasons += "رویداد مالی" }
        if (amount) { score += 3; reasons += "مبلغ با ساختار پولی" }
        if (account) { score += 2; reasons += "شناسه حساب یا کارت" }
        if (balance) { score += 2; reasons += "مانده حساب" }
        if (reference) { score += 1; reasons += "شماره پیگیری" }
        if (promoCount > 0) { score -= minOf(6, promoCount * 3); reasons += "نشانه تبلیغاتی" }
        if (action) { score -= 4; reasons += "دعوت به اقدام" }
        if (offer) { score -= 2; reasons += "پیشنهاد یا زمان آینده" }
        if (url) { score -= 2; reasons += "پیوند وب" }

        // هیچ عدد بلند یا نام بانک به‌تنهایی تراکنش محسوب نمی‌شود.
        val strongStructure = event && amount && (knownSender || account || balance || reference)
        val promotional = (promoCount > 0 || action || offer || url) && (!strongStructure || score < 8)
        val verdict = when {
            strongStructure && score >= 8 -> BankSmsVerdict.TRANSACTION
            promotional -> BankSmsVerdict.PROMOTION
            knownSender && event && (amount || account) && score >= 5 -> BankSmsVerdict.REVIEW
            else -> BankSmsVerdict.NON_FINANCIAL
        }
        return BankSmsEvidence(verdict, score, reasons)
    }
}
