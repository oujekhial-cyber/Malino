package ir.kharjyar.app.core.date

import ir.kharjyar.app.core.text.Digits
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * تقویم جلالی (شمسی) با الگوریتم jalaali.js (Behrang Noruzi Niya) —
 * الگوریتم مرجع و سازگار با تقویم رسمی ایران، شامل سال‌های کبیسه.
 * زمان‌ها به شکل epoch millis ذخیره می‌شوند و فقط برای نمایش/مرزبندی به شمسی تبدیل می‌شوند.
 */
data class PersianDate(val year: Int, val month: Int, val day: Int) : Comparable<PersianDate> {

    override fun compareTo(other: PersianDate): Int =
        compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    fun format(persianDigits: Boolean = true): String {
        val s = "%04d/%02d/%02d".format(year, month, day)
        return if (persianDigits) Digits.toPersian(s) else s
    }

    fun monthName(): String = MONTH_NAMES[month - 1]

    /** نام روز هفته به فارسی (شنبه تا جمعه). */
    fun dayOfWeekName(): String = WEEK_DAY_NAMES[
        // DayOfWeek: MONDAY=1 .. SUNDAY=7 ؛ شنبه شروع هفته ایرانی است
        (toLocalDate().dayOfWeek.value + 1) % 7
    ]

    fun monthLength(): Int = monthLength(year, month)

    fun firstOfMonth(): PersianDate = PersianDate(year, month, 1)

    fun lastOfMonth(): PersianDate = PersianDate(year, month, monthLength())

    fun plusDays(days: Int): PersianDate = fromLocalDate(toLocalDate().plusDays(days.toLong()))

    fun plusMonths(count: Int): PersianDate {
        var y = year
        var m = month + count
        while (m > 12) { m -= 12; y++ }
        while (m < 1) { m += 12; y-- }
        return PersianDate(y, m, day.coerceAtMost(monthLength(y, m)))
    }

    fun toLocalDate(): LocalDate {
        val r = jalCal(year)
        val marchFirst = LocalDate.of(r.gy, 3, r.march)
        val dayOfJalaliYear = (month - 1) * 31 - (month / 7) * (month - 7) + day - 1
        return marchFirst.plusDays(dayOfJalaliYear.toLong())
    }

    /** ابتدای این روز شمسی در منطقه زمانی داده‌شده. */
    fun startOfDayMillis(zone: ZoneId = TEHRAN): Long =
        toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    /** پایان (exclusive) این روز شمسی. */
    fun endOfDayMillisExclusive(zone: ZoneId = TEHRAN): Long =
        toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    companion object {
        val TEHRAN: ZoneId = ZoneId.of("Asia/Tehran")

        /** ایندکس ۰ = شنبه */
        val WEEK_DAY_NAMES = listOf(
            "شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
        )

        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )

        private val BREAKS = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )

        private data class JalCal(val leap: Int, val gy: Int, val march: Int)

        /** محاسبه کبیسه، سال میلادی و روز مارس شروع سال جلالی (jalaali.js jalCal). */
        private fun jalCal(jy: Int): JalCal {
            require(jy in BREAKS.first()..(BREAKS.last() - 1)) { "سال جلالی خارج از بازه معتبر: $jy" }
            val gy = jy + 621
            var leapJ = -14
            var jp = BREAKS[0]
            var jump = 0
            for (i in 1 until BREAKS.size) {
                val jm = BREAKS[i]
                jump = jm - jp
                if (jy < jm) break
                leapJ += jump / 33 * 8 + jump % 33 / 4
                jp = jm
            }
            var n = jy - jp
            leapJ += n / 33 * 8 + (n % 33 + 3) / 4
            if (jump % 33 == 4 && jump - n == 4) leapJ += 1
            val leapG = gy / 4 - (gy / 100 + 1) * 3 / 4 - 150
            val march = 20 + leapJ - leapG
            if (jump - n < 6) n = n - jump + (jump + 4) / 33 * 33
            var leap = ((n + 1) % 33 - 1) % 4
            if (leap == -1) leap = 4
            return JalCal(leap, gy, march)
        }

        fun isLeapYear(year: Int): Boolean = jalCal(year).leap == 0

        fun monthLength(year: Int, month: Int): Int = when {
            month <= 6 -> 31
            month <= 11 -> 30
            else -> if (isLeapYear(year)) 30 else 29
        }

        fun fromLocalDate(date: LocalDate): PersianDate {
            val gy = date.year
            var jy = gy - 621
            val r = jalCal(jy)
            val marchFirst = LocalDate.of(r.gy, 3, r.march)
            var k = (date.toEpochDay() - marchFirst.toEpochDay()).toInt()
            if (k >= 0) {
                if (k <= 185) {
                    return PersianDate(jy, 1 + k / 31, k % 31 + 1)
                }
                k -= 186
            } else {
                jy -= 1
                k += 179
                if (r.leap == 1) k += 1
            }
            return PersianDate(jy, 7 + k / 30, k % 30 + 1)
        }

        fun fromMillis(millis: Long, zone: ZoneId = TEHRAN): PersianDate =
            fromLocalDate(Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())

        fun today(zone: ZoneId = TEHRAN): PersianDate = fromLocalDate(LocalDate.now(zone))

        fun formatDateTime(millis: Long, zone: ZoneId = TEHRAN, persianDigits: Boolean = true): String {
            val zdt = Instant.ofEpochMilli(millis).atZone(zone)
            val pd = fromLocalDate(zdt.toLocalDate())
            val time = "%02d:%02d".format(zdt.hour, zdt.minute)
            val s = "${pd.format(persianDigits = false)} $time"
            return if (persianDigits) Digits.toPersian(s) else s
        }

        /** ساخت epoch millis از تاریخ شمسی و ساعت محلی. */
        fun toMillis(date: PersianDate, hour: Int, minute: Int, zone: ZoneId = TEHRAN): Long {
            val ld = date.toLocalDate()
            return LocalDateTime.of(ld.year, ld.month, ld.dayOfMonth, hour, minute)
                .atZone(zone).toInstant().toEpochMilli()
        }
    }
}
