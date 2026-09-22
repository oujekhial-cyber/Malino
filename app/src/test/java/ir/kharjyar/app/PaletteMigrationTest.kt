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
            listOf("SAKURA", "INDIGO", "VIOLET", "LOTUS", "MIDNIGHT", "SUNSET", "OCEAN", "GOLD"),
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
    fun `all skins are dark and carry neon colors`() {
        // هر هشت تم تیره‌اند و قاب نئونی مخصوص خودشان را دارند
        assertTrue(AllSkins.all { it.dark })
        assertTrue(AllSkins.all { it.neonColors.size >= 2 })
        assertTrue(AllSkins.all { it.heroImage != null })
    }
}
