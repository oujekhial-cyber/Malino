package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NewFinancialBackupCoverageGuardTest {
 @Test fun `new financial and asset state is snapshotted merged and restored`() {
  val payload=File("src/main/java/ir/kharjyar/app/core/backup/BackupPayload.kt").readText()
  val manager=File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText()
  assertTrue(payload.contains("val localFinancialState: Map<String, String> = emptyMap()"))
  listOf("ration.members","ration.balance","ration.checked","payload.localFinancialState + current.localFinancialState","restoreLocalFinancialState(payload.localFinancialState)").forEach{assertTrue(it,manager.contains(it))}
  listOf("allAssetsOnce()","allTradesOnce()","allLoansOnce()","allInstallmentsOnce()","allPaymentsOnce()","transactionAttachmentDao().allOnce()").forEach{assertTrue(it,manager.contains(it))}
 }
}
