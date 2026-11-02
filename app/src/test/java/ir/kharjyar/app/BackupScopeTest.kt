package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** تنظیمات کاربر برای بازیابی کامل تجربه برنامه در نسخه پشتیبان نگهداری می‌شوند. */
class BackupScopeTest {
 private val source=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
 @Test fun `theme is included in complete backup payload`(){val export=source.substringAfter("exportForBackup").substringBefore("importFromBackup");assertTrue(export.contains("\"palette\""));assertTrue(export.contains("\"theme_mode\""))}
 @Test fun `theme is restored from complete backup`(){val import=source.substringAfter("importFromBackup");assertTrue(import.contains("enum(\"theme_mode\""));assertTrue(import.contains("enum(\"palette\""))}
 @Test fun `financial settings are backed up`(){val export=source.substringAfter("exportForBackup").substringBefore("importFromBackup");assertTrue(export.contains("money_unit"));assertTrue(export.contains("digit_style"))}
}
