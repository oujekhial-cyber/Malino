package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class GoldMultiRatePurchaseGuardTest {
 @Test fun `gold supports dated multi rate purchase lots and weighted cost`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();listOf("GoldPurchaseEntry","افزودن خرید جدید فلز","نرخ خرید هر گرم","میانگین موزون جدید هر گرم","AssetTradeEntity(assetId=asset.id,isSale=false","purchasePriceRial=newCost","quantity=newWeight").forEach{assertTrue(it,s.contains(it))}}
 @Test fun `all accumulated gold is valued at one current market rate`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("currentRate=if(asset.quantity>0)asset.currentValueRial/asset.quantity"));assertTrue(s.contains("projectedCurrent=(currentRate*newWeight).toLong()"));assertTrue(s.contains("projectedGain=projectedCurrent-newCost"))}
}
