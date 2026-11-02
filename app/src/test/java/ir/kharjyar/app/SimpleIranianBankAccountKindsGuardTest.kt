package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleIranianBankAccountKindsGuardTest {
 @Test fun usefulIranianAccountKindsAreAvailableInOneSimpleSelector(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
  listOf("قرض‌الحسنه جاری","قرض‌الحسنه پس‌انداز","کوتاه‌مدت عادی","کوتاه‌مدت ویژه","بلندمدت","حساب حقوق","حساب وکالتی","حساب تسهیلات","حساب امتیازی","پس‌انداز مسکن","حساب ارزی","حساب تجاری","حساب مشترک","حساب دیجیتال","سپرده وثیقه‌ای","سایر").forEach{assertTrue(it,s.contains(it))}
  assertTrue(s.contains("bankAccountKinds.map{it.code}"))
  assertTrue(s.contains("selectedKind.subtitle"))
 }
 @Test fun interestSettingsStayProgressivelyDisclosed(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
  assertTrue(s.contains("if(selectedKind.interestCapable)"))
  assertTrue(s.contains("if(monthlyInterestBearing)"))
  assertTrue(s.contains("فقط واریز واقعی پیامک بانک ثبت می‌شود"))
 }
}
