package ir.kharjyar.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.data.Repository
import ir.kharjyar.app.notify.Notifier
import ir.kharjyar.app.widget.WidgetUpdater

/**
 * پردازش کامل پیامک در پس‌زمینه.
 * ورودی فقط شناسه رکورد است؛ متن پیامک از دیتابیس خوانده می‌شود.
 * idempotent: اجرای مجدد Worker تراکنش دوم نمی‌سازد.
 */
class SmsProcessWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val smsId = inputData.getLong(KEY_SMS_ID, -1L)
        if (smsId <= 0) return Result.failure()
        val app = applicationContext as? KharjYarApp ?: return Result.failure()
        val repo = app.repository

        return try {
            when (val outcome = repo.processSms(smsId)) {
                is Repository.ProcessOutcome.DraftReady -> {
                    Notifier.notifyDraftReady(applicationContext, outcome.smsId, outcome.txId)
                    WidgetUpdater.requestUpdate(applicationContext)
                    Result.success()
                }
                is Repository.ProcessOutcome.NeedsAccount -> {
                    Notifier.notifyNeedsAccount(applicationContext, outcome.smsId)
                    Result.success()
                }
                is Repository.ProcessOutcome.AmbiguousAccount -> {
                    Notifier.notifyNeedsAccount(applicationContext, outcome.smsId)
                    Result.success()
                }
                is Repository.ProcessOutcome.NeedsTemplate -> {
                    Notifier.notifyNeedsTemplate(applicationContext, outcome.smsId)
                    Result.success()
                }
                Repository.ProcessOutcome.AlreadyProcessed -> Result.success()
                Repository.ProcessOutcome.NotFound -> Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_SMS_ID = "sms_id"
    }
}
