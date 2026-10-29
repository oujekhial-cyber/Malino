package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class AppAndWidgetThemeSeparationGuardTest {
 @Test fun `app and widget themes use independent preference keys and setters`() {
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  val appSetter=prefs.substringAfter("suspend fun setPalette").substringBefore("suspend fun setMoneyUnit")
  val widgetSetter=prefs.substringAfter("suspend fun setWidgetPalette").substringBefore("suspend fun setWidgetTitleSize")
  assertTrue(appSetter.contains("Keys.PALETTE"));assertFalse(appSetter.contains("WIDGET_PALETTE"))
  assertTrue(widgetSetter.contains("Keys.WIDGET_PALETTE"));assertFalse(widgetSetter.contains("Keys.PALETTE]"))
  val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
  assertTrue(widget.contains("skinOf(settings.widgetPalette)"));assertFalse(widget.contains("skinOf(settings.palette)"))
 }
 @Test fun `fresh startup defaults are system minimal day and sakura split`() {
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("themeMode: ThemeMode = ThemeMode.SYSTEM","palette: Palette = Palette.MINIMAL_DAY","widgetPalette: Palette = Palette.SAKURA","widgetLayout: WidgetLayout = WidgetLayout.SPLIT","ThemeMode.SYSTEM)","Palette.MINIMAL_DAY)","Palette.SAKURA)","WidgetLayout.SPLIT)").forEach{assertTrue(it,prefs.contains(it))}
 }
}
