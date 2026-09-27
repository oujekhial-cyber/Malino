package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class CompleteBackupGuardTest {
 @Test fun `backup includes all financial personal civic and private image data but excludes templates`(){val m=File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText();listOf("debtPeople =","checks =","notes =","loans =","assets =","reminders =","profiles =","coveredPeople =","vehicles =","oilServices =","civicMessages =","privateFiles =").forEach{assertTrue("missing $it",m.contains(it))};assertTrue(m.contains("templates = emptyList()"));assertFalse(m.contains("payload.templates.forEach"));assertFalse(m.contains("DELETE FROM sms_templates"))}
}
