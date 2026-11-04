package ir.kharjyar.app.assets

import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import java.math.BigDecimal
import java.math.RoundingMode

/** Total in the user's input unit; null means incomplete, invalid, or too large to store in rials. */
object StockPurchaseCalculator {
    fun totalInput(quantity: String, unitPrice: String, unit: MoneyUnit): String? {
        val normalized = Digits.normalize(quantity.trim()).replace('٫', '.')
        if (!normalized.matches(Regex("[0-9]+(\\.[0-9]+)?"))) return null
        val count = normalized.toBigDecimalOrNull() ?: return null
        val price = Digits.parseAmount(unitPrice) ?: return null
        if (count <= BigDecimal.ZERO || price <= 0L) return null
        return try {
            val total = count.multiply(BigDecimal.valueOf(price)).setScale(0, RoundingMode.DOWN).longValueExact()
            if (total <= 0L || Digits.parseAmount(total.toString()) == null) return null
            if (unit == MoneyUnit.TOMAN) Money.tomanToRial(total)
            total.toString()
        } catch (_: ArithmeticException) {
            null
        }
    }
}
