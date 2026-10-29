package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ReminderListFirstGuardTest {
 @Test fun `reminders mirror debt list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("یادآور یک‌باره")&&s.contains("یادآور تکرارشونده"));assertTrue(s.contains("ReminderEntryPage"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"));assertTrue(s.contains("onDone()"))}
}
