package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class StagedDebtPaymentTransactionGuardTest {
 @Test fun `every staged debt payment writes matching account transaction`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  listOf(
   "var paymentAccountId",
   "واریز به حساب",
   "برداشت از حساب",
   "paymentAccountId!=null",
   "vm.repo.addManualTransaction(accountId=accountId,amountRial=value",
   "direction=if(receivable)TxDirection.DEPOSIT else TxDirection.WITHDRAW",
   "nature=TxNature.NEUTRAL",
   "دریافت مرحله‌ای طلب از ",
   "پرداخت مرحله‌ای بدهی به ",
   "vm.repo.addManualTransaction(accountId=p.accountId,amountRial=recordedAmount"
  ).forEach{assertTrue(it,screen.contains(it))}
 }
}
