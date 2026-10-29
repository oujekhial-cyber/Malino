package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class DashboardLiveMarketGlassGuardTest {
 @Test fun `home has animated glass market dropdown with gold and dollar rates`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val service=File("src/main/java/ir/kharjyar/app/assets/GoldPriceService.kt").readText()
  listOf("LiveMarketGlass","نبض بازار","AnimatedVisibility","expandVertically","Brush.linearGradient","طلای ۱۸ عیار","دلار آزاد","refreshKey").forEach{assertTrue(it,dashboard.contains(it))}
  assertTrue(service.contains("dollarRial"));assertTrue(service.contains("price_dollar_rl"))
 }
}
