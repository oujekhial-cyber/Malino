package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class MarketPulseListDesignGuardTest{
 @Test fun marketPulseUsesCompactListsAndRealRefreshComparison(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/MarketPulseScreen.kt").readText()
  assertTrue(s.contains("LazyColumn")&&s.contains("MarketList(groupItems")&&s.contains("0->metalItems")&&s.contains("1->coinItems")&&s.contains("else->currencyItems"))
  assertFalse("Old two-column price grid must not return",s.contains("items.chunked(2)"))
  assertTrue("Daily trend must come from the online quote",s.contains("GoldPriceService.marketQuote")&&s.contains("dailyChanges=fresh.mapValues"))
  assertTrue(s.contains("٪ روزانه")&&s.contains("dailyChange?.compareTo(0.0)"))
  assertFalse("Refresh-to-refresh changes are not daily market changes",s.contains("previousValues=values"))
 }
}
