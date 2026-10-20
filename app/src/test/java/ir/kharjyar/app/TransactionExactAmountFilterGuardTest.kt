package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TransactionExactAmountFilterGuardTest {
 @Test fun `transactions support a dedicated exact amount filter`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(source.contains("exactAmountRial==null || tx.amountRial==exactAmountRial"))
  assertTrue(source.contains("label=\"فقط تراکنش‌های با این مبلغ\""))
  assertTrue(source.contains("مبلغ دقیق: ${'$'}{Money.format(amount,settings.moneyUnit)}"))
  assertTrue(source.contains("exactAmountInput=\"\";onlyPending=false"))
 }
}
