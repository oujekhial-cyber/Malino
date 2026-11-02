package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class HeaderNotificationBadgeGuardTest{@Test fun badge(){val r=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();val c=File("src/main/java/ir/kharjyar/app/ui/screens/NotificationCenterScreen.kt").readText();assertTrue(r.contains("height(21.dp)"));assertTrue(r.contains("softWrap=false"));assertFalse(r.contains("size(38.dp).clip(CircleShape).clickable"));assertTrue(c.contains("اقدام‌های آماده"));assertTrue(c.contains("ActionCard"))}}
