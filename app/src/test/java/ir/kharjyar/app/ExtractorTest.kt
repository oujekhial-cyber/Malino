package ir.kharjyar.app

import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.Extractor
import ir.kharjyar.app.core.sms.FieldRole
import ir.kharjyar.app.core.sms.FieldRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** پیامک‌های تست مصنوعی هستند. */
class ExtractorTest {

    @Test
    fun `auto extract amount and balance separately`() {
        val sms = "بانک نمونه\nبرداشت: 1,500,000 ریال\nحساب: *1234\nمانده: 12,345,678"
        val r = Extractor.autoExtract(sms)
        assertEquals(1_500_000L, r.amountRial)
        assertEquals(12_345_678L, r.balanceRial)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
    }

    @Test
    fun `amount not confused with balance when balance comes first`() {
        val sms = "مانده: 9,000,000\nواریز مبلغ: 2,000,000 ریال"
        val r = Extractor.autoExtract(sms)
        assertEquals(2_000_000L, r.amountRial)
        assertEquals(9_000_000L, r.balanceRial)
        assertEquals(ExtractedDirection.DEPOSIT, r.directionEnum())
    }

    @Test
    fun `persian digits in sms`() {
        val sms = "برداشت: ۳۵۰،۰۰۰ ریال\nمانده: ۵،۰۰۰،۰۰۰"
        val r = Extractor.autoExtract(sms)
        assertEquals(350_000L, r.amountRial)
        assertEquals(5_000_000L, r.balanceRial)
    }

    @Test
    fun `date and time extraction`() {
        val sms = "برداشت مبلغ: 100,000\n1403/05/12 - 14:35\nمانده: 1,000,000"
        val r = Extractor.autoExtract(sms)
        assertEquals("1403/05/12", r.dateText)
        assertEquals("14:35", r.timeText)
        assertNotNull(r.occurredAtMillis)
        val pd = ir.kharjyar.app.core.date.PersianDate.fromMillis(r.occurredAtMillis!!)
        assertEquals(ir.kharjyar.app.core.date.PersianDate(1403, 5, 12), pd)
    }

    @Test
    fun `two digit year is expanded`() {
        val sms = "خرید مبلغ: 200,000\n03/05/12_14:35"
        val r = Extractor.autoExtract(sms)
        assertNotNull(r.occurredAtMillis)
    }

    @Test
    fun `account id extraction`() {
        val sms = "برداشت از حساب *5678 مبلغ: 100,000 مانده: 500,000"
        val r = Extractor.autoExtract(sms)
        assertEquals("*5678", r.accountIdHint)
    }

    @Test
    fun `template rules extraction`() {
        val sms = "بانک تست\nکسر: 750,000\nکارت: 1111\nمانده: 3,000,000"
        val rules = listOf(
            FieldRule(FieldRole.AMOUNT.name, anchor = "کسر:"),
            FieldRule(FieldRole.BALANCE.name, anchor = "مانده:"),
            FieldRule(FieldRole.ACCOUNT_ID.name, anchor = "کارت:"),
            FieldRule(FieldRole.DIRECTION.name, anchor = "کسر", valueType = "WITHDRAW")
        )
        val r = Extractor.applyRules(sms, rules)
        assertEquals(750_000L, r.amountRial)
        assertEquals(3_000_000L, r.balanceRial)
        assertEquals("1111", r.accountIdHint)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
    }

    @Test
    fun `template with toman unit converts to rial`() {
        val sms = "پرداخت: 50,000 تومان مانده: 1,000,000"
        val rules = listOf(
            FieldRule(FieldRole.AMOUNT.name, anchor = "پرداخت:"),
            FieldRule(FieldRole.DIRECTION.name, anchor = "پرداخت", valueType = "WITHDRAW")
        )
        val r = Extractor.applyRules(sms, rules, amountUnit = "TOMAN")
        assertEquals(500_000L, r.amountRial)
    }

    @Test
    fun `template fails gracefully when anchor missing`() {
        val sms = "متن کاملاً متفاوت بدون لنگرها 12345"
        val rules = listOf(FieldRule(FieldRole.AMOUNT.name, anchor = "کسر:"))
        val r = Extractor.applyRules(sms, rules)
        assertNull(r.amountRial)
    }

    @Test
    fun `validate rules rejects empty anchor and missing amount`() {
        val bad = listOf(FieldRule(FieldRole.BALANCE.name, anchor = ""))
        val errors = Extractor.validateRules(bad)
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun `validate rules accepts good rules`() {
        val good = listOf(
            FieldRule(FieldRole.AMOUNT.name, anchor = "مبلغ:"),
            FieldRule(FieldRole.BALANCE.name, anchor = "مانده:")
        )
        assertTrue(Extractor.validateRules(good).isEmpty())
    }

    @Test
    fun `suggest rules from sample builds working template`() {
        val sms = "بانک نمونه\nبرداشت: 1,500,000\nمانده: 12,000,000"
        val rules = Extractor.suggestRules(
            sms,
            mapOf(
                FieldRole.AMOUNT to "1,500,000",
                FieldRole.BALANCE to "12,000,000",
                FieldRole.DIRECTION to "برداشت|WITHDRAW"
            )
        )
        assertTrue(Extractor.validateRules(rules).isEmpty())
        val r = Extractor.applyRules(sms, rules)
        assertEquals(1_500_000L, r.amountRial)
        assertEquals(12_000_000L, r.balanceRial)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
    }

    @Test
    fun `suggested template works on second sms with same format different values`() {
        val sample = "بانک نمونه\nبرداشت: 1,500,000\nمانده: 12,000,000"
        val rules = Extractor.suggestRules(
            sample,
            mapOf(FieldRole.AMOUNT to "1,500,000", FieldRole.BALANCE to "12,000,000", FieldRole.DIRECTION to "برداشت|WITHDRAW")
        )
        val second = "بانک نمونه\nبرداشت: 240,000\nمانده: 11,760,000"
        val r = Extractor.applyRules(second, rules)
        assertEquals(240_000L, r.amountRial)
        assertEquals(11_760_000L, r.balanceRial)
    }

    @Test
    fun `ambiguous sms yields low confidence`() {
        val sms = "تراکنش 500000 200000 300000"
        val r = Extractor.autoExtract(sms)
        // چند عدد بدون لنگر => نباید مبلغ قطعی برگردد یا اطمینان بالا باشد
        assertTrue(r.amountRial == null || r.confidenceEnum() != ir.kharjyar.app.core.sms.Confidence.HIGH)
    }

    @Test
    fun `multiline multipart-like sms parses`() {
        // شبیه پیامک چندبخشی به‌هم‌چسبیده
        val sms = "بانک نمونه عزیز، خرید شما\nمبلغ: 4,750,000 ریال از فروشگاه بزرگ انجام شد. مانده: 22,120,500 ریال. 1403/07/01 09:15 رمز خود را محفوظ نگه دارید"
        val r = Extractor.autoExtract(sms)
        assertEquals(4_750_000L, r.amountRial)
        assertEquals(22_120_500L, r.balanceRial)
        assertEquals(ExtractedDirection.WITHDRAW, r.directionEnum())
    }
}
