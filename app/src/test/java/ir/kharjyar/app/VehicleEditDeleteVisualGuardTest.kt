package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class VehicleEditDeleteVisualGuardTest {
 @Test fun `vehicle cards support edit and confirmed delete with richer plate visual`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val entities=File("src/main/java/ir/kharjyar/app/data/db/CivicEntities.kt").readText()
  listOf("editingVehicleId","ویرایش وسیله نقلیه","ذخیره تغییرات","حذف وسیله نقلیه؟","Text(\"تأیید\")","VehiclePlateBadge","Brush.linearGradient","dao.updateVehicle").forEach{assertTrue(it,screen.contains(it))}
  listOf("updateVehicle","detachVehicleMessages","deleteVehicleServices","deleteVehicle").forEach{assertTrue(it,entities.contains(it))}
 }
}
