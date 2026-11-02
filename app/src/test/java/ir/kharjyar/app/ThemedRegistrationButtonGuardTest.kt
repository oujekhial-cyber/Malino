package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ThemedRegistrationButtonGuardTest {
 @Test fun `registration buttons use theme fill with only semantic colored border`(){listOf("DebtsScreen.kt","ChecksScreen.kt","LoansScreen.kt","RemindersScreen.kt","CivicCenterScreen.kt","VehiclesScreen.kt").forEach{name->val s=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText();assertTrue(name,s.contains("BorderStroke(1.5.dp"));assertFalse(name,s.contains("ButtonDefaults.buttonColors(containerColor=accent)"));assertFalse(name,s.contains("ButtonDefaults.buttonColors(containerColor=Color(0xFF1B8F52))"));assertFalse(name,s.contains("ButtonDefaults.buttonColors(containerColor=Color(0xFFD33B45))"))}}
}
