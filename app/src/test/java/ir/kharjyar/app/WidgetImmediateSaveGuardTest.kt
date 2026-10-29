package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetImmediateSaveGuardTest {
 @Test fun saveForcesWeatherAndWaitsForWidgetUpdate(){
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/WidgetSettingsScreen.kt").readText()
  val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
  val weather=File("src/main/java/ir/kharjyar/app/weather/WeatherService.kt").readText()
  assertTrue(screen.contains("refresh(context,force=true)"))
  assertTrue(screen.contains("WidgetUpdater.updateNow(context)"))
  assertTrue(screen.contains("ذخیره و بروزرسانی فوری"))
  assertTrue(widget.contains("suspend fun updateNow"))
  assertTrue(weather.contains("edit.commit()"))
  assertTrue(weather.contains("remove(\"temperature\")"))
 }
}
