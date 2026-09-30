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
        val payload = snapshot()
        val json = payload.toJson().toByteArray(Charsets.UTF_8)
        return if (password == null) BackupCrypto.packPlain(json)
        else BackupCrypto.encrypt(json, password)
    }

    private suspend fun snapshot(): BackupPayload =
        BackupPayload(
            createdAt = System.currentTimeMillis(),
            accounts = repo.accountDao.allOnce().map { BAccount.of(it) },
            senders = repo.accountDao.allSenders().map { BSender.of(it) },
            categories = repo.categoryDao.allOnce().map { BCategory.of(it) },
            rules = repo.categoryDao.allRulesOnce().map { BRule.of(it) },
            templates = repo.templateDao.allOnce().map { BTemplate.of(it) },
            transactions = repo.txDao.allOnce().map { BTransaction.of(it) },
            transferGroups = repo.transferDao.allOnce().map { BTransferGroup.of(it) },
            smsQueue = repo.smsDao.allOnce().map { BSms.of(it) },
            blockedSenders = repo.blockedSenderDao.allOnce().map { BBlockedSender.of(it) },
            debtPeople = repo.db.debtDao().allPeopleOnce(), debts = repo.db.debtDao().allDebtsOnce(), debtPayments = repo.db.debtDao().allPaymentsOnce(),
            checks = repo.db.checkDao().allOnce(), bankBalances = repo.db.bankBalanceSnapshotDao().allOnce(), notes = repo.db.noteDao().allOnce(),
            loans = repo.db.loanDao().allLoansOnce(), installments = repo.db.loanDao().allInstallmentsOnce(), assets = repo.db.assetDao().allAssetsOnce(), stockSmsDrafts = repo.db.stockDao().allOnce(),
            assetTrades = repo.db.assetDao().allTradesOnce(), attachments = repo.db.transactionAttachmentDao().allOnce(), reminders = repo.db.reminderDao().allOnce(),
            profiles = repo.db.civicDao().allProfilesOnce(), coveredPeople = repo.db.civicDao().allPeopleOnce(), vehicles = repo.db.civicDao().allVehiclesOnce(),
            oilServices = repo.db.civicDao().allOilServicesOnce(), civicMessages = repo.db.civicDao().allMessagesOnce(),
            privateFiles = collectPrivateFiles(repo.db.checkDao().allOnce().map { it.imagePath } + repo.db.transactionAttachmentDao().allOnce().map { it.imagePath } + repo.db.civicDao().allProfilesOnce().map { it.imagePath }),
            settings = settings.exportForBackup()
        )

    private fun collectPrivateFiles(paths: List<String>): List<BPrivateFile> = paths.filter { it.isNotBlank() }.distinct().mapNotNull { path ->
        runCatching { val file=java.io.File(path); if(!file.isFile) return@runCatching null; BPrivateFile(path,android.util.Base64.encodeToString(file.readBytes(),android.util.Base64.NO_WRAP)) }.getOrNull()
    }

    private fun restorePrivateFiles(files: List<BPrivateFile>) { files.forEach { item ->
        runCatching { if(!item.path.contains("/ir.kharjyar.app/") || !item.path.contains("/files/")) return@runCatching; val file=java.io.File(item.path);file.parentFile?.mkdirs();file.writeBytes(android.util.Base64.decode(item.base64,android.util.Base64.NO_WRAP)) }
    } }

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

    /** ادغام بکاپ با داده فعلی؛ موارد هم‌شناسه فعلی حفظ و SMS با fingerprint یکتا می‌شود. */
    suspend fun merge(payload: BackupPayload): RestoreReport {
        val current = snapshot()
        fun <T, K> merged(backup: List<T>, now: List<T>, key: (T) -> K): List<T> =
            (backup + now).associateBy(key).values.toList()
        val combined = payload.copy(
            createdAt = System.currentTimeMillis(),
            accounts = merged(payload.accounts,current.accounts){it.id}, senders = merged(payload.senders,current.senders){it.id},
            categories = merged(payload.categories,current.categories){it.id}, rules = merged(payload.rules,current.rules){it.id}, templates = merged(payload.templates,current.templates){it.id},
            transactions = merged(payload.transactions,current.transactions){it.id}, transferGroups = merged(payload.transferGroups,current.transferGroups){it.id},
            smsQueue = merged(payload.smsQueue,current.smsQueue){it.fingerprint}, blockedSenders = merged(payload.blockedSenders,current.blockedSenders){it.sender},
            debtPeople = merged(payload.debtPeople,current.debtPeople){it.id}, debts = merged(payload.debts,current.debts){it.id}, debtPayments = merged(payload.debtPayments,current.debtPayments){it.id},
            checks = merged(payload.checks,current.checks){it.id}, bankBalances = merged(payload.bankBalances,current.bankBalances){it.accountId}, notes = merged(payload.notes,current.notes){it.id},
            loans = merged(payload.loans,current.loans){it.id}, installments = merged(payload.installments,current.installments){it.id}, assets = merged(payload.assets,current.assets){it.id}, stockSmsDrafts = merged(payload.stockSmsDrafts,current.stockSmsDrafts){it.fingerprint},
            assetTrades = merged(payload.assetTrades,current.assetTrades){it.id}, attachments = merged(payload.attachments,current.attachments){it.id}, reminders = merged(payload.reminders,current.reminders){it.id},
            profiles = merged(payload.profiles,current.profiles){it.id}, coveredPeople = merged(payload.coveredPeople,current.coveredPeople){it.id}, vehicles = merged(payload.vehicles,current.vehicles){it.id},
            oilServices = merged(payload.oilServices,current.oilServices){it.id}, civicMessages = merged(payload.civicMessages,current.civicMessages){it.fingerprint},
            privateFiles = merged(payload.privateFiles,current.privateFiles){it.path}, settings = current.settings
        )
        return restore(combined)
    }

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
            val restoredTemplateIds = (if (payload.templates.isNotEmpty()) payload.templates.map { it.id } else repo.templateDao.allOnce().map { it.id }).toSet()
            val merge = SmsQueueMerge.merge(
                backupQueue = payload.smsQueue.map { it.toEntity().let { sms -> if (sms.matchedTemplateId in restoredTemplateIds) sms else sms.copy(matchedTemplateId = null) } },
                currentQueue = current,
                knownAccountIds = payload.accounts.mapTo(mutableSetOf()) { it.id },
                knownTemplateIds = restoredTemplateIds
            )

            db.clearAllTablesInTransaction(clearTemplates = payload.templates.isNotEmpty())
            if (payload.templates.isNotEmpty()) payload.templates.forEach { repo.templateDao.insert(it.toEntity()) }
            payload.accounts.forEach { repo.accountDao.insert(it.toEntity()) }
            payload.senders.forEach { repo.accountDao.insertSender(it.toEntity()) }
            payload.categories.forEach { repo.categoryDao.insert(it.toEntity()) }
            payload.rules.forEach { repo.categoryDao.insertRule(it.toEntity()) }
            payload.transferGroups.forEach { repo.transferDao.insert(it.toEntity()) }
            payload.smsQueue.forEach { repo.smsDao.insertIgnore(it.toEntity()) }
            // صف نگه‌داشته‌شده بعد از صف بکاپ درج می‌شود تا شناسه‌ها با هم تداخل نکنند
            merge.carriedOver.forEach { repo.smsDao.insertIgnore(it) }
            payload.transactions.forEach { repo.txDao.insert(it.toEntity()) }
            payload.blockedSenders.forEach { repo.blockedSenderDao.insertIgnore(it.toEntity()) }
            payload.debtPeople.forEach { repo.db.debtDao().insertPerson(it) }; payload.debts.forEach { repo.db.debtDao().insertDebt(it) }; payload.debtPayments.forEach { repo.db.debtDao().insertPayment(it) }
            payload.checks.forEach { repo.db.checkDao().insert(it) }; payload.bankBalances.forEach { repo.db.bankBalanceSnapshotDao().upsert(it) }; payload.notes.forEach { repo.db.noteDao().insert(it) }
            payload.loans.forEach { repo.db.loanDao().insertLoan(it) }; if(payload.installments.isNotEmpty()) repo.db.loanDao().insertInstallments(payload.installments)
            payload.assets.forEach { repo.db.assetDao().insert(it) }; payload.assetTrades.forEach { repo.db.assetDao().insertTrade(it) }; payload.stockSmsDrafts.forEach { repo.db.stockDao().insertDraft(it) }; payload.attachments.forEach { repo.db.transactionAttachmentDao().insert(it) }
            payload.reminders.forEach { repo.db.reminderDao().insert(it) }; payload.profiles.forEach { repo.db.civicDao().saveProfile(it) }; payload.coveredPeople.forEach { repo.db.civicDao().insertPerson(it) }
            payload.vehicles.forEach { repo.db.civicDao().insertVehicle(it) }; payload.oilServices.forEach { repo.db.civicDao().insertOilService(it) }; payload.civicMessages.forEach { repo.db.civicDao().insertMessage(it) }
            restorePrivateFiles(payload.privateFiles)

            report = RestoreReport(merge.carriedOver.size, merge.duplicates)
        }
        settings.importFromBackup(payload.settings)
        return report
    }
}

