package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanSwipeEditDeleteGuardTest {
 @Test fun `loan cards provide swipe edit and confirmed delete`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  assertTrue(screen.contains("SwipeActionRow("))
  assertTrue(screen.contains("onEdit={editingLoan=loan}"))
  assertTrue(screen.contains("onDelete={deletingLoan=loan}"))
  assertTrue(screen.contains("title={Text(\"ویرایش وام و برنامه اقساط\")}"))
  assertTrue(screen.contains("title={Text(\"حذف وام؟\")}"))
  assertTrue(screen.contains("deleteInstallments(loan.id)"))
  assertTrue(screen.contains("deleteLoan(loan.id)"))
 }

 @Test fun `editing preserves paid history and rebuilds only unpaid schedule`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  val dao=File("src/main/java/ir/kharjyar/app/data/db/PlanningDaos.kt").readText()
  assertTrue(dao.contains("DELETE FROM loan_installments WHERE loanId=:loanId AND paid=0"))
  assertTrue(screen.contains("val paidNumbers=paidRows.map{it.number}.toSet()"))
  assertTrue(screen.contains("filterNot{it in paidNumbers}"))
  assertTrue(screen.contains("اصل وام نمی‌تواند از مجموع مبالغ پرداخت‌شده کمتر باشد"))
  assertTrue(screen.contains("تعداد اقساط نمی‌تواند از آخرین قسط پرداخت‌شده کمتر باشد"))
 }

 @Test fun `edit delete and installment settlement clear stale notifications`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  val notifier=File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
  assertTrue(screen.contains("cancelInstallmentReminder(context,it.id)"))
  assertTrue(screen.contains("cancelInstallmentReminder(context,installment.id)"))
  assertTrue(notifier.contains("TAG_INSTALLMENT_REMINDER"))
 }
}
