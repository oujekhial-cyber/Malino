package ir.kharjyar.app.core.sms

import java.security.MessageDigest

/**
 * اثر انگشت پایدار پیامک برای جلوگیری از پردازش تکراری.
 * از فرستنده + متن کامل + دقیقه دریافت ساخته می‌شود؛
 * دو خرید واقعی هم‌مبلغ متن یا زمان متفاوتی دارند و یکی نمی‌شوند،
 * ولی Broadcast تکراری همان پیامک (در همان دقیقه) حذف می‌شود.
 */
object SmsFingerprint {
    fun of(sender: String, body: String, receivedAtMillis: Long): String {
        val minuteBucket = receivedAtMillis / 60_000L
        val input = "$sender|$body|$minuteBucket"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
