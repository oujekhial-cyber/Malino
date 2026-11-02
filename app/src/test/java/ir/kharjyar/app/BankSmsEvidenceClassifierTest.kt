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

 @Test fun `compact live bank sms without currency unit remains a transaction`() {
  val samples=listOf(
   "2404.306.5918267.1\n-1,000,000\n07/03_01:53\nمانده: 10,466,426",
   "639-824-882613-1\n+16,240,000\nمانده 16,425,824\n1405/2/9-9:53",
   "بانک ملی ایران\nبرداشت:2,000,000-\nحساب:17000\nمانده:385,384\n0127-22:00",
   "بانک رفاه\nحساب213478470\nخرید1,000,000-\nمانده1,845,608\n06/04-14:32"
  )
  samples.forEach{assertEquals(it,BankSmsVerdict.TRANSACTION,verdict(it))}
 }

 @Test fun `signed personal number from unknown sender is not a transaction`() {
  assertEquals(BankSmsVerdict.NON_FINANCIAL,verdict("شماره من +1,234,567 است",false))
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
