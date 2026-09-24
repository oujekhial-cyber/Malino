package ir.kharjyar.app.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * کلید رمزنگاری دیتابیس.
 *
 * یک عبارت عبور تصادفی ۳۲ بایتی ساخته می‌شود و خودش با کلیدی که داخل
 * Android Keystore نگه داشته می‌شود رمز می‌گردد. کلید Keystore هرگز از
 * سخت‌افزار امن خارج نمی‌شود، بنابراین حتی اگر کسی فایل‌های برنامه را
 * بردارد، بدون همان دستگاه نمی‌تواند دیتابیس را باز کند.
 */
object DatabaseKey {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "kharjyar_db_key"
    private const val PREFS = "kharjyar_secure"
    private const val PREF_PASSPHRASE = "db_passphrase"
    private const val GCM_TAG_BITS = 128
    private const val IV_LENGTH = 12

    /** عبارت عبور دیتابیس؛ در اولین اجرا ساخته و ذخیره می‌شود. */
    fun getOrCreate(context: Context): ByteArray {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(PREF_PASSPHRASE, null)
        if (stored != null) {
            runCatching { return decrypt(Base64.decode(stored, Base64.NO_WRAP)) }
            // اگر رمزگشایی شکست خورد (مثلاً کلید Keystore باطل شده)، کلید تازه می‌سازیم
        }
        val passphrase = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(PREF_PASSPHRASE, Base64.encodeToString(encrypt(passphrase), Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(GCM_TAG_BITS, blob, 0, IV_LENGTH)
        )
        return cipher.doFinal(blob, IV_LENGTH, blob.size - IV_LENGTH)
    }
}
