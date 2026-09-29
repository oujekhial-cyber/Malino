package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class VehicleListFirstGuardTest {
 @Test fun `vehicles mirror debt list first interaction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText();assertTrue(s.contains("FloatingActionButton"));assertTrue(s.contains("وسیله نقلیه جدید")&&s.contains("سرویس و تعویض روغن"));assertTrue(s.contains("VehicleEntryPage"));assertTrue(s.contains("Color(0xFF1B8F52)")&&s.contains("Color(0xFFD33B45)"));assertTrue(s.contains("Alignment.BottomEnd"));assertTrue(s.contains("onDone()"))}
}
