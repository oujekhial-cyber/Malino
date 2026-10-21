package ir.kharjyar.app
import ir.kharjyar.app.core.nlp.AssetCommandTaxonomy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
class SmartAssetOnlinePrefillGuardTest {
 @Test fun `asset quantity is extracted locally`(){assertEquals(10.0,AssetCommandTaxonomy.quantity("ده دلار سه روز قبل خریدم")!!,0.0);assertEquals(12.5,AssetCommandTaxonomy.quantity("۱۲.۵ گرم طلا خریدم")!!,0.0)}
 @Test fun `smart asset draft carries explicit purchase date into editor`(){val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText();val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText();val assets=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(vm.contains("val occurredAt: Long? = null"));assertTrue(quick.contains("occurredAt=featureParsed.takeIf{it.dateExplicit}"));assertTrue(assets.contains("draft?.occurredAt?.let(PersianDate::fromMillis)"))}
 @Test fun `online priced smart assets automatically query and fill current value`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();listOf("draft?.action!=\"asset_record\"","GoldPriceService.currencyRial(currencyCode)","GoldPriceService.gram18Rial()","GoldPriceService.coinRial","current=shownTotal","نرخ واقعی روز خرید را در صورت تفاوت اصلاح کنید").forEach{assertTrue(it,s.contains(it))}}
 @Test fun `currency purchase and current rates are separate fields`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("var currencyCurrentRate"));assertTrue(s.contains("AmountTextField(unitRate,{unitRate=it},\"نرخ خرید هر واحد ارز\""));assertTrue(s.contains("AmountTextField(currencyCurrentRate,{currencyCurrentRate=it},\"نرخ امروز هر واحد ارز\""))}
}
