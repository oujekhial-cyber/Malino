package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WithdrawalBalanceValidationGuardTest {
 @Test fun `manual withdrawal and either side of own transfer validate source balance`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
  assertTrue(source.contains("val sourceAccountId=when{internalTransfer&&direction==TxDirection.DEPOSIT->targetAccountId"))
  assertTrue(source.contains("AccountBalance.estimate(it,allTx).rial"))
  assertTrue(source.contains("amount>available->error=\"مانده حساب"))
  assertTrue(source.contains("کمتر از مبلغ برداشت است"))
 }

 @Test fun `smart quick registration also refuses overdraft`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
  assertTrue(source.contains("AccountBalance.estimate(it,transactions).rial"))
  assertTrue(source.contains("if(isWithdrawal&&available!=null&&amount>available)"))
  assertTrue(source.contains("autoRegisterPending=false"))
 }
}
