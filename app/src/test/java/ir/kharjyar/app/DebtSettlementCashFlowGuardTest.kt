package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSettlementCashFlowGuardTest {
 @Test fun `settlements change balance but are excluded from deposit and withdrawal totals`() {
  val transactions=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  val debts=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  assertTrue(transactions.contains("filtered.filterNot { it.isDebtSettlementTransaction() }"))
  assertTrue(transactions.contains("nature == TxNature.NEUTRAL"))
  assertTrue(transactions.contains("دریافت مرحله‌ای طلب از"))
  assertTrue(transactions.contains("پرداخت مرحله‌ای بدهی به"))
  assertTrue(debts.contains("nature=TxNature.NEUTRAL"))
  assertTrue(debts.contains("direction=if(receivable)TxDirection.DEPOSIT else TxDirection.WITHDRAW"))
  assertTrue(debts.contains("direction = if (receivable) TxDirection.WITHDRAW else TxDirection.DEPOSIT"))
 }
}
