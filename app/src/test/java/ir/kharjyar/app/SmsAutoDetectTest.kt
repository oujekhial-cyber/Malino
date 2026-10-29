package ir.kharjyar.app

import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.Extractor
import ir.kharjyar.app.core.sms.SmsClassifier
import ir.kharjyar.app.core.sms.SmsKind
import ir.kharjyar.app.core.text.Digits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تشخیص خودکار پیامک بانکی: جهت، مبلغ، شناسه حساب و تاریخ باید بدون آموزش قالب
 * پیدا شوند تا از کاربر سؤال تکراری پرسیده نشود.
 */
class SmsAutoDetectTest {

    @Test
    fun `arabic letters do not break detection`() {
        // «خريد» و «كسر» با حروف عربی؛ پیش‌تر هیچ کلیدواژه‌ای پیدا نمی‌شد
        val body = """
            6037****1234
            خريد: 1,250,000 ريال
            مانده: 9,870,000
            1403/05/12 14:22
        """.trimIndent()

        assertEquals(SmsKind.FINANCIAL_LIKELY, SmsClassifier.classify(body))
        val r = Extractor.autoExtract(body)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(1_250_000L, r.amountRial)
        assertEquals(9_870_000L, r.balanceRial)
        assertNotNull("شناسه حساب پیدا نشد", r.accountIdHint)
        assertTrue(r.accountIdHint!!.contains("1234"))
        assertNotNull("تاریخ پیدا نشد", r.occurredAtMillis)
    }

    @Test
    fun `deposit is detected from the first keyword`() {
        val body = "واريز 5,000,000 ريال\nمانده 12,000,000\nبانك ملت"
        val r = Extractor.autoExtract(body)
        assertEquals(ExtractedDirection.DEPOSIT, r.directionEnum())
        assertEquals(5_000_000L, r.amountRial)
    }

    @Test
    fun `toman amounts are stored in rial`() {
        val body = "برداشت 250,000 تومان از حساب\nمانده 1,000,000 تومان"
        val r = Extractor.autoExtract(body)
        assertEquals(2_500_000L, r.amountRial)
        assertEquals(10_000_000L, r.balanceRial)
    }

    @Test
    fun `sign decides the direction when no keyword exists`() {
        val body = "مبلغ -3,000,000\nمانده 7,000,000"
        val r = Extractor.autoExtract(body)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
    }

    @Test
    fun `direction word is offered for template training`() {
        assertEquals("خرید", Extractor.directionWordIn("خريد 100,000 ريال"))
        assertEquals("واریز", Extractor.directionWordIn("واريز 100,000 ريال"))
    }

    @Test
    fun `text normalization is stable`() {
        assertEquals("خرید کالا", Digits.normalizeForMatch("خريد كالا"))
        assertEquals("1234", Digits.normalizeForMatch("۱۲۳۴"))
    }
}
