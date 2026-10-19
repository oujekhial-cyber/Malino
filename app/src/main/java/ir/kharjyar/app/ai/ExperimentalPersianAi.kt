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
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** خروجی محدود و قابل اعتبارسنجی مدل؛ مدل هرگز مستقیماً چیزی در دیتابیس نمی‌نویسد. */
data class AiTransactionDraft(
    val amountRial: Long,
    val direction: String,
    val category: String,
    val description: String,
    val explanation: String
)

/**
 * آزمایش جداشدنی Groq/Qwen. تنها ورودی صریح کاربر و نام دسته‌ها ارسال می‌شود؛
 * شماره حساب، موجودی، پیامک و تراکنش‌های قبلی هرگز وارد درخواست نمی‌شوند.
 */
object ExperimentalPersianAi {
    const val MODEL = "qwen/qwen3.8-27b"
    private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val KEY_ALIAS = "kharjyar_experimental_groq_key"
    private const val PREFS = "experimental_groq_secure"
    private const val TOKEN = "token"
    private const val LEGACY_PREFS = "experimental_ai_secure"
    private const val LEGACY_ALIAS = "kharjyar_experimental_ai_key"
    private val json = Json { ignoreUnknownKeys = true }

    fun hasToken(context: Context): Boolean {
        clearLegacyAvalAi(context)
        return readToken(context).isNotBlank()
    }

    fun saveToken(context: Context, token: String) {
        clearLegacyAvalAi(context)
        val clean = token.trim()
        if (clean.isBlank()) return clearToken(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val packed = Base64.encodeToString(cipher.iv + cipher.doFinal(clean.toByteArray()), Base64.NO_WRAP)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(TOKEN, packed).apply()
    }

    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(TOKEN).apply()
    }

    suspend fun interpretTransaction(
        context: Context,
        message: String,
        categoryNames: List<String>
    ): Result<AiTransactionDraft> = withContext(Dispatchers.IO) {
        runCatching {
            val token = readToken(context).takeIf(String::isNotBlank)
                ?: error("ابتدا کلید API رایگان Groq را ذخیره کنید.")
            require(message.isNotBlank()) { "متن درخواست خالی است." }
            val categories = categoryNames.take(80).joinToString("، ")
            val body = buildJsonObject {
                put("model", MODEL)
                put("temperature", 0.1)
                put("max_tokens", 350)
                put("response_format", buildJsonObject { put("type", "json_object") })
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", """
                            درخواست فارسی کاربر را فقط به پیش‌نویس یک تراکنش تبدیل کن.
                            فقط JSON معتبر برگردان: {"amount":عدد,"unit":"rial|toman","direction":"deposit|withdraw","category":"نام دسته","description":"توضیح کوتاه","explanation":"خلاصه فارسی"}
                            مبلغ باید مثبت باشد. اگر واحد گفته نشده، تومان فرض کن. direction فقط deposit یا withdraw باشد.
                            نزدیک‌ترین دسته را فقط از این فهرست انتخاب کن: $categories
                            هیچ عملیات مالی انجام نده و هیچ متن دیگری بیرون JSON ننویس.
                        """.trimIndent())
                    })
                    add(buildJsonObject { put("role", "user"); put("content", message.take(2_000)) })
                })
            }.toString()
            val raw = post(token, body)
            val content = json.parseToJsonElement(raw).jsonObject["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")
                ?.jsonPrimitive?.contentOrNull?.trim().takeUnless { it.isNullOrBlank() }
                ?: error("پاسخ قابل‌خواندن از Groq دریافت نشد.")
            parseDraft(content)
        }
    }

    private fun parseDraft(content: String): AiTransactionDraft {
        val clean = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val value = json.parseToJsonElement(clean).jsonObject
        val amount = value["amount"]?.jsonPrimitive?.longOrNull ?: error("مدل مبلغ معتبری برنگرداند.")
        require(amount > 0) { "مبلغ پیشنهادی باید بیشتر از صفر باشد." }
        val unit = value["unit"]?.jsonPrimitive?.contentOrNull?.lowercase()
        val amountRial = if (unit == "rial") amount else Math.multiplyExact(amount, 10L)
        val direction = value["direction"]?.jsonPrimitive?.contentOrNull?.lowercase().orEmpty()
        require(direction == "deposit" || direction == "withdraw") { "نوع واریز یا برداشت در پاسخ معتبر نیست." }
        return AiTransactionDraft(
            amountRial = amountRial,
            direction = direction,
            category = value["category"]?.jsonPrimitive?.contentOrNull.orEmpty().take(80),
            description = value["description"]?.jsonPrimitive?.contentOrNull.orEmpty().take(200),
            explanation = value["explanation"]?.jsonPrimitive?.contentOrNull.orEmpty().take(300)
        )
    }

    private fun post(token: String, body: String): String {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15_000; readTimeout = 45_000; doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error(apiError(code, raw, connection.getHeaderField("Retry-After")))
            raw
        } finally { connection.disconnect() }
    }

    private fun apiError(code: Int, raw: String, retryAfter: String?): String {
        val error = runCatching { json.parseToJsonElement(raw).jsonObject["error"]?.jsonObject }.getOrNull()
        val detail = error?.get("message")?.jsonPrimitive?.contentOrNull?.take(180)
        return when (code) {
            401 -> "کلید Groq معتبر نیست؛ یک کلید تازه در console.groq.com بسازید."
            403 -> "Groq این اتصال یا حساب را مجاز ندانست؛ احتمال محدودیت منطقه‌ای وجود دارد."
            429 -> "سهمیه رایگان یا محدودیت سرعت Groq پر شده است${retryAfter?.let { "؛ حدود $it ثانیه بعد امتحان کنید" }.orEmpty()}."
            else -> "Groq پاسخ نداد (کد $code)${detail?.let { ": $it" }.orEmpty()}"
        }
    }

    private fun clearLegacyAvalAi(context: Context) {
        context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(LEGACY_ALIAS)
        }
    }

    private fun encryptionKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }

    private fun readToken(context: Context): String {
        val packed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TOKEN, null) ?: return ""
        return runCatching {
            val bytes = Base64.decode(packed, Base64.NO_WRAP); require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)))
        }.getOrDefault("")
    }
}
