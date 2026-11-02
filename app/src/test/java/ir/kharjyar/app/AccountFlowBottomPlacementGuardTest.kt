package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AccountFlowBottomPlacementGuardTest {
 @Test fun `flow summaries stay at bottom of bank and cash cards`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains(".matchParentSize()\n            .padding(16.dp)"))
  assertTrue(s.contains("Column(Modifier.matchParentSize().padding(17.dp))"))
  assertTrue(Regex("Spacer\\(Modifier\\.weight\\(1f\\)\\)\\s*content\\(\\)").findAll(s).count()>=2)
  assertTrue(s.contains("همیشه به لبه پایین کارت"))
 }
}
