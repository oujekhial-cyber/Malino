package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiAssetPrefillGuardTest {
    private val ai=File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()
    private val assets=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()

    @Test fun `AI asset schema carries quantity and total purchase amount`() {
        assertTrue(ai.contains("quantity را وزن/تعداد"))
        assertTrue(ai.contains("amount را قیمت کل خرید"))
        assertTrue(ai.contains("quantity=value[\"quantity\"]"))
        assertTrue(vm.contains("val quantity: Double? = null"))
    }

    @Test fun `asset editor receives and applies the smart command draft`() {
        assertTrue(assets.contains("AssetEditor(vm,editor,selectedKind?:AssetKind.GOLD,pending"))
        assertTrue(assets.contains("draft?.quantity"))
        assertTrue(assets.contains("draftAmountInput"))
        assertTrue(assets.contains("draft?.let{it.title.ifBlank{it.details}}"))
    }
}
