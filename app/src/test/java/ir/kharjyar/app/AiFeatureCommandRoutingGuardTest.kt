package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiFeatureCommandRoutingGuardTest {
    private val ai=File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
    private val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()

    @Test fun `domain commands use specialist sections rather than ordinary transactions`() {
        val actions=listOf("asset_record","reminder_create","check_issued","check_received","note_create","vehicle_record","installment_payment","civic_record")
        actions.forEach{assertTrue("missing $it",ai.contains(it)&&quick.contains(it))}
        assertTrue(ai.contains("خرید دارایی، پرداخت قسط و انتقال‌ها هرگز income یا expense عادی نیستند"))
        assertTrue(vm.contains("data class PendingFeatureCommand"))
        assertTrue(quick.contains("prepareFeatureCommand"))
    }

    @Test fun `specialist forms consume pending commands`() {
        assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText().contains("pendingFeatureCommand"))
        assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText().contains("pendingFeatureCommand"))
        assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText().contains("pendingFeatureCommand"))
        assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/NotesScreen.kt").readText().contains("pendingFeatureCommand"))
    }
}
