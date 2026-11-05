package ir.kharjyar.app

import ir.kharjyar.app.core.date.persianYearChoices
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class DropdownAndPermanentNeonGuardTest {
    @Test fun `years and months appear with latest at top and earliest at bottom`() {
        val years = persianYearChoices()
        assertEquals(1450, years.first())
        assertEquals(1400, years.last())
        val transactions = File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
        val reports = File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
        assertTrue(transactions.contains("listOf(0) + (12 downTo 1).toList()"))
        assertTrue(transactions.contains("همه ماه‌ها"))
        assertTrue(transactions.contains("همه سال‌ها"))
        assertTrue(reports.contains("options = (12 downTo 1).toList()"))
    }

    @Test fun `neon remains on regardless of legacy setting and uses theme fallback`() {
        val settings = File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
        val prefs = File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
        val dashboard = File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        val surfaces = File("src/main/java/ir/kharjyar/app/ui/components/Surfaces.kt").readText()
        assertFalse(settings.contains("قاب نئونی کارت اصلی"))
        assertTrue(prefs.contains("cardShine = true,"))
        assertTrue(dashboard.contains("neon = true"))
        assertFalse(dashboard.contains("neon = settings.cardShine"))
        assertTrue(surfaces.contains("skin.neonColors.ifEmpty { listOf(skin.accent, skin.accent) }"))
    }

    @Test fun `selected sidebar item casts soft tinted halo without clipping it`() {
        val root = File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText().substringAfter("private fun DrawerRow(")
        assertTrue(root.contains("if (selected) Modifier.shadow("))
        assertTrue(root.contains("shape = shape, clip = false"))
        assertTrue(root.contains("ambientColor = skin.accent.copy(alpha = 0.36f)"))
        assertTrue(root.indexOf("Modifier.shadow(") < root.indexOf(".clip(shape)"))
    }
}
