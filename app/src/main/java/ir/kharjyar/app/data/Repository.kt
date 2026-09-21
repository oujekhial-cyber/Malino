package ir.kharjyar.app.data

import androidx.room.withTransaction
import ir.kharjyar.app.core.category.CategorySuggester
import ir.kharjyar.app.core.category.Rule
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.sms.AccountMatch
import ir.kharjyar.app.core.sms.AccountMatcher
import ir.kharjyar.app.core.sms.Confidence
import ir.kharjyar.app.core.sms.ExtractedDirection
import ir.kharjyar.app.core.sms.ExtractionResult
import ir.kharjyar.app.core.sms.Extractor
import ir.kharjyar.app.core.sms.FieldRule
import ir.kharjyar.app.core.sms.SenderMapping
import ir.kharjyar.app.core.sms.SmsClassifier
import ir.kharjyar.app.core.sms.SmsFingerprint
import ir.kharjyar.app.core.sms.SmsKind
import ir.kharjyar.app.core.transfer.TransferCandidate
import ir.kharjyar.app.core.transfer.TransferMatch
import ir.kharjyar.app.core.transfer.TransferMatcher
import ir.kharjyar.app.data.db.KharjYarDatabase
import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsStatus
import ir.kharjyar.app.data.db.SmsTemplateEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TransferGroupEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxSource
import ir.kharjyar.app.data.db.TxStatus

/**
 * لایه Repository: منطق پردازش پیامک، ساخت پیش‌نویس، انتقال و دسته‌بندی.
 */
class Repository(val db: KharjYarDatabase) {

    val accountDao = db.accountDao()
    val smsDao = db.smsDao()
    val templateDao = db.templateDao()
    val txDao = db.transactionDao()
    val transferDao = db.transferDao()
    val categoryDao = db.categoryDao()

    /** نتیجه ثبت اولیه پیامک (از Receiver). */
    data class IngestResult(val smsId: Long?, val duplicate: Boolean, val kind: SmsKind)

    /**
     * ثبت اولیه پیامک: طبقه‌بندی + ذخیره idempotent.
     * پیامک غیرمالی ذخیره نمی‌شود (OTP و پیامک شخصی نگه داشته نمی‌شوند).
     */
    suspend fun ingestSms(sender: String, body: String, receivedAt: Long): IngestResult {
        val kind = SmsClassifier.classify(body)
        if (kind == SmsKind.NON_FINANCIAL) return IngestResult(null, false, kind)
        val fp = SmsFingerprint.of(sender, body, receivedAt)
        val row = SmsCandidateEntity(
            sender = sender,
            body = body,
            receivedAt = receivedAt,
            fingerprint = fp,
            status = SmsStatus.RAW,
            updatedAt = System.currentTimeMillis()
        )
        val id = smsDao.insertIgnore(row)
        return if (id == -1L) IngestResult(null, true, kind) else IngestResult(id, false, kind)
    }

    /** نتیجه پردازش کامل پیامک (در Worker). */
    sealed class ProcessOutcome {
        data class DraftReady(val smsId: Long, val txId: Long) : ProcessOutcome()
        data class NeedsAccount(val smsId: Long) : ProcessOutcome()
        data class NeedsTemplate(val smsId: Long) : ProcessOutcome()
        data class AmbiguousAccount(val smsId: Long, val accountIds: List<Long>) : ProcessOutcome()
        object AlreadyProcessed : ProcessOutcome()
        object NotFound : ProcessOutcome()
    }

    /**
     * پردازش پیامک ذخیره‌شده: تشخیص حساب، اعمال قالب، ساخت پیش‌نویس تراکنش.
     * idempotent: پیامک DONE یا دارای تراکنش، دوباره پردازش نمی‌شود.
     */
    suspend fun processSms(smsId: Long): ProcessOutcome {
        val sms = smsDao.byId(smsId) ?: return ProcessOutcome.NotFound
        if (sms.status == SmsStatus.DONE || sms.status == SmsStatus.DISMISSED) return ProcessOutcome.AlreadyProcessed
        txDao.bySmsId(smsId)?.let { return ProcessOutcome.AlreadyProcessed }

        // ۱) تشخیص حساب
        val mappings = accountDao.allSenders().map {
            SenderMapping(it.id, it.accountId, it.sender, it.identifierHint)
        }
        val match = AccountMatcher.match(sms.sender, sms.body, mappings)
        val accountId = when (match) {
            is AccountMatch.Single -> match.accountId
            is AccountMatch.Ambiguous -> {
                smsDao.update(sms.copy(status = SmsStatus.NEEDS_ACCOUNT, updatedAt = now()))
                return ProcessOutcome.AmbiguousAccount(smsId, match.accountIds)
            }
            AccountMatch.Unknown -> {
                smsDao.update(sms.copy(status = SmsStatus.NEEDS_ACCOUNT, updatedAt = now()))
                return ProcessOutcome.NeedsAccount(smsId)
            }
        }

        return processWithAccount(sms, accountId)
    }

