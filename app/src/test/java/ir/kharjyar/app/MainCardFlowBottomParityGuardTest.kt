package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MainCardFlowBottomParityGuardTest {
 @Test fun `main card uses same bottom flow bars as account cards`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val hero=s.substringAfter("// ----- صفحه نخست: خلاصه همه حساب‌ها").substringBefore("// ----- صفحه‌های بعدی")
  assertTrue(hero.contains("Spacer(Modifier.weight(1f))"))
  assertTrue(hero.contains("CompactAccountFlow(\"واریز\""))
  assertTrue(hero.contains("CompactAccountFlow(\"برداشت\""))
  assertTrue(hero.indexOf("Spacer(Modifier.weight(1f))")<hero.indexOf("CompactAccountFlow(\"واریز\""))
 }
}
