package ir.kharjyar.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class UnreadBankSmsRecoveryGuardTest {
 @Test fun `app open reuses normal sms pipeline for unread inbox messages`() {
  val recovery=File("src/main/java/ir/kharjyar/app/core/sms/UnreadBankSmsRecovery.kt").readText()
  val activity=File("src/main/java/ir/kharjyar/app/MainActivity.kt").readText()
  assertTrue(activity.contains("UnreadBankSmsRecovery.scanOnAppOpen"))
  assertTrue(recovery.contains("Manifest.permission.READ_SMS"))
  assertTrue(recovery.contains("\"read = 0\""))
  assertTrue(recovery.contains("BankSmsDecisionEngine.analyze"))
  assertTrue(recovery.contains("if(!decision.shouldIngest)return@forEach"))
  assertTrue(recovery.contains("repository.ingestSms"))
  assertTrue(recovery.contains("SmsProcessWorker"))
  assertTrue(recovery.contains("enqueueUniqueWork"))
  assertTrue(recovery.contains("!result.duplicate"))
  assertFalse(recovery.contains("ContentValues"))
  assertFalse(recovery.contains("contentResolver.update"))
 }
}
