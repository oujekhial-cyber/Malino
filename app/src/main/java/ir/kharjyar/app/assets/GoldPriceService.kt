package ir.kharjyar.app.assets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** قیمت لحظه‌ای جهانی هر اونس طلا؛ برای اطلاع کاربر، ارزش ریالی همچنان دستی است. */
object GoldPriceService {
 suspend fun ounceUsd():Double?=withContext(Dispatchers.IO){runCatching{val c=URL("https://api.gold-api.com/price/XAU").openConnection() as HttpURLConnection;c.connectTimeout=8000;c.readTimeout=8000;val t=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();Regex("\"price\"\\s*:\\s*(\\d+(?:\\.\\d+)?)").find(t)?.groupValues?.get(1)?.toDoubleOrNull()}.getOrNull()}
}
