package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExperimentalPersianAiGuardTest {
    private val client = File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val ui = File("src/main/java/ir/kharjyar/app/ui/screens/ExperimentalAiSettingsSection.kt").readText()
    private val prefs = File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()

    @Test fun `experimental assistant is opt in isolated and removable`() {
        assertTrue(prefs.contains("experimentalAiEnabled: Boolean = false"))
        assertTrue(ui.contains("checked = enabled"))
        assertTrue(ui.contains("setExperimentalAiEnabled(false)"))
        assertTrue(client.contains("object ExperimentalPersianAi"))
        assertFalse(client.contains("Repository"))
        assertFalse(client.contains("TransactionEntity"))
        assertFalse(client.contains("Sms"))
    }

    @Test fun `api key is encrypted and never backed up with app settings`() {
        assertTrue(client.contains("AndroidKeyStore"))
        assertTrue(client.contains("AES/GCM/NoPadding"))
        assertTrue(client.contains("https://api.avalai.ir/"))
        val backupBlock = prefs.substringAfter("suspend fun exportForBackup")
        assertFalse(backupBlock.contains("experimental_ai_secure"))
        assertFalse(backupBlock.contains("AVALAI_API_KEY"))
    }

    @Test fun `assistant sends only explicit test prompt and cannot write financial data`() {
        assertTrue(client.contains("message.take(4_000)"))
        assertTrue(client.contains("هیچ دسترسی‌ای به Room، پیامک‌ها، حساب‌ها یا تراکنش‌ها ندارد"))
        assertFalse(client.contains("txDao"))
        assertFalse(client.contains("accountDao"))
        assertFalse(client.contains("insert("))
    }
}
