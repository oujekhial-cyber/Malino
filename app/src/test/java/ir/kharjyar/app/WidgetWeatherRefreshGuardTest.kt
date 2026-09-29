package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class WidgetWeatherRefreshGuardTest {
 @Test fun `widget weather refreshes every two hours and tracks day night`(){val app=File("src/main/java/ir/kharjyar/app/KharjYarApp.kt").readText();assertTrue(app.contains("WeatherWidgetWorker")&&app.contains("widget-weather-refresh")&&app.contains("(2, java.util.concurrent.TimeUnit.HOURS)"));val weather=File("src/main/java/ir/kharjyar/app/weather/WeatherService.kt").readText();assertTrue(weather.contains("weather_code,is_day"));assertTrue(weather.contains("putBoolean(\"is_day\""));assertTrue(weather.contains("force: Boolean = false"));val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText();assertTrue(widget.contains("!weather.isDay")&&widget.contains("weather_moon"))}
 @Test fun `fresh install widget defaults to neon blossom split`(){val s=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText();assertTrue(s.contains("widgetLayout: WidgetLayout = WidgetLayout.SPLIT"));assertTrue(s.contains("widgetPalette: Palette = Palette.SAKURA"));assertTrue(s.contains("?: Palette.SAKURA"))}
}
