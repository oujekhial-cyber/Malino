package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class LoansListFirstGuardTest {
 @Test fun `loans mirror debt list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("ثبت وام پرداختی")&&s.contains("ثبت وام دریافتی"));assertTrue(s.contains("LoanEntryPage"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"));assertTrue(s.contains("plusMonths"))}
}
