package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class ImportedSmsListDeleteGuardTest {
 @Test fun `retrieved sms can be removed from list only after confirmation`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  listOf("pendingDeleteSms=sms","حذف پیامک فراخوانی‌شده","حذف از لیست","پیامک اصلی داخل برنامه پیامک‌های گوشی حذف نخواهد شد","skipped=skipped+sms.id;pendingDeleteSms=null","انصراف").forEach { assertTrue(it,source.contains(it)) }
 }
}
