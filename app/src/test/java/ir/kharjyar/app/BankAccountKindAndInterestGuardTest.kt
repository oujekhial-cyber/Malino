package ir.kharjyar.app

import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.Extractor
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BankAccountKindAndInterestGuardTest {
 @Test fun interestSmsIsRecognizedAsDeposit(){
  val result=Extractor.autoExtract("سود سپرده شماره 1234 به مبلغ 1,250,000 ریال واریز شد")
  assertEquals(1_250_000,result.amountRial)
  assertEquals(ExtractedDirection.DEPOSIT,result.directionEnum())
 }
 @Test fun accountFormAndMigrationPersistInterestRouting(){
  val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  val ui=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
  assertTrue(entity.contains("bankAccountKind")&&entity.contains("interestDestinationAccountId"))
  assertTrue(db.contains("MIGRATION_15_16")&&db.contains("version = 16"))
  assertTrue(ui.contains("قرض‌الحسنه")&&ui.contains("سپرده کوتاه‌مدت")&&ui.contains("حساب مقصد واریز سود"))
 }
}
