package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class MarketPopupAndFreshDefaultsGuardTest {
 @Test fun `market pulse is centered in app header and uses slow dismissible popup`() {
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(root.contains("Box(Modifier.align(Alignment.Center)){LiveMarketGlass(settings.moneyUnit)}"))
  listOf("Popup(alignment=Alignment.TopCenter","fillMaxSize().clickable(onClick={closePanel()})","expandVertically","tween(650)","shrinkVertically","kotlinx.coroutines.delay(550)").forEach{assertTrue(it,dashboard.contains(it))}
 }
 @Test fun `fresh install defaults are day light and neon blossom split widget at fifty percent`() {
  val settings=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("themeMode: ThemeMode = ThemeMode.LIGHT","palette: Palette = Palette.MINIMAL_DAY","widgetLayout: WidgetLayout = WidgetLayout.SPLIT","widgetOpacity: Int = 50","widgetPalette: Palette = Palette.SAKURA","p[Keys.WIDGET_OPACITY] ?: 50").forEach{assertTrue(it,settings.contains(it))}
 }
}
