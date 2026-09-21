package ir.kharjyar.app.core.money

import ir.kharjyar.app.core.text.Digits

/** واحد نمایش پول. ذخیره‌سازی همیشه به ریال و از نوع Long است. */
enum class MoneyUnit { RIAL, TOMAN }

object Money {

    /** تبدیل تومان به ریال (دقیق). */
    fun tomanToRial(toman: Long): Long = Math.multiplyExact(toman, 10L)

    /** بخش تومانی یک مبلغ ریالی (بدون گردکردن مخفی). */
    fun rialToTomanWhole(rial: Long): Long = rial / 10

    /** باقی‌مانده ریالی هنگام نمایش تومان. */
    fun rialToTomanRemainder(rial: Long): Long = rial % 10

    /**
     * قالب‌بندی مبلغ ریالی برای نمایش.
     * اگر واحد تومان انتخاب شده و باقی‌مانده ریالی وجود دارد، باقی‌مانده صریحاً نمایش داده می‌شود
     * تا هیچ ریالی بی‌صدا حذف نشود.
     */
    fun format(rial: Long, unit: MoneyUnit, withUnit: Boolean = true): String {
        return when (unit) {
            MoneyUnit.RIAL -> {
                val body = Digits.group(rial)
                if (withUnit) "$body ریال" else body
            }
            MoneyUnit.TOMAN -> {
                val whole = rialToTomanWhole(rial)
                val rem = rialToTomanRemainder(rial)
                val body = Digits.group(whole)
                val remPart = if (rem != 0L) " و ${Digits.toPersian(kotlin.math.abs(rem).toString())} ریال" else ""
                if (withUnit) "$body تومان$remPart" else body + remPart
            }
        }
    }

    /** مبلغ واردشده توسط کاربر (با واحد نمایش فعلی) به ریال. */
    fun inputToRial(input: String, unit: MoneyUnit): Long? {
        val value = Digits.parseAmount(input) ?: return null
        return when (unit) {
            MoneyUnit.RIAL -> value
            MoneyUnit.TOMAN -> tomanToRial(value)
        }
    }
}
