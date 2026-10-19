package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallmentPaymentDateGuardTest {
 @Test fun `installment payment date is user selected and editable`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  assertTrue(screen.contains("var paymentDate by remember(installment.id)"))
  assertTrue(screen.contains("installment.paidAt?.let(PersianDate::fromMillis)?:PersianDate.today()"))
  assertTrue(screen.contains("تاریخ پرداخت واقعی"))
  assertTrue(screen.contains("PersianDateField(paymentDate,{paymentDate=it})"))
  assertTrue(screen.contains("paidAt=paymentDate.startOfDayMillis()"))
  assertFalse(screen.contains("installment.copy(paid=true,paidAt=System.currentTimeMillis()"))
 }
}
