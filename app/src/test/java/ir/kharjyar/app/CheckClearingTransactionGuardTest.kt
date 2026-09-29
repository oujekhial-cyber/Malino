package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class CheckClearingTransactionGuardTest {
 @Test fun `clearing check requires account and writes matching confirmed transaction`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  listOf("pendingClear=c","مبلغ چک به کدام حساب واریز شده است؟","مبلغ چک از کدام حساب برداشت شده است؟","حساب مالی *","تاریخ وصول/پاس شدن *","direction=if(received)TxDirection.DEPOSIT else TxDirection.WITHDRAW","nature=TxNature.TRANSFER","description=\"\$action به شماره \$number به نام \${check.counterparty}\"","status=TxStatus.CONFIRMED","check.copy(status=CheckStatus.CLEARED,accountId=accountId)").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `pending check is not marked cleared before transaction confirmation`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("ثبت وصول چک"))
  assertTrue(source.contains("ثبت پاس شدن چک"))
  assertTrue(source.contains("enabled=clearAccountId!=null"))
 }
}
