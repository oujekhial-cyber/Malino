package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.Palette
import org.junit.Assert.assertEquals
import org.junit.Test

/** تم‌های جدید باید دقیقاً چهارتا باشند و تم پیش‌فرض «شیشه‌ای». */
class PaletteMigrationTest {

    @Test
    fun exactlyFourThemes() {
        assertEquals(4, Palette.entries.size)
        assertEquals(
            listOf("GLASS", "NEON", "PASTEL", "AURORA"),
            Palette.entries.map { it.name }
        )
    }

    @Test
    fun defaultIsGlass() {
        assertEquals(Palette.GLASS, ir.kharjyar.app.data.prefs.AppSettings().palette)
    }
}
