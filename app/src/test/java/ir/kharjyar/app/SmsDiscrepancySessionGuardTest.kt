package ir.kharjyar.app

import ir.kharjyar.app.core.sms.*
import ir.kharjyar.app.data.db.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmsDiscrepancySessionGuardTest {
 private fun tx(at:Long,amount:Long=1000)=TransactionEntity(id=1,accountId=7,amountRial=amount,direction=TxDirection.WITHDRAW,occurredAt=at,recordedAt=at,status=TxStatus.CONFIRMED)
 @Test fun `matching requires account amount direction date and clock time`() {
  assertEquals(ReconcileKind.MATCHED,AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1000,TxDirection.WITHDRAW,10_000)),listOf(tx(10_050)),7).single().kind)
  assertEquals(ReconcileKind.MISSING_TRANSACTION,AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1000,TxDirection.WITHDRAW,10_000)),listOf(tx(200_001)),7).single().kind)
  assertEquals(ReconcileKind.AMOUNT_MISMATCH,AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1500,TxDirection.WITHDRAW,10_000)),listOf(tx(10_050)),7).single().kind)
 }
 @Test fun `screen compares interval transactions before rendering only discrepancies`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  assertTrue(source.contains("intervalTransactions=allTransactions.filter{it.accountId==targetId&&it.occurredAt in from..to}"))
  assertTrue(source.contains("findings[it.id]?.kind==ReconcileKind.MISSING_TRANSACTION||findings[it.id]?.kind==ReconcileKind.AMOUNT_MISMATCH"))
 }
 @Test fun `reconciliation list and range survive transaction editor round trip`() {
  val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText();val screen=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  assertTrue(vm.contains("data class SmsDiscrepancySession"));assertTrue(vm.contains("val items:List<ReconcilePhoneSms>"))
  assertTrue(screen.contains("savedSession?.takeIf{it.accountId==initialAccountId}"));assertTrue(screen.contains("saveSmsDiscrepancySession"))
  assertTrue(screen.contains("LaunchedEffect(items,allTransactions"))
 }
}
