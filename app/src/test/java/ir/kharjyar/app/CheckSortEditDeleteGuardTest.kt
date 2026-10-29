package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CheckSortEditDeleteGuardTest {
 @Test fun `checks sort by due date and support swipe edit and confirmed delete`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  val dao=File("src/main/java/ir/kharjyar/app/data/db/ObligationDaos.kt").readText()
  listOf(
   ".sortedBy{it.dueAt}",
   "SwipeActionRow(",
   "onDelete = { pendingDelete = check }",
   "onEdit = { editingCheck = check; entryDirection = check.direction }",
   "title={Text(\"حذف چک؟\")}",
   "CheckEntryPage(vm,entryDirection!!,editingCheck",
   "if(existing==null)vm.repo.db.checkDao().insert(record)else vm.repo.db.checkDao().update(record)",
   "Text(if(existing!=null)\"ذخیره تغییرات چک\""
  ).forEach{assertTrue(it,source.contains(it))}
  assertTrue(dao.contains("@Delete suspend fun delete(v: CheckEntity)"))
 }

 @Test fun `clearing check creates matching account transaction automatically`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("vm.repo.txDao.insert(TransactionEntity(accountId=accountId,amountRial=check.amountRial"))
  assertTrue(source.contains("direction=if(received)TxDirection.DEPOSIT else TxDirection.WITHDRAW"))
  assertTrue(source.contains("nature=TxNature.TRANSFER"))
  assertTrue(source.contains("check.copy(status=CheckStatus.CLEARED,accountId=accountId)"))
 }
}
