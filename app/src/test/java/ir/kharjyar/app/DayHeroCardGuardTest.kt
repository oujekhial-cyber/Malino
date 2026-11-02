package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DayHeroCardGuardTest {
    @Test fun `light theme hero numbers are flat without emboss shadows`() {
        val emboss = File("src/main/java/ir/kharjyar/app/ui/components/Emboss.kt").readText()
        assertTrue(emboss.contains("if (!skin.dark)"))
        assertTrue(emboss.contains("style.copy(shadow = null)"))
    }

    @Test fun `light hero card has a visible outline`() {
        val surfaces = File("src/main/java/ir/kharjyar/app/ui/components/Surfaces.kt").readText()
        assertTrue(surfaces.contains("if (!skin.dark) Modifier.border"))
    }
}
