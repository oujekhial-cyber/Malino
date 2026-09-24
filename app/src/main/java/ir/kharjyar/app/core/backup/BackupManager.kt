package ir.kharjyar.app.core.backup

import androidx.room.withTransaction
import ir.kharjyar.app.data.Repository
import ir.kharjyar.app.data.prefs.SettingsRepository

/**
 * ساخت و بازیابی بکاپ رمزنگاری‌شده.
 * Restore با «جایگزینی کامل با تأیید» و اتمیک (در یک تراکنش DB) انجام می‌شود.
 * فایل خراب یا رمز اشتباه قبل از هر تغییری رد می‌شود.
 */
class BackupManager(
    private val repo: Repository,
    private val settings: SettingsRepository
) {

    /** @param password اگر null باشد، بکاپ بدون رمزنگاری ساخته می‌شود (به انتخاب کاربر). */
    suspend fun createBackup(password: CharArray?): ByteArray {
        val payload = BackupPayload(
            createdAt = System.currentTimeMillis(),
            accounts = repo.accountDao.allOnce().map { BAccount.of(it) },
            senders = repo.accountDao.allSenders().map { BSender.of(it) },
            categories = repo.categoryDao.allOnce().map { BCategory.of(it) },
            rules = repo.categoryDao.allRulesOnce().map { BRule.of(it) },
            templates = repo.templateDao.allOnce().map { BTemplate.of(it) },
            transactions = repo.txDao.allOnce().map { BTransaction.of(it) },
            transferGroups = repo.transferDao.allOnce().map { BTransferGroup.of(it) },
            smsQueue = repo.smsDao.allOnce().map { BSms.of(it) },
            settings = settings.exportForBackup()
        )
        val json = payload.toJson().toByteArray(Charsets.UTF_8)
        return if (password == null) BackupCrypto.packPlain(json)
        else BackupCrypto.encrypt(json, password)
    }

    /** آیا فایل انتخاب‌شده برای باز شدن به رمز نیاز دارد؟ */
    fun needsPassword(fileBytes: ByteArray): Boolean = BackupCrypto.isEncrypted(fileBytes)

    /**
     * اعتبارسنجی فایل (و رمز، اگر لازم باشد)؛ بدون تغییر داده.
     * برای نمایش تعداد رکوردها به کاربر پیش از Restore.
     */
    fun validate(fileBytes: ByteArray, password: CharArray?): BackupPayload {
        val plain = BackupCrypto.open(fileBytes, password)
        val payload = BackupPayload.fromJson(String(plain, Charsets.UTF_8))
        if (payload.formatVersion != 1) {
            throw BackupCrypto.BackupFormatException("نسخه فرمت (${payload.formatVersion}) پشتیبانی نمی‌شود")
        }
        return payload
    }

    /**
     * نتیجه بازیابی، برای اطلاع دادن به کاربر.
     * @param carriedOverSms پیامک‌های بررسی‌نشده‌ای که از بین نرفتند و به صف برگشتند.
     * @param mergedDuplicates مواردی که چون در خود بکاپ هم بودند دوباره اضافه نشدند.
     */
    data class RestoreReport(val carriedOverSms: Int, val mergedDuplicates: Int)

    /**
     * جایگزینی کامل داده‌ها؛ در یک تراکنش تا نیمه‌کاره نماند.
     *
     * استثنا: پیامک‌های «نیازمند بررسی» که هنوز تعیین تکلیف نشده‌اند پاک نمی‌شوند.
     * این‌ها معمولاً بعد از ساخت بکاپ رسیده‌اند، در هیچ فایلی نیستند و با پاک شدن
     * برای همیشه از دست می‌رفتند. بر اساس `fingerprint` با صف داخل بکاپ ادغام
     * می‌شوند تا مورد تکراری در صف نیفتد.
     */
    suspend fun restore(payload: BackupPayload): RestoreReport {
        val db = repo.db
        var report = RestoreReport(0, 0)
        db.withTransaction {
            // قبل از پاک‌سازی خوانده می‌شود؛ داخل همان تراکنش تا داده‌ای جا نماند
            val current = repo.smsDao.listByStatus(SmsQueueMerge.UNREVIEWED_STATUSES)
            val merge = SmsQueueMerge.merge(
                backupQueue = payload.smsQueue.map { it.toEntity() },
                currentQueue = current,
                knownAccountIds = payload.accounts.mapTo(mutableSetOf()) { it.id },
                knownTemplateIds = payload.templates.mapTo(mutableSetOf()) { it.id }
            )

            db.clearAllTablesInTransaction()
            payload.accounts.forEach { repo.accountDao.insert(it.toEntity()) }
            payload.senders.forEach { repo.accountDao.insertSender(it.toEntity()) }
            payload.categories.forEach { repo.categoryDao.insert(it.toEntity()) }
            payload.rules.forEach { repo.categoryDao.insertRule(it.toEntity()) }
            payload.templates.forEach { repo.templateDao.insert(it.toEntity()) }
            payload.transferGroups.forEach { repo.transferDao.insert(it.toEntity()) }
            payload.smsQueue.forEach { repo.smsDao.insertIgnore(it.toEntity()) }
            // صف نگه‌داشته‌شده بعد از صف بکاپ درج می‌شود تا شناسه‌ها با هم تداخل نکنند
            merge.carriedOver.forEach { repo.smsDao.insertIgnore(it) }
            payload.transactions.forEach { repo.txDao.insert(it.toEntity()) }

            report = RestoreReport(merge.carriedOver.size, merge.duplicates)
        }
        settings.importFromBackup(payload.settings)
        return report
    }
}

/** پاک‌سازی جدول‌ها داخل تراکنش (clearAllTables خودش تراکنش می‌سازد و اینجا قابل استفاده نیست). */
private suspend fun ir.kharjyar.app.data.db.KharjYarDatabase.clearAllTablesInTransaction() {
    openHelper.writableDatabase.apply {
        execSQL("DELETE FROM transactions")
        execSQL("DELETE FROM transfer_groups")
        execSQL("DELETE FROM sms_candidates")
        execSQL("DELETE FROM sms_templates")
        execSQL("DELETE FROM category_rules")
        execSQL("DELETE FROM categories")
        execSQL("DELETE FROM account_senders")
        execSQL("DELETE FROM accounts")
    }
}
