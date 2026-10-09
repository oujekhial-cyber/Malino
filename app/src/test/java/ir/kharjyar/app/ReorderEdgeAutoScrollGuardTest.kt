package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReorderEdgeAutoScrollGuardTest {
 @Test fun `all reorderable lists auto scroll at physical window edges`() {
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  val categories=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  val vehicles=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("horizontalScroll(rowState)","active.forEachIndexed","while(draggingId==account.id&&accountAutoScrollEdge==physicalEdge)","rowState.scrollBy(requested)","floatingX+=consumed*indexDirection","dragTargetIndex=(dragOriginIndex+kotlin.math.round",".zIndex(if (isDragging) 10f else 0f)").forEach{assertTrue(it,dashboard.contains(it))}
  listOf("verticalScroll(categoryListState)","orderedCategories.forEachIndexed","while(draggingCategoryId==category.id&&categoryAutoScrollEdge==edge)","categoryListState.scrollBy(edge*categoryScrollStepPx)","categoryDragY+=consumed","categoryDragTarget=(categoryDragStart+kotlin.math.round",".zIndex(if (dragging) 10f else 0f)").forEach{assertTrue(it,categories.contains(it))}
  listOf("while(draggingPlateId==vehicle.id&&vehicleAutoScrollEdge==edge)","vehicleListScroll.scrollBy(edge*vehicleScrollStepPx)","plateDragY+=consumed","dragTargetIndex=(dragStartIndex+kotlin.math.round",".zIndex(if(draggingPlateId==vehicle.id)10f else 0f)").forEach{assertTrue(it,vehicles.contains(it))}
 }
}
