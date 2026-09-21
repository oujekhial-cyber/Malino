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

    suspend fun createBackup(password: CharArray): ByteArray {
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
        return BackupCrypto.encrypt(payload.toJson().toByteArray(Charsets.UTF_8), password)
    }

    /** اعتبارسنجی فایل و رمز؛ بدون تغییر داده. برای نمایش تعداد به کاربر پیش از Restore. */
    fun validate(fileBytes: ByteArray, password: CharArray): BackupPayload {
        val plain = BackupCrypto.decrypt(fileBytes, password)
        val payload = BackupPayload.fromJson(String(plain, Charsets.UTF_8))
        if (payload.formatVersion != 1) {
            throw BackupCrypto.BackupFormatException("نسخه فرمت (${payload.formatVersion}) پشتیبانی نمی‌شود")
        }
        return payload
    }

    /** جایگزینی کامل داده‌ها؛ در یک تراکنش تا نیمه‌کاره نماند. */
    suspend fun restore(payload: BackupPayload) {
        val db = repo.db
        db.withTransaction {
            db.clearAllTablesInTransaction()
            payload.accounts.forEach { repo.accountDao.insert(it.toEntity()) }
            payload.senders.forEach { repo.accountDao.insertSender(it.toEntity()) }
            payload.categories.forEach { repo.categoryDao.insert(it.toEntity()) }
            payload.rules.forEach { repo.categoryDao.insertRule(it.toEntity()) }
            payload.templates.forEach { repo.templateDao.insert(it.toEntity()) }
            payload.transferGroups.forEach { repo.transferDao.insert(it.toEntity()) }
            payload.smsQueue.forEach { repo.smsDao.insertIgnore(it.toEntity()) }
            payload.transactions.forEach { repo.txDao.insert(it.toEntity()) }
        }
        settings.importFromBackup(payload.settings)
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
