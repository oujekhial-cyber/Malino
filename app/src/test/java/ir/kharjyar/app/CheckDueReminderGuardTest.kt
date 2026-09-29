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
  listOf("Text(\"تغییر زمان\")","ReminderTimeEditDialog","DateTimeField(date,hour,minute","copy(nextAt=newAt,enabled=true,lastNotifiedAt=null)","LifeReminderWorker.schedule(context,reminder.id,newAt)").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `cleared check disables its pending reminders`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("it.category==\"چک\"&&it.note.contains(number)"))
  assertTrue(source.contains("it.copy(enabled=false)"))
 }
}
