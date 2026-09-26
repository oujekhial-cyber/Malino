package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ObligationsAndSmsHistoryGuardTest {
 @Test fun `database migration adds debts payments and checks`() { val s=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText();assertTrue(s.contains("version = 5"));assertTrue(s.contains("MIGRATION_3_4"));assertTrue(s.contains("MIGRATION_4_5"));assertTrue(s.contains("debt_payments"));assertTrue(s.contains("checks")) }
 @Test fun `historical sms import is permission gated and opens transaction form`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText();assertTrue(s.contains("READ_SMS"));assertTrue(s.contains("date BETWEEN ? AND ?"));assertTrue(s.contains("date ASC"));assertTrue(s.contains("nav.navigate(\"tx/${'$'}id\")")) }
 @Test fun `new account can be populated from pasted bank sms`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText();assertTrue(s.contains("تکمیل خودکار از آخرین پیامک بانک"));assertTrue(s.contains("Extractor.autoExtract(sampleSms)")) }
 @Test fun `obligations have reminders and partial payments`() { val debt=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText();val check=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText();assertTrue(debt.contains("DebtPaymentEntity"));assertTrue(check.contains("TextRecognition"));assertTrue(check.contains("imagePath"));assertTrue(File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").exists()) }
}
