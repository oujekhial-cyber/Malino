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
 @Test fun `dashboard account cards share one stable height`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val card=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(dashboard.contains("val accountCardHeight = 300.dp"))
  assertTrue(dashboard.contains(".height(accountCardHeight)"))
  assertTrue(dashboard.contains("showDetailsWhenSelected = false"))
  assertTrue(card.contains("visible = selected && showDetailsWhenSelected"))
 }
}
