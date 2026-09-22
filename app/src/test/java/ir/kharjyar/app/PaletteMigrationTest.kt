package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.Palette
import org.junit.Assert.assertEquals
import org.junit.Test

/** فقط یک تم در برنامه باقی مانده است: شفق قطبی. */
class PaletteMigrationTest {

    @Test
    fun onlyAuroraTheme() {
        assertEquals(1, Palette.entries.size)
        assertEquals(listOf("AURORA"), Palette.entries.map { it.name })
    }

    @Test
    fun defaultIsAurora() {
        assertEquals(Palette.AURORA, ir.kharjyar.app.data.prefs.AppSettings().palette)
    }
}
