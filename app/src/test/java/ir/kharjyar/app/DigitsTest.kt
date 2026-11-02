package ir.kharjyar.app

import ir.kharjyar.app.core.text.Digits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DigitsTest {

    @org.junit.Before
    fun setUp() {
        // این تست‌ها با ارقام فارسی نوشته شده‌اند؛ پرچم سراسری ممکن است از تست دیگری مانده باشد
        Digits.usePersianDigits = true
    }

    @Test
    fun `persian digits normalize`() {
        assertEquals("1234567890", Digits.normalize("۱۲۳۴۵۶۷۸۹۰"))
    }

    @Test
    fun `arabic digits normalize`() {
        assertEquals("1234567890", Digits.normalize("١٢٣٤٥٦٧٨٩٠"))
    }

    @Test
    fun `mixed digits normalize`() {
        assertEquals("مبلغ 1500 ریال", Digits.normalize("مبلغ ۱5٠0 ریال"))
    }

    @Test
    fun `parse amount with latin comma`() {
        assertEquals(1_500_000L, Digits.parseAmount("1,500,000"))
    }

    @Test
    fun `parse amount with persian separator`() {
        assertEquals(2_750_000L, Digits.parseAmount("۲،۷۵۰،۰۰۰"))
    }

    @Test
    fun `parse amount with arabic thousands separator`() {
        assertEquals(345_000L, Digits.parseAmount("٣٤٥٬٠٠٠"))
    }

    @Test
    fun `parse amount with dot as thousands separator`() {
        assertEquals(1_500_000L, Digits.parseAmount("1.500.000"))
    }

    @Test
    fun `parse amount with slash separator`() {
        assertEquals(1_500_000L, Digits.parseAmount("1/500/000"))
    }

    @Test
    fun `dot not thousands separator returns null`() {
        assertNull(Digits.parseAmount("12.34"))
    }

    @Test
    fun `parse amount plain`() {
        assertEquals(50000L, Digits.parseAmount("50000"))
    }

    @Test
    fun `parse amount with zwnj and spaces`() {
        assertEquals(50000L, Digits.parseAmount(" ۵۰\u200c۰۰۰ "))
    }

    @Test
    fun `garbage returns null`() {
        assertNull(Digits.parseAmount("abc"))
        assertNull(Digits.parseAmount(""))
        assertNull(Digits.parseAmount("۱۲a۳"))
    }

    @Test
    fun `group formatting persian`() {
        assertEquals("۱،۲۳۴،۵۶۷", Digits.group(1234567))
    }

    @Test
    fun `ltr isolate wraps text without changing digits`() {
        val wrapped = Digits.ltr("۵۰۲۹  ۰۸۱۰")
        assertEquals('\u2066', wrapped.first())
        assertEquals('\u2069', wrapped.last())
        assertEquals("۵۰۲۹  ۰۸۱۰", Digits.stripBidi(wrapped))
    }

    @Test
    fun `ltr leaves empty text alone`() {
        assertEquals("", Digits.ltr(""))
    }

    @Test
    fun `card groups keep the printed order of the card`() {
        // ترتیب گروه‌ها باید همان ترتیب روی کارت بماند: ۵۰۲۹ اول و ۸۶۰۵ آخر
        val shown = Digits.cardGroups("5029081083248605", separator = " ")
        val plain = Digits.stripBidi(shown)
        assertEquals("۵۰۲۹ ۰۸۱۰ ۸۳۲۴ ۸۶۰۵", plain)
        assertTrue("باید داخل ایزوله چپ‌به‌راست باشد", shown.first() == '\u2066' && shown.last() == '\u2069')
        // رقم‌های خام هم باید دست‌نخورده بمانند
        assertEquals("5029 0810 8324 8605", Digits.normalize(plain))
    }
}
