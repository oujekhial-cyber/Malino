package ir.kharjyar.app

import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.DigitStyle
import ir.kharjyar.app.data.prefs.Palette
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** پیش‌فرض‌های نمایش و سوییچ سراسری ارقام. */
class DisplaySettingsTest {

    @After
    fun restoreDefault() {
        Digits.usePersianDigits = true
    }

    @Test
    fun `defaults are rial persian sakura with shine`() {
        val s = AppSettings()
        assertEquals(MoneyUnit.RIAL, s.moneyUnit)
        assertEquals(DigitStyle.PERSIAN, s.digitStyle)
        assertEquals(Palette.SAKURA, s.palette)
        assertTrue(s.cardShine)
    }

    @Test
    fun `digit switch changes toPersian output`() {
        Digits.usePersianDigits = true
        assertEquals("۱۴۰۵", Digits.toPersian("1405"))

        Digits.usePersianDigits = false
        assertEquals("1405", Digits.toPersian("1405"))
        // حالت اجباری باید مستقل از تنظیم کار کند
        assertEquals("۱۴۰۵", Digits.toPersianAlways("1405"))
    }

    @Test
    fun `normalize is unaffected by digit style`() {
        Digits.usePersianDigits = false
        assertEquals("1405", Digits.normalize("۱۴۰۵"))
        Digits.usePersianDigits = true
        assertEquals("1405", Digits.normalize("۱۴۰۵"))
    }

    @Test
    fun `grouping follows digit style`() {
        Digits.usePersianDigits = false
        assertEquals("1،200،000", Digits.group(1_200_000))
        Digits.usePersianDigits = true
        assertEquals("۱،۲۰۰،۰۰۰", Digits.group(1_200_000))
    }
}
