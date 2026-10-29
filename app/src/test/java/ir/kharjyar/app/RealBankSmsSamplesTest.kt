package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.Extractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** نمونه‌های واقعی و ناشناس‌سازی‌شده‌ای که کاربر از پیامک چند بانک فرستاد. */
class RealBankSmsSamplesTest {

    private fun assertDateTime(millis: Long?, month: Int, day: Int, hour: Int, minute: Int) {
        assertNotNull("تاریخ/ساعت تشخیص داده نشد", millis)
        val shown = PersianDate.formatDateTime(millis!!, persianDigits = false)
        assertTrue("تاریخ اشتباه است: $shown", shown.contains("/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"))
        assertTrue("ساعت اشتباه است: $shown", shown.endsWith("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"))
    }

    @Test
    fun `signed withdrawal without any bank labels is automatic`() {
        val r = Extractor.autoExtract(
            """
            2404.306.5918267.1
            -1,000,000
            07/03_01:53
            مانده: 10,466,426
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(1_000_000L, r.amountRial)
        assertEquals(10_466_426L, r.balanceRial)
        assertEquals("2404.306.5918267.1", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 7, 3, 1, 53)
    }

    @Test
    fun `signed deposit without any bank labels is automatic`() {
        val r = Extractor.autoExtract(
            """
            2404.306.5918267.1
            +15,000,000
            07/02_17:40
            مانده: 20,326,426
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.DEPOSIT, r.directionEnum())
        assertEquals(15_000_000L, r.amountRial)
        assertEquals(20_326_426L, r.balanceRial)
        assertDateTime(r.occurredAtMillis, 7, 2, 17, 40)
    }

    @Test
    fun `melli compact month day means farvardin twenty seventh`() {
        val r = Extractor.autoExtract(
            """
            بانک ملی ایران
            برداشت:2,000,000-
            حساب:17000
            مانده:385,384
            0127-22:00
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(2_000_000L, r.amountRial)
        assertEquals("17000", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 1, 27, 22, 0)
    }

    @Test
    fun `melli enteqali plus is a deposit`() {
        val r = Extractor.autoExtract(
            """
            بانک ملی ایران
            انتقالی:10,260,042+
            حساب:17000
            مانده:11,715,384
            0118-15:10
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.DEPOSIT, r.directionEnum())
        assertEquals(10_260_042L, r.amountRial)
        assertDateTime(r.occurredAtMillis, 1, 18, 15, 10)
    }

    @Test
    fun `fee sms uses its signed amount not the account number`() {
        val r = Extractor.autoExtract(
            """
            10.6056277.1
            -39,000
            07/02_10:20
            مانده: 163,759
            کارمزد پیامک تیر ماه 1405
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(39_000L, r.amountRial)
        assertEquals("10.6056277.1", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 7, 2, 10, 20)
    }

    @Test
    fun `refah compact date and stuck labels are automatic`() {
        val r = Extractor.autoExtract(
            """
            بانک رفاه
            حساب213478470
            خرید1,000,000-
            مانده1,845,608
            06/04-14:32
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(1_000_000L, r.amountRial)
        assertEquals("213478470", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 6, 4, 14, 32)
    }

    @Test
    fun `sepah full short year date is automatic`() {
        val r = Extractor.autoExtract(
            """
            بانک سپه
            بانکداری مدرن
            برداشت از: 252080213010571
            مبلغ: 35,000,000 ریال
            03/10/06_12:12
            موجودی: 4,708,203 ریال
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
        assertEquals(35_000_000L, r.amountRial)
        assertEquals("252080213010571", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 10, 6, 12, 12)
    }

    @Test
    fun `hyphenated account and full jalali date are automatic`() {
        val r = Extractor.autoExtract(
            """
            639-824-882613-1
            +16,240,000
            مانده 16,425,824
            1405/2/9-9:53
            """.trimIndent()
        )
        assertEquals(ExtractedDirection.DEPOSIT, r.directionEnum())
        assertEquals(16_240_000L, r.amountRial)
        assertEquals("639-824-882613-1", r.accountIdHint)
        assertDateTime(r.occurredAtMillis, 2, 9, 9, 53)
    }
}
