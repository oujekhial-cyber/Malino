package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class VehicleVerticalPlateReorderGuardTest {
 @Test fun `whole vehicle cards reorder only by vertical long press drag and persist`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("detectDragGesturesAfterLongPress","plateDragY+=drag.y","plateDragY>120f","plateDragY< -120f","setVehicleOrder(vehicleOrder)","draggingPlateId==vehicle.id->plateDragY").forEach{assertTrue(it,screen.contains(it))}
  assertFalse("horizontal delta must not control vehicle ordering",screen.contains("plateDragX"))
  listOf("vehicleOrder: List<Long>","VEHICLE_ORDER","setVehicleOrder","\"vehicle_order\"").forEach{assertTrue(it,prefs.contains(it))}
 }
 @Test fun `drag modifier wraps whole swipe card and displaced card settles gently`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(screen.contains("SwipeActionRow(onDelete={pendingDelete=vehicle}"))
  assertTrue(screen.contains("modifier=Modifier.graphicsLayer"))
  assertTrue(screen.contains("displacedCardY.animateTo(0f,tween(480))"))
  assertTrue(screen.contains("scaleX=if(draggingPlateId==vehicle.id)1.018f"))
  assertTrue(screen.contains("هر جای کارت را نگه دارید و آزادانه بالا یا پایین بکشید"))
 }
}
