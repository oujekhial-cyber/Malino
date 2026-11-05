package ir.kharjyar.app

import ir.kharjyar.app.assets.StockPurchaseCalculator
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class StockPurchaseCalculatorTest {
    @Test fun `purchase amount follows quantity and average in both currency units`() {
        assertEquals("250000", StockPurchaseCalculator.totalInput("۱۰۰", "۲٬۵۰۰", MoneyUnit.RIAL))
        val toman = StockPurchaseCalculator.totalInput("۱۰۰", "۲٬۵۰۰", MoneyUnit.TOMAN)!!
        assertEquals("250000", toman)
        assertEquals(2_500_000L, Money.inputToRial(toman, MoneyUnit.TOMAN))
        assertEquals("375000", StockPurchaseCalculator.totalInput("150", "2,500", MoneyUnit.RIAL))
        assertEquals("3125", StockPurchaseCalculator.totalInput("۱٫۲۵", "۲۵۰۰", MoneyUnit.RIAL))
    }

    @Test fun `incomplete invalid or overflowing inputs do not leave stale totals`() {
        listOf("", "0", "-1", "abc", "1,000").forEach { assertNull(StockPurchaseCalculator.totalInput(it, "100", MoneyUnit.RIAL)) }
        listOf("", "0", "-1", "abc").forEach { assertNull(StockPurchaseCalculator.totalInput("2", it, MoneyUnit.RIAL)) }
        assertNull(StockPurchaseCalculator.totalInput("99999999999999999999", "999999999999", MoneyUnit.RIAL))
        assertNull(StockPurchaseCalculator.totalInput("100000000000000000", "1", MoneyUnit.RIAL))
        assertNull(StockPurchaseCalculator.totalInput("1", "922337203685477581", MoneyUnit.TOMAN))
    }

    @Test fun `stock form uses stock name and recomputes without overwriting existing totals on entry`() {
        val screen = File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
        assertTrue(screen.contains("AssetKind.STOCK->\"دارایی سهام بورس\""))
        assertTrue(screen.contains("if(kind!=AssetKind.STOCK) OutlinedTextField(title"))
        assertTrue(screen.contains("title=if(kind==AssetKind.STOCK)stockSymbol.trim() else title.trim()"))
        assertTrue(screen.contains("existing?.takeIf { it.kind == AssetKind.STOCK }?.title"))
        assertTrue(screen.contains("if(stockUnitPrice.isNotBlank()) purchase=StockPurchaseCalculator.totalInput"))
        assertTrue(screen.contains("purchase=StockPurchaseCalculator.totalInput(quantity.text,value,settings.moneyUnit).orEmpty()"))
        assertTrue(screen.contains("AmountTextField(purchase,{purchase=it}"))
        assertFalse(screen.contains("محاسبه ارزش خرید سهام"))
    }
}
