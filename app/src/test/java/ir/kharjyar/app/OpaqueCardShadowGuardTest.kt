package ir.kharjyar.app

import androidx.compose.ui.graphics.Color
import ir.kharjyar.app.ui.components.opaqueCardTint
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class OpaqueCardShadowGuardTest {
    @Test fun `tinted stops are fully opaque over both light and dark surfaces`() {
        val accent = Color(0xFFE0525E)
        listOf(Color.White, Color.Black).forEach { surface ->
            val tint = opaqueCardTint(accent, surface.copy(alpha = 0.5f), 0.17f)
            assertEquals(1f, tint.alpha, 0.001f)
            assertTrue(tint.red in 0f..1f)
        }
        assertEquals(Color.White, opaqueCardTint(accent, Color.White, 0f))
    }

    @Test fun `elevated gradient cards use opaque surfaces instead of transparent cards`() {
        listOf("LoansScreen.kt", "ChecksScreen.kt", "DebtsScreen.kt", "VehiclesScreen.kt").forEach { name ->
            val source = File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText()
            assertTrue(name, source.contains("opaqueCardTint("))
            assertFalse(name, Regex("CardDefaults\\.cardColors\\(containerColor\\s*=\\s*Color\\.Transparent\\)").containsMatchIn(source))
        }
        val hero = File("src/main/java/ir/kharjyar/app/ui/components/ModernSummaryHero.kt").readText()
        assertTrue(hero.contains("surface.copy(alpha=1f)"))
    }
}
