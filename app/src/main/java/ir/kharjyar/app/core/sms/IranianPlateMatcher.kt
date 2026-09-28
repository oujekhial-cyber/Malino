package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.VehicleEntity

/** استخراج و تطبیق پلاک ملی ایران، مستقل از فاصله، خط تیره و ترتیب نوشتاری پیامک. */
object IranianPlateMatcher {
    data class Plate(val leftTwo:String,val letter:Char,val serialThree:String,val iranCode:String){
        val signature:String get()="$leftTwo|$letter|$serialThree|$iranCode"
    }

    fun extract(text:String):Plate? {
        val value=normalize(text)
        // شکل روی پلاک/ورودی کاربر: 69ی519ایران83
        Regex("(\\d{2})([آ-ی])(\\d{3})ایران(\\d{2})").find(value)?.let{m->
            return Plate(m.groupValues[1],m.groupValues[2].single(),m.groupValues[3],m.groupValues[4])
        }
        // شکل رایج راهور در SMS: ایران83 ــ 519ی69
        Regex("ایران(\\d{2})(\\d{3})([آ-ی])(\\d{2})").find(value)?.let{m->
            return Plate(m.groupValues[4],m.groupValues[3].single(),m.groupValues[2],m.groupValues[1])
        }
        // همان ترتیب پیامک در ورودی‌هایی که واژه ایران نوشته نشده است.
        Regex("(?:^|[^\\d])(\\d{2})(\\d{3})([آ-ی])(\\d{2})(?:$|[^\\d])").find(value)?.let{m->
            return Plate(m.groupValues[4],m.groupValues[3].single(),m.groupValues[2],m.groupValues[1])
        }
        return null
    }

    fun matches(message:String,savedPlate:String):Boolean {
        val incoming=extract(message)?:return false
        val saved=extract(savedPlate)?:return false
        return incoming.signature==saved.signature
    }

    /** فقط در تطبیق یکتا خودرو را خودکار متصل می‌کنیم. */
    fun uniqueVehicleId(message:String,vehicles:List<VehicleEntity>):Long? =
        vehicles.filter{matches(message,it.plate)}.map{it.id}.distinct().singleOrNull()

    private fun normalize(raw:String):String = Digits.normalize(raw)
        .replace('ي','ی').replace('ى','ی').replace('ك','ک')
        .replace("ايران","ایران",ignoreCase=true)
        .filter{it.isDigit()||it in 'آ'..'ی'}
}
