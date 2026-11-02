package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtPersonCardDateGuardTest {
 @Test fun `person card shows only one nearest open due date`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  listOf("receivableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()","payableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()","val nearestDue = listOfNotNull(receivableDate, payableDate).minOrNull()","نزدیک‌ترین موعد:").forEach{assertTrue(it,source.contains(it))}
 }
}
