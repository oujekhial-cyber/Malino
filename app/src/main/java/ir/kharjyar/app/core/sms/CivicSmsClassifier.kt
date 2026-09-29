package ir.kharjyar.app.core.sms

import ir.kharjyar.app.data.db.CivicMessageKind
import java.security.MessageDigest

object CivicSmsClassifier {
 fun classify(sender:String,body:String):Int? { val t=(sender+" "+body).lowercase()
  return when {
   listOf("عدل ایران","adliran","adl-iran","ثنا","ابلاغیه","ابلاغ الکترونیک","قوه قضاییه").any(t::contains)->CivicMessageKind.ADLIRAN
   listOf("راهور","rahvar","جریمه","تخلف رانندگی","پلاک","قبض جریمه").any(t::contains)->CivicMessageKind.TRAFFIC_FINE
   listOf("بیمه","insurance","بیمه نامه","بیمه‌نامه","تامین اجتماعی","تأمین اجتماعی").any(t::contains)->CivicMessageKind.INSURANCE
   (listOf("قبض","شناسه قبض","شناسه پرداخت","مهلت پرداخت").any(t::contains)&&listOf("آب","برق","گاز","توانیر","آبفا").any(t::contains))->CivicMessageKind.UTILITY_BILL
   else->null
  }
 }
 fun fingerprint(sender:String,body:String,receivedAt:Long):String { val raw="$sender\u0000$body\u0000$receivedAt";return MessageDigest.getInstance("SHA-256").digest(raw.toByteArray()).joinToString(""){"%02x".format(it)} }
}
