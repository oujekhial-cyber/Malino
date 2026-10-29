package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class AssetHistoricalPurchaseGuardTest {
 @Test fun `historical purchase uses its own date and gram rate and is not overwritten by live valuation`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("purchaseDate"));assertTrue(s.contains("قیمت خرید کل در تاریخ خرید"));assertTrue(s.contains("نرخ خرید هر گرم"));assertTrue(s.contains("purchasedAt=purchaseDate.startOfDayMillis()"));val valuation=s.substringAfter("Text(\"ارزش امروز\"").substringBefore("val purchaseValue");assertTrue(valuation.contains("IranianGoldCalculator.rawOrUsed"));assertFalse(valuation.contains("purchase="))}
 @Test fun `current value and profit loss are explicit for every asset kind`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("استعلام نرخ روز طلای ۱۸ عیار ایران"));assertTrue(s.contains("قیمت روز خودرو را کاربر وارد می‌کند"));assertTrue(s.contains("قیمت روز ملک را کاربر وارد می‌کند"));assertTrue(s.contains("سود فعلی")&&s.contains("زیان فعلی")&&s.contains("نسبت تغییر"))}
}
