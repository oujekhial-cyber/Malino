package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
class MarketPulseFullPageGuardTest {
 @Test fun `popup opens a graphical full market page with metals and currencies`() {
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val popup=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val page=File("src/main/java/ir/kharjyar/app/ui/screens/MarketPulseScreen.kt").readText()
  val service=File("src/main/java/ir/kharjyar/app/assets/GoldPriceService.kt").readText()
  assertTrue(root.contains("composable(\"marketPulse\")")&&root.contains("MarketPulseScreen"))
  assertTrue(popup.contains("Icons.Filled.QueryStats")&&popup.contains("ModernMarketEntryButton(dark=darkMarket,onClick={expanded=false;onOpenMarket()})"))
  assertFalse(popup.contains("برای مشاهده کامل، آیکون را لمس کنید"))
  listOf("طلا و نقره","سکه‌های رایج بازار ایران","نرخ آزاد بازار ایران","عمومی TGJU","قیمت‌ها صرفاً جهت اطلاع‌اند","MarketList").forEach{assertTrue(it,page.contains(it))}
  listOf("GOLD18","GOLD24","MESGHAL","SILVER","USD","EUR","GBP","AED","TRY","CAD","AUD","CHF","CNY").forEach{assertTrue(it,page.contains(it)||service.contains(it))}
  assertTrue(service.contains("suspend fun preciousMetalRial"))
 }
}
