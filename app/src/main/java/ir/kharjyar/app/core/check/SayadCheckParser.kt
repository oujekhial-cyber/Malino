package ir.kharjyar.app.core.check

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits

data class ParsedSayadCheck(
    val sayadId:String="", val serialNumber:String="", val amountRial:Long?=null,
    val amountWords:String="", val dueDate:PersianDate?=null, val bankName:String="",
    val issuerName:String="", val receiverName:String="", val nationalId:String="",
    val accountNumber:String="", val iban:String=""
)

/** استخراج محافظه‌کارانه اطلاعات چاپی/دست‌نویس خوانده‌شده از چک صیادی؛ همه نتایج باید توسط کاربر تأیید شوند. */
object SayadCheckParser {
 fun parse(raw:String):ParsedSayadCheck { val text=Digits.normalize(raw).replace('ي','ی').replace('ك','ک');val compact=text.replace(" ","").replace("-","")
  val iban=Regex("IR\\d{24}",RegexOption.IGNORE_CASE).find(compact)?.value?.uppercase().orEmpty()
  val ids=Regex("(?<!\\d)\\d{16}(?!\\d)").findAll(text.replace("٬","").replace(",","")).map{it.value}.toList()
  val sayad=Regex("(?:صیاد(?:ی)?|شناسه)\\s*[:：]?\\s*(\\d[\\d\\s-]{14,22}\\d)").find(text)?.groupValues?.get(1)?.filter(Char::isDigit)?.takeIf{it.length==16} ?: ids.firstOrNull().orEmpty()
  val serial=Regex("(?:سریال|سری)\\s*(?:چک)?\\s*[:：]?\\s*(\\d{5,12})").find(text)?.groupValues?.get(1) ?: Regex("(?<!\\d)\\d{6,8}(?!\\d)").find(text)?.value.orEmpty()
  val amount=Regex("(?:مبلغ|ریال)\\s*[:：]?\\s*([\\d,،٬]{4,})").findAll(text).mapNotNull{Digits.parseAmount(it.groupValues[1])}.maxOrNull()
  val amountWords=Regex("(?:مبلغ|به مبلغ)\\s*[:：]?\\s*([^\\n]{3,80})(?:ریال|تومان)").find(text)?.groupValues?.get(1)?.trim().orEmpty()
  val dateMatch=Regex("(?<!\\d)(1[34]\\d{2})[/.-](\\d{1,2})[/.-](\\d{1,2})(?!\\d)").find(text) ?: Regex("(?<!\\d)(\\d{2})[/.-](\\d{1,2})[/.-](\\d{1,2})(?!\\d)").find(text)
  val date=dateMatch?.let{m->runCatching{val y=m.groupValues[1].toInt().let{if(it<100)1400+it else it};PersianDate(y,m.groupValues[2].toInt(),m.groupValues[3].toInt())}.getOrNull()}
  fun labeled(vararg labels:String)=labels.firstNotNullOfOrNull{label->Regex("$label\\s*[:：]?\\s*([^\\n]{3,60})").find(text)?.groupValues?.get(1)?.trim()}.orEmpty()
  val banks=listOf("ملی","ملت","صادرات","تجارت","سپه","کشاورزی","مسکن","رفاه","پاسارگاد","سامان","پارسیان","شهر","آینده","دی","سینا","اقتصاد نوین","کارآفرین","گردشگری")
  return ParsedSayadCheck(sayad,serial,amount,amountWords,date,banks.firstOrNull{text.contains(it)}.orEmpty(),labeled("صادرکننده","صاحب حساب","نام و نام خانوادگی"),labeled("در وجه","گیرنده"),Regex("(?:کد ملی|شناسه ملی)\\s*[:：]?\\s*(\\d{10,11})").find(text)?.groupValues?.get(1).orEmpty(),Regex("(?:شماره حساب|حساب)\\s*[:：]?\\s*([\\d.-]{6,30})").find(text)?.groupValues?.get(1).orEmpty(),iban)
 }
}
