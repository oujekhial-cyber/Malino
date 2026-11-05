package ir.kharjyar.app

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectedLauncherIconTest {
    @Test fun `launcher uses the unchanged selected image for both icon variants`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:icon=\"@drawable/app_launcher_selected\""))
        assertTrue(manifest.contains("android:roundIcon=\"@drawable/app_launcher_selected\""))
        val bytes = File("src/main/res/drawable-nodpi/app_launcher_selected.png").readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        assertEquals("63a84e045b81323c12654d343309272bf14e22ae4a75cc60c64f7bc32cffe761", hash)
    }
}
