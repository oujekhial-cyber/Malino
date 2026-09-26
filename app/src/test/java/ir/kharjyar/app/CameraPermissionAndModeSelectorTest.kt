package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CameraPermissionAndModeSelectorTest {
    @Test fun `check camera requests runtime permission before capture`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
        assertTrue(source.contains("ActivityResultContracts.RequestPermission"))
        assertTrue(source.contains("Manifest.permission.CAMERA"))
        assertTrue(source.contains("checkSelfPermission"))
        assertTrue(source.contains("if(granted)openCamera()"))
    }

    @Test fun `debt and check modes use prominent selector at top`() {
        val debt = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        val check = File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
        assertTrue(debt.contains("TwoWayModeSelector(\"طلب از دیگران\""))
        assertTrue(check.contains("TwoWayModeSelector(\"چک صادرشده\""))
    }
}
