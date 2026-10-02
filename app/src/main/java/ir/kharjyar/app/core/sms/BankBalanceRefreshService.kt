package ir.kharjyar.app.core.sms

import android.content.Context
import android.provider.Telephony
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountSenderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LatestAccountBalanceSms(val smsId:Long,val sender:String,val body:String,val sentAt:Long,val occurredAt:Long,val balanceRial:Long)

/** Reads only messages that explicitly identify this account; this avoids cross-account contamination. */
object BankBalanceRefreshService {
 suspend fun latest(context:Context,account:AccountEntity,siblingAccounts:List<AccountEntity>,mappings:List<AccountSenderEntity>):LatestAccountBalanceSms?=withContext(Dispatchers.IO){
  val matchables=(siblingAccounts+account).distinctBy{it.id}.map{MatchableAccount(it.id,it.maskedNumber,it.accountNumber,it.iban,it.cardNumber)}
  val bankMappings=mappings.filter{it.accountId==account.id}.map{AccountMatcher.normalizeSender(it.sender)}.toSet()
  var best:LatestAccountBalanceSms?=null
  context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,arrayOf("_id","address","body","date"),null,null,"date DESC")?.use{c->
   while(c.moveToNext()){
    val id=c.getLong(0);val sender=c.getString(1).orEmpty();val body=c.getString(2).orEmpty();val sent=c.getLong(3)
    val senderOk=AccountMatcher.normalizeSender(sender) in bankMappings || BankSenderResolver.bankName(sender)?.let{BankSenderResolver.sameBank(account.bankName,it)}==true || Digits.normalizeForMatch(body).contains(Digits.normalizeForMatch(account.bankName))
    if(!senderOk)continue
    // A unique explicit 4–6 digit/card/account/IBAN fragment is mandatory, even when sender is known.
    val match=AccountNumberMatcher.match(body,matchables) as? AccountMatch.Single ?: continue
    if(match.accountId!=account.id)continue
    val ex=Extractor.autoExtract(body);val balance=ex.balanceRial?:continue
    val occurred=ex.occurredAtMillis?.takeIf{it in 1..sent+86_400_000L}?:sent
    val row=LatestAccountBalanceSms(id,sender,body,sent,occurred,balance)
    if(best==null||row.occurredAt>best!!.occurredAt)best=row
   }
  };best
 }
}
