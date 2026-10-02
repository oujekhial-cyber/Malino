package ir.kharjyar.app
import ir.kharjyar.app.ui.screens.marketChangeLabel
import ir.kharjyar.app.ui.screens.marketChangePercent
import org.junit.Assert.*
import org.junit.Test
class MarketChangePrecisionTest{
 @Test fun smallRealChangesDoNotRoundToFalseZero(){
  val change=marketChangePercent(10_000_400,10_000_000)!!
  assertEquals(0.004,change,0.000001)
  val label=marketChangeLabel(change)!!
  assertTrue(label,label.contains("۰٫۰۰۴۰")||label.contains("0.0040"))
  assertFalse(label.contains("۰٫۰۰٪"))
 }
 @Test fun fallingPriceUsesAbsoluteNumberAndDownDirection(){
  val label=marketChangeLabel(marketChangePercent(990,1000))!!
  assertTrue(label.contains("↘"));assertTrue(label.contains("۱٫۰۰")||label.contains("1.00"))
 }
 @Test fun unchangedPriceIsDescribedInsteadOfFakeRise(){assertEquals("بدون تغییر",marketChangeLabel(marketChangePercent(1000,1000)))}
}
