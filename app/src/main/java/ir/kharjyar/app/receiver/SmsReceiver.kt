package ir.kharjyar.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.work.SmsProcessWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * دریافت پیامک جدید.
 * - بخش‌های پیامک چندبخشی توسط Telephony.Sms.Intents.getMessagesFromIntent کنار هم قرار می‌گیرند.
 * - کار کوتاه (طبقه‌بندی + درج idempotent) با goAsync انجام می‌شود.
 * - پردازش کامل به WorkManager سپرده می‌شود و فقط شناسه رکورد منتقل می‌شود، نه متن پیامک.
 * - هیچ متن پیامکی در لاگ ثبت نمی‌شود.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        // اتصال بخش‌های پیامک چندبخشی از یک فرستنده
        val sender = messages[0].displayOriginatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        if (body.isBlank()) return
        val receivedAt = messages[0].timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()

        val app = context.applicationContext as? KharjYarApp ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = app.repository.ingestSms(sender, body, receivedAt)
                val smsId = result.smsId
                if (smsId != null && !result.duplicate) {
                    val work = OneTimeWorkRequestBuilder<SmsProcessWorker>()
                        .setInputData(Data.Builder().putLong(SmsProcessWorker.KEY_SMS_ID, smsId).build())
                        .build()
                    WorkManager.getInstance(context.applicationContext)
                        .enqueueUniqueWork(
                            "sms-process-$smsId",
                            androidx.work.ExistingWorkPolicy.KEEP,
                            work
                        )
                }
            } catch (_: Exception) {
                // خطا نباید متن پیامک را لاگ کند
            } finally {
                pending.finish()
            }
        }
    }
}
