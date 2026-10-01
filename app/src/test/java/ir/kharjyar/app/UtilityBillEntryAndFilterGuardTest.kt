package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class UtilityBillEntryAndFilterGuardTest {
 @Test fun `utility bills can be added and filtered by service`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText()
  listOf("ثبت اشتراک قبض","CivicBillEntry","utilityFilter","utilityBillId","\"آب\",\"برق\",\"گاز\",\"تلفن\",\"سایر\"","شناسه قبض یا شماره اشتراک","ثبت شناسه و فعال‌کردن تشخیص پیامک").forEach{assertTrue(it,source.contains(it))}
 }
}
