package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModernLoanDesignGuardTest {
 @Test fun `loan list and details mirror modern debt visual language`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  listOf(
   "private fun ModernLoanCard(",
   "Brush.linearGradient",
   "RoundedCornerShape(24.dp)",
   "LoanMetricTile(\"پرداخت‌شده\"",
   "LoanMetricTile(\"مانده اقساط\"",
   "پیشرفت بازپرداخت",
   "قسط بعدی:",
   "مشاهده جزئیات",
   "ModernSummaryHero(loan.title",
   "Text(\"برنامه اقساط\"",
   "Icons.Filled.CheckCircle else Icons.Filled.Schedule",
   "if(installment.paid)\"پرداخت‌شده\" else \"در انتظار\""
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
