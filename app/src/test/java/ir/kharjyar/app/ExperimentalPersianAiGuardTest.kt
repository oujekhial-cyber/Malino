package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExperimentalPersianAiGuardTest {
    private val client = File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val ui = File("src/main/java/ir/kharjyar/app/ui/screens/ExperimentalAiSettingsSection.kt").readText()
    private val prefs = File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()

    @Test fun `Groq Qwen assistant is opt in isolated and removable`() {
        assertTrue(prefs.contains("experimentalAiEnabled: Boolean = false"))
        assertTrue(ui.contains("checked=enabled"))
        assertTrue(ui.contains("setExperimentalAiEnabled(false)"))
        assertTrue(client.contains("qwen/qwen3.8-27b"))
        assertTrue(client.contains("https://api.groq.com/openai/v1/chat/completions"))
        assertFalse(client.contains("Repository"))
        assertFalse(client.contains("TransactionEntity"))
    }

    @Test fun `Groq key is encrypted and never backed up with app settings`() {
        assertTrue(client.contains("AndroidKeyStore"))
        assertTrue(client.contains("AES/GCM/NoPadding"))
        val backupBlock = prefs.substringAfter("suspend fun exportForBackup")
        assertFalse(backupBlock.contains("experimental_groq_secure"))
        assertFalse(backupBlock.contains("GROQ_API_KEY"))
    }

    @Test fun `model emits validated draft and user explicitly confirms database write`() {
        assertTrue(client.contains("data class AiTransactionDraft"))
        assertTrue(client.contains("message.take(2_000)"))
        assertTrue(client.contains("direction == \"deposit\" || direction == \"withdraw\""))
        assertTrue(client.contains("amount > 0"))
        assertFalse(client.contains("txDao"))
        assertFalse(client.contains("accountDao"))
        assertTrue(ui.contains("پیش‌نمایش؛ هنوز چیزی ثبت نشده است"))
        assertTrue(ui.contains("تأیید و ثبت تراکنش"))
        assertTrue(ui.contains("viewModel.repo.addManualTransaction"))
        assertTrue(ui.indexOf("پیش‌نمایش؛ هنوز چیزی ثبت نشده است") < ui.indexOf("viewModel.repo.addManualTransaction"))
    }
}
