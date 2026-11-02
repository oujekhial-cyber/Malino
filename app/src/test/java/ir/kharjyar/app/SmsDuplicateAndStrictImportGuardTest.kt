package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class SmsDuplicateAndStrictImportGuardTest{
 @Test fun duplicateUsesAccountAmountDirectionAndTwoMinuteTimeWindow(){
  val dao=File("src/main/java/ir/kharjyar/app/data/db/Daos.kt").readText();val repo=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText();val worker=File("src/main/java/ir/kharjyar/app/work/SmsProcessWorker.kt").readText()
  assertTrue(dao.contains("accountId=:accountId AND amountRial=:amount AND direction=:direction")&&dao.contains("at-120000"))
  assertTrue(repo.contains("ProcessOutcome.DuplicateFound")&&repo.contains("categoryDao.byId"))
  assertTrue(worker.contains("notifyDuplicateTransaction"))
 }
 @Test fun discrepancyImportRejectsOtpOtherBanksAndNonBankSenders(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  assertTrue(s.contains("SmsClassifier.isOtp(body)")&&s.contains("if(!mappedSender&&!bankSender&&!bankInBody)continue"))
  assertTrue(s.contains("if(accountFilter!=null&&aid!=accountFilter)continue"))
  assertTrue(s.contains("قبلاً ثبت شده")&&s.contains("بابت: \$reason"))
 }
}
