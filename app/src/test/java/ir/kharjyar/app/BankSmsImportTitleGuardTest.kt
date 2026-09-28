package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class BankSmsImportTitleGuardTest {
 @Test fun `bank sms import uses requested retrieval title`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  assertTrue(source.contains("Text(\"فراخوانی پیامک‌های مالی بانک\",style=MaterialTheme.typography.headlineSmall)"))
 }
}
