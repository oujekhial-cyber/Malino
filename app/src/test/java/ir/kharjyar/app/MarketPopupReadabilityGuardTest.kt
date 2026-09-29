package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class MarketPopupReadabilityGuardTest {
 @Test fun `market popup matches selected premium neon reference`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  listOf("fillMaxWidth(.97f)","Color(0xFF0B101B).copy(alpha=.97f)","RoundedCornerShape(34.dp)","Color(0xFFFF4EA3)","Color(0xFF45E4E0)","PremiumMarketTile","shadow(22.dp","\"Au\"","\"\\$\"","آخرین تغییرات بازار","آخرین بروزرسانی").forEach{assertTrue(it,source.contains(it))}
 }
}