    /** پردازش پیامک وقتی حساب معلوم است (پس از معرفی حساب هم صدا زده می‌شود). */
    suspend fun processWithAccount(sms: SmsCandidateEntity, accountId: Long): ProcessOutcome {
        // ۲) اعمال قالب‌های آموزش‌دیده این فرستنده
        val templates = templateDao.enabledForSender(sms.sender)
        var best: ExtractionResult? = null
        var bestTemplateId: Long? = null
        for (t in templates) {
            val rules = FieldRule.listFromJson(t.rulesJson)
            if (rules.isEmpty()) continue
            val r = Extractor.applyRules(sms.body, rules, t.amountUnit)
            if (r.amountRial != null && (best == null || rank(r) > rank(best!!))) {
                best = r; bestTemplateId = t.id
            }
        }
        // ۳) استخراج خودکار به عنوان جایگزین
        val auto = Extractor.autoExtract(sms.body)
        val chosen = when {
            best != null && rank(best!!) >= rank(auto) -> best!!
            else -> auto
        }
        val templateId = if (chosen === best) bestTemplateId else null

        // ۴) اعتبارسنجی: مبلغ یا جهت مبهم => صف بررسی؛ داده اشتباه قطعی ساخته نمی‌شود
        if (chosen.amountRial == null || chosen.amountRial <= 0 ||
            chosen.confidenceEnum() == Confidence.LOW ||
            chosen.directionEnum() == ExtractedDirection.UNKNOWN
        ) {
            smsDao.update(
                sms.copy(
                    status = SmsStatus.NEEDS_TEMPLATE,
                    matchedAccountId = accountId,
                    matchedTemplateId = templateId,
                    extractionJson = chosen.toJson(),
                    updatedAt = now()
                )
            )
            return ProcessOutcome.NeedsTemplate(sms.id)
        }

        // ۵) ساخت پیش‌نویس تراکنش (PENDING) — پیشنهاد است، نه ثبت قطعی
        val direction = if (chosen.directionEnum() == ExtractedDirection.DEPOSIT) TxDirection.DEPOSIT else TxDirection.WITHDRAW
        val suggestedCategory = suggestCategory(sms.body, chosen.counterparty ?: "")
        val occurredAt = chosen.occurredAtMillis ?: sms.receivedAt

        val txId = db.withTransaction {
            val existing = txDao.bySmsId(sms.id)
            if (existing != null) return@withTransaction existing.id
            val id = txDao.insert(
                TransactionEntity(
                    accountId = accountId,
                    amountRial = chosen.amountRial,
                    direction = direction,
                    nature = TxNature.UNKNOWN,
                    categoryId = suggestedCategory,
                    occurredAt = occurredAt,
                    recordedAt = now(),
                    timeIsApproximate = chosen.occurredAtMillis == null,
                    source = TxSource.SMS,
                    smsId = sms.id,
                    status = TxStatus.PENDING,
                    balanceAfterRial = chosen.balanceRial,
                    counterparty = chosen.counterparty ?: "",
                    refNumber = chosen.refNumber ?: ""
                )
            )
            smsDao.update(
                sms.copy(
                    status = SmsStatus.DRAFT_READY,
                    matchedAccountId = accountId,
                    matchedTemplateId = templateId,
                    extractionJson = chosen.toJson(),
                    updatedAt = now()
                )
            )
            id
        }
        return ProcessOutcome.DraftReady(sms.id, txId)
    }

    private fun rank(r: ExtractionResult): Int = when (r.confidenceEnum()) {
        Confidence.HIGH -> 3
        Confidence.MEDIUM -> 2
        Confidence.LOW -> 1
    }

    suspend fun suggestCategory(text: String, counterparty: String): Long? {
        val rules = categoryDao.enabledRules().map {
            Rule(it.id, it.keyword, it.categoryId, it.priority, it.createdByUser)
        }
        return CategorySuggester.suggest(text, counterparty, rules)
    }

    /**
     * تأیید تراکنش توسط کاربر. اگر ماهیت انتقال است، تلاش برای تطبیق با سمت مقابل.
     */
    suspend fun confirmTransaction(
        txId: Long,
        accountId: Long,
        amountRial: Long,
        direction: Int,
        nature: Int,
        categoryId: Long?,
        description: String,
        occurredAt: Long
    ): Long? {
        var groupId: Long? = null
        db.withTransaction {
            val tx = txDao.byId(txId) ?: return@withTransaction
            var updated = tx.copy(
                accountId = accountId,
                amountRial = amountRial,
                direction = direction,
                nature = nature,
                categoryId = categoryId,
                description = description,
                occurredAt = occurredAt,
                status = TxStatus.CONFIRMED,
                userEdited = true
            )
            if (nature == TxNature.TRANSFER) {
                groupId = tryMatchTransfer(updated)
                if (groupId != null) updated = updated.copy(transferGroupId = groupId)
            }
            txDao.update(updated)
            // پیامک منبع => DONE
            tx.smsId?.let { sid ->
                smsDao.byId(sid)?.let { smsDao.update(it.copy(status = SmsStatus.DONE, updatedAt = now())) }
            }
        }
        return groupId
    }

