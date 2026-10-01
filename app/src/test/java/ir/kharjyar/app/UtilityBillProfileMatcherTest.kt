package ir.kharjyar.app

import ir.kharjyar.app.core.sms.UtilityBillMatcher
import ir.kharjyar.app.data.db.UtilityBillProfileEntity
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class UtilityBillProfileMatcherTest {
 private val home=UtilityBillProfileEntity(id=7,title="برق خانه",type="برق",identifier="۱۲۳۴۵۶۷۸")
 @Test fun matchesOnlyPreviouslyRegisteredIdentifier(){
  assertEquals(7L,UtilityBillMatcher.uniqueProfileId("توانیر: شناسه قبض 12345678 مبلغ ۵۰۰٬۰۰۰ ریال",listOf(home)))
  assertNull(UtilityBillMatcher.uniqueProfileId("قبض برق با شناسه 87654321 صادر شد",listOf(home)))
 }
 @Test fun ambiguousIdentifierDoesNotGuessDestination(){
  val duplicate=home.copy(id=8,title="محل دیگر")
  assertNull(UtilityBillMatcher.uniqueProfileId("شناسه قبض 12345678",listOf(home,duplicate)))
 }
 @Test fun receiverAndDatabaseRequireProfileLink(){
  val receiver=File("src/main/java/ir/kharjyar/app/receiver/SmsReceiver.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  assertTrue(receiver.contains("allUtilityBillsOnce"))
  assertTrue(receiver.contains("utilityBillId == null) return@launch"))
  assertTrue(db.contains("MIGRATION_16_17")&&db.contains("version = 17"))
 }
}
