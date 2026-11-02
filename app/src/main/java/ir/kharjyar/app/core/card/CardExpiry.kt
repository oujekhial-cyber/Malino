package ir.kharjyar.app.core.card

import ir.kharjyar.app.core.text.Digits

/**
 * مقدار پایگاه داده برای سازگاری به صورت ماه/سال باقی می‌ماند، ولی در تمام UI
 * تاریخ به صورت سال/ماه نمایش داده می‌شود؛ در متن LTR یعنی سال در چپ و ماه در راست.
 */
object CardExpiry {
    private val separator = Regex("[/\\-.\\s]+")

    fun storageToDisplay(raw: String): String = swap(raw)

    fun displayToStorage(raw: String): String = swap(raw)

    private fun swap(raw: String): String {
        val normalized = Digits.normalize(raw.trim())
        val parts = normalized.split(separator, limit = 2)
        return if (parts.size == 2 && parts.all { it.isNotBlank() }) {
            "${parts[1]}/${parts[0]}"
        } else normalized
    }

    /** ورودی فرم با سال چهاررقمی در چپ و ماه دورقمی در راست ساخته می‌شود. */
    fun formatDisplayInput(raw: String): String {
        val digits = Digits.normalize(raw).filter(Char::isDigit).take(6)
        return if (digits.length <= 4) digits else digits.take(4) + "/" + digits.drop(4)
    }
}
