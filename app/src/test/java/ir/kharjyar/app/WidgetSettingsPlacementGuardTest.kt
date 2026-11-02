package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class WidgetSettingsPlacementGuardTest {
 @Test fun `widget settings is inside templates appearance page rather than main settings list`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val mainMenu=source.substringAfter("if (section == null)").substringBefore("// ---------- تم ----------")
  val appearance=source.substringAfter("if (section == \"appearance\")").substringBefore("// ---------- حساب پیش‌فرض ----------")
  assertFalse(mainMenu.contains("widgetSettings"))
  assertTrue(appearance.contains("NavRow(\"تنظیمات ویجت\") { nav.navigate(\"widgetSettings\") }"))
  assertTrue(appearance.indexOf("ظاهر و تم")<appearance.indexOf("تنظیمات ویجت"))
 }
}
