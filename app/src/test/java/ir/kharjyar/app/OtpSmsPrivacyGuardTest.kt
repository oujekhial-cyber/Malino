package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpSmsPrivacyGuardTest {
    @Test fun `non financial otp exits ingestion before fingerprint and database row creation`() {
        val source = File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
        val ingest = source.substring(source.indexOf("suspend fun ingestSms("), source.indexOf("suspend fun blockSender("))
        val reject = ingest.indexOf("if (kind == SmsKind.NON_FINANCIAL) return")
        val fingerprint = ingest.indexOf("SmsFingerprint.of")
        val row = ingest.indexOf("SmsCandidateEntity(")
        assertTrue(reject >= 0)
        assertTrue(reject < fingerprint)
        assertTrue(reject < row)
    }
}
