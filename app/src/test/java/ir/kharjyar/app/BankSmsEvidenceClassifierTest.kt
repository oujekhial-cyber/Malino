package ir.kharjyar.app

import ir.kharjyar.app.core.sms.BankSmsEvidenceClassifier
import ir.kharjyar.app.core.sms.BankSmsVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class BankSmsEvidenceClassifierTest {
 private fun verdict(body:String,known:Boolean=true)=BankSmsEvidenceClassifier.classify("BANK",body,known).verdict

 @Test fun `completed bank transactions need converging evidence`() {
  val samples=listOf(
   "برداشت از کارت ****1234 مبلغ 2,500,000 ریال انجام شد. مانده 8,100,000 ریال",
   "مبلغ 750000 تومان به حساب منتهی به 4321 واریز شد شماره پیگیری 928374",
   "خرید به مبلغ ۱,۲۳۰,۰۰۰ ریال از کارت **۸۶۰۵ انجام شد موجودی ۵,۰۰۰,۰۰۰ ریال",
   "حواله پایا مبلغ: 12,000,000 ریال از حساب **991 کسر گردید مرجع 88221"
  )
  samples.forEach{assertEquals(it,BankSmsVerdict.TRANSACTION,verdict(it))}
 }

 @Test fun `bank promotions with amounts or purchase words are not transactions`() {
  val samples=listOf(
   "جشنواره خرید بانک؛ تا 5,000,000 تومان جایزه، همین حالا ثبت نام کنید",
   "با خرید از فروشگاه‌های منتخب از تخفیف ویژه بهره مند شوید https://bank.ir/festival",
   "پیشنهاد ویژه اقساط 24 ماهه برای شما؛ برای اطلاعات بیشتر کلیک کنید",
   "قرعه‌کشی بزرگ بانک با 100000000 ریال جایزه؛ فرصت تا پایان ماه"
  )
  samples.forEach{assertEquals(it,BankSmsVerdict.PROMOTION,verdict(it))}
 }

 @Test fun `otp security and unrelated unread messages are rejected`() {
  assertEquals(BankSmsVerdict.OTP_OR_SECURITY,verdict("رمز پویای شما 483921 است اعتبار 120 ثانیه"))
  assertEquals(BankSmsVerdict.OTP_OR_SECURITY,verdict("ورود به همراه بانک از دستگاه جدید انجام شد"))
  assertEquals(BankSmsVerdict.NON_FINANCIAL,verdict("سلام جلسه امروز ساعت 17 برگزار می‌شود",false))
  assertEquals(BankSmsVerdict.NON_FINANCIAL,verdict("مانده اینترنت شما 5000 مگابایت است",false))
 }

 @Test fun `a long number or bank sender alone is never enough`() {
  assertEquals(BankSmsVerdict.NON_FINANCIAL,verdict("کد عضویت شما 123456789 است"))
  assertEquals(BankSmsVerdict.NON_FINANCIAL,verdict("از همراهی شما سپاسگزاریم"))
 }
}
