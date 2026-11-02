package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class RationCardHubGuardTest {
 @Test fun `civic center offers safe local ration card hub`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText()
  listOf(
   "کالابرگ خانواده","RationCardHub","tel:*500*1463%23","tel:0216369",
   "V.Refah","پیامک رسمی کالابرگ لینک ندارد","ration_card_local",
   "تعداد اعضای خانوار","مانده‌ای که از مسیر رسمی دیده‌اید",
   "این اطلاعات فقط روی همین گوشی ذخیره می‌شود","ملاک قطعی مشمول‌بودن"
  ).forEach{assertTrue(it,screen.contains(it))}
 }
}
