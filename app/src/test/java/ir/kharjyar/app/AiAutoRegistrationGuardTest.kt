package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiAutoRegistrationGuardTest {
    private val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
    private val settings=File("src/main/java/ir/kharjyar/app/ui/screens/ExperimentalAiSettingsSection.kt").readText()
    private val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()

    @Test fun `automatic registration is opt in and controlled from AI settings`() {
        assertTrue(prefs.contains("aiAutoRegisterEnabled: Boolean = false"))
        assertTrue(prefs.contains("setAiAutoRegisterEnabled"))
        assertTrue(settings.contains("حالت ثبت خودکار"))
        assertTrue(settings.contains("settings.aiAutoRegisterEnabled"))
    }

    @Test fun `auto button asks for missing slots then persists only a complete draft`() {
        assertTrue(quick.contains("Text(\"ثبت خودکار\")"))
        assertTrue(quick.contains("autoRegisterPending=true;analyzeWithAi()"))
        assertTrue(quick.contains("LaunchedEffect(parsed,autoRegisterPending)"))
        assertTrue(quick.contains("p.isComplete&&p.dateExplicit"))
        assertTrue(quick.contains("persistCompletedTransaction"))
        assertTrue(quick.contains("showAccountPicker"))
    }
}
