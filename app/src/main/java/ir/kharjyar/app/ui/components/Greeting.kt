package ir.kharjyar.app.ui.components

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits

/** «صبح بخیر / ظهر بخیر / عصر بخیر / شب بخیر» بر اساس ساعت تهران. */
fun greetingByHour(hour: Int = java.time.ZonedDateTime.now(PersianDate.TEHRAN).hour): String =
    when (hour) {
        in 5..11 -> "صبح بخیر 🌤"
        in 12..16 -> "ظهر بخیر ☀️"
        in 17..20 -> "عصر بخیر 🌇"
        else -> "شب بخیر 🌙"
    }

/** تاریخ امروز به شکل «شنبه ۱۲ مهر». */
fun todayHeaderLine(date: PersianDate = PersianDate.today()): String =
    "${date.dayOfWeekName()} ${Digits.toPersian(date.day.toString())} ${date.monthName()}"
