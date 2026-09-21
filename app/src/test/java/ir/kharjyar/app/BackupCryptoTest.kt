package ir.kharjyar.app

import ir.kharjyar.app.core.backup.BackupCrypto
import ir.kharjyar.app.core.backup.BackupPayload
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCryptoTest {

    @Test
    fun `encrypt decrypt roundtrip`() {
        val data = "سلام دنیا — داده تست خرج‌یار ۱۲۳".toByteArray(Charsets.UTF_8)
        val password = "test-pass-123".toCharArray()
        val encrypted = BackupCrypto.encrypt(data, password)
        val decrypted = BackupCrypto.decrypt(encrypted, "test-pass-123".toCharArray())
        assertArrayEquals(data, decrypted)
    }

    @Test
    fun `wrong password throws without corrupting`() {
        val data = "secret".toByteArray()
        val encrypted = BackupCrypto.encrypt(data, "correct".toCharArray())
        assertThrows(BackupCrypto.WrongPasswordOrCorruptException::class.java) {
            BackupCrypto.decrypt(encrypted, "wrong".toCharArray())
        }
    }

    @Test
    fun `corrupt file throws`() {
        val data = "secret".toByteArray()
        val encrypted = BackupCrypto.encrypt(data, "pass".toCharArray())
        encrypted[encrypted.size - 5] = (encrypted[encrypted.size - 5] + 1).toByte()
        assertThrows(BackupCrypto.WrongPasswordOrCorruptException::class.java) {
            BackupCrypto.decrypt(encrypted, "pass".toCharArray())
        }
    }

    @Test
    fun `invalid magic throws format exception`() {
        val garbage = ByteArray(100) { it.toByte() }
        assertThrows(BackupCrypto.BackupFormatException::class.java) {
            BackupCrypto.decrypt(garbage, "pass".toCharArray())
        }
    }

    @Test
    fun `truncated file throws format exception`() {
        assertThrows(BackupCrypto.BackupFormatException::class.java) {
            BackupCrypto.decrypt(ByteArray(10), "pass".toCharArray())
        }
    }

    @Test
    fun `fresh nonce per backup - ciphertexts differ`() {
        val data = "same data".toByteArray()
        val e1 = BackupCrypto.encrypt(data, "pass".toCharArray())
        val e2 = BackupCrypto.encrypt(data, "pass".toCharArray())
        assertFalse(e1.contentEquals(e2))
    }

    @Test
    fun `payload json roundtrip`() {
        val payload = BackupPayload(
            createdAt = 123456789L,
            settings = mapOf("theme_mode" to "DARK", "money_unit" to "TOMAN")
        )
        val restored = BackupPayload.fromJson(payload.toJson())
        assertEquals(payload.createdAt, restored.createdAt)
        assertEquals(payload.settings, restored.settings)
        assertEquals(1, restored.formatVersion)
    }

    @Test
    fun `decrypt on different 'device' with same password works`() {
        // شبیه‌سازی دستگاه دیگر: فقط بایت‌های فایل و رمز منتقل می‌شوند
        val payload = BackupPayload(createdAt = 1L).toJson().toByteArray()
        val file = BackupCrypto.encrypt(payload, "portable-pass".toCharArray())
        val restored = BackupCrypto.decrypt(file.copyOf(), "portable-pass".toCharArray())
        assertArrayEquals(payload, restored)
    }
}
