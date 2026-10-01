package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class MarketPulseListDesignGuardTest{
 @Test fun marketPulseUsesCompactListsAndRealRefreshComparison(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/MarketPulseScreen.kt").readText()
  assertTrue(s.contains("LazyColumn")&&s.contains("MarketList(metalItems")&&s.contains("MarketList(currencyItems"))
  assertFalse("Old two-column price grid must not return",s.contains("items.chunked(2)"))
  assertTrue("Trend must compare two fetched values",s.contains("previousValues=values")&&s.contains("value.compareTo(previous)"))
  assertTrue(s.contains("if(values.isNotEmpty())"))
 }
}
