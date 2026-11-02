package ir.kharjyar.app

import ir.kharjyar.app.core.sms.BankSmsDecisionEngine
import ir.kharjyar.app.core.sms.BankSmsSemanticType
import ir.kharjyar.app.core.sms.ExtractedDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalBankSmsDecisionEngineTest {
    @Test fun `diverse completed transaction formats pass with extracted amount and direction`() {
        val samples = listOf(
            Triple("MELLIBANK", "بانک ملی ایران\nبرداشت:2,000,000-\nحساب:17000\nمانده:385,384\n0127-22:00", ExtractedDirection.WITHDRAW),
            Triple("TTBANK", "2404.306.5918267.1\n+15,000,000\n07/02_17:40\nمانده: 20,326,426", ExtractedDirection.DEPOSIT),
            Triple("BANKREFAH", "حساب213478470\nخرید1,000,000-\nمانده1,845,608\n06/04-14:32", ExtractedDirection.WITHDRAW),
            Triple("BANKSEPAH", "برداشت از: 252080213010571\nمبلغ: 35,000,000 ریال\nموجودی: 4,708,203 ریال", ExtractedDirection.WITHDRAW),
            Triple("MELLAT", "مبلغ 750000 تومان به حساب منتهی به 4321 واریز شد شماره پیگیری 928374", ExtractedDirection.DEPOSIT)
        )
        samples.forEach { (sender, body, direction) ->
            val result = BankSmsDecisionEngine.analyze(sender, body)
            assertEquals(body, BankSmsSemanticType.TRANSACTION, result.type)
            assertTrue(body, result.shouldIngest)
            assertTrue(body, (result.extraction?.amountRial ?: 0) > 0)
            assertEquals(body, direction, result.extraction?.directionEnum())
            assertTrue(body, result.confidence >= 85)
        }
    }

    @Test fun `otp with purchase amount card or bank sender never enters transactions`() {
        val samples = listOf(
            "رمز پویای کارت شما ۱۲۳۴۵۶ برای خرید 3,500,000 ریال است. اعتبار 120 ثانیه",
            "رمز دوم یک بار مصرف: 987654 مبلغ 2,000,000 ریال؛ در اختیار دیگران قرار ندهید",
            "Verification Code 778899 for online payment",
            "کد تایید ورود به همراه بانک 445566"
        )
        samples.forEach {
            val result = BankSmsDecisionEngine.analyze("MELLIBANK", it)
            assertEquals(it, BankSmsSemanticType.OTP, result.type)
            assertFalse(it, result.shouldIngest)
        }
    }

    @Test fun `failed pending security promotional and balance-only messages are excluded`() {
        val samples = listOf(
            BankSmsSemanticType.FAILED_OR_PENDING to "برداشت مبلغ 2,000,000 ریال به علت کسری موجودی انجام نشد",
            BankSmsSemanticType.FAILED_OR_PENDING to "انتقال 5,000,000 ریال در انتظار تایید است",
            BankSmsSemanticType.SECURITY_NOTICE to "ورود به همراه بانک از دستگاه جدید انجام شد",
            BankSmsSemanticType.PROMOTION to "جشنواره خرید بانک با 50,000,000 ریال جایزه؛ همین حالا ثبت نام کنید",
            BankSmsSemanticType.BALANCE_ONLY to "مانده حساب شما: 12,000,000 ریال"
        )
        samples.forEach { (expected, body) ->
            val result = BankSmsDecisionEngine.analyze("MELLIBANK", body)
            assertEquals(body, expected, result.type)
            assertFalse(body, result.shouldIngest)
        }
    }

    @Test fun `unknown sender with signed personal number is rejected`() {
        val result = BankSmsDecisionEngine.analyze("+989121234567", "شماره من +1,234,567 است", false)
        assertFalse(result.shouldIngest)
        assertEquals(BankSmsSemanticType.UNKNOWN, result.type)
    }
}
