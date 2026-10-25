package ir.kharjyar.app
import ir.kharjyar.app.core.sms.SpamSmsClassifier
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class RecoverableSpamFilterTest {
 @Test fun confidentPromotionNeedsMultipleSignals(){
  assertTrue(SpamSmsClassifier.decide("فروش ویژه با کد تخفیف؛ مشاهده کنید https://shop.ir").confident)
  assertFalse(SpamSmsClassifier.decide("تخفیف شما اعمال شد").confident)
 }
 @Test fun bankTransactionIsNeverSpam(){
  assertFalse(SpamSmsClassifier.decide("واریز 1,500,000 ریال به حساب؛ مانده 8,000,000 ریال").confident)
  assertFalse(SpamSmsClassifier.decide("خرید 2,300,000 ریال؛ جشنواره امتیازی بانک؛ مانده 9,000,000").confident)
  assertFalse(SpamSmsClassifier.decide("پرداخت 450000 ریال انجام شد کد پیگیری 77881").confident)
 }
 @Test fun definiteFinancialSmsBypassesAndRepairsAccidentalBlocking(){
  val repo=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
  assertTrue(repo.contains("BankSmsEvidenceClassifier.classify"))
  assertTrue(repo.contains("BankSmsVerdict.TRANSACTION"))
  assertTrue(repo.contains("if (blockedSenderDao.isBlocked(sender) && !protectedFinancial)"))
  assertTrue(repo.contains("if (protectedFinancial) blockedSenderDao.unblock(sender)"))
  assertTrue(repo.indexOf("val evidence=") < repo.indexOf("SpamSmsClassifier.decide(body)"))
 }
 @Test fun quarantineIsRestorableAndMigrated(){
  val repo=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/BlockedSendersScreen.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  assertTrue(repo.contains("restoreSpamMessage")&&repo.contains("knownFinancialSender"))
  assertTrue(screen.contains("تبلیغاتی نیست؛ بازگرداندن")&&screen.contains("دلیل:"))
  assertTrue(db.contains("version = 23")&&db.contains("MIGRATION_18_19"))
 }
}
