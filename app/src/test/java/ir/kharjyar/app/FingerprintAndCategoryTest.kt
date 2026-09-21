package ir.kharjyar.app

import ir.kharjyar.app.core.category.CategorySuggester
import ir.kharjyar.app.core.category.Rule
import ir.kharjyar.app.core.sms.SmsFingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FingerprintAndCategoryTest {

    // ---------- اثر انگشت پیامک ----------

    @Test
    fun `same sms same fingerprint - duplicate broadcast is deduplicated`() {
        val t = 1_700_000_000_000L
        val f1 = SmsFingerprint.of("700701", "برداشت 1000", t)
        val f2 = SmsFingerprint.of("700701", "برداشت 1000", t + 500) // همان دقیقه
        assertEquals(f1, f2)
    }

    @Test
    fun `two real purchases with equal amount different text are distinct`() {
        val t = 1_700_000_000_000L
        val f1 = SmsFingerprint.of("700701", "خرید 300,000 ریال از فروشگاه الف مانده 1,000", t)
        val f2 = SmsFingerprint.of("700701", "خرید 300,000 ریال از فروشگاه ب مانده 700", t)
        assertNotEquals(f1, f2)
    }

    @Test
    fun `identical text different minutes are distinct transactions`() {
        val t = 1_700_000_000_000L
        val f1 = SmsFingerprint.of("700701", "خرید 300,000", t)
        val f2 = SmsFingerprint.of("700701", "خرید 300,000", t + 120_000)
        assertNotEquals(f1, f2)
    }

    @Test
    fun `different senders are distinct`() {
        val t = 1_700_000_000_000L
        assertNotEquals(
            SmsFingerprint.of("700701", "خرید 300,000", t),
            SmsFingerprint.of("700702", "خرید 300,000", t)
        )
    }

    // ---------- پیشنهاد دسته ----------

    @Test
    fun `keyword matches category`() {
        val rules = listOf(Rule(1, "اسنپ", categoryId = 3, priority = 0, createdByUser = false))
        assertEquals(3L, CategorySuggester.suggest("خرید اسنپ فود", "", rules))
    }

    @Test
    fun `user rule wins over generic rule`() {
        val rules = listOf(
            Rule(1, "فروشگاه", categoryId = 1, priority = 0, createdByUser = false),
            Rule(2, "فروشگاه", categoryId = 2, priority = 10, createdByUser = true)
        )
        assertEquals(2L, CategorySuggester.suggest("خرید از فروشگاه", "", rules))
    }

    @Test
    fun `no keyword match returns null - no guessing from amount`() {
        val rules = listOf(Rule(1, "اسنپ", 3, 0, false))
        assertNull(CategorySuggester.suggest("برداشت 500,000 ریال", "", rules))
    }

    @Test
    fun `counterparty is also searched`() {
        val rules = listOf(Rule(1, "داروخانه", 7, 0, true))
        assertEquals(7L, CategorySuggester.suggest("خرید", "داروخانه مرکزی", rules))
    }

    @Test
    fun `longer keyword wins at same priority`() {
        val rules = listOf(
            Rule(1, "فود", 1, 0, false),
            Rule(2, "اسنپ فود", 2, 0, false)
        )
        assertEquals(2L, CategorySuggester.suggest("پرداخت اسنپ فود", "", rules))
    }
}