    /**
     * تطبیق سمت مقابل انتقال داخلی. فقط تطبیق قطعی (تک‌کاندید) خودکار انجام می‌شود.
     */
    private suspend fun tryMatchTransfer(tx: TransactionEntity): Long? {
        val window = TransferMatcher.DEFAULT_WINDOW_MILLIS
        val candidates = txDao.findTransferCounterpart(
            amount = tx.amountRial,
            direction = if (tx.direction == TxDirection.DEPOSIT) TxDirection.WITHDRAW else TxDirection.DEPOSIT,
            excludeAccountId = tx.accountId,
            from = tx.occurredAt - window,
            to = tx.occurredAt + window
        ).map { TransferCandidate(it.id, it.accountId, it.amountRial, it.direction, it.occurredAt) }

        val me = TransferCandidate(tx.id, tx.accountId, tx.amountRial, tx.direction, tx.occurredAt)
        return when (val m = TransferMatcher.match(me, candidates, window)) {
            is TransferMatch.Matched -> {
                val gid = transferDao.insert(TransferGroupEntity(createdAt = now(), incomplete = false))
                val other = txDao.byId(m.counterpartTxId)
                if (other != null) txDao.update(other.copy(transferGroupId = gid))
                gid
            }
            else -> {
                // سمت دیگر پیدا نشد: گروه ناقص می‌سازیم تا وضعیت «انتقال ناقص» مشخص باشد
                val gid = transferDao.insert(TransferGroupEntity(createdAt = now(), incomplete = true))
                gid
            }
        }
    }

    /** ثبت دستی تراکنش. */
    suspend fun addManualTransaction(
        accountId: Long,
        amountRial: Long,
        direction: Int,
        nature: Int,
        categoryId: Long?,
        description: String,
        occurredAt: Long,
        counterparty: String = ""
    ): Long {
        var id = 0L
        db.withTransaction {
            var tx = TransactionEntity(
                accountId = accountId,
                amountRial = amountRial,
                direction = direction,
                nature = nature,
                categoryId = categoryId,
                description = description,
                occurredAt = occurredAt,
                recordedAt = now(),
                source = TxSource.MANUAL,
                status = TxStatus.CONFIRMED,
                counterparty = counterparty,
                userEdited = true
            )
            id = txDao.insert(tx)
            if (nature == TxNature.TRANSFER) {
                val gid = tryMatchTransfer(tx.copy(id = id))
                if (gid != null) {
                    txDao.update(tx.copy(id = id, transferGroupId = gid))
                }
            }
        }
        return id
    }

    /** خلاصه مالی یک بازه: انتقال داخلی در جمع درآمد/هزینه حساب نمی‌شود. */
    data class Summary(
        val incomeRial: Long,
        val expenseRial: Long,
        val pendingCount: Int,
        val pendingIncomeRial: Long,
        val pendingExpenseRial: Long
    ) {
        val netRial: Long get() = incomeRial - expenseRial
    }

    suspend fun summary(from: Long, to: Long): Summary {
        val txs = txDao.listRange(from, to)
        var income = 0L; var expense = 0L
        var pIncome = 0L; var pExpense = 0L; var pCount = 0
        for (t in txs) {
            if (t.nature == TxNature.TRANSFER) continue
            val isConfirmed = t.status == TxStatus.CONFIRMED
            when {
                t.nature == TxNature.INCOME || (t.nature == TxNature.UNKNOWN && t.direction == TxDirection.DEPOSIT) -> {
                    if (isConfirmed) income += t.amountRial else { pIncome += t.amountRial; pCount++ }
                }
                t.nature == TxNature.EXPENSE || (t.nature == TxNature.UNKNOWN && t.direction == TxDirection.WITHDRAW) -> {
                    if (isConfirmed) expense += t.amountRial else { pExpense += t.amountRial; pCount++ }
                }
            }
        }
        return Summary(income, expense, pCount, pIncome, pExpense)
    }

    /** بازه ماه شمسی جاری. */
    fun currentPersianMonthRange(): Pair<Long, Long> {
        val today = PersianDate.today()
        val first = today.firstOfMonth()
        return first.startOfDayMillis() to today.lastOfMonth().endOfDayMillisExclusive()
    }

    private fun now() = System.currentTimeMillis()
}
