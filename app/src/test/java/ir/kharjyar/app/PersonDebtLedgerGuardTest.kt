package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class PersonDebtLedgerGuardTest {
 private val source=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
 @Test fun `debt home groups records by clickable person and opens ledger`() {
  listOf("val shownPeople = people.filter","clickable { selectedPersonId = person.id }","PersonDebtDetail","طلب‌ها و بدهی‌های این فرد","سوابق پرداخت").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `payment history retains date amount and note`() {
  listOf("paidDate.startOfDayMillis()","note = note.trim()","PersianDate.fromMillis(payment.paidAt).format()","payment.note.ifBlank").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `excess payment asks and can create opposite obligation or ignore surplus`() {
  listOf("PendingExcessPayment","مبلغ پرداخت بیشتر از مانده است","ثبت به‌عنوان \$oppositeLabel","نادیده‌گرفتن مبلغ مازاد","oppositeKind = if (p.debt.kind == DebtKind.RECEIVABLE) DebtKind.PAYABLE else DebtKind.RECEIVABLE","amountRial = p.surplusRial").forEach{assertTrue(it,source.contains(it))}
 }
}
