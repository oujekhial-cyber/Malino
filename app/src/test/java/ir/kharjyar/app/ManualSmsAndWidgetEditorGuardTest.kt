package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManualSmsAndWidgetEditorGuardTest {
    @Test fun `manual and quick entry expose bank sms analyzer`() {
        val manual = File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
        val quick = File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
        assertTrue(manual.contains("smsPaste")); assertTrue(quick.contains("smsPaste"))
        assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/SmsPasteScreen.kt").readText().contains("Extractor.autoExtract"))
    }

    @Test fun `weather uses manual city and has condition icons without location`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val weather = File("src/main/java/ir/kharjyar/app/weather/WeatherService.kt").readText()
        assertFalse(manifest.contains("ACCESS_COARSE_LOCATION"))
        assertFalse(manifest.contains("ACCESS_FINE_LOCATION"))
        assertTrue(weather.contains("geocoding-api.open-meteo.com"))
        assertTrue(weather.contains("☁️"))
    }

    @Test fun `widget editor has touch grid and independent visibility controls`() {
        val screen = File("src/main/java/ir/kharjyar/app/ui/screens/WidgetSettingsScreen.kt").readText()
        assertTrue(screen.contains("جای‌گذاری لمسی اجزای ویجت"))
        assertTrue(screen.contains("setWidgetTitleAlign"))
        assertTrue(screen.contains("setWidgetClockVAlign"))
        assertTrue(screen.contains("setWidgetShowTitle"))
        assertTrue(screen.contains("setWidgetShowClock"))
        assertTrue(screen.contains("setWidgetShowDates"))
    }
}
