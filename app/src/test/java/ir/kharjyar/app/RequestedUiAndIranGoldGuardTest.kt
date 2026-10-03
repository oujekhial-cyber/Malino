package ir.kharjyar.app
import ir.kharjyar.app.assets.IranianGoldCalculator
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class RequestedUiAndIranGoldGuardTest {
 @Test fun `iran gold formula taxes only wage and profit`(){assertEquals(11_947L,IranianGoldCalculator.newGold(1.0,10_000,10.0,7.0,10.0))}
 @Test fun `drawer is compact and labels are explicit`(){val s=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertFalse(s.contains("DrawerEntry(\"quickAdd\""));assertFalse(s.contains("DrawerEntry(\"notes\""));assertTrue(s.contains("DrawerEntry(\"backup\", \"پشتیبان‌گیری و بازیابی\""));assertTrue(s.indexOf("DrawerEntry(\"backup\"")<s.indexOf("DrawerEntry(\"settings\""));assertTrue(s.contains("پیامک‌های بررسی‌نشده"));assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/BankSmsHubScreen.kt").readText().contains("ورود پیامک‌های بانکی"))}
 @Test fun `home deletion is confirmed`(){assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText().contains("آیا از حذف این تراکنش مطمئن هستید؟"))}
}
