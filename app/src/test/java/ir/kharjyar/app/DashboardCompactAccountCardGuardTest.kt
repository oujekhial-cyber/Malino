package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardCompactAccountCardGuardTest {
 @Test fun accountCardsStayUniformAndNeverExpandOnSelection(){
  val d=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();val card=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(d.contains("val compactAccountCardHeight = 230.dp"))
  assertTrue(d.contains("val accountCarouselHeight = 240.dp"))
  assertTrue(d.contains(".height(compactAccountCardHeight)"))
  assertFalse(d.contains(".height(if(defaultAccount?.id == account.id)"))
  assertFalse(d.contains("AnimatedVisibility(defaultAccount?.id == account.id)"))
  assertTrue(d.contains("مانده پیامک: ثبت نشده")&&d.contains("AccountBelowCardPanel("))
  assertTrue(card.contains("rotationX = flipRotation")&&card.contains("اطلاعات کامل حساب"))
 }
}
