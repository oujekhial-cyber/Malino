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
    fun `every skin carries neon colors and a hero image`() {
        // همه تم‌ها قاب نئونی و تصویر کارت مخصوص خودشان را دارند
        assertTrue(AllSkins.all { it.neonColors.size >= 2 })
        assertTrue(AllSkins.all { it.heroImage != null })
    }

    @Test
    fun `there is exactly one light skin among the palettes`() {
        // تم بنفش تیره به یک تم روشن تبدیل شد؛ بقیه تیره می‌مانند
        val light = AllSkins.filter { !it.dark }
        assertEquals(1, light.size)
        assertEquals(Palette.VIOLET, light.first().id)
    }
}
