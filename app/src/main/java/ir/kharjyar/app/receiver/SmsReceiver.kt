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
                // پیامک‌های خدمات شهروندی مستقل از تراکنش‌های بانکی نگهداری می‌شوند.
                // نوع قبض از پروفایل ثبت‌شده می‌آید؛ لازم نیست خود پیامک حتماً نام آب/برق/گاز را بنویسد.
                val civicDao = app.database.civicDao()
                val registeredBillId = if (ir.kharjyar.app.core.sms.UtilityBillMatcher.looksLikeBillMessage(body))
                    ir.kharjyar.app.core.sms.UtilityBillMatcher.uniqueProfileId(body,civicDao.allUtilityBillsOnce()) else null
                val civicKind = if(registeredBillId!=null) ir.kharjyar.app.data.db.CivicMessageKind.UTILITY_BILL else ir.kharjyar.app.core.sms.CivicSmsClassifier.classify(sender, body)
                civicKind?.let { kind ->
                    val utilityBillId = if (kind == ir.kharjyar.app.data.db.CivicMessageKind.UTILITY_BILL) registeredBillId else null
                    // قبض ناشناس وارد هیچ محل حدسی نمی‌شود؛ ابتدا باید شناسه آن در
                    // «قبوض خدماتی» معرفی شده باشد.
                    if (kind == ir.kharjyar.app.data.db.CivicMessageKind.UTILITY_BILL && utilityBillId == null) return@launch
                    val vehicleId = if (kind == ir.kharjyar.app.data.db.CivicMessageKind.TRAFFIC_FINE) {
                        ir.kharjyar.app.core.sms.IranianPlateMatcher.uniqueVehicleId(body, civicDao.allVehiclesOnce())
                    } else null
                    val civicMessageId = civicDao.insertMessage(
                        ir.kharjyar.app.data.db.CivicMessageEntity(
                            kind = kind, sender = sender, body = body, receivedAt = receivedAt,
                            fingerprint = ir.kharjyar.app.core.sms.CivicSmsClassifier.fingerprint(sender, body, receivedAt),
                            vehicleId = vehicleId, utilityBillId = utilityBillId
                        )
                    )
                    if (kind == ir.kharjyar.app.data.db.CivicMessageKind.TRAFFIC_FINE && civicMessageId > 0) {
                        val vehicleTitle = vehicleId?.let { id -> civicDao.allVehiclesOnce().firstOrNull { it.id == id }?.let { "${it.title} (${it.plate})" } }
                        ir.kharjyar.app.notify.Notifier.notifyTrafficFine(context,civicMessageId,vehicleTitle,ir.kharjyar.app.core.sms.TrafficFineParser.amountRial(body))
                    }
                    return@launch
                }
                // پیام انجام معامله کارگزاری فقط به‌صورت پیش‌نویس ذخیره می‌شود و تا
                // تأیید کاربر هیچ تغییری در سبد سهام نمی‌دهد.
                val stock = ir.kharjyar.app.core.sms.StockTradeSmsParser.parse(sender, body)
                if (stock != null) {
                    app.database.stockDao().insertDraft(
                        ir.kharjyar.app.data.db.StockSmsDraftEntity(
                            fingerprint = ir.kharjyar.app.core.sms.SmsFingerprint.of(sender, body, receivedAt),
                            sender = sender, broker = stock.broker, symbol = stock.symbol,
                            side = stock.side, quantity = stock.quantity,
                            unitPriceRial = stock.unitPriceRial, totalRial = stock.totalRial,
                            occurredAt = receivedAt
                        )
                    )
                    return@launch
                }
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
