package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class MarketPopupSolidIconNavigationGuardTest{
 @Test fun popupIsReadableAndIconOpensFullPage(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("alpha=if(darkMarket).97f else .98f"))
  assertTrue(s.contains("Icons.Filled.QueryStats")&&s.contains("Icons.Filled.ArrowOutward"))
  assertTrue(s.contains("برای مشاهده کامل، آیکون را لمس کنید"))
  assertFalse(s.contains("Text(\"مشاهده کامل نبض بازار\""))
 }
}
