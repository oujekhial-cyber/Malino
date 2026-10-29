package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CategoryLongPressReorderGuardTest {
 @Test fun `category reorder mirrors vehicle card motion without mutating lazy list during drag`() {
  val ui=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  listOf(
   "detectDragGesturesAfterLongPress",
   "categoryDragY += amount.y",
   "kotlin.math.round(categoryDragY / liveStep)",
   "categoryNeighborSettleLikeVehicle",
   "animationSpec = tween(620)",
   "verticalScroll(categoryListState)",
   ".zIndex(if (dragging) 10f else 0f)",
   "translationY = if (dragging) categoryDragY else neighborY",
   "ids.removeAt(startIndex)",
   "ids.add(targetIndex, category.id)",
   "setSortOrder(id, position)",
   "Icons.Filled.DragHandle",
   "لمس و نگه دارید"
  ).forEach{assertTrue(it,ui.contains(it))}
  assertFalse("SnapshotStateList mutation during pointer drag caused the crash",ui.contains("orderedCategories.removeAt(current)"))
  assertFalse(ui.contains("orderedCategories.add(current"))
 }
}
