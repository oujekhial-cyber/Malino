package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardAddAccountHeightGuardTest{
 @Test fun allDashboardCardsShareCompactHeight(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("val compactAccountCardHeight = 230.dp"))
  assertTrue(s.contains("val accountCarouselHeight = 240.dp"))
  assertTrue(s.contains("HeroCard(\n                                modifier = Modifier\n                                    .width(pageWidth)\n                                    .height(compactAccountCardHeight)"))
  assertTrue(s.split(".height(compactAccountCardHeight)").size-1>=3)
 }
}
