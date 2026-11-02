package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionArrowDirectionGuardTest {
 @Test fun `deposit points inward green and withdrawal points outward red everywhere`() {
  val common=File("src/main/java/ir/kharjyar/app/ui/components/Common.kt").readText()
  val entry=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val widget=File("src/main/java/ir/kharjyar/app/ui/components/WidgetPreview.kt").readText()
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  assertTrue(common.contains("TxDirection.DEPOSIT -> Triple(Icons.Filled.CallReceived"))
  assertTrue(common.contains("Triple(Icons.Filled.CallMade, \"برداشت\""))
  assertTrue(entry.contains("Icons.Filled.CallMade,skin.expenseColor"))
  assertTrue(entry.contains("Icons.Filled.CallReceived,skin.incomeColor"))
  assertTrue(dashboard.contains("if (deposit) Icons.Filled.CallReceived else Icons.Filled.CallMade"))
  assertTrue(widget.contains("true -> Icons.Filled.CallReceived")&&widget.contains("false -> Icons.Filled.CallMade"))
  assertTrue(root.contains("title = \"واریز\"")&&root.contains("icon = Icons.Filled.CallReceived"))
  assertTrue(root.contains("title = \"برداشت\"")&&root.contains("icon = Icons.Filled.CallMade"))
  assertFalse(common.contains("TxDirection.DEPOSIT -> Triple(Icons.AutoMirrored.Filled.TrendingUp"))
 }
}
