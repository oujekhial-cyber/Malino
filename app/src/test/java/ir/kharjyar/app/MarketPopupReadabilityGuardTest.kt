package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class MarketPopupReadabilityGuardTest {
 @Test fun `market popup is readable glass with compact expressive graphics`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  listOf("fillMaxWidth(.90f)","surface.copy(alpha=.95f)","shadow(18.dp","RoundedCornerShape(30.dp)","Color.White.copy(alpha=.24f)","surfaceVariant.copy(alpha=.94f)","Brush.verticalGradient","\"Au\"","else \"\\$\"").forEach{assertTrue(it,source.contains(it))}
 }
}
