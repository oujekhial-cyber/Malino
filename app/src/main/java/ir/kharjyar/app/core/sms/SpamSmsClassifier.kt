package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

data class SpamDecision(val confident:Boolean,val reason:String,val score:Int)

/** تشخیص محافظه‌کارانه تبلیغات؛ یک واژه یا صرف وجود مبلغ برای حذف کافی نیست. */
object SpamSmsClassifier {
 private val promo=listOf("تخفیف","فروش ویژه","پیشنهاد ویژه","جشنواره","قرعه کشی","قرعه‌کشی","کد تخفیف","هدیه خرید","حراج","اقساط ویژه","همین حالا خرید","فرصت ویژه")
 private val calls=listOf("تماس بگیرید","سفارش دهید","خرید کنید","ثبت نام کنید","ثبت‌نام کنید","کلیک کنید","مشاهده کنید")
 private val financial=listOf("برداشت","واریز","واریزی","مانده","موجودی","بدهکار","بستانکار","خرید","کسر","پرداخت","تراکنش","شماره پیگیری","حواله","کارت به کارت","کارت‌به‌کارت","پایا","ساتنا","سود سپرده","سود ماهانه","سود علی‌الحساب","واریز سود")
 fun decide(body:String):SpamDecision{
  val t=Digits.normalizeForMatch(body).lowercase()
  val amount=Regex("\\d{1,3}(?:[,،٬]\\d{3})+|\\d{5,}").containsMatchIn(t)
  val strongFinancial=financial.any(t::contains)&&amount
  if(strongFinancial)return SpamDecision(false,"ساختار مالی معتبر",0)
  var score=0;val reasons=mutableListOf<String>()
  val promoCount=promo.count(t::contains);if(promoCount>0){score+=minOf(2,promoCount);reasons+="عبارت تبلیغاتی"}
  if(calls.any(t::contains)){score++;reasons+="دعوت به اقدام"}
  if(Regex("https?://|www\\.|\\.ir(?:/|\\s|$)|\\.com(?:/|\\s|$)").containsMatchIn(t)){score++;reasons+="پیوند تبلیغاتی"}
  if(Regex("لغو\\s*1?1|ارسال\\s*لغو").containsMatchIn(t)){score+=2;reasons+="کد لغو تبلیغات"}
  if(Regex("(?:تماس|تلفن|سفارش)\\s*[:：]?\\s*0?9\\d{9}").containsMatchIn(t)){score++;reasons+="شماره سفارش"}
  return SpamDecision(score>=2,reasons.joinToString("، ").ifBlank{"نامطمئن"},score)
 }
}
