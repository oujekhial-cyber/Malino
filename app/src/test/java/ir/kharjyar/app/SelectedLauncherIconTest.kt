package ir.kharjyar.app

import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectedLauncherIconTest {
    @Test fun `launcher uses the cropped selected square for both icon variants`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:icon=\"@drawable/app_launcher_selected\""))
        assertTrue(manifest.contains("android:roundIcon=\"@drawable/app_launcher_selected\""))
        val icon = File("src/main/res/drawable-nodpi/app_launcher_selected.png")
        val image = ImageIO.read(icon)
        assertEquals(1152, image.width)
        assertEquals(1152, image.height)
        val bytes = icon.readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        assertEquals("8dd6e6ebdc59e6223aa0ec0e9a1adf4a4917230acfe8b7d1eef2bfaa694d3b14", hash)
    }
}
