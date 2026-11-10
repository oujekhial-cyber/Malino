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
    fun `all seven palettes are available`() {
        assertEquals(
            listOf("SAKURA", "VIOLET", "LOTUS", "OCEAN", "GOLD", "MINIMAL_DAY", "MINIMAL_NIGHT"),
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
    fun `decorative skins have art while minimal skins stay minimal`() {
        val minimal = setOf(Palette.MINIMAL_DAY, Palette.MINIMAL_NIGHT)
        assertTrue(AllSkins.filter { it.id !in minimal }.all { it.neonColors.size >= 2 && it.heroImage != null })
        assertTrue(AllSkins.filter { it.id in minimal }.all { it.neonColors.isEmpty() && it.heroImage == null })
    }

    @Test
    fun `daylight and minimal day are the two light skins`() {
        val light = AllSkins.filter { !it.dark }.map { it.id }.toSet()
        assertEquals(setOf(Palette.VIOLET, Palette.MINIMAL_DAY), light)
    }
}
