package ir.kharjyar.app.core.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.work.SmsProcessWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * بازیابی پیامک‌های مالی خوانده‌نشده‌ای که BroadcastReceiver هنگام دریافت آن‌ها
 * اجرا نشده است. وضعیت read پیامک را تغییر نمی‌دهد و هر مورد را دقیقاً از همان
 * خط ingest/worker پیامک زنده عبور می‌دهد.
 */
object UnreadBankSmsRecovery {
    private const val MAX_ROWS = 200
    @Volatile private var lastScanAt = 0L

    suspend fun scanOnAppOpen(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) return
        val now = System.currentTimeMillis()
        synchronized(this) {
            // رفت‌وبرگشت سریع بین برنامه‌ها نباید صندوق پیامک را پی‌درپی اسکن کند.
            if (now - lastScanAt < 60_000L) return
            lastScanAt = now
        }
        val app = context.applicationContext as? KharjYarApp ?: return
        withContext(Dispatchers.IO) {
            val messages = ArrayList<PhoneSms>()
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf("_id", "address", "body", "date"),
                "read = 0",
                null,
                "date DESC"
            )?.use { cursor ->
                while (cursor.moveToNext() && messages.size < MAX_ROWS) {
                    val sender = cursor.getString(1) ?: continue
                    val body = cursor.getString(2) ?: continue
                    if (body.isBlank()) continue
                    messages += PhoneSms(sender, body, cursor.getLong(3))
                }
            }
            val senderMappings=app.database.accountDao().allSenders()
            // قدیمی‌ترها اول پردازش می‌شوند تا مرکز اعلان‌ها ترتیب طبیعی داشته باشد.
            messages.asReversed().forEach { sms ->
                // اسکن هنگام بازشدن سخت‌گیرانه است: فقط تراکنش بانکی با اطمینان بالا
                // وارد مسیر عادی می‌شود؛ تبلیغ، OTP و پیام مبهم اصلاً اعلان نمی‌سازند.
                val normalizedSender=AccountMatcher.normalizeSender(sms.sender)
                val knownSender=senderMappings.any{AccountMatcher.normalizeSender(it.sender)==normalizedSender}||
                    BankSenderResolver.bankName(sms.sender)!=null
                val evidence=BankSmsEvidenceClassifier.classify(sms.sender,sms.body,knownSender)
                if(!evidence.isHighConfidenceTransaction)return@forEach
                // خرابی یک پیامک نباید مانع بازیابی بقیه پیام‌ها شود.
                runCatching {
                    val result = app.repository.ingestSms(sms.sender, sms.body, sms.receivedAt)
                    val smsId = result.smsId
                    if (smsId != null && !result.duplicate) {
                        val work = OneTimeWorkRequestBuilder<SmsProcessWorker>()
                            .setInputData(Data.Builder().putLong(SmsProcessWorker.KEY_SMS_ID, smsId).build())
                            .build()
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "sms-process-$smsId",
                            ExistingWorkPolicy.KEEP,
                            work
                        )
                    }
                }
            }
        }
    }

    private data class PhoneSms(val sender: String, val body: String, val receivedAt: Long)
}
