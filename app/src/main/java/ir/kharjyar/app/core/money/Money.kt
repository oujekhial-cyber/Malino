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
     *
     * در واحد تومان، رقم آخرِ ریالی نمایش داده نمی‌شود چون برای کاربر معنایی ندارد
     * و فقط عدد را شلوغ می‌کند. مقدار ذخیره‌شده همچنان ریال کامل است و همه
     * محاسبه‌ها (جمع، مانده، گزارش) روی همان عدد دقیق انجام می‌شود؛ یعنی چیزی
     * گرد نمی‌شود، فقط نمایش داده نمی‌شود.
     */
    fun format(rial: Long, unit: MoneyUnit, withUnit: Boolean = true): String {
        return when (unit) {
            MoneyUnit.RIAL -> {
                val body = Digits.group(rial)
                if (withUnit) "$body ریال" else body
            }
            MoneyUnit.TOMAN -> {
                val body = Digits.group(rialToTomanWhole(rial))
                if (withUnit) "$body تومان" else body
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
