package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class VehicleVerticalPlateReorderGuardTest {
 @Test fun `plates reorder only by vertical long press drag and persist`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("detectDragGesturesAfterLongPress","plateDragY+=drag.y","plateDragY>70f","plateDragY< -70f","setVehicleOrder(vehicleOrder)","translationY=if(draggingPlateId==vehicle.id)").forEach{assertTrue(it,screen.contains(it))}
  assertFalse("horizontal delta must not control plate ordering",screen.contains("plateDragX"))
  listOf("vehicleOrder: List<Long>","VEHICLE_ORDER","setVehicleOrder","\"vehicle_order\"").forEach{assertTrue(it,prefs.contains(it))}
 }
 @Test fun `drag handle is the rendered plate rather than whole swipe card`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(screen.contains("VehiclePlateBadge(vehicle.plate,Modifier.graphicsLayer"))
  assertTrue(screen.contains("خود پلاک را نگه دارید و فقط بالا یا پایین بکشید"))
 }
}
