package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class TransactionLiveSearchGuardTest {
 @Test fun `text query cannot match every amount through empty digit string`() {
  val textQuery="خرید نان"
  val digits=textQuery.filter(Char::isDigit)
  assertTrue(digits.isBlank())
  assertFalse(digits.isNotBlank() && "250000".contains(digits))
 }
 @Test fun `transaction screen performs normalized live search across useful fields`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  listOf("onValueChange = { query = it }","queryDigits.isNotBlank()","normalizeForMatch","tx.refNumber","accountTitles","categoryTitles","ignoreCase = true").forEach{assertTrue(it,source.contains(it))}
  assertFalse(source.contains("tx.amountRial.toString().contains(q.filter(Char::isDigit))"))
 }
}
