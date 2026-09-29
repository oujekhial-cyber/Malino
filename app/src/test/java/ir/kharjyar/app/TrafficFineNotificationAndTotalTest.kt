package ir.kharjyar.app
import ir.kharjyar.app.core.sms.TrafficFineParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class TrafficFineNotificationAndTotalTest {
 @Test fun `fine amount parser normalizes rial and toman`() {
  assertEquals(1_500_000L,TrafficFineParser.amountRial("جریمه به مبلغ ۱،۵۰۰،۰۰۰ ریال برای پلاک ثبت شد"))
  assertEquals(25_000_000L,TrafficFineParser.amountRial("مبلغ جریمه: 2,500,000 تومان"))
 }
 @Test fun `new unique fine sends notification`() {
  val receiver=File("src/main/java/ir/kharjyar/app/receiver/SmsReceiver.kt").readText()
  assertTrue(receiver.contains("CivicMessageKind.TRAFFIC_FINE && civicMessageId > 0"))
  assertTrue(receiver.contains("Notifier.notifyTrafficFine"))
  val notifier=File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
  assertTrue(notifier.contains("جریمه راهنمایی و رانندگی"))
  assertTrue(notifier.contains("\"civicCenter\""))
 }
 @Test fun `vehicle card displays total fine amount for its plate`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(source.contains("val totalFineRial=allFines(vehicle).mapNotNull"))
  assertTrue(source.contains("جمع مبلغ جریمه‌های این پلاک"))
 }
}
