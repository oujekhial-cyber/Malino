package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SystemStatusBarIconContrastGuardTest {
    private val theme=File("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt").readText()

    @Test fun `official Android status icons follow effective app lightness`() {
        assertTrue(theme.contains("WindowCompat.getInsetsController(window, view)"))
        assertTrue(theme.contains("isAppearanceLightStatusBars = !wantsDark"))
        assertTrue(theme.contains("isAppearanceLightNavigationBars = !wantsDark"))
    }

    @Test fun `system mode and explicit modes all feed the same icon decision`() {
        assertTrue(theme.contains("ThemeMode.SYSTEM -> isSystemInDarkTheme()"))
        assertTrue(theme.contains("ThemeMode.DARK -> true"))
        assertTrue(theme.contains("ThemeMode.LIGHT -> false"))
        assertTrue(theme.indexOf("isAppearanceLightStatusBars = !wantsDark") > theme.indexOf("val wantsDark"))
    }
}
