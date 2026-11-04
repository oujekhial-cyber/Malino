package ir.kharjyar.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ManualVoiceEntryGuardTest {
    @Test fun `voice entry is visible before account-dependent form and opens smart command screen`() {
        val manual = File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
        val root = File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
        val quick = File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
        val label = manual.indexOf("ثبت هوشمند کلامی")
        assertTrue(label >= 0 && label < manual.indexOf("if(active.isNotEmpty())"))
        assertTrue(manual.contains("onClick = { nav.navigate(\"quickAdd\") }"))
        assertTrue(manual.contains("Icons.Filled.Mic"))
        assertTrue(manual.contains("nav.navigate(\"smsPaste\")"))
        assertTrue(root.contains("composable(\"quickAdd\") { QuickAddScreen(viewModel, navController) }"))
        assertTrue(quick.contains("if (voiceAvailable) startVoice()"))
    }
}
