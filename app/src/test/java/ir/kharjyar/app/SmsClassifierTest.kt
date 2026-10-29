package ir.kharjyar.app

import ir.kharjyar.app.core.sms.SmsClassifier
import ir.kharjyar.app.core.sms.SmsKind
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * پیامک‌های تست مصنوعی و فاقد اطلاعات واقعی هستند.
 */
class SmsClassifierTest {

    @Test
    fun `withdrawal sms is financial`() {
        val sms = "بانک نمونه\nبرداشت: 1,500,000 ریال\nحساب: *1234\nمانده: 12,345,678\n1403/05/01 - 12:30"
        assertEquals(SmsKind.FINANCIAL_LIKELY, SmsClassifier.classify(sms))
    }

    @Test
    fun `deposit sms with persian digits is financial`() {
        val sms = "واریز به حساب ۵۶۷۸: مبلغ ۲،۰۰۰،۰۰۰ ریال. مانده: ۸،۵۰۰،۰۰۰"
        assertEquals(SmsKind.FINANCIAL_LIKELY, SmsClassifier.classify(sms))
    }

    @Test
    fun `otp sms is non financial`() {
        val sms = "رمز یکبار مصرف شما: 123456\nاین کد را در اختیار دیگران قرار ندهید"
        assertEquals(SmsKind.NON_FINANCIAL, SmsClassifier.classify(sms))
    }

    @Test
    fun `dynamic password mentioning purchase and amount is still non financial`() {
        val sms = "رمز پویا خرید شما 123456 مبلغ 2,500,000 ریال است"
        assertEquals(SmsKind.NON_FINANCIAL, SmsClassifier.classify(sms))
    }

    @Test
    fun `verification code is non financial`() {
        val sms = "کد تایید شما 98765 می‌باشد"
        assertEquals(SmsKind.NON_FINANCIAL, SmsClassifier.classify(sms))
    }

    @Test
    fun `common Iranian dynamic password variants are silently ignored`() {
        val samples = listOf(
            "رمز پویای کارت شما ۱۲۳۴۵۶ است. اعتبار رمز ۱۲۰ ثانیه",
            "رمز دوم یک‌بار مصرف: 987654 مبلغ خرید 3,500,000 ریال",
            "رمز خرید اینترنتی شما 445566 است",
            "رمز موقت کارت 112233 زمان اعتبار رمز دو دقیقه",
            "One Time Password: 778899 for online payment"
        )
        samples.forEach { assertEquals(it, SmsKind.NON_FINANCIAL, SmsClassifier.classify(it)) }
    }

    @Test
    fun `promo sms is non financial`() {
        val sms = "جشنواره فروش ویژه! تخفیف ۵۰٪ همین حالا کلیک کنید"
        assertEquals(SmsKind.NON_FINANCIAL, SmsClassifier.classify(sms))
    }

    @Test
    fun `real transaction containing word ramz stays financial`() {
        // تراکنش واقعی با هشدار امنیتی و واژه «رمز» نباید حذف شود
        val sms = "خرید از فروشگاه نمونه\nمبلغ: 350,000 ریال\nمانده: 5,000,000\nرمز خود را به کسی ندهید"
        assertEquals(SmsKind.FINANCIAL_LIKELY, SmsClassifier.classify(sms))
    }

    @Test
    fun `balance only sms is suspicious not financial`() {
        // فقط مانده حساب => تراکنش نیست ولی برای بررسی می‌ماند
        val sms = "مانده حساب شما: 12,000,000 ریال"
        assertEquals(SmsKind.SUSPICIOUS, SmsClassifier.classify(sms))
    }

    @Test
    fun `personal message is non financial`() {
        val sms = "سلام، فردا ساعت ۵ می‌بینمت"
        assertEquals(SmsKind.NON_FINANCIAL, SmsClassifier.classify(sms))
    }

    @Test
    fun `card to card is financial`() {
        val sms = "انتقال کارت به کارت\nمبلغ 5,000,000 ریال\nاز کارت *1111 به *2222"
        assertEquals(SmsKind.FINANCIAL_LIKELY, SmsClassifier.classify(sms))
    }
}
