package ir.kharjyar.app

import ir.kharjyar.app.ui.screens.calculateLoanPlan
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticLoanCalculationTest {
 @Test fun `zero interest divides principal and preserves exact total`() {
  val plan=calculateLoanPlan(120_000_000,0.0,12)
  assertEquals(10_000_000,plan.monthlyRial)
  assertEquals(120_000_000,plan.totalRial)
  assertEquals(0,plan.interestRial)
  assertEquals(plan.totalRial,plan.installments.sum())
 }
 @Test fun `positive annual rate creates an amortized exact schedule`() {
  val plan=calculateLoanPlan(1_000_000_000,18.0,24)
  assertTrue(plan.monthlyRial>0)
  assertTrue(plan.interestRial>0)
  assertEquals(24,plan.installments.size)
  assertEquals(plan.totalRial,plan.installments.sum())
 }
 @Test fun `form persists rate term total and migration`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  listOf("درصد سود سالانه","مدت بازپرداخت (ماه)","محاسبه خودکار اقساط","سود کل","جمع بازپرداخت","annualInterestRatePercent=rate","totalRepayableRial=calculated.totalRial").forEach{assertTrue(it,screen.contains(it))}
  listOf("version = 24","MIGRATION_21_22","ADD COLUMN annualInterestRatePercent","ADD COLUMN termMonths","ADD COLUMN totalRepayableRial").forEach{assertTrue(it,db.contains(it))}
 }
}
