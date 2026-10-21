package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AssetKind

/** واژه‌نامه قطعی دارایی‌ها؛ قبل از مدل آنلاین اجرا می‌شود و قابل حدس‌زدن نیست. */
object AssetCommandTaxonomy {
    enum class Destination { ASSET, VEHICLE }
    data class Match(val destination:Destination,val assetKind:Int?,val matchedWord:String)

    private val vehicles=listOf(
        "خودرو","ماشین","اتومبیل","موتورسیکلت","موتور سیکلت","موتور","اسکوتر","وانت","کامیون","کامیونت","تریلی","کشنده","اتوبوس","مینی بوس","تاکسی","ماشین آلات","وسیله نقلیه",
        "پراید","پژو","سمند","دنا","تارا","رانا","شاهین","کوییک","ساینا","تیبا","جک","چری","فونیکس","هایما","سانتافه","تویوتا","هیوندای","کیا","بنز","بی ام و"
    )
    private val properties=listOf(
        "ملک","املاک","خانه","خونه","منزل","مسکن","آپارتمان","اپارتمان","زمین","ویلا","باغ","باغچه","مغازه","فروشگاه","دفتر کار","دفتر اداری","واحد تجاری","واحد اداری","سوله","انبار","کارگاه","ساختمان","کلنگی"
    )
    private val metals=listOf(
        "طلا","طلای آب شده","طلای دست دوم","طلای نو","جواهر","زیورآلات","سکه","نیم سکه","ربع سکه","سکه گرمی","پارسیان","شمش","نقره","پلاتین"
    )
    private val currencies=listOf(
        "ارز","دلار","یورو","پوند","درهم","لیر","فرانک","یوان","دینار","ریال عمان","ریال قطر","دلار کانادا","دلار استرالیا"
    )
    private val stocks=listOf(
        "سهام","سهم","بورس","صندوق بورسی","صندوق سرمایه گذاری","ETF","اوراق","حق تقدم"
    )
    private val personal=listOf(
        "دارایی شخصی","موبایل","گوشی","تلفن همراه","لپ تاپ","لپتاپ","کامپیوتر","رایانه","تبلت","تلویزیون","کنسول بازی","دوربین","ساعت","دوچرخه","لوازم خانگی","فرش","تابلو","کلکسیون","ساز","جهیزیه"
    )

    /** تعداد/وزن گفته‌شده پیش از نام واحد دارایی، برای مسیر آفلاین. */
    fun quantity(text:String):Double? {
        val value=normalize(text).lowercase()
        val units="گرم|کیلو|دلار|یورو|پوند|درهم|لیر|فرانک|یوان|سکه|سهم|واحد"
        Regex("([0-9]+(?:[.]?[0-9]+)?)\\s*(?:$units)").find(value)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let{return it}
        val words=mapOf("یک" to 1.0,"دو" to 2.0,"سه" to 3.0,"چهار" to 4.0,"پنج" to 5.0,"شش" to 6.0,"هفت" to 7.0,"هشت" to 8.0,"نه" to 9.0,"ده" to 10.0)
        Regex("(یک|دو|سه|چهار|پنج|شش|هفت|هشت|نه|ده)\\s*(?:$units)").find(value)?.groupValues?.getOrNull(1)?.let{return words[it]}
        return null
    }

    fun classify(text:String):Match? {
        val value=normalize(text)
        fun first(words:List<String>)=words.sortedByDescending{it.length}.firstOrNull{value.contains(normalize(it))}
        first(vehicles)?.let{return Match(Destination.VEHICLE,null,it)}
        first(properties)?.let{return Match(Destination.ASSET,AssetKind.PROPERTY,it)}
        first(metals)?.let{return Match(Destination.ASSET,AssetKind.GOLD,it)}
        first(currencies)?.let{return Match(Destination.ASSET,AssetKind.CURRENCY,it)}
        first(stocks)?.let{return Match(Destination.ASSET,AssetKind.STOCK,it)}
        first(personal)?.let{return Match(Destination.ASSET,AssetKind.OTHER,it)}
        return null
    }

    private fun normalize(value:String)=Digits.normalizeForMatch(value).replace('‌',' ').replace(Regex("\\s+")," ").uppercase()
}
