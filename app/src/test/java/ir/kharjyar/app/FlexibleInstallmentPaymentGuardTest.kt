package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexibleInstallmentPaymentGuardTest {
 @Test fun `installment payment accepts custom principal late fee and note`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  val entity=File("src/main/java/ir/kharjyar/app/data/db/PlanningEntities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  listOf("مبلغ اصلی پرداخت‌شده","جریمه دیرکرد (اختیاری)","توضیحات پرداخت (اختیاری)","paidAmountRial=enteredAmount","penaltyRial=enteredPenalty","paymentNote=paymentNote.trim()","residual>0","LoanInstallmentEntity(loanId=loan.id,number=residualNumber").forEach{assertTrue(it,screen.contains(it))}
  listOf("val paidAmountRial:Long?=null","val penaltyRial:Long=0","val paymentNote:String=\"\"").forEach{assertTrue(it,entity.contains(it))}
  listOf("version = 22","MIGRATION_19_20","ADD COLUMN paidAmountRial","ADD COLUMN penaltyRial","ADD COLUMN paymentNote").forEach{assertTrue(it,db.contains(it))}
 }
}
