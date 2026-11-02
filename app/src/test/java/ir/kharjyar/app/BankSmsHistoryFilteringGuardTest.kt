package ir.kharjyar.app
import ir.kharjyar.app.core.sms.BankSenderResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
class BankSmsHistoryFilteringGuardTest {
 @Test fun `common named bank senders resolve without bank name in message body`() {
  assertEquals("ملت",BankSenderResolver.bankName("MELLAT"))
  assertEquals("اقتصاد نوین",BankSenderResolver.bankName("ENBANK"))
  assertEquals("کشاورزی",BankSenderResolver.bankName("BKI"))
  assertEquals("رفاه",BankSenderResolver.bankName("BANKREFAH"))
 }
 @Test fun `history classifies and extracts before accepting mapped inferred or account matched bank`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  listOf("SmsClassifier.isOtp(body)","SmsClassifier.classify(body)==SmsKind.NON_FINANCIAL","val bankSender=inferredBank!=null&&BankSenderResolver.sameBank","!mappedSender&&!bankSender&&!bankInBody","senderAccountIds.singleOrNull()").forEach{assertTrue(it,source.contains(it))}
 }
}
