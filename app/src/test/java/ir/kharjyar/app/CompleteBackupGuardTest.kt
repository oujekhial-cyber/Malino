package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class CompleteBackupGuardTest {
 @Test fun `backup includes all financial personal civic template and private image data`(){val m=File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText();listOf("debtPeople =","checks =","notes =","loans =","assets =","reminders =","profiles =","coveredPeople =","vehicles =","oilServices =","civicMessages =","privateFiles =","templates = repo.templateDao.allOnce()","payload.templates.forEach").forEach{assertTrue("missing $it",m.contains(it))}}
}
