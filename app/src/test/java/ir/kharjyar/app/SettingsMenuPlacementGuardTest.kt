package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class SettingsMenuPlacementGuardTest {
 @Test fun `categories follows number display and info rows are independent cards`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val numbers=source.indexOf("SettingsMenuRow(\"نمایش اعداد و واحد پول\")")
  val categories=source.indexOf("SettingsMenuRow(\"دسته‌بندی‌ها\")")
  val permissions=source.indexOf("SettingsMenuRow(\"مجوزها و اعلان‌ها\")")
  assertTrue(numbers>=0&&categories>numbers&&categories<permissions)
  listOf("پیامک‌های تبلیغاتی","حریم خصوصی","درباره ما").forEach{label->assertTrue(source.contains("SettingsMenuRow(\"$label\")"))}
  assertFalse(source.contains("SettingsInfoGroup("))
  assertFalse(source.contains("SettingsGroupedItem("))
 }
}
