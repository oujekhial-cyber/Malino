package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSettlementReminderCancellationGuardTest {
 @Test fun `full early or due-date settlement disables and removes debt reminder immediately`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  val notifier=File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
  val worker=File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").readText()
  assertTrue(screen.contains("copy(settled = true,reminderAt=null)"))
  assertTrue(screen.contains("copy(settled=true,reminderAt=null)"))
  assertTrue(screen.contains("if(nowSettled)null else editDueDate.startOfDayMillis()"))
  assertTrue(screen.contains("Notifier.cancelDebtReminder(context,p.debt.id)"))
  assertTrue(screen.contains("Notifier.cancelDebtReminder(context,debt.id)"))
  assertTrue(notifier.contains("fun cancelDebtReminder(context: Context, debtId: Long)"))
  assertTrue(notifier.contains("cancel(TAG_DEBT_REMINDER, debtId.toInt())"))
  assertTrue(worker.contains("Notifier.TAG_DEBT_REMINDER"))
 }
}
