package ir.kharjyar.app

import ir.kharjyar.app.core.nlp.AppCommandRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PurchasedItemSmartRoutingTest {
    @Test fun `property family always routes to property assets`() {
        listOf("خانه","خونه","آپارتمان","زمین","ملک","ویلا","باغ","مغازه","واحد تجاری").forEach { item ->
            assertEquals("asset_record" to "assets", AppCommandRouter.featureAction("یک $item خریدم"))
        }
        val assets=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
        val taxonomy=File("src/main/java/ir/kharjyar/app/core/nlp/AssetCommandTaxonomy.kt").readText()
        listOf("خانه","خونه","آپارتمان","زمین","ویلا","باغ","مغازه").forEach { assertTrue(taxonomy.contains("\"$it\"")) }
        assertTrue(assets.contains("AssetCommandTaxonomy.classify"))
        assertTrue(taxonomy.contains("AssetKind.PROPERTY"))
    }

    @Test fun `vehicle family always routes to independent vehicle section`() {
        listOf("ماشین","خودرو","اتومبیل","موتورسیکلت","وانت","کامیون","اتوبوس","وسیله نقلیه").forEach { item ->
            assertEquals("vehicle_record" to "vehicles", AppCommandRouter.featureAction("یک $item خریدم"))
            assertEquals("vehicle_record" to "vehicles", AppCommandRouter.featureAction("$item را فروختم"))
        }
        val vehicles=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
        assertTrue(vehicles.contains("pending?.takeIf{it.action==\"vehicle_record\"}"))
        assertTrue(vehicles.contains("if(vehicleDraft!=null)VEHICLE_ENTRY"))
    }

    @Test fun `deterministic family routing overrides an incorrect AI route`() {
        val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
        assertTrue(quick.contains("val deterministicFeature=AppCommandRouter.featureAction(input)"))
        assertTrue(quick.contains("val resolvedAction=deterministicFeature?.first?:command.action"))
        assertTrue(quick.contains("nav.navigate(resolvedRoute)"))
    }
}
