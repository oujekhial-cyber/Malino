package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoAutomaticRulePromptAfterTransactionGuardTest {
 @Test fun `saving a transaction never interrupts with automatic rule prompt`() {
  val edit=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionEditScreen.kt").readText()
  val categories=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
  assertFalse(edit.contains("suggestRule"))
  assertFalse(edit.contains("ساخت قانون خودکار؟"))
  assertFalse(edit.contains("بله، بساز"))
  assertTrue(edit.contains("nav.popBackStack()"))
  assertTrue("Rule management must remain available in its dedicated screen",categories.contains("قانون خودکار جدید"))
 }
}
