package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReminderModernHeroGuardTest {
 @Test fun listAndEntryUseModernSummaryHeroes(){
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText()
  assertTrue(source.split("ModernSummaryHero(").size-1>=2)
  assertTrue(source.contains("برنامه‌ریزی کارها و موعدهای مهم"))
  assertTrue(source.contains("SummaryMetric(\"فعال\""))
  assertTrue(source.contains("SummaryMetric(\"انجام‌شده\""))
  assertTrue(source.contains("SummaryMetric(\"موعد بعدی\""))
  assertTrue(source.contains("SummaryMetric(\"تکرار\""))
 }
}