/** پاک‌سازی جدول‌ها داخل تراکنش (clearAllTables خودش تراکنش می‌سازد و اینجا قابل استفاده نیست). */
private suspend fun ir.kharjyar.app.data.db.KharjYarDatabase.clearAllTablesInTransaction(clearTemplates: Boolean) {
    openHelper.writableDatabase.apply {
        if (clearTemplates) execSQL("DELETE FROM sms_templates")
        execSQL("DELETE FROM vehicle_oil_services")
        execSQL("DELETE FROM civic_messages")
        execSQL("DELETE FROM vehicles")
        execSQL("DELETE FROM covered_people")
        execSQL("DELETE FROM user_profiles")
        execSQL("DELETE FROM reminders")
        execSQL("DELETE FROM transaction_attachments")
        execSQL("DELETE FROM asset_trades")
        execSQL("DELETE FROM stock_sms_drafts")
        execSQL("DELETE FROM assets")
        execSQL("DELETE FROM loan_installments")
        execSQL("DELETE FROM loans")
        execSQL("DELETE FROM notes")
        execSQL("DELETE FROM bank_balance_snapshots")
        execSQL("DELETE FROM checks")
        execSQL("DELETE FROM debt_payments")
        execSQL("DELETE FROM debts")
        execSQL("DELETE FROM debt_people")
        execSQL("DELETE FROM blocked_senders")
        execSQL("DELETE FROM transactions")
        execSQL("DELETE FROM transfer_groups")
        execSQL("DELETE FROM sms_candidates")
        execSQL("DELETE FROM category_rules")
        execSQL("DELETE FROM categories")
        execSQL("DELETE FROM account_senders")
        execSQL("DELETE FROM accounts")
    }
}
