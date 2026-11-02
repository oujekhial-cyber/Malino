package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import kotlin.math.abs

data class SmartMutationTarget(val type:String,val id:Long,val label:String,val detail:String)

/** تطبیق فقط محلی است؛ مدل اجازه انتخاب ID یا اجرای حذف را ندارد. نتیجه مبهم عمداً رد می‌شود. */
object SmartRecordMutationResolver {
 fun resolve(entityType:String,text:String,amount:Long?,quantity:Double?,accountHint:String,title:String,party:String,
             transactions:List<TransactionEntity>,accounts:List<AccountEntity>,loans:List<LoanEntity>,debts:List<DebtEntity>,people:List<DebtPersonEntity>,assets:List<AssetEntity>):SmartMutationTarget? {
  val q=Digits.normalizeForMatch("$text $title $party $accountHint").replace('‌',' ')
  val yesterday=q.contains("دیروز");val yesterdayDate=PersianDate.today().plusDays(-1)
  data class Candidate(val target:SmartMutationTarget,val score:Int,val at:Long,val value:Long?,val qty:Double?=null)
  val candidates=when(entityType){
   "transaction"->transactions.map{t->val acc=accounts.firstOrNull{it.id==t.accountId};Candidate(SmartMutationTarget("transaction",t.id,t.description.ifBlank{if(t.direction==TxDirection.DEPOSIT)"واریز" else "برداشت"},"${acc?.title.orEmpty()} • ${t.amountRial}"),wordScore(q,listOf(t.description,t.counterparty,acc?.title.orEmpty(),acc?.bankName.orEmpty())),t.occurredAt,t.amountRial)}
   "loan"->loans.map{l->Candidate(SmartMutationTarget("loan",l.id,l.title,"${l.party} • ${l.principalRial}"),wordScore(q,listOf(l.title,l.party,l.note)),l.startAt,l.principalRial)}
   "debt"->debts.map{d->val person=people.firstOrNull{it.id==d.personId};Candidate(SmartMutationTarget("debt",d.id,d.title,"${person?.name.orEmpty()} • ${d.amountRial}"),wordScore(q,listOf(d.title,person?.name.orEmpty())),d.createdAt,d.amountRial)}
   "asset"->assets.filter{it.active}.map{a->Candidate(SmartMutationTarget("asset",a.id,a.title,"${a.quantity} • ${a.purchasePriceRial}"),wordScore(q,listOf(a.title,a.note)),a.purchasedAt,a.purchasePriceRial,a.quantity)}
   else->emptyList()
  }.map{c->var score=c.score;if(amount!=null&&c.value!=null){if(c.value==amount)score+=8 else if(abs(c.value-amount)<=amount/100)score+=4};if(quantity!=null&&c.qty!=null&&abs(c.qty-quantity)<0.001)score+=7;if(yesterday&&PersianDate.fromMillis(c.at)==yesterdayDate)score+=6;c.copy(score=score)}.sortedByDescending{it.score}
  val best=candidates.firstOrNull()?:return null
  // حداقل یک نشانه قوی و اختلاف روشن با گزینه دوم لازم است؛ حذف مبهم هرگز پیشنهاد نمی‌شود.
  return best.target.takeIf{best.score>=6&&(candidates.getOrNull(1)?.score?:-1)<=best.score-2}
 }
 private fun wordScore(query:String,values:List<String>):Int=values.fold(0){total,v->val n=Digits.normalizeForMatch(v).replace('‌',' ');total+when{n.isBlank()->0;query.contains(n)->5;n.split(' ').filter{it.length>2}.any(query::contains)->2;else->0}}
}
