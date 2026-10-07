package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OnlineBackupComingSoonGuardTest {
    @Test fun `online backup card is visible and explicitly marked coming soon`() {
        val source=File("src/main/java/ir/kharjyar/app/ui/screens/BackupScreen.kt").readText()
        listOf("پشتیبان‌گیری آنلاین","بکاپ امن روی حساب کاربری","همگام‌سازی رمزگذاری‌شده بین دستگاه‌ها","فعال‌سازی پشتیبان‌گیری آنلاین","به زودی افزوده خواهد شد","Icons.Filled.CloudSync").forEach { assertTrue(it,source.contains(it)) }
        assertTrue(source.contains("showSavedMessage(context, \"به زودی افزوده خواهد شد\")"))
        assertTrue(source.contains("if (skin.dark)") && source.contains("Color.Black.copy(alpha = .52f)"))
        assertTrue(source.contains("Color.White.copy(alpha = .92f)") && source.contains("onlineBackupGlass"))
    }
}
