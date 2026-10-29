package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ChecksListFirstGuardTest {
 @Test fun `checks mirror debt list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("ثبت چک دریافت‌شده")&&s.contains("ثبت چک صادرشده"));assertTrue(s.contains("CheckEntryPage"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"))}
}
