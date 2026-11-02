package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.StockSide

data class ParsedStockTrade(val broker:String,val symbol:String,val side:Int,val quantity:Long,val unitPriceRial:Long,val totalRial:Long)

/** استخراج محافظه‌کارانه پیامک انجام معامله بورس؛ سفارش ثبت‌نشده پذیرفته نمی‌شود. */
object StockTradeSmsParser {
 private val brokerHints=listOf("کارگزاری","ایزی تریدر","آگاه","مفید","فارابی","اقتصاد بیدار","مبین سرمایه","بانک پاسارگاد","بورسیران","سامانه معاملات")
 private fun numberAfter(text:String,vararg labels:String):Long?=labels.asSequence().mapNotNull{label->Regex("$label\\s*[:：]?\\s*([0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]*)").find(text)?.groupValues?.get(1)?.let{Digits.parseAmount(it)}}.firstOrNull()
 fun parse(sender:String,body:String):ParsedStockTrade? {
  val text=Digits.normalizeForMatch(body)
  val isBroker=brokerHints.any{text.contains(it,ignoreCase=true)}||sender.contains("broker",true)||sender.contains("bourse",true)
  if(!isBroker||(!text.contains("انجام شد")&&!text.contains("انجام گردید")&&!text.contains("معامله")))return null
  val side=when{Regex("خرید|خريـد").containsMatchIn(text)->StockSide.BUY;Regex("فروش").containsMatchIn(text)->StockSide.SELL;else->return null}
  val symbol=sequenceOf("نماد","سهم").mapNotNull{label->Regex("$label\\s*[:：]?\\s*([آ-یA-Za-z0-9‌_-]{2,20})").find(text)?.groupValues?.get(1)}.firstOrNull()?.trim()?:return null
  val quantity=numberAfter(text,"تعداد","حجم")?:return null
  val unit=numberAfter(text,"قیمت هر سهم","قیمت","نرخ")?:0L
  val total=numberAfter(text,"ارزش معامله","مبلغ کل","مبلغ")?:if(unit>0)quantity*unit else return null
  if(quantity<=0||total<=0)return null
  val broker=brokerHints.firstOrNull{text.contains(it,ignoreCase=true)}?.let{if(it=="کارگزاری")Regex("کارگزاری\\s+([آ-ی‌ ]{2,30})").find(text)?.groupValues?.get(1)?.trim()?:it else it}?:sender
  return ParsedStockTrade(broker,symbol,side,quantity,if(unit>0)unit else total/quantity,total)
 }
}
