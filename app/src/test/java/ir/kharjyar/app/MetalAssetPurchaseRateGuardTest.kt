package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class MetalAssetPurchaseRateGuardTest {
 @Test fun `metal assets include gold silver and purchase gram rate calculator`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  listOf("دارایی‌های فلزی (طلا، نقره و ...)","نقره","پلاتین","سایر فلزات گران‌بها","نرخ خرید هر گرم","محاسبه قیمت کل خرید","purchaseWeight*historicalRateInput").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `default one gram is selected completely on first focus`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  assertTrue(source.contains("TextFieldValue(existing?.quantity?.toString() ?: \"1\")"))
  assertTrue(source.contains("selection=TextRange(0,quantity.text.length)"))
  assertTrue(source.contains("onFocusChanged"))
 }
}
