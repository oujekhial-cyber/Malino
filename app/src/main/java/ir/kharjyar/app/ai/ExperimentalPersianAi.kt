package ir.kharjyar.app.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * قابلیت آزمایشی و کاملاً جداشدنی دستیار فارسی.
 * این ماژول هیچ دسترسی‌ای به Room، پیامک‌ها، حساب‌ها یا تراکنش‌ها ندارد.
 */
object ExperimentalPersianAi {
    const val MODEL = "qwen3.8-flash"
    private const val ENDPOINT = "https://api.avalai.ir/v1/chat/completions"
    private const val KEY_ALIAS = "kharjyar_experimental_ai_key"
    private const val PREFS = "experimental_ai_secure"
    private const val TOKEN = "token"
    private val json = Json { ignoreUnknownKeys = true }

    fun hasToken(context: Context): Boolean = readToken(context).isNotBlank()

    fun saveToken(context: Context, token: String) {
        val clean = token.trim()
        if (clean.isBlank()) {
            clearToken(context)
            return
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val encrypted = cipher.doFinal(clean.toByteArray(Charsets.UTF_8))
        val packed = Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(TOKEN, packed).apply()
    }

    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(TOKEN).apply()
    }

    suspend fun ask(context: Context, message: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val token = readToken(context).takeIf { it.isNotBlank() }
                ?: error("ابتدا کلید API را در تنظیمات ذخیره کنید.")
            require(message.isNotBlank()) { "متن پرسش خالی است." }
            val body = buildJsonObject {
                put("model", MODEL)
                put("temperature", 0.2)
                put("max_tokens", 500)
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", "به فارسی روان و کوتاه پاسخ بده. تو دستیار آزمایشی خرج‌یار هستی. هیچ ادعایی درباره دسترسی به حساب‌ها یا تراکنش‌های کاربر نکن و توصیه مالی قطعی نده.")
                    })
                    add(buildJsonObject { put("role", "user"); put("content", message.take(4_000)) })
                })
            }.toString()
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) error(apiError(code, raw))
                json.parseToJsonElement(raw).jsonObject["choices"]?.jsonArray
                    ?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")
                    ?.jsonPrimitive?.contentOrNull?.trim().takeUnless { it.isNullOrBlank() }
                    ?: error("پاسخ قابل‌خواندن از سرویس دریافت نشد.")
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun apiError(code: Int, raw: String): String {
        val detail = runCatching {
            json.parseToJsonElement(raw).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        }.getOrNull()?.take(180)
        return when (code) {
            401, 403 -> "کلید API پذیرفته نشد یا دسترسی مدل فعال نیست."
            429 -> "سهمیه یا محدودیت سرعت سرویس تمام شده است؛ کمی بعد دوباره امتحان کنید."
            else -> "سرویس هوش مصنوعی پاسخ نداد (کد $code)${detail?.let { ": $it" }.orEmpty()}"
        }
    }

    private fun encryptionKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
            generateKey()
        }
    }

    private fun readToken(context: Context): String {
        val packed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TOKEN, null) ?: return ""
        return runCatching {
            val bytes = Base64.decode(packed, Base64.NO_WRAP)
            require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        }.getOrDefault("")
    }
}
