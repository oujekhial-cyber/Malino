package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class HeaderNotificationBadgeGuardTest {
 @Test fun `left header bell shows combined unread message count and opens center`() {
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val center=File("src/main/java/ir/kharjyar/app/ui/screens/NotificationCenterScreen.kt").readText()
  listOf("notificationCount = reviewCount + civicMessages.count { !it.read } + stockDrafts.size","ModernNotificationButton","rememberInfiniteTransition","notificationBellRotation","durationMillis=3600","Icons.Filled.NotificationsActive","size(if(active)20.dp else 18.dp)","99+","go(\"notifications\")","composable(\"notifications\")").forEach{assertTrue(it,root.contains(it))}
  val bell=root.substring(root.indexOf("private fun ModernNotificationButton"),root.indexOf("@OptIn(ExperimentalMaterial3Api::class)"))
  assertTrue(!bell.contains(".border("))
  listOf("مرکز اعلان‌ها","پیامک‌های بانکی نیازمند بررسی","معاملات پیامکی بورس","خدمات شهروندی","همه را خواندم").forEach{assertTrue(it,center.contains(it))}
 }
}
