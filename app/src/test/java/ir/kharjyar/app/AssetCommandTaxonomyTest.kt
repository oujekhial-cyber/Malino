package ir.kharjyar.app

import ir.kharjyar.app.core.nlp.AssetCommandTaxonomy
import ir.kharjyar.app.core.nlp.AppCommandRouter
import ir.kharjyar.app.data.db.AssetKind
import org.junit.Assert.assertEquals
import org.junit.Test

class AssetCommandTaxonomyTest {
 private fun kind(word:String)=AssetCommandTaxonomy.classify("یه $word خریدم")?.assetKind
 @Test fun `property names never become personal assets`() {
  listOf("خانه","خونه","آپارتمان","اپارتمان","زمین","ویلا","باغ","مغازه","فروشگاه","دفتر اداری","واحد تجاری","سوله","انبار","کارگاه","ساختمان").forEach{assertEquals(it,AssetKind.PROPERTY,kind(it))}
 }
 @Test fun `vehicles including models route to dedicated vehicle section`() {
  listOf("ماشین","خودرو","موتورسیکلت","وانت","کامیون","تاکسی","پراید","پژو","دنا","تارا","جک","چری","تویوتا").forEach{word->
   assertEquals(word,AssetCommandTaxonomy.Destination.VEHICLE,AssetCommandTaxonomy.classify("یک $word خریدم")?.destination)
   assertEquals(word,"vehicle_record" to "vehicles",AppCommandRouter.featureAction("یک $word خریدم"))
  }
 }
 @Test fun `financial and personal asset families have explicit kinds`() {
  listOf("طلا","سکه","نقره","شمش","پلاتین").forEach{assertEquals(it,AssetKind.GOLD,kind(it))}
  listOf("دلار","یورو","پوند","درهم","لیر","یوان").forEach{assertEquals(it,AssetKind.CURRENCY,kind(it))}
  listOf("سهام","بورس","صندوق بورسی","اوراق").forEach{assertEquals(it,AssetKind.STOCK,kind(it))}
  listOf("موبایل","لپ تاپ","کامپیوتر","تبلت","تلویزیون","دوربین","ساعت","دوچرخه","فرش","کلکسیون").forEach{assertEquals(it,AssetKind.OTHER,kind(it))}
 }
}
