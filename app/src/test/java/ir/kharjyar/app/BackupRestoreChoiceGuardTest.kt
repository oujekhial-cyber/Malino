package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class BackupRestoreChoiceGuardTest {
 @Test fun `restore offers replace merge and safety backup choices`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/BackupScreen.kt").readText()
  listOf("حذف اطلاعات موجود و بازیابی","ادغام با اطلاعات موجود","ابتدا بکاپ از اطلاعات موجود","saveSafetyBackup.launch","manager.merge(payload)","manager.restore(payload)").forEach{assertTrue(it,s.contains(it))}
  val safety=s.substring(s.indexOf("val saveSafetyBackup"),s.indexOf("val openBackup"))
  assertTrue(safety.indexOf("manager.createBackup(null)")<safety.indexOf("manager.restore(payload)"))
 }
 @Test fun `operation result message disappears after four seconds`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/BackupScreen.kt").readText()
  assertTrue(s.contains("LaunchedEffect(message)"))
  assertTrue(s.contains("delay(4_000)"))
  assertTrue(s.contains("if (message == shown) message = null"))
 }
}
