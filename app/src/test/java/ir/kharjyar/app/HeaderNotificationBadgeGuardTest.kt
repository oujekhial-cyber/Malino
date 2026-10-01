package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class HeaderNotificationBadgeGuardTest {
 @Test fun `left header bell shows combined unread message count and opens center`() {
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val center=File("src/main/java/ir/kharjyar/app/ui/screens/NotificationCenterScreen.kt").readText()
  listOf("notificationCount = reviewCount + civicMessages.count { !it.read } + stockDrafts.size","ModernNotificationButton","Brush.radialGradient","Icons.Filled.NotificationsActive","99+","go(\"notifications\")","composable(\"notifications\")").forEach{assertTrue(it,root.contains(it))}
  listOf("مرکز اعلان‌ها","پیامک‌های بانکی نیازمند بررسی","معاملات پیامکی بورس","خدمات شهروندی","همه را خواندم").forEach{assertTrue(it,center.contains(it))}
 }
}
