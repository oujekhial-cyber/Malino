package ir.kharjyar.app.assets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** نرخ‌های عمومی بازار ایران از صفحات TGJU، بدون ارسال هیچ داده کاربری. */
object GoldPriceService {
    private fun digitsToLong(raw:String):Long? = raw.mapNotNull {
        when(it) {
            in '۰'..'۹' -> ('0'.code+it.code-'۰'.code).toChar()
            in '٠'..'٩' -> ('0'.code+it.code-'٠'.code).toChar()
            in '0'..'9' -> it
            ',', '٬' -> null
            else -> null
        }
    }.joinToString("").toLongOrNull()

    /**
     * HTML سایت در بعضی پاسخ‌ها نرخ را میان تگ‌ها قرار می‌دهد. ابتدا متن قابل‌دیدن
     * ساخته می‌شود و سپس نرخ برچسب‌خورده خوانده می‌شود؛ در پایان الگوهای JSON/
     * data-* خود صفحه به‌عنوان مسیر جایگزین بررسی می‌شوند.
     */
    fun parseCurrentRial(html:String,profile:String):Long? {
        val withoutNoise=html
            .replace(Regex("(?is)<script[^>]*>.*?</script>")," ")
            .replace(Regex("(?is)<style[^>]*>.*?</style>")," ")
        val text=withoutNoise.replace(Regex("(?s)<[^>]+>")," ")
            .replace("&nbsp;"," ").replace("&#44;",",").replace("&zwnj;","")
            .replace(Regex("\\s+")," ")
        val labelled=Regex("[0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]{3,}")
            .findAll(text.substringAfter("نرخ فعلی",missingDelimiterValue=""))
            .mapNotNull { digitsToLong(it.value) }.toList()
        val rawCandidates=listOf(
            Regex("\\\"(?:price|value|last|p)\\\"\\s*:\\s*\\\"?([0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]{3,})"),
            Regex("data-(?:price|value|last)=[\\\"']([0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]{3,})")
        ).flatMap { regex -> regex.findAll(html).mapNotNull { digitsToLong(it.groupValues[1]) }.toList() }
        val minimum=if(profile=="geram18")1_000_000L else 1_000L
        return (labelled+rawCandidates).firstOrNull { it>=minimum }
    }

    private fun fetch(url:String):String? = runCatching {
        val connection=URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout=10_000;connection.readTimeout=10_000
        connection.instanceFollowRedirects=true
        connection.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36 KharjYar/1.0")
        connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
        connection.setRequestProperty("Accept-Language","fa-IR,fa;q=0.9,en;q=0.6")
        connection.setRequestProperty("Accept-Encoding","identity")
        val code=connection.responseCode
        if(code !in 200..299){connection.disconnect();return@runCatching null}
        val result=connection.inputStream.bufferedReader(Charsets.UTF_8).use{it.readText()}
        connection.disconnect();result
    }.getOrNull()

    private suspend fun currentRial(profile:String):Long? = withContext(Dispatchers.IO) {
        val urls=listOf(
            "https://www.tgju.org/profile/$profile",
            "https://www.tgju.org/profile/$profile/today",
            "https://www.tgju.org/profile/$profile/history"
        )
        urls.asSequence().mapNotNull { url -> fetch(url)?.let { parseCurrentRial(it,profile) } }.firstOrNull()
    }

    /** نرخ هر گرم طلای ۱۸ عیار بازار ایران به ریال. */
    suspend fun gram18Rial():Long? = currentRial("geram18")

    /** نرخ دلار بازار آزاد ایران به ریال؛ صفحه اصلی و دو مسیر جایگزین امتحان می‌شوند. */
    suspend fun dollarRial():Long? = currentRial("price_dollar_rl")

    /** پروفایل نرخ آزاد ارزهای متداول بازار ایران. */
    private val currencyProfiles = mapOf(
        "USD" to "price_dollar_rl", "EUR" to "price_eur", "GBP" to "price_gbp",
        "AED" to "price_aed", "TRY" to "price_try", "CAD" to "price_cad",
        "AUD" to "price_aud", "CHF" to "price_chf", "CNY" to "price_cny"
    )

    /** نرخ یک واحد ارز در بازار آزاد به ریال؛ null یعنی منبع در دسترس نبوده است. */
    suspend fun currencyRial(code:String):Long? = currencyProfiles[code]?.let { currentRial(it) }

    /** پروفایل عمومی فلزات گران‌بهای رایج؛ مقدار ناموجود هرگز تخمین زده نمی‌شود. */
    private val preciousMetalProfiles = mapOf(
        "GOLD18" to "geram18", "GOLD24" to "geram24", "MESGHAL" to "mesghal", "SILVER" to "silver_999"
    )

    suspend fun preciousMetalRial(code:String):Long? = preciousMetalProfiles[code]?.let { currentRial(it) }
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
