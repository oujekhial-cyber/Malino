package ir.kharjyar.app
import ir.kharjyar.app.ui.components.AmountInput
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class AmountLimitAndUniformAccountCardGuardTest {
 @Test fun `amount input accepts two additional digits`() {
  assertEquals("12345678901234567",AmountInput.sanitize("1234567890123456789"))
  assertEquals(17,AmountInput.sanitize("9".repeat(30)).length)
 }
 @Test fun `dashboard account area is stable and visual card is compact`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val card=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(dashboard.contains("val compactAccountCardHeight = 230.dp"))
  assertTrue(dashboard.contains("val accountCarouselHeight = 240.dp") && dashboard.contains(".height(accountCarouselHeight)"))
  assertFalse(dashboard.contains("365.dp else 205.dp"))
  assertTrue(dashboard.contains("showDetailsWhenSelected = false"))
  assertTrue(card.contains("detectDragGestures")&&card.contains("rotationY = flipRotation else rotationX = flipRotation"))
 }
}
