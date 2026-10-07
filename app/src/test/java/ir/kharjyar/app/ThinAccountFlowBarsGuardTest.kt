package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThinAccountFlowBarsGuardTest {
 @Test fun `deposit and withdrawal bars are thin and keep label and value inline`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val compact=s.substringAfter("private fun CompactAccountFlow").substringBefore("private fun SummaryChip")
  assertTrue(compact.contains("vertical=4.dp"))
  assertTrue(compact.contains("Color.Black.copy(alpha=.32f)"))
  assertTrue(compact.contains("typography.labelMedium")&&compact.contains("Text(label")&&compact.contains("Text(value"))
  assertFalse(compact.contains("Column(Modifier.weight(1f))"))
  val summary=s.substringAfter("private fun SummaryChip").substringBefore("internal fun buildDailySeries")
  assertTrue(summary.contains("vertical = 5.dp"))
  assertTrue(summary.contains("Text(label")&&summary.contains("Modifier.weight(1f)"))
 }
}
