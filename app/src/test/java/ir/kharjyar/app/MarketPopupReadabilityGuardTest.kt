package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class MarketPopupReadabilityGuardTest {
 @Test fun `market popup has a fully opaque readable surface`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(source.contains("background(MaterialTheme.colorScheme.surface)"))
  assertFalse(source.contains("surface.copy(alpha=.70f)"))
  assertTrue(source.contains("primary.copy(alpha=.07f)"))
  assertTrue(source.contains("tertiary.copy(alpha=.05f)"))
  assertTrue(source.contains("outline.copy(alpha=.38f)"))
 }
}
