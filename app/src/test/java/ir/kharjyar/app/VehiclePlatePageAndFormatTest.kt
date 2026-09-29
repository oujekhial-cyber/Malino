package ir.kharjyar.app
import ir.kharjyar.app.core.sms.IranianPlateMatcher
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
class VehiclePlatePageAndFormatTest {
 @Test fun `iran plate accepts arbitrary separators between typed sections`() {
  val expected="86س673ایران73"
  assertEquals(expected,IranianPlateMatcher.canonical("86 س 673 ایران 73"))
  assertEquals(expected,IranianPlateMatcher.canonical("۸۶-س.۶۷۳_ایران،۷۳"))
  assertEquals(expected,IranianPlateMatcher.canonical("86/س\\673---ایران__73"))
  assertEquals("12♿345ایران11",IranianPlateMatcher.canonical("۱۲ ♿ ۳۴۵ ایران ۱۱"))
  assertNull(IranianPlateMatcher.canonical("86-673-ایران-73"))
 }
 @Test fun `plate list opens dedicated vehicle page`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("selectedVehicleId=vehicle.id","VehiclePlateDetail","بازگشت به فهرست پلاک‌ها","جرائم و پیامک‌های این پلاک","سوابق سرویس","IranianPlateInput","پلاک استاندارد:","canonicalPlate?:return@launch").forEach{assertTrue(it,source.contains(it))}
 }
}
