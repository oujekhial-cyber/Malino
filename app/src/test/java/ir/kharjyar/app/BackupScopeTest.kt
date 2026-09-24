package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * تم بخشی از سلیقه ظاهری روی همین دستگاه است، نه داده کاربر.
 * این تست مطمئن می‌شود تم دوباره وارد بکاپ نشود.
 */
class BackupScopeTest {

    private val source =
        File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()

    @Test
    fun `theme is excluded from backup payload`() {
        val export = source.substringAfter("exportForBackup").substringBefore("importFromBackup")
        assertFalse("تم نباید در بکاپ ذخیره شود", export.contains("\"palette\""))
        assertFalse("حالت روشن/تاریک نباید در بکاپ ذخیره شود", export.contains("\"theme_mode\""))
    }

    @Test
    fun `theme is not restored from backup`() {
        val import = source.substringAfter("importFromBackup")
        assertFalse("تم نباید از بکاپ بازگردانی شود", import.contains("map[\"palette\"]"))
        assertFalse("حالت نمایش نباید از بکاپ بازگردانی شود", import.contains("map[\"theme_mode\"]"))
    }

    @Test
    fun `financial settings are still backed up`() {
        val export = source.substringAfter("exportForBackup").substringBefore("importFromBackup")
        // واحد پول و شکل ارقام همچنان باید بکاپ شوند
        assertTrue(export.contains("money_unit"))
        assertTrue(export.contains("digit_style"))
    }
}
