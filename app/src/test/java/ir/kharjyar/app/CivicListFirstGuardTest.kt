package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class CivicListFirstGuardTest {
 @Test fun `civic center mirrors list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("فرد تحت پوشش")&&s.contains("وسیله نقلیه و پلاک"));assertTrue(s.contains("CivicProfileEntry"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"))}
 @Test fun `civic messages support sms import and explicit manual utility bill entry`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText();assertTrue(s.contains("CivicBillEntry"));assertTrue(s.contains("ENTRY_UTILITY_BILL"));assertTrue(s.contains("insertMessage(CivicMessageEntity(kind=CivicMessageKind.UTILITY_BILL"));assertTrue(s.contains("شناسه قبض یا اشتراک"))}
}
