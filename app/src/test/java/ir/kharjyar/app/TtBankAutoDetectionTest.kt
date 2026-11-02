package ir.kharjyar.app
import ir.kharjyar.app.core.sms.*
import org.junit.Assert.*
import org.junit.Test
class TtBankAutoDetectionTest {
 private val body="""2404.306.5918267.1
16,211,052-
18:42 07/05
مانده: 9,775,759"""
 @Test fun `TTBANK resolves to Tosee Taavon`(){assertEquals("توسعه تعاون",BankSenderResolver.bankName("TTBANK"));assertTrue(BankSenderResolver.sameBank("بانک توسعه تعاون","توسعه تعاون"))}
 @Test fun `titleless signed TTBank message extracts transaction automatically`(){assertEquals(SmsKind.SUSPICIOUS,SmsClassifier.classify(body));val x=Extractor.autoExtract(body);assertEquals(16_211_052L,x.amountRial);assertEquals(9_775_759L,x.balanceRial);assertEquals(ExtractedDirection.WITHDRAW,x.directionEnum());assertEquals("2404.306.5918267.1",x.accountIdHint)}
}
