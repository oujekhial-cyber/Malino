package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class IranCoinAndCurrencyAssetGuardTest {
 @Test fun `Iranian market coin types are offered`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();listOf("سکه امامی (طرح جدید)","سکه تمام بهار آزادی (طرح قدیم)","نیم سکه بهار آزادی","ربع سکه بهار آزادی","سکه گرمی بانک مرکزی","سکه پارسیان ۱۸ عیار (وزن آزاد)").forEach{assertTrue(it,s.contains(it))} }
 @Test fun `currency assets preserve historical purchase and support online editable current rate`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();listOf("AssetKind.CURRENCY","دلار آمریکا","یورو","پوند انگلیس","درهم امارات","لیر ترکیه","دلار کانادا","یوان چین","نرخ خرید هر واحد ارز","currencyRial(currencyCode)","استعلام آنلاین نرخ روز ارز","نرخ امروز هر واحد ارز","محاسبه ارزش امروز ارز","CURRENCY:\$currencyCode").forEach{assertTrue(it,s.contains(it))};val service=File("src/main/java/ir/kharjyar/app/assets/GoldPriceService.kt").readText();listOf("price_dollar_rl","price_eur","price_gbp","price_aed","price_try","price_cad","price_cny").forEach{assertTrue(it,service.contains(it))} }
}
