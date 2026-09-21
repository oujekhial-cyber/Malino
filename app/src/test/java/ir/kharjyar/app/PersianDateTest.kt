package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PersianDateTest {

    @Test
    fun `known conversion nowruz 1403`() {
        // ۱ فروردین ۱۴۰۳ = 20 March 2024
        val pd = PersianDate(1403, 1, 1)
        assertEquals(LocalDate.of(2024, 3, 20), pd.toLocalDate())
    }

    @Test
    fun `known conversion nowruz 1404`() {
        // ۱ فروردین ۱۴۰۴ = 21 March 2025
        val pd = PersianDate(1404, 1, 1)
        assertEquals(LocalDate.of(2025, 3, 21), pd.toLocalDate())
    }

    @Test
    fun `roundtrip conversion across years`() {
        var date = LocalDate.of(2020, 1, 1)
        repeat(3000) {
            val pd = PersianDate.fromLocalDate(date)
            assertEquals("roundtrip failed at $date", date, pd.toLocalDate())
            date = date.plusDays(1)
        }
    }

    @Test
    fun `leap year 1403 is leap`() {
        assertTrue(PersianDate.isLeapYear(1403))
        assertEquals(30, PersianDate.monthLength(1403, 12))
    }

    @Test
    fun `year 1404 is not leap`() {
        assertFalse(PersianDate.isLeapYear(1404))
        assertEquals(29, PersianDate.monthLength(1404, 12))
    }

    @Test
    fun `month lengths`() {
        assertEquals(31, PersianDate.monthLength(1404, 1))
        assertEquals(31, PersianDate.monthLength(1404, 6))
        assertEquals(30, PersianDate.monthLength(1404, 7))
        assertEquals(30, PersianDate.monthLength(1404, 11))
    }

    @Test
    fun `esfand end of leap year roundtrip`() {
        val pd = PersianDate(1403, 12, 30)
        val back = PersianDate.fromLocalDate(pd.toLocalDate())
        assertEquals(pd, back)
    }

    @Test
    fun `day boundary in tehran timezone`() {
        val pd = PersianDate(1404, 5, 10)
        val start = pd.startOfDayMillis()
        val end = pd.endOfDayMillisExclusive()
        assertEquals(24 * 3600 * 1000L, end - start)
        // شروع روز باید همان روز شمسی باشد
        assertEquals(pd, PersianDate.fromMillis(start))
        assertEquals(pd, PersianDate.fromMillis(end - 1))
        assertEquals(pd.plusDays(1), PersianDate.fromMillis(end))
    }

    @Test
    fun `month arithmetic`() {
        val pd = PersianDate(1403, 12, 30)
        val next = pd.plusMonths(1)
        assertEquals(PersianDate(1404, 1, 30), next)
        // ماه ۱۲ سال غیرکبیسه ۲۹ روزه: روز باید clamp شود
        val esf = PersianDate(1404, 11, 30).plusMonths(1)
        assertEquals(PersianDate(1404, 12, 29), esf)
    }

    @Test
    fun `format persian digits`() {
        assertEquals("۱۴۰۳/۰۱/۰۱", PersianDate(1403, 1, 1).format())
    }
}
