package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSwipeEditDeleteGuardTest {
 @Test fun `individual receivables and payables support swipe edit and confirmed delete`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  val dao=File("src/main/java/ir/kharjyar/app/data/db/ObligationDaos.kt").readText()
  listOf(
   "onDelete = { pendingDeleteDebt = debt }",
   "onEdit = { editingDebt = debt }",
   "removeOnDelete = false",
   "ویرایش طلب",
   "ویرایش بدهی",
   "editedRial>=alreadyPaid",
   "ذخیره تغییرات",
   "حذف طلب؟",
   "حذف بدهی؟",
   "deletePaymentsOf(debt.id)",
   "deleteDebt(debt)"
  ).forEach{assertTrue(it,screen.contains(it))}
  assertTrue(dao.contains("DELETE FROM debt_payments WHERE debtId = :debtId"))
  assertTrue(dao.contains("@Delete suspend fun deleteDebt(v: DebtEntity)"))
 }
}
