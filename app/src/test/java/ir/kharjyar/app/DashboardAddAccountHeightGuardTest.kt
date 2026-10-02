package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardAddAccountHeightGuardTest{
 @Test fun addAccountMatchesCompactCardsAndCarouselCollapses(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("val compactAccountCardHeight = 205.dp"))
  assertTrue(s.contains(".height(compactAccountCardHeight)"))
  assertTrue(s.contains("if (defaultAccount == null) 215.dp else 375.dp"))
  assertTrue(s.contains("Modifier.height(accountCarouselHeight).animateContentSize"))
 }
}
