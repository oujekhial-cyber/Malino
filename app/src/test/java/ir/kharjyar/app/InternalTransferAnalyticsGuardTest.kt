package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InternalTransferAnalyticsGuardTest {
 @Test fun `internal transfer is counted once and never as real income or expense`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  val summary=File("src/main/java/ir/kharjyar/app/core/balance/TxSummary.kt").readText()
  assertTrue(screen.contains("it.nature == TxNature.INCOME"))
  assertTrue(screen.contains("it.nature == TxNature.EXPENSE"))
  assertTrue(screen.contains("it.transferGroupId != null"))
  assertTrue(screen.contains("groupBy { requireNotNull(it.transferGroupId) }"))
  assertTrue(screen.contains("pair.maxOfOrNull { it.amountRial }"))
  assertTrue(screen.contains("درآمد واقعی"))
  assertTrue(screen.contains("هزینه واقعی"))
  assertTrue(screen.contains("انتقال بین حساب‌ها"))
  assertTrue(screen.contains("سایر جابه‌جایی‌ها"))
  assertTrue(summary.contains("if (t.nature == TxNature.TRANSFER) continue"))
 }
}
