package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.ui.theme.AllSkins
import ir.kharjyar.app.ui.theme.skinOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** تم‌های برنامه و نگاشت آن‌ها به پوسته‌ها. */
class PaletteMigrationTest {

    @Test
    fun `all eight palettes are available`() {
        assertEquals(
            listOf("AURORA", "EMERALD", "PAPER", "PLUM", "SLATE", "GOLD", "SAKURA", "OCEAN"),
            Palette.entries.map { it.name }
        )
    }

    @Test
    fun `default palette is sakura`() {
        assertEquals(Palette.SAKURA, ir.kharjyar.app.data.prefs.AppSettings().palette)
    }

    @Test
    fun `every palette maps to a distinct skin`() {
        assertEquals(Palette.entries.size, AllSkins.size)
        Palette.entries.forEach { p -> assertEquals(p, skinOf(p).id) }
        assertEquals(AllSkins.size, AllSkins.map { it.title }.toSet().size)
    }

    @Test
    fun `only paper skin is light`() {
        assertTrue(AllSkins.filter { !it.dark }.map { it.id } == listOf(Palette.PAPER))
    }
}
