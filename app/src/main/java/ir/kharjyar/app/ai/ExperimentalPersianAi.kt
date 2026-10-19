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

data class AiAppCommand(
    val action: String,
    val route: String? = null,
    val transaction: AiTransactionDraft? = null,
    val amountRial: Long? = null,
    val party: String = "",
    val accountHint: String = "",
    val title: String = "",
    val explanation: String = ""
)

/**
 * آزمایش جداشدنی Groq/Qwen. تنها ورودی صریح کاربر و نام دسته‌ها ارسال می‌شود؛
 * شماره حساب، موجودی، پیامک و تراکنش‌های قبلی هرگز وارد درخواست نمی‌شوند.
 */
enum class AiProvider(val title: String, val model: String, val endpoint: String) {
    GROQ("Groq", "qwen/qwen3.8-27b", "https://api.groq.com/openai/v1/chat/completions"),
    AVALAI("AvalAI", "qwen3.8-flash", "https://api.avalai.ir/v1/chat/completions")
}

object ExperimentalPersianAi {
    private const val PROVIDER_PREFS = "experimental_ai_provider"
    private const val ACTIVE_PROVIDER = "active_provider"
    private const val TOKEN = "token"
    private val json = Json { ignoreUnknownKeys = true }

    fun activeProvider(context: Context): AiProvider = runCatching {
        AiProvider.valueOf(context.getSharedPreferences(PROVIDER_PREFS, Context.MODE_PRIVATE)
            .getString(ACTIVE_PROVIDER, AiProvider.GROQ.name).orEmpty())
    }.getOrDefault(AiProvider.GROQ)

    fun setActiveProvider(context: Context, provider: AiProvider) {
        context.getSharedPreferences(PROVIDER_PREFS, Context.MODE_PRIVATE).edit()
            .putString(ACTIVE_PROVIDER, provider.name).apply()
    }

    private fun securePrefs(provider: AiProvider) = "experimental_${provider.name.lowercase()}_secure"
    private fun keyAlias(provider: AiProvider) = "kharjyar_experimental_${provider.name.lowercase()}_key"

    fun hasToken(context: Context, provider: AiProvider = activeProvider(context)): Boolean =
        readToken(context, provider).isNotBlank()

