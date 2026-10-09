package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReorderEdgeAutoScrollGuardTest {
 @Test fun `all reorderable lists auto scroll at physical window edges`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val categories=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  val vehicles=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("horizontalScroll(rowState)","active.forEachIndexed","fingerWindowX","accountAutoScrollEdgePx","rowState.scrollBy(requested)","floatingX+=consumed*indexDirection",".zIndex(if (isDragging) 10f else 0f)").forEach{assertTrue(it,dashboard.contains(it))}
  listOf("verticalScroll(categoryListState)","orderedCategories.forEachIndexed","fingerWindowY","categoryWindowY[category.id]","categoryListState.scrollBy(edge*categoryScrollStepPx)","categoryDragY+=consumed",".zIndex(if (dragging) 10f else 0f)").forEach{assertTrue(it,categories.contains(it))}
  listOf("fingerWindowY","cardWindowY[vehicle.id]","vehicleListScroll.scrollBy(edge*vehicleScrollStepPx)","plateDragY+=consumed",".zIndex(if(draggingPlateId==vehicle.id)10f else 0f)").forEach{assertTrue(it,vehicles.contains(it))}
 }
}
