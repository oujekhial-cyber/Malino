package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsPastePurposeCompletionGuardTest {
 @Test fun `analyzed pasted sms can be classified and described before confirmation`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/SmsPasteScreen.kt").readText()
  listOf(
   "این تراکنش بابت چه بوده؟",
   "NaturePicker(nature=nature,direction=direction",
   "showDirection=false",
   "CategoryPicker(categories=categories",
   "توضیح واریز (اختیاری)",
   "توضیح برداشت (اختیاری)",
   "پیش از ثبت، حساب، نوع جریان مالی و بابت تراکنش را بررسی کنید.",
   "description.trim().ifBlank",
   "تأیید و ثبت تراکنش"
  ).forEach{assertTrue(it,screen.contains(it))}
 }
}
