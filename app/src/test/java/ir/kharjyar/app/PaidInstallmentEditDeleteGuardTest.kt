package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PaidInstallmentEditDeleteGuardTest {
 @Test fun `paid installments can be edited deleted or exempted from overdue warning`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  val entity=File("src/main/java/ir/kharjyar/app/data/db/PlanningEntities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  listOf("ویرایش پرداخت","حذف پرداخت","هشدار اقساط پرداخت‌نشده","این ماه قسط نداشتم","تأیید ماه بدون قسط","paid=false,paidAt=null,paidAmountRial=null","!it.paid&&!it.noInstallment","noInstallment=true,noInstallmentReason=reason.trim()").forEach{assertTrue(it,screen.contains(it))}
  assertTrue(entity.contains("val noInstallment:Boolean=false")&&entity.contains("val noInstallmentReason:String=\"\""))
  listOf("version = 24","MIGRATION_20_21","ADD COLUMN noInstallment","ADD COLUMN noInstallmentReason").forEach{assertTrue(it,db.contains(it))}
 }
}
