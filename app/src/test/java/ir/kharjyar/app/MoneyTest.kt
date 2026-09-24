package ir.kharjyar.app

import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun `toman to rial exact`() {
        assertEquals(150_000L, Money.tomanToRial(15_000L))
    }

    @Test
    fun `rial to toman whole and remainder`() {
        assertEquals(15_000L, Money.rialToTomanWhole(150_005L))
        assertEquals(5L, Money.rialToTomanRemainder(150_005L))
    }

    @Test
    fun `toman display hides the trailing rial digit`() {
        // نمایش تومان ساده می‌ماند: «۱۵،۰۰۰ تومان» بدون «و ۵ ریال»
        val formatted = Money.format(150_005L, MoneyUnit.TOMAN)
        assertTrue(formatted.contains("تومان"))
        assertTrue("رقم ریالی نباید نمایش داده شود: $formatted", !formatted.contains("ریال"))
        assertTrue(formatted.contains("۱۵،۰۰۰"))
    }

    @Test
    fun `hidden rial digit is only hidden, not lost`() {
        // مقدار ذخیره‌شده دست‌نخورده است و محاسبه‌ها روی ریال کامل انجام می‌شوند
        val stored = 150_005L
        assertEquals(15_000L, Money.rialToTomanWhole(stored))
        assertEquals(5L, Money.rialToTomanRemainder(stored))
        // جمع دو مبلغ با رقم ریالی، ریال‌ها را نگه می‌دارد
        assertEquals(300_010L, stored + stored)
        assertEquals(30_001L, Money.rialToTomanWhole(stored + stored))
        // نمایش ریالی همچنان عدد کامل را نشان می‌دهد
        assertTrue(Money.format(stored, MoneyUnit.RIAL).contains("۱۵۰،۰۰۵"))
    }

    @Test
    fun `round toman displays without remainder`() {
        val formatted = Money.format(150_000L, MoneyUnit.TOMAN)
        assertTrue(formatted.contains("تومان"))
        assertTrue(!formatted.contains("ریال"))
    }

    @Test
    fun `input in toman stored as rial`() {
        assertEquals(150_000L, Money.inputToRial("15000", MoneyUnit.TOMAN))
        assertEquals(15_000L, Money.inputToRial("15000", MoneyUnit.RIAL))
    }

    @Test
    fun `changing display unit does not change stored value`() {
        val storedRial = 2_500_000L
        // نمایش با هر دو واحد، عدد ذخیره‌شده ثابت است
        val rialView = Money.format(storedRial, MoneyUnit.RIAL)
        val tomanView = Money.format(storedRial, MoneyUnit.TOMAN)
        assertTrue(rialView.contains("۲،۵۰۰،۰۰۰"))
        assertTrue(tomanView.contains("۲۵۰،۰۰۰"))
    }

    @Test
    fun `persian digit input parses`() {
        assertEquals(1_000_000L, Money.inputToRial("۱۰۰،۰۰۰", MoneyUnit.TOMAN))
    }
}
