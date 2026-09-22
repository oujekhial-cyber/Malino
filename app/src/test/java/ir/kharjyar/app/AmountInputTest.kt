package ir.kharjyar.app

import androidx.compose.ui.text.AnnotatedString
import ir.kharjyar.app.ui.components.AmountInput
import ir.kharjyar.app.ui.components.ThousandsSeparatorTransformation
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    @Test
    fun sanitize_keepsOnlyDigits_andNormalizesPersian() {
        assertEquals("123456", AmountInput.sanitize("۱۲۳,۴۵۶"))
        assertEquals("123456", AmountInput.sanitize("123،456"))
        assertEquals("98700", AmountInput.sanitize("۹۸٬۷۰۰ تومان"))
        assertEquals("", AmountInput.sanitize("abc"))
    }

    @Test
    fun sanitize_stripsLeadingZeros_butKeepsSingleZero() {
        assertEquals("5", AmountInput.sanitize("0005"))
        assertEquals("0", AmountInput.sanitize("000"))
        assertEquals("", AmountInput.sanitize(""))
    }

    @Test
    fun sanitize_respectsMaxDigits() {
        assertEquals(15, AmountInput.sanitize("1".repeat(40)).length)
    }

    @Test
    fun grouped_addsPersianThousandSeparators() {
        assertEquals("۱۲۳", AmountInput.grouped("123"))
        assertEquals("۱،۲۳۴", AmountInput.grouped("1234"))
        assertEquals("۱۲۳،۴۵۶،۷۸۹", AmountInput.grouped("123456789"))
        assertEquals("", AmountInput.grouped(""))
    }

    @Test
    fun transformation_rendersGroupedPersianDigits() {
        val t = ThousandsSeparatorTransformation()
        assertEquals("۱،۲۳۴،۵۶۷", t.filter(AnnotatedString("1234567")).text.text)
        assertEquals("۱۲", t.filter(AnnotatedString("12")).text.text)
        assertEquals("", t.filter(AnnotatedString("")).text.text)
    }

    @Test
    fun transformation_offsetMappingIsConsistent() {
        val t = ThousandsSeparatorTransformation()
        val digits = "1234567"
        val res = t.filter(AnnotatedString(digits))
        val map = res.offsetMapping
        // انتهای متن باید به انتهای متن نمایشی نگاشت شود
        assertEquals(res.text.length, map.originalToTransformed(digits.length))
        // رفت‌وبرگشت باید پایدار باشد
        for (i in 0..digits.length) {
            assertEquals(i, map.transformedToOriginal(map.originalToTransformed(i)))
        }
        // هیچ نگاشتی نباید از محدوده خارج شود
        for (i in 0..res.text.length) {
            val o = map.transformedToOriginal(i)
            assert(o in 0..digits.length)
        }
    }
}
