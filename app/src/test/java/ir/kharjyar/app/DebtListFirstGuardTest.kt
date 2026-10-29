package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class DebtListFirstGuardTest {
 @Test fun `debts open as colored list with add chooser and separate entry page`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("ثبت طلب")&&s.contains("ثبت بدهی"));assertTrue(s.contains("DebtEntryPage"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"))}
}
