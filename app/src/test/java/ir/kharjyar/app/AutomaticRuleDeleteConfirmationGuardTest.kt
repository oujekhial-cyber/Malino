package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticRuleDeleteConfirmationGuardTest {
 @Test fun `automatic rule deletion is available only by swipe and requires confirmation`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  listOf(
   "pendingRuleDelete by remember",
   "onDelete = { pendingRuleDelete = r }",
   "title = { Text(\"حذف قانون خودکار\") }",
   "Text(\"حذف قانون\"",
   "pendingRuleDelete = null\n                    deleteRuleWithUndo(rule)",
   "removeOnDelete = false"
  ).forEach { assertTrue(it,source.contains(it)) }
  assertFalse(
   "قانون نباید دکمه حذف دائمی روی خود کارت داشته باشد",
   source.contains("TextButton(onClick = { pendingRuleDelete = r })")
  )
 }
}
