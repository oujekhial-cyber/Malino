package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class AppFontScaleGuardTest {
 @Test fun `appearance offers persistent global text and number size slider`(){
  val settings=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val theme=File("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt").readText()
  listOf("appFontScale: Int = 100","APP_FONT_SCALE","setAppFontScale","coerceIn(85, 130)").forEach{assertTrue(it,settings.contains(it))}
  listOf("اندازه متن و اعداد","Slider(","valueRange = 85f..130f","اندازه استاندارد").forEach{assertTrue(it,screen.contains(it))}
  assertTrue(theme.contains("LocalDensity provides scaledDensity"))
  assertTrue(theme.contains("systemDensity.fontScale *"))
 }
}
