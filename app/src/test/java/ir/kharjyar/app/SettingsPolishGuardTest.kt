package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SettingsPolishGuardTest {
 @Test fun `screenshot prevention defaults off`() { val s=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText();assertTrue(s.contains("secureScreen: Boolean = false"));assertTrue(s.contains("p[Keys.SECURE_SCREEN] ?: false")) }
 @Test fun `permissions use enable disable switches`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText();assertTrue(s.contains("onDisable"));assertTrue(s.contains("ACTION_APPLICATION_DETAILS_SETTINGS"));assertTrue(s.contains("Switch(checked = granted")) }
 @Test fun `widget weather follows dates and uses drawable icons`() { val s=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText();assertTrue(s.contains("weather_sun"));assertTrue(s.contains("if (settings.widgetShowDates)"));assertFalse(s.contains("if (settings.widgetShowWeather)")) }
 @Test fun `requested Persian labels are corrected`() { assertTrue(File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText().contains("دستیار دخل و خرج شما"));assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText().contains("SettingsMenuRow(\"ویجت\")")) }
}
