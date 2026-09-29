package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class BackupRestoreChoiceGuardTest {
 @Test fun `restore asks between safety backup replacement and merge`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/BackupScreen.kt").readText();assertTrue(s.contains("بکاپ از فعلی و جایگزینی"));assertTrue(s.contains("ادغام با اطلاعات موجود"));assertTrue(s.contains("saveSafetyBackup.launch"));assertTrue(s.indexOf("manager.createBackup(null)")<s.indexOf("manager.restore(payload)"))}
 @Test fun `merge preserves current records and deduplicates sms by fingerprint`(){val s=File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText();val merge=s.substringAfter("suspend fun merge(payload").substringBefore("suspend fun restore(payload");assertTrue(merge.contains("backup + now"));assertTrue(merge.contains("smsQueue = merged(payload.smsQueue,current.smsQueue){it.fingerprint}"));assertTrue(merge.contains("civicMessages = merged(payload.civicMessages,current.civicMessages){it.fingerprint}"));assertTrue(merge.contains("settings = current.settings"))}
}
