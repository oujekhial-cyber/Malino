package ir.kharjyar.app.core.sms

import ir.kharjyar.app.data.db.TransactionEntity

enum class ReconcileKind { MATCHED, MISSING_TRANSACTION, AMOUNT_MISMATCH }
data class ReconcileSms(val id:Long,val amountRial:Long,val direction:Int,val occurredAt:Long,val refNumber:String="")
data class ReconcilePhoneSms(val id:Long,val sender:String,val body:String,val at:Long,val accountId:Long?,val extraction:ExtractionResult)
data class ReconcileFinding(val sms:ReconcileSms,val kind:ReconcileKind,val transaction:TransactionEntity?=null,val amountDifferenceRial:Long=0)

/** One-to-one, account-scoped matching. Exact reference/amount wins; near-time unequal amounts are proposed, never auto-edited. */
object AccountSmsReconciler {
 fun reconcile(messages:List<ReconcileSms>,transactions:List<TransactionEntity>,accountId:Long):List<ReconcileFinding>{
  val txs=transactions.filter{it.accountId==accountId}.toMutableList();val used=mutableSetOf<Long>();val out=mutableListOf<ReconcileFinding>()
  messages.sortedBy{it.occurredAt}.forEach{sms->
   val sameDirection=txs.filter{it.id !in used&&it.direction==sms.direction}
   val exact=sameDirection.filter{it.amountRial==sms.amountRial}.minByOrNull{tx->
    val refPenalty=if(sms.refNumber.isNotBlank()&&tx.refNumber==sms.refNumber)0L else 3_600_000L
    kotlin.math.abs(tx.occurredAt-sms.occurredAt)+refPenalty
   }?.takeIf{kotlin.math.abs(it.occurredAt-sms.occurredAt)<=90_000L}
   if(exact!=null){used+=exact.id;out+=ReconcileFinding(sms,ReconcileKind.MATCHED,exact);return@forEach}
   // اختلاف مبلغ فقط وقتی پیشنهاد می‌شود که حساب، جهت، تاریخ و ساعت همان تراکنش باشند.
   val near=sameDirection.minByOrNull{kotlin.math.abs(it.occurredAt-sms.occurredAt)}?.takeIf{kotlin.math.abs(it.occurredAt-sms.occurredAt)<=90_000L}
   if(near!=null){used+=near.id;out+=ReconcileFinding(sms,ReconcileKind.AMOUNT_MISMATCH,near,kotlin.math.abs(near.amountRial-sms.amountRial))}
   else out+=ReconcileFinding(sms,ReconcileKind.MISSING_TRANSACTION)
  };return out
 }
}
