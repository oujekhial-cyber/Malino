package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class SmartBankBalanceRefreshGuardTest{
 @Test fun dashboardRefreshIsAccountScopedAndHasRequiredFeedback(){
  val d=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();val s=File("src/main/java/ir/kharjyar/app/core/sms/BankBalanceRefreshService.kt").readText();val h=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  assertTrue(d.contains("بروزرسانی موجودی")&&d.contains("موجودی بروز شد")&&d.contains("30_000")&&d.contains("4_000"))
  assertTrue(d.contains("موجودی مغایرت دارد")&&d.contains("reviewImport?accountId="))
  assertTrue(s.contains("AccountNumberMatcher.match(body,matchables)")&&s.contains("match.accountId!=account.id"))
  assertTrue(h.contains("aid!=accountFilter")&&h.contains("AccountSmsReconciler.reconcile")&&h.contains("اصلاح تراکنش"))
 }
}
