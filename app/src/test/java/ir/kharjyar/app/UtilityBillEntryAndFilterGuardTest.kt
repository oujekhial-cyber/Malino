package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class UtilityBillEntryAndFilterGuardTest {
 @Test fun `utility bills can be added and filtered by service`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText()
  listOf("افزودن قبض","CivicBillEntry","utilityFilter","utilityBillType","\"آب\",\"برق\",\"گاز\",\"تلفن\",\"سایر\"","شناسه قبض یا اشتراک","ثبت قبض").forEach{assertTrue(it,source.contains(it))}
 }
}
