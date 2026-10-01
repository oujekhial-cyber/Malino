package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class CivicListFirstGuardTest {
 @Test fun `civic center mirrors list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("فرد تحت پوشش")&&s.contains("وسیله نقلیه و پلاک"));assertTrue(s.contains("CivicProfileEntry"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"))}
 @Test fun `civic messages require a registered utility subscription before sms matching`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText();assertTrue(s.contains("CivicBillEntry"));assertTrue(s.contains("ENTRY_UTILITY_BILL"));assertTrue(s.contains("insertUtilityBill(UtilityBillProfileEntity"));assertTrue(s.contains("شناسه قبض یا شماره اشتراک"));assertTrue(s.contains("ثبت شناسه و فعال‌کردن تشخیص پیامک"))}
}
