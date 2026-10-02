package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardCompactAccountCardGuardTest {
 @Test fun accountDetailsAndFlowsLiveBelowShortCard(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains(".height(200.dp)")&&s.contains("AccountBelowCardPanel("))
  assertTrue(s.contains("شماره شبا")&&s.contains("شماره حساب")&&s.contains("CVV2")&&s.contains("انقضا"))
  assertTrue(s.contains("CompactAccountFlow(\"واریز\"")&&s.contains("CompactAccountFlow(\"برداشت\""))
  assertTrue(s.contains("مانده پیامک:")&&s.contains("onDiscrepancy"))
 }
}