    fun saveToken(context: Context, token: String, provider: AiProvider = activeProvider(context)) {
        val clean = token.trim()
        if (clean.isBlank()) return clearToken(context, provider)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(provider))
        val packed = Base64.encodeToString(cipher.iv + cipher.doFinal(clean.toByteArray()), Base64.NO_WRAP)
        context.getSharedPreferences(securePrefs(provider), Context.MODE_PRIVATE).edit().putString(TOKEN, packed).apply()
    }

    fun clearToken(context: Context, provider: AiProvider = activeProvider(context)) {
        context.getSharedPreferences(securePrefs(provider), Context.MODE_PRIVATE).edit().remove(TOKEN).apply()
    }

    suspend fun interpretCommand(
        context: Context,
        message: String,
        categoryNames: List<String>
    ): Result<AiAppCommand> = withContext(Dispatchers.IO) {
        runCatching {
            val provider = activeProvider(context)
            val token = readToken(context, provider).takeIf(String::isNotBlank)
                ?: error("ابتدا کلید API سرویس ${provider.title} را در تنظیمات ذخیره کنید.")
            require(message.isNotBlank()) { "متن فرمان خالی است." }
            val routes = "transactions,reports,accounts,categories,debts,loans,checks,assets,vehicles,reminders,notes,review,smsPaste,marketPulse,backup,settings,manual,home,notifications,profile,widgetSettings,civicCenter"
            val categories = categoryNames.take(80).joinToString("، ")
            val body = buildJsonObject {
                put("model", provider.model); put("temperature", 0.0); put("max_tokens", 420)
                put("response_format", buildJsonObject { put("type", "json_object") })
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", """
                            فرمان فارسی کاربر برای برنامه مدیریت مالی را فقط به JSON تبدیل کن.
                            اگر ثبت برداشت/واریز است: {"action":"transaction","amount":عدد,"unit":"rial|toman","direction":"deposit|withdraw","category":"نام دسته","description":"شرح","explanation":"خلاصه"}
                            اگر دریافت وام است: {"action":"loan_borrowed","amount":عدد,"unit":"rial|toman","party":"بانک یا وام‌دهنده","account":"نام حساب مقصد","title":"عنوان","explanation":"خلاصه"}
                            اگر وام‌دادن است: {"action":"loan_lent","amount":عدد,"unit":"rial|toman","party":"وام‌گیرنده","account":"حساب پرداخت","title":"عنوان","explanation":"خلاصه"}
                            اگر قرض‌دادن/طلب است: {"action":"debt_receivable","amount":عدد,"unit":"rial|toman","party":"شخص","account":"حساب پرداخت","title":"عنوان","explanation":"خلاصه"}
                            اگر قرض‌گرفتن/بدهی است: {"action":"debt_payable","amount":عدد,"unit":"rial|toman","party":"شخص","account":"حساب دریافت","title":"عنوان","explanation":"خلاصه"}
                            برای ثبت تخصصی سایر بخش‌ها از این actionها استفاده کن و در صورت وجود amount/unit/title/party/account را هم بده:
                            asset_record برای خرید/فروش/ثبت طلا، ارز، سهام، ملک یا دارایی؛ reminder_create برای یادآور؛ check_issued یا check_received برای چک؛ note_create برای یادداشت؛ vehicle_record برای خودرو، هزینه یا سرویس آن؛ installment_payment برای پرداخت قسط؛ civic_record برای قبض، جریمه یا خدمات شهروندی.
                            اصل وام، قرض، خرید دارایی، پرداخت قسط و انتقال‌ها هرگز income یا expense عادی نیستند و باید action تخصصی بگیرند.
                            اگر درخواست بازکردن، دیدن، فهرست یا مدیریت یک بخش است: {"action":"navigate","route":"شناسه","explanation":"خلاصه"}
                            شناسه route فقط یکی از اینها باشد: $routes
                            نگاشت: تراکنش=transactions، گزارش=reports، حساب=accounts، دسته=categories، بدهی/طلب=debts، وام/قسط=loans، چک=checks، دارایی/طلا/ارز=assets، خودرو=vehicles، یادآوری=reminders، یادداشت=notes، پیامک بانکی=review، چسباندن پیامک=smsPaste، بازار=marketPulse، پشتیبان=backup، تنظیمات=settings، ثبت دستی=manual، خانه=home، اعلان=notifications، پروفایل=profile، ویجت=widgetSettings، خدمات شهروندی=civicCenter.
                            دسته تراکنش فقط از این فهرست: $categories. اگر واحد نیامده تومان فرض کن. هیچ متن دیگری ننویس.
                        """.trimIndent())
                    })
                    add(buildJsonObject { put("role", "user"); put("content", message.take(2_000)) })
                })
            }.toString()
            val raw = post(provider, token, body)
            val content = json.parseToJsonElement(raw).jsonObject["choices"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
                ?: error("پاسخ قابل‌خواندن دریافت نشد.")
            parseCommand(content)
        }
    }

    suspend fun interpretTransaction(
        context: Context,
        message: String,
        categoryNames: List<String>
    ): Result<AiTransactionDraft> = withContext(Dispatchers.IO) {
        runCatching {
            val provider = activeProvider(context)
            val token = readToken(context, provider).takeIf(String::isNotBlank)
                ?: error("ابتدا کلید API سرویس ${provider.title} را در تنظیمات ذخیره کنید.")
            require(message.isNotBlank()) { "متن درخواست خالی است." }
            val categories = categoryNames.take(80).joinToString("، ")
            val body = buildJsonObject {
                put("model", provider.model)
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
            val raw = post(provider, token, body)
            val content = json.parseToJsonElement(raw).jsonObject["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")
                ?.jsonPrimitive?.contentOrNull?.trim().takeUnless { it.isNullOrBlank() }
                ?: error("پاسخ قابل‌خواندن از Groq دریافت نشد.")
            parseDraft(content)
        }
    }

    private fun parseCommand(content: String): AiAppCommand {
        val clean = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val value = json.parseToJsonElement(clean).jsonObject
        val action = value["action"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: error("نوع فرمان مشخص نشد.")
        val explanation = value["explanation"]?.jsonPrimitive?.contentOrNull.orEmpty().take(300)
        if (action == "navigate") {
            val allowed = setOf("transactions","reports","accounts","categories","debts","loans","checks","assets","vehicles","reminders","notes","review","smsPaste","marketPulse","backup","settings","manual","home","notifications","profile","widgetSettings","civicCenter")
            val route = value["route"]?.jsonPrimitive?.contentOrNull
            require(route in allowed) { "مقصد فرمان معتبر نیست." }
            return AiAppCommand(action="navigate", route=route, explanation=explanation)
        }
        val obligationActions = setOf("loan_borrowed", "loan_lent", "debt_receivable", "debt_payable")
        if (action in obligationActions) {
            val amount = value["amount"]?.jsonPrimitive?.longOrNull ?: error("مبلغ وام یا قرض مشخص نشد.")
            require(amount > 0) { "مبلغ باید بیشتر از صفر باشد." }
            val unit = value["unit"]?.jsonPrimitive?.contentOrNull?.lowercase()
            return AiAppCommand(
                action=action,
                route=if(action.startsWith("loan_")) "loans" else "debts",
                amountRial=if(unit=="rial") amount else Math.multiplyExact(amount,10L),
                party=value["party"]?.jsonPrimitive?.contentOrNull.orEmpty().take(100),
                accountHint=value["account"]?.jsonPrimitive?.contentOrNull.orEmpty().take(100),
                title=value["title"]?.jsonPrimitive?.contentOrNull.orEmpty().take(150),
                explanation=explanation
            )
        }
        val featureRoutes = mapOf(
            "asset_record" to "assets", "reminder_create" to "reminders",
            "check_issued" to "checks", "check_received" to "checks",
            "note_create" to "notes", "vehicle_record" to "vehicles",
            "installment_payment" to "loans", "civic_record" to "civicCenter"
        )
        featureRoutes[action]?.let { route ->
            val rawAmount=value["amount"]?.jsonPrimitive?.longOrNull
            val unit=value["unit"]?.jsonPrimitive?.contentOrNull?.lowercase()
            return AiAppCommand(
                action=action,route=route,
                amountRial=rawAmount?.takeIf{it>0}?.let{if(unit=="rial")it else Math.multiplyExact(it,10L)},
                party=value["party"]?.jsonPrimitive?.contentOrNull.orEmpty().take(100),
                accountHint=value["account"]?.jsonPrimitive?.contentOrNull.orEmpty().take(100),
                title=value["title"]?.jsonPrimitive?.contentOrNull.orEmpty().take(150),
                explanation=explanation
            )
        }
        require(action == "transaction") { "این فرمان هنوز عملیات امن و قابل‌تأییدی ندارد." }
        return AiAppCommand(action="transaction", transaction=parseDraft(clean), explanation=explanation)
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

    private fun post(provider: AiProvider, token: String, body: String): String {
        val connection = (URL(provider.endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15_000; readTimeout = 45_000; doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error(apiError(provider, code, raw, connection.getHeaderField("Retry-After")))
            raw
        } finally { connection.disconnect() }
    }

    private fun apiError(provider: AiProvider, code: Int, raw: String, retryAfter: String?): String {
        val error = runCatching { json.parseToJsonElement(raw).jsonObject["error"]?.jsonObject }.getOrNull()
        val detail = error?.get("message")?.jsonPrimitive?.contentOrNull?.take(180)
        return when (code) {
            401 -> "کلید ${provider.title} معتبر نیست یا دسترسی مدل فعال نشده است."
            403 -> "${provider.title} این اتصال یا حساب را مجاز ندانست؛ احتمال محدودیت حساب یا منطقه‌ای وجود دارد."
            429 -> "سهمیه یا محدودیت سرعت ${provider.title} پر شده است${retryAfter?.let { "؛ حدود $it ثانیه بعد امتحان کنید" }.orEmpty()}."
            else -> "${provider.title} پاسخ نداد (کد $code)${detail?.let { ": $it" }.orEmpty()}"
        }
    }

    private fun encryptionKey(provider: AiProvider): SecretKey {
        val alias = keyAlias(provider)
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }

    private fun readToken(context: Context, provider: AiProvider): String {
        val packed = context.getSharedPreferences(securePrefs(provider), Context.MODE_PRIVATE).getString(TOKEN, null) ?: return ""
        return runCatching {
            val bytes = Base64.decode(packed, Base64.NO_WRAP); require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(provider), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)))
        }.getOrDefault("")
    }
}
