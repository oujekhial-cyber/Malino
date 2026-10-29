package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class WidgetWeatherRefreshGuardTest {
 @Test fun `widget weather refresh is battery aware and tracks day night`(){val app=File("src/main/java/ir/kharjyar/app/KharjYarApp.kt").readText();val weather=File("src/main/java/ir/kharjyar/app/weather/WeatherService.kt").readText();assertTrue(app.contains("WidgetUpdater.syncWeatherWork(this)")&&app.contains("WidgetUpdater.hasWidgets(this)"));assertTrue(weather.contains("weather_code,is_day"));assertTrue(weather.contains("putBoolean(\"is_day\""));assertTrue(weather.contains("force: Boolean = false"));val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText();assertTrue(widget.contains("!weather.isDay")&&widget.contains("weather_moon"));assertTrue(widget.contains("setRequiresBatteryNotLow(true)")&&widget.contains("6, java.util.concurrent.TimeUnit.HOURS"));val xml=File("src/main/res/xml/widget_info.xml").readText();assertTrue(xml.contains("android:updatePeriodMillis=\"0\""))}
 @Test fun `fresh install widget defaults to neon blossom split`(){val s=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText();assertTrue(s.contains("widgetLayout: WidgetLayout = WidgetLayout.SPLIT"));assertTrue(s.contains("widgetPalette: Palette = Palette.SAKURA"));assertTrue(s.contains("?: Palette.SAKURA"))}
}
