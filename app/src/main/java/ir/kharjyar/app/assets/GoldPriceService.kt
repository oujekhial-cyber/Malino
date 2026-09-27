package ir.kharjyar.app.assets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** نرخ هر گرم طلای ۱۸ عیار بازار ایران به ریال، از صفحه عمومی TGJU. */
object GoldPriceService {
    suspend fun gram18Rial(): Long? = withContext(Dispatchers.IO) { runCatching {
        val c = URL("https://www.tgju.org/profile/geram18/today").openConnection() as HttpURLConnection
        c.connectTimeout = 8_000; c.readTimeout = 8_000
        c.setRequestProperty("User-Agent", "KharjYar/1.0 Android")
        val html = c.inputStream.bufferedReader().use { it.readText() }; c.disconnect()
        val aroundRate = Regex("نرخ فعلی[^0-9۰-۹]{0,120}([0-9۰-۹][0-9۰-۹,٬]{4,})").find(html)?.groupValues?.get(1)
        aroundRate?.map { when(it) { in '۰'..'۹' -> ('0'.code + it.code - '۰'.code).toChar(); '٬', ',' -> null; else -> it } }?.filterNotNull()?.joinToString("")?.toLongOrNull()
    }.getOrNull() }
}

object IranianGoldCalculator {
    /** مالیات فقط روی اجرت و سود محاسبه می‌شود، نه اصل طلا. */
    fun newGold(weightGram: Double, gram18Rial: Long, wagePercent: Double, profitPercent: Double, vatPercent: Double): Long {
        val principal = weightGram * gram18Rial
        val wage = principal * wagePercent / 100.0
        val profit = (principal + wage) * profitPercent / 100.0
        val vat = (wage + profit) * vatPercent / 100.0
        return (principal + wage + profit + vat).toLong()
    }
    fun rawOrUsed(weightGram: Double, gram18Rial: Long): Long = (weightGram * gram18Rial).toLong()
}
