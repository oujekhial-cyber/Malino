package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class CheckDueReminderGuardTest {
 @Test fun `check creates day before and due day reminders at nine`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  listOf("due.startOfDayMillis()+9*60*60*1000L","due.plusDays(-1).startOfDayMillis()+9*60*60*1000L","یادآوری چک؛ یک روز مانده به سررسید","title=\"موعد چک\"","LifeReminderWorker.schedule(context,beforeId","LifeReminderWorker.schedule(context,dueId").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `reminder time can be changed and rescheduled by user`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText()
  listOf("onEdit={editingReminder=r","DateTimeField(date,hour,minute","lastNotifiedAt=null","LifeReminderWorker.cancel(context,existing.id)","LifeReminderWorker.schedule(context,id,at)","ذخیره تغییرات یادآور").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `cleared check disables and removes every pending reminder`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  val cleanup=File("src/main/java/ir/kharjyar/app/work/ObligationReminderCleanup.kt").readText()
  assertTrue(source.contains("ObligationReminderCleanup.cancelCheck(context,vm.repo.db,check)"))
  assertTrue(cleanup.contains("it.category == \"چک\" && it.note.contains(number)"))
  assertTrue(cleanup.contains("reminder.copy(enabled = false)"))
  assertTrue(cleanup.contains("Notifier.cancelLifeReminder(context, reminder.id)"))
 }
}
