package ir.kharjyar.app.core.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * رمزنگاری بکاپ: AES-256-GCM با کلید مشتق‌شده از رمز عبور کاربر
 * توسط PBKDF2-HMAC-SHA256 (210,000 تکرار، مطابق توصیه OWASP 2023).
 *
 * قالب فایل (نسخه ۱):
 *   [8B magic "KHRJBKP1"][1B version=1][4B iterations BE][16B salt][12B nonce][ciphertext+tag]
 *
 * کلید به دستگاه وابسته نیست؛ بازیابی روی هر دستگاه با همان رمز ممکن است.
 * GCM صحت و اصالت محتوا را کنترل می‌کند (رمز اشتباه/فایل خراب => خطا).
 */
object BackupCrypto {

    private val MAGIC = "KHRJBKP1".toByteArray(Charsets.US_ASCII)
    /** فایل بدون رمز (کاربر رمزگذاری را خاموش کرده است). */
    private val MAGIC_PLAIN = "KHRJBKP0".toByteArray(Charsets.US_ASCII)
    private const val VERSION: Byte = 1
    const val PBKDF2_ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val SALT_LEN = 16
    private const val NONCE_LEN = 12
    private const val TAG_BITS = 128

    class BackupFormatException(message: String) : Exception(message)
    class WrongPasswordOrCorruptException : Exception("رمز اشتباه یا فایل خراب است")
    /** فایل رمزدار است و برای باز کردن به رمز نیاز دارد. */
    class PasswordRequiredException : Exception("این فایل رمزدار است")

    /** آیا این فایل برای بازگشایی به رمز نیاز دارد؟ */
    fun isEncrypted(fileBytes: ByteArray): Boolean {
        if (fileBytes.size < MAGIC.size) return false
        return fileBytes.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)
    }

    /** بسته‌بندی بدون رمزنگاری: [8B magic "KHRJBKP0"][1B version][payload] */
    fun packPlain(plaintext: ByteArray): ByteArray =
        MAGIC_PLAIN + byteArrayOf(VERSION) + plaintext

    private fun unpackPlain(fileBytes: ByteArray): ByteArray {
        val headerLen = MAGIC_PLAIN.size + 1
        if (fileBytes.size <= headerLen) throw BackupFormatException("فایل بکاپ ناقص است")
        val version = fileBytes[MAGIC_PLAIN.size]
        if (version != VERSION) throw BackupFormatException("نسخه بکاپ ($version) پشتیبانی نمی‌شود")
        return fileBytes.copyOfRange(headerLen, fileBytes.size)
    }

    /**
     * خواندن محتوای فایل صرف‌نظر از رمزدار بودن.
     * اگر فایل رمزدار باشد و [password] داده نشده باشد، [PasswordRequiredException] پرتاب می‌شود.
     */
    fun open(fileBytes: ByteArray, password: CharArray?): ByteArray {
        if (fileBytes.size >= MAGIC_PLAIN.size &&
            fileBytes.copyOfRange(0, MAGIC_PLAIN.size).contentEquals(MAGIC_PLAIN)
        ) {
            return unpackPlain(fileBytes)
        }
        if (!isEncrypted(fileBytes)) throw BackupFormatException("این فایل بکاپ خرج‌یار نیست")
        if (password == null) throw PasswordRequiredException()
        return decrypt(fileBytes, password)
    }

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LEN).also(random::nextBytes)
        val nonce = ByteArray(NONCE_LEN).also(random::nextBytes)
        val key = deriveKey(password, salt, PBKDF2_ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(MAGIC)
        val ct = cipher.doFinal(plaintext)
        return MAGIC + byteArrayOf(VERSION) + intToBytes(PBKDF2_ITERATIONS) + salt + nonce + ct
    }

    fun decrypt(fileBytes: ByteArray, password: CharArray): ByteArray {
        val headerLen = MAGIC.size + 1 + 4 + SALT_LEN + NONCE_LEN
        if (fileBytes.size <= headerLen) throw BackupFormatException("فایل بکاپ ناقص است")
        val magic = fileBytes.copyOfRange(0, MAGIC.size)
        if (!magic.contentEquals(MAGIC)) throw BackupFormatException("این فایل بکاپ خرج‌یار نیست")
        val version = fileBytes[MAGIC.size]
        if (version != VERSION) throw BackupFormatException("نسخه بکاپ ($version) پشتیبانی نمی‌شود")
        var off = MAGIC.size + 1
        val iterations = bytesToInt(fileBytes, off); off += 4
        if (iterations < 10_000 || iterations > 10_000_000) throw BackupFormatException("پارامتر نامعتبر")
        val salt = fileBytes.copyOfRange(off, off + SALT_LEN); off += SALT_LEN
        val nonce = fileBytes.copyOfRange(off, off + NONCE_LEN); off += NONCE_LEN
        val ct = fileBytes.copyOfRange(off, fileBytes.size)
        val key = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(MAGIC)
        return try {
            cipher.doFinal(ct)
        } catch (e: Exception) {
            throw WrongPasswordOrCorruptException()
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        val key = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(key, "AES")
    }

    private fun intToBytes(v: Int) = byteArrayOf(
        (v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte()
    )

    private fun bytesToInt(b: ByteArray, off: Int): Int =
        ((b[off].toInt() and 0xFF) shl 24) or ((b[off + 1].toInt() and 0xFF) shl 16) or
            ((b[off + 2].toInt() and 0xFF) shl 8) or (b[off + 3].toInt() and 0xFF)
}
