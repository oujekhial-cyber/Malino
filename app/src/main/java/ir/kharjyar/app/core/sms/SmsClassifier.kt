package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/**
 * تفکیک پیامک غیرمالی از مالی/مشکوک به مالی. کاملاً محلی و قانون‌محور.
 *
 * اصل طراحی: false negative (ازدست‌دادن تراکنش واقعی) بدتر از false positive است؛
 * موارد مشکوک به صف بررسی می‌روند و چیزی بی‌صدا ثبت نمی‌شود.
 */
enum class SmsKind { NON_FINANCIAL, FINANCIAL_LIKELY, SUSPICIOUS }

object SmsClassifier {

    private val transactionKeywords = listOf(
        "برداشت", "واریز", "خرید", "انتقال", "کسر", "کارت به کارت", "کارت‌به‌کارت",
        "پرداخت", "حواله", "تراکنش", "کسر شد", "واریز شد", "برداشت شد", "خرید از",
        "paya", "satna", "پایا", "ساتنا", "قبض", "بدهکار", "بستانکار", "وصول", "شارژ"
    )

    private val balanceKeywords = listOf("مانده", "موجودی", "مانده:", "موجودی:")

    private val otpKeywords = listOf(
        "رمز یکبار مصرف", "رمز یک‌بار مصرف", "رمز یکبارمصرف", "کد تایید", "کد تأیید",
        "کد فعالسازی", "کد فعال‌سازی", "رمز پویا", "otp", "verification code",
        "کد ورود", "کد اعتبارسنجی", "این کد را در اختیار", "رمز دوم پویا"
    )

    private val promoKeywords = listOf(
        "تخفیف", "جشنواره", "قرعه کشی", "قرعه‌کشی", "اقساط ویژه", "کلیک کنید",
        "لغو11", "لغو 11", "پیشنهاد ویژه", "تبلیغ"
    )

    /**
     * طبقه‌بندی پیامک.
     * وجود واژه «رمز» یا «مسدود» به‌تنهایی دلیل حذف نیست؛ اگر نشانه تراکنش + مبلغ باشد مالی است.
     */
    fun classify(body: String): SmsKind {
        // حروف عربی هم یکدست می‌شوند تا «خريد/كسر» عربی هم شناخته شود
        val text = Digits.normalizeForMatch(body).lowercase()
        val hasTxKeyword = transactionKeywords.any { text.contains(it) }
        val hasBalance = balanceKeywords.any { text.contains(it) }
        val hasAmountLike = Regex("\\d{1,3}([,،٬./]\\d{3})+|\\d{4,}").containsMatchIn(text)
        val isOtp = otpKeywords.any { text.contains(it) }
        val isPromo = promoKeywords.any { text.contains(it) }

        // پیامک تراکنش واقعی حتی اگر هشدار امنیتی/واژه «رمز» داشته باشد
        if (hasTxKeyword && hasAmountLike) return SmsKind.FINANCIAL_LIKELY

        // OTP خالص بدون نشانه تراکنش
        if (isOtp) return SmsKind.NON_FINANCIAL

        // تبلیغ بدون نشانه تراکنش
        if (isPromo && !hasTxKeyword) return SmsKind.NON_FINANCIAL

        // فقط مانده حساب: تراکنش نیست ولی مشکوک نگه می‌داریم؟ خیر —
        // «صرف وجود عدد یا عبارت مانده به معنی وقوع تراکنش نیست».
        // مانده بدون کلیدواژه تراکنش => مشکوک (برای بررسی، نه ثبت خودکار)
        if (hasBalance && hasAmountLike) return SmsKind.SUSPICIOUS

        if (hasTxKeyword) return SmsKind.SUSPICIOUS

        return SmsKind.NON_FINANCIAL
    }
}
