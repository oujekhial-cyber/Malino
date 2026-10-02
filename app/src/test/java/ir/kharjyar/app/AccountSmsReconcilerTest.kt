package ir.kharjyar.app
import ir.kharjyar.app.core.sms.*
import ir.kharjyar.app.data.db.*
import org.junit.Assert.*
import org.junit.Test
class AccountSmsReconcilerTest{
 private fun tx(id:Long,account:Long,amount:Long,time:Long)=TransactionEntity(id=id,accountId=account,amountRial=amount,direction=TxDirection.WITHDRAW,occurredAt=time,recordedAt=time)
 @Test fun exactMatchesAreHiddenAndOtherAccountsNeverMatch(){
  val result=AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1000,TxDirection.WITHDRAW,10_000)),listOf(tx(1,7,1000,10_100),tx(2,8,1000,10_000)),7)
  assertEquals(ReconcileKind.MATCHED,result.single().kind);assertEquals(1,result.single().transaction!!.id)
 }
 @Test fun nearbyWrongAmountIsReportedForCorrection(){
  val result=AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1500,TxDirection.WITHDRAW,10_000)),listOf(tx(3,7,1000,11_000)),7).single()
  assertEquals(ReconcileKind.AMOUNT_MISMATCH,result.kind);assertEquals(500,result.amountDifferenceRial)
 }
 @Test fun absentTransactionIsReported(){assertEquals(ReconcileKind.MISSING_TRANSACTION,AccountSmsReconciler.reconcile(listOf(ReconcileSms(1,1500,TxDirection.WITHDRAW,10_000)),emptyList(),7).single().kind)}
}
