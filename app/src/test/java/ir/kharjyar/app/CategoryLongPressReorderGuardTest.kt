package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class CategoryLongPressReorderGuardTest {
 @Test fun `categories persist long press vertical reorder`() {
  val ui=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
  val dao=File("src/main/java/ir/kharjyar/app/data/db/Daos.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  listOf("detectDragGesturesAfterLongPress","orderedCategories.removeAt","orderedCategories.add","setSortOrder(item.id,position)","Icons.Filled.DragHandle","لمس و نگه دارید").forEach{assertTrue(it,ui.contains(it))}
  assertTrue(entity.contains("val sortOrder: Int = 0"))
  assertTrue(dao.contains("ORDER BY sortOrder, name")&&dao.contains("suspend fun setSortOrder"))
  assertTrue(db.contains("version = 19")&&db.contains("MIGRATION_17_18"))
 }
}
