package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class MarketOfflineCacheGuardTest{
 @Test fun popupAndFullPageFallBackToTimestampedCache(){
  val cache=File("src/main/java/ir/kharjyar/app/assets/MarketPriceCache.kt").readText();val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();val page=File("src/main/java/ir/kharjyar/app/ui/screens/MarketPulseScreen.kt").readText()
  assertTrue(cache.contains("value_\$code")&&cache.contains("at_\$code")&&cache.contains("MODE_PRIVATE"))
  assertTrue(dashboard.contains("MarketPriceCache.read")&&dashboard.contains("آخرین قیمت ذخیره‌شده")&&dashboard.contains("PersianDate.formatDateTime"))
  assertTrue(page.contains("value?:cached[code]?.valueRial")&&page.contains("MaterialTheme.colorScheme.error")&&page.contains("آخرین بروزرسانی:"))
  assertTrue(page.contains("fresh.values.any{it==null}"))
 }
}
