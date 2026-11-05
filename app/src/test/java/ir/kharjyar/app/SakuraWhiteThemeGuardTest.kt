package ir.kharjyar.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.data.prefs.ThemeMode
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class SakuraWhiteThemeGuardTest {
    @Test fun `new installations use light sakura and existing saved choices remain readable`() {
        assertEquals(Palette.SAKURA, AppSettings().palette)
        assertEquals(ThemeMode.LIGHT, AppSettings().themeMode)
        val prefs = File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
        assertTrue(prefs.contains("palette = enumOf(p[Keys.PALETTE], Palette.SAKURA)"))
        assertTrue(prefs.contains("themeMode = enumOf(p[Keys.THEME], ThemeMode.LIGHT)"))
        assertTrue(prefs.contains("setPalette(v: Palette)"))
        assertTrue(prefs.contains("setThemeMode(v: ThemeMode)"))
    }
    @Test fun `light sakura is white with accessible blossom pink controls`() {
        val theme = File("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt").readText()
        val light = theme.substringAfter("private val SakuraLightSkin =").substringBefore("// ===")
        assertTrue(light.contains("backgroundColors = listOf(Color.White, Color.White)"))
        assertTrue(light.contains("backdropBlobs = emptyList()"))
        assertTrue(light.contains("accent = Color(0xFFD41471)"))
        assertTrue(theme.contains("Palette.SAKURA -> if(wantsDark) SakuraSkin else SakuraLightSkin"))
        val pink = Color(0xFFD41471)
        assertTrue((Color.White.luminance() + 0.05f) / (pink.luminance() + 0.05f) >= 4.5f)
        val root = File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
        val dashboard = File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        assertTrue(root.contains("if (sakuraSelected) Color.White else skin.accent"))
        assertTrue(dashboard.contains("if (sakuraLight) listOf(skin.accent, skin.accent)"))
        assertTrue(dashboard.contains("if (sakuraLight) Color.White else skin.onBackdrop"))
    }
}
