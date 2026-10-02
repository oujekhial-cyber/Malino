package ir.kharjyar.app

import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.Extractor
import ir.kharjyar.app.core.sms.InterestSmsDetector
import ir.kharjyar.app.core.sms.SmsClassifier
import ir.kharjyar.app.core.sms.SmsKind
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BankAccountKindAndInterestGuardTest {
 @Test fun interestSmsIsRecognizedAsDeposit(){
  val result=Extractor.autoExtract("سود سپرده شماره 1234 به مبلغ 1,250,000 ریال واریز شد")
  assertEquals(1_250_000L,result.amountRial)
  assertEquals(ExtractedDirection.DEPOSIT,result.directionEnum())
 }
 @Test fun commonSavingsInterestWordingIsFinancialDeposit(){
  val samples=listOf("واریز سود متعلقه حساب پس‌انداز 1234 مبلغ 850,000 ریال","سود دوره‌ای سپرده شما به مبلغ ۲,۰۰۰,۰۰۰ ریال واریز گردید","سود علی‌الحساب مبلغ 500000 ریال بستانکار شد")
  samples.forEach{body->
   assertTrue(InterestSmsDetector.isInterest(body))
   assertEquals(SmsKind.FINANCIAL_LIKELY,SmsClassifier.classify(body))
   assertEquals(ExtractedDirection.DEPOSIT,Extractor.autoExtract(body).directionEnum())
  }
 }
 @Test fun repositoryMarksInterestAsIncomeAndInvestmentCategory(){
  val source=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
  assertTrue(source.contains("nature = if(interestDeposit) TxNature.INCOME"))
  assertTrue(source.contains("سود و سرمایه‌گذاری"))
  assertTrue(source.contains("سود واریزشده سپرده"))
 }
 @Test fun accountFormAndMigrationPersistInterestRouting(){
  val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  val ui=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
  assertTrue(entity.contains("bankAccountKind")&&entity.contains("interestDestinationAccountId"))
  assertTrue(db.contains("MIGRATION_15_16")&&db.contains("version = 19"))
  assertTrue(ui.contains("قرض‌الحسنه")&&ui.contains("کوتاه‌مدت عادی")&&ui.contains("واریز سود به"))
 }
}
