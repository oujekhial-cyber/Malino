package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonDebtDetailExperienceGuardTest {
 @Test fun `person ledger explains and visualizes staged repayments`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  listOf(
   "ModernSummaryHero(",
   "دفتر مالی و سابقه بازپرداخت‌های این شخص",
   "بازپرداخت چندمرحله‌ای",
   "لازم نیست کل مبلغ را یک‌جا تسویه کنید",
   "val progress=(paidTotal.toFloat()",
   "پیشرفت بازپرداخت",
   "مرحله ثبت‌شده",
   "ثبت دریافت مرحله‌ای طلب",
   "ثبت پرداخت مرحله‌ای بدهی",
   "مبلغ دریافتی این مرحله",
   "مبلغ پرداختی این مرحله",
   "مانده خودکار محاسبه می‌شود",
   "این مورد به‌طور کامل تسویه شده است",
   "history.forEachIndexed"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
