package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThinAccountFlowBarsGuardTest {
 @Test fun `deposit and withdrawal tiles stay readable and adapt vertically`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val compact=s.substringAfter("private fun CompactAccountFlow").substringBefore("private fun SummaryChip")
  assertTrue(compact.contains("Column("))
  assertTrue(compact.contains("vertical=7.dp"))
  assertTrue(compact.contains("Color.Black.copy(alpha=.34f)"))
  assertTrue(compact.contains("typography.bodyMedium")&&compact.contains("Text(label")&&compact.contains("Text(value"))
  assertTrue(compact.contains("maxLines=2"))
  val summary=s.substringAfter("private fun SummaryChip").substringBefore("internal fun buildDailySeries")
  assertTrue(summary.contains("vertical = 5.dp"))
  assertTrue(summary.contains("Text(label")&&summary.contains("Modifier.weight(1f)"))
 }
}
