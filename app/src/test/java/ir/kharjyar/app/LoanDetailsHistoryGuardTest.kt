package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class LoanDetailsHistoryGuardTest {
 @Test fun `loan list opens details with summary and paid installment history`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  listOf("selectedLoanId=loan.id","LoanDetailsPage","مشخصات وام","برنامه اقساط","پرداخت‌شده در","Money.format(installment.amountRial","installments.sortedBy { it.number }").forEach { assertTrue(it,source.contains(it)) }
 }
}
