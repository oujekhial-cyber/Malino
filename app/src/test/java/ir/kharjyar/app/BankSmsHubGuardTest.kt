package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class BankSmsHubGuardTest {
 @Test fun `bank sms hub defaults to unchecked and contains import tab`(){val hub=File("src/main/java/ir/kharjyar/app/ui/screens/BankSmsHubScreen.kt").readText();assertTrue(hub.contains("initialTab: Int = 0"));assertTrue(hub.contains("پیامک‌های بررسی‌نشده"));assertTrue(hub.contains("ورود پیامک‌های بانکی"));assertTrue(hub.contains("ReviewScreen")&&hub.contains("SmsHistoryImportScreen"))}
 @Test fun `bank import is absent from drawer and review opens hub`(){val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertFalse(root.contains("DrawerEntry(\"smsHistory\""));assertTrue(root.contains("composable(\"review\") { BankSmsHubScreen"))}
}
