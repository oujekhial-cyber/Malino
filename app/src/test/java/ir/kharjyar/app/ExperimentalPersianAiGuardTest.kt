package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExperimentalPersianAiGuardTest {
    private val client = File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val settingsUi = File("src/main/java/ir/kharjyar/app/ui/screens/ExperimentalAiSettingsSection.kt").readText()
    private val commandUi = File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
    private val prefs = File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()

    @Test fun `both providers are selectable and assistant remains opt in`() {
        assertTrue(prefs.contains("experimentalAiEnabled: Boolean = false"))
        assertTrue(settingsUi.contains("AiProvider.entries"))
        assertTrue(settingsUi.contains("checked=enabled"))
        assertTrue(client.contains("qwen/qwen3.8-27b"))
        assertTrue(client.contains("qwen3.8-flash"))
        assertTrue(client.contains("api.groq.com"))
        assertTrue(client.contains("api.avalai.ir"))
        assertFalse(client.contains("Repository"))
        assertFalse(client.contains("TransactionEntity"))
    }

    @Test fun `provider keys are separately encrypted and excluded from backup`() {
        assertTrue(client.contains("AndroidKeyStore"))
        assertTrue(client.contains("AES/GCM/NoPadding"))
        assertTrue(client.contains("keyAlias(provider)"))
        val backupBlock = prefs.substringAfter("suspend fun exportForBackup")
        assertFalse(backupBlock.contains("experimental_groq_secure"))
        assertFalse(backupBlock.contains("experimental_avalai_secure"))
    }

    @Test fun `AI creates validated draft while command center requires confirmation`() {
        assertTrue(client.contains("data class AiTransactionDraft"))
        assertTrue(client.contains("direction == \"deposit\" || direction == \"withdraw\""))
        assertFalse(client.contains("txDao"))
        assertTrue(commandUi.contains("مرکز فرمان هوشمند"))
        assertTrue(client.contains("data class AiAppCommand"))
        assertTrue(client.contains("route in allowed"))
        assertTrue(commandUi.contains("ExperimentalPersianAi.interpretCommand"))
        assertTrue(commandUi.contains("analyzeSpokenCommand"))
        assertTrue(commandUi.contains("settings.experimentalAiEnabled && ExperimentalPersianAi.hasToken(context)"))
        assertTrue(commandUi.contains("متن گفتار برای سرویس هوش مصنوعی انتخاب‌شده فرستاده می‌شود"))
        assertTrue(commandUi.contains("ثبت کن"))
        assertTrue(commandUi.contains("viewModel.repo.addManualTransaction"))
    }
}
