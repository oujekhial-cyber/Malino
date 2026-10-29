package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SidebarBackupPlacementGuardTest {
    @Test fun `backup and restore appears immediately above settings in sidebar`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
        val backup = "DrawerEntry(\"backup\", \"پشتیبان‌گیری و بازیابی\", Icons.Filled.CloudUpload)"
        val settings = "DrawerEntry(\"settings\", \"تنظیمات\", Icons.Filled.Settings)"
        assertTrue(source.contains(backup))
        assertTrue(source.indexOf(backup) < source.indexOf(settings))
        assertTrue(source.contains("route == \"backup\" -> \"پشتیبان‌گیری و بازیابی\""))
        assertTrue(source.contains("composable(\"backup\") { BackupScreen(viewModel) }"))
    }
}
