package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class VehicleVerticalPlateReorderGuardTest {
 @Test fun `whole vehicle card moves freely and order changes only when released`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("detectDragGesturesAfterLongPress","plateDragY+=drag.y","onDragEnd={","dragStartIndex","kotlin.math.round(plateDragY/step)","reordered.removeAt(startIndex)","reordered.add(targetIndex,vehicle.id)","setVehicleOrder(vehicleOrder)").forEach{assertTrue(it,screen.contains(it))}
  assertFalse("ordering must not swap while pointer merely crosses a small threshold",screen.contains("plateDragY>120f"))
  assertFalse("horizontal movement must not control ordering",screen.contains("plateDragX"))
  listOf("vehicleOrder: List<Long>","VEHICLE_ORDER","setVehicleOrder","\"vehicle_order\"").forEach{assertTrue(it,prefs.contains(it))}
 }
 @Test fun `drop can cross multiple cards and displaced cards settle slowly`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("cardHeights.values.average()","targetIndex","settlingVehicleIds=affected","displacedCardY.animateTo(0f,tween(760))","onGloballyPositioned","draggingPlateId==vehicle.id->plateDragY").forEach{assertTrue(it,screen.contains(it))}
  assertTrue(screen.contains("liveDisplacement")&&screen.contains("vehicleNeighborSettle")&&screen.contains("tween(620)"))
  assertTrue(screen.contains("vehicleListScroll.scrollBy(edge*vehicleScrollStepPx)"))
  assertTrue(screen.contains("cardWindowY[vehicle.id]")&&screen.contains("vehicleWindowHeightPx"))
  assertTrue(screen.contains("هر جای کارت را نگه دارید و آزادانه بالا یا پایین بکشید"))
 }
}
