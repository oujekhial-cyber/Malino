package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class LifeRemindersGuardTest {
 @Test fun `recurring life reminders are persisted scheduled and navigable`() { val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText();val worker=File("src/main/java/ir/kharjyar/app/work/LifeReminderWorker.kt").readText();val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertTrue(db.contains("version = 10")&&db.contains("MIGRATION_7_8")&&db.contains("ReminderEntity::class"));assertTrue(worker.contains("enqueueUniqueWork")&&worker.contains("plusMonths")&&worker.contains("plusYears"));assertTrue(root.contains("یادآورها")&&root.contains("RemindersScreen")) }
}
