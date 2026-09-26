package ir.kharjyar.app

import ir.kharjyar.app.core.sms.AccountMatcher
import ir.kharjyar.app.core.sms.Extractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetSettingsAndAccountHintTest {
 @Test fun `short account hint preserves separators and removes masks`() {
  assertEquals("18267.1", AccountMatcher.shortIdentifier("2404.306.5918267.1"))
  assertEquals("12-3456", AccountMatcher.shortIdentifier("****12-3456"))
  assertFalse(AccountMatcher.shortIdentifier("6037****1234").contains('*'))
 }
 @Test fun `extractor retains punctuated account number`() {
  val r=Extractor.autoExtract("برداشت از حساب 2404.306.5918267.1 مبلغ 1,000,000 ریال مانده 2,000,000")
  assertEquals("2404.306.5918267.1",r.accountIdHint)
 }
 @Test fun `widget editor uses one selected element size and position control`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/WidgetSettingsScreen.kt").readText()
  assertTrue(s.contains("جزء ویجت"));assertTrue(s.contains("جای قرارگیری"));assertTrue(s.contains("بازنشانی تنظیمات ویجت"));assertTrue(s.contains("تم ویجت"))
 }
 @Test fun `weather has independent widget view and settings`() {
  val renderer=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
  assertTrue(renderer.contains("R.id.w_weather"));assertTrue(renderer.contains("widgetWeatherSize"));assertTrue(renderer.contains("widgetWeatherAlign"))
 }
}
