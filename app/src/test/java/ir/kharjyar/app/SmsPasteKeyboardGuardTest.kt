package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmsPasteKeyboardGuardTest {
    @Test fun `analyze sms clears focus and hides keyboard before showing result`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SmsPasteScreen.kt").readText()
        assertTrue(source.contains("val keyboardController = LocalSoftwareKeyboardController.current"))
        assertTrue(source.contains("val focusManager = LocalFocusManager.current"))
        assertTrue(source.contains("focusManager.clearFocus(force = true)"))
        assertTrue(source.contains("keyboardController?.hide()"))
        assertTrue(source.indexOf("keyboardController?.hide()") < source.indexOf("val extracted = Extractor.autoExtract(body)"))
    }
}
