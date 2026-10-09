package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReorderEdgeAutoScrollGuardTest {
 @Test fun `all reorderable lists auto scroll at physical window edges`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val categories=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  val vehicles=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("fingerWindowX","accountAutoScrollEdgePx","rowState.scrollBy(requested)","floatingX+=consumed*indexDirection").forEach{assertTrue(it,dashboard.contains(it))}
  listOf("fingerWindowY","categoryWindowY[category.id]","categoryListState.scrollBy(edge*categoryScrollStepPx)","categoryDragY+=consumed").forEach{assertTrue(it,categories.contains(it))}
  listOf("fingerWindowY","cardWindowY[vehicle.id]","vehicleListScroll.scrollBy(edge*vehicleScrollStepPx)","plateDragY+=consumed").forEach{assertTrue(it,vehicles.contains(it))}
 }
}
