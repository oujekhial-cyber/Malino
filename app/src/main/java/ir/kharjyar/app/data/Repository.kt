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
import ir.kharjyar.app.core.balance.TxSummarizer
import ir.kharjyar.app.data.db.BlockedSenderEntity
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
    val blockedSenderDao = db.blockedSenderDao()
    val spamSmsDao = db.spamSmsDao()

    /** نتیجه ثبت اولیه پیامک (از Receiver). */
    data class IngestResult(val smsId: Long?, val duplicate: Boolean, val kind: SmsKind)

    /**
     * ثبت اولیه پیامک: طبقه‌بندی + ذخیره idempotent.
     * پیامک غیرمالی ذخیره نمی‌شود (OTP و پیامک شخصی نگه داشته نمی‌شوند).
     */
    suspend fun ingestSms(sender: String, body: String, receivedAt: Long): IngestResult {
        // رمز پویا حتی برای فرستنده مسدودشده ذخیره نمی‌شود.
        if (SmsClassifier.isOtp(body)) return IngestResult(null,false,SmsKind.NON_FINANCIAL)
        val fp = SmsFingerprint.of(sender, body, receivedAt)
        if (blockedSenderDao.isBlocked(sender)) {
            spamSmsDao.insert(ir.kharjyar.app.data.db.SpamSmsEntity(sender=sender,body=body,receivedAt=receivedAt,reason="فرستنده مسدودشده توسط کاربر",fingerprint=fp))
            return IngestResult(null, false, SmsKind.NON_FINANCIAL)
        }
        val mappings=accountDao.allSenders()
        val knownFinancialSender=mappings.any{AccountMatcher.normalizeSender(it.sender)==AccountMatcher.normalizeSender(sender)}||ir.kharjyar.app.core.sms.BankSenderResolver.bankName(sender)!=null
        val spam=ir.kharjyar.app.core.sms.SpamSmsClassifier.decide(body)
        if(!knownFinancialSender&&spam.confident){
            spamSmsDao.insert(ir.kharjyar.app.data.db.SpamSmsEntity(sender=sender,body=body,receivedAt=receivedAt,reason=spam.reason,fingerprint=fp))
            return IngestResult(null,false,SmsKind.NON_FINANCIAL)
        }
        val kind = SmsClassifier.classify(body)
        if (kind == SmsKind.NON_FINANCIAL) return IngestResult(null, false, kind)
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

    /**
     * علامت‌گذاری یک فرستنده به‌عنوان تبلیغاتی:
     * پیامک‌های در صفِ همان فرستنده صرف‌نظر می‌شوند و پیام‌های بعدی هم نادیده گرفته می‌شوند.
     */
    suspend fun blockSender(sender: String) {
        blockedSenderDao.insertIgnore(
            BlockedSenderEntity(sender = sender, createdAt = now())
        )
        // صف فعلی را هم از همین فرستنده پاک می‌کنیم
        smsDao.allOnce()
            .filter { it.sender == sender && it.status != SmsStatus.DONE }
            .forEach {
                spamSmsDao.insert(ir.kharjyar.app.data.db.SpamSmsEntity(sender=it.sender,body=it.body,receivedAt=it.receivedAt,reason="علامت‌گذاری دستی کاربر",fingerprint=it.fingerprint))
                smsDao.update(it.copy(status = SmsStatus.DISMISSED, updatedAt = now()))
            }
    }

    /** برداشتن علامت تبلیغاتی از یک فرستنده. */
    suspend fun unblockSender(sender: String) = blockedSenderDao.unblock(sender)

    /** بازگردانی پیام قرنطینه‌شده به صف مالی؛ فرستنده دستی نیز آزاد می‌شود. */
    suspend fun restoreSpamMessage(id:Long):ProcessOutcome? {
        val row=spamSmsDao.byId(id)?:return null
        blockedSenderDao.unblock(row.sender)
        val existing=smsDao.byFingerprint(row.fingerprint)
        val smsId=if(existing!=null){smsDao.update(existing.copy(status=SmsStatus.RAW,updatedAt=now()));existing.id}else smsDao.insertIgnore(SmsCandidateEntity(sender=row.sender,body=row.body,receivedAt=row.receivedAt,fingerprint=row.fingerprint,status=SmsStatus.RAW,updatedAt=now()))
        spamSmsDao.delete(id)
        return if(smsId>0)processSms(smsId) else null
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
        val senderMatch = AccountMatcher.match(sms.sender, sms.body, mappings)
        // اگر نگاشت فرستنده هنوز ساخته نشده یا چند حساب از یک سرشماره پیام می‌گیرند،
        // شماره کارت/حساب/شبا را مستقیماً از متن با همه حساب‌های ذخیره‌شده تطبیق بده.
        val activeAccounts = accountDao.allOnce().filter { !it.archived }
        val numberMatch = ir.kharjyar.app.core.sms.AccountNumberMatcher.match(
            sms.body,
            activeAccounts.map {
                ir.kharjyar.app.core.sms.MatchableAccount(
                    it.id, it.maskedNumber, it.accountNumber, it.iban, it.cardNumber
                )
            }
        )
        // پیامک سود ممکن است شماره سپرده مبدأ را بنویسد، در حالی‌که وجه به حساب
        // دیگری واریز شده است. مقصد فقط از تنظیم صریح کاربر انتخاب می‌شود؛ حدس نمی‌زنیم.
        val normalizedSms = ir.kharjyar.app.core.text.Digits.normalizeForMatch(sms.body)
        val isInterestSms = listOf("سود سپرده","سود ماهانه","سود علی الحساب").any { normalizedSms.contains(it) }
        val configuredInterestDestinations = if(isInterestSms) activeAccounts.filter { it.monthlyInterestBearing }.mapNotNull { source ->
            val sourceMentioned = ir.kharjyar.app.core.sms.AccountNumberMatcher.match(sms.body,listOf(ir.kharjyar.app.core.sms.MatchableAccount(source.id,source.maskedNumber,source.accountNumber,source.iban,source.cardNumber))) is AccountMatch.Single
            if(sourceMentioned) (source.interestDestinationAccountId ?: source.id) else null
        }.distinct() else emptyList()
        val interestMatch:AccountMatch=when(configuredInterestDestinations.size){1->AccountMatch.Single(configuredInterestDestinations.single());in 2..Int.MAX_VALUE->AccountMatch.Ambiguous(configuredInterestDestinations);else->AccountMatch.Unknown}
        // سرشماره‌های نام‌دار بانک: فقط وقتی دقیقاً یک حساب از همان بانک وجود دارد
        // به‌صورت خودکار انتخاب می‌شوند؛ با چند حساب هرگز حدس خطرناک نمی‌زنیم.
        val inferredBank = ir.kharjyar.app.core.sms.BankSenderResolver.bankName(sms.sender)
        val bankIds = inferredBank?.let { bank -> activeAccounts.filter { ir.kharjyar.app.core.sms.BankSenderResolver.sameBank(it.bankName, bank) }.map { it.id }.distinct() }.orEmpty()
        val bankMatch: AccountMatch = when(bankIds.size){1->AccountMatch.Single(bankIds.single());in 2..Int.MAX_VALUE->AccountMatch.Ambiguous(bankIds);else->AccountMatch.Unknown}
        val match = when {
            interestMatch is AccountMatch.Single -> interestMatch
            interestMatch is AccountMatch.Ambiguous -> interestMatch
            numberMatch is AccountMatch.Single -> numberMatch
            senderMatch is AccountMatch.Single -> senderMatch
            numberMatch is AccountMatch.Ambiguous -> numberMatch
            senderMatch is AccountMatch.Ambiguous -> senderMatch
            else -> bankMatch
        }
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

        // پس از تطبیق امن، فرستنده برای پیامک‌های بعدی همین حساب یاد گرفته می‌شود.
        if (mappings.none { it.accountId == accountId && AccountMatcher.normalizeSender(it.sender) == AccountMatcher.normalizeSender(sms.sender) }) {
            val hint = Extractor.autoExtract(sms.body).accountIdHint?.let { AccountMatcher.shortIdentifier(it) }.orEmpty()
            accountDao.insertSender(ir.kharjyar.app.data.db.AccountSenderEntity(accountId = accountId, sender = sms.sender, identifierHint = hint))
        }
        return processWithAccount(sms, accountId)
    }

    /** پردازش پیامک وقتی حساب معلوم است (پس از معرفی حساب هم صدا زده می‌شود). */
    suspend fun processWithAccount(sms: SmsCandidateEntity, accountId: Long): ProcessOutcome {
        // ۲) اعمال قالب‌های آموزش‌دیده: اول قالب‌های همین فرستنده، و اگر نتیجه
        // نداد، همه قالب‌های آموزش‌دیده. بانک‌ها گاهی از چند سرشماره پیامک
        // می‌فرستند و قالبِ یاد گرفته‌شده نباید فقط به یک سرشماره گره بخورد.
        var best: ExtractionResult? = null
        var bestTemplateId: Long? = null
        fun tryTemplates(list: List<ir.kharjyar.app.data.db.SmsTemplateEntity>) {
            for (t in list) {
                val rules = FieldRule.listFromJson(t.rulesJson)
                if (rules.isEmpty()) continue
                val r = Extractor.applyRules(sms.body, rules, t.amountUnit)
                if (r.amountRial != null && (best == null || rank(r) > rank(best!!))) {
                    best = r; bestTemplateId = t.id
                }
            }
        }
        tryTemplates(templateDao.enabledForSender(sms.sender))
        if (best == null || rank(best!!) < 3) {
            tryTemplates(templateDao.allEnabled().filter { it.sender != sms.sender })
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

    /** تلاش دوباره برای پیامک‌های بی‌حساب پس از اضافه‌شدن نگاشت‌ها یا پشتیبانی بانک جدید. */
    suspend fun reprocessPendingAccounts(): List<ProcessOutcome> {
        val results = mutableListOf<ProcessOutcome>()
        for (sms in smsDao.listByStatus(listOf(SmsStatus.RAW, SmsStatus.NEEDS_ACCOUNT))) {
            results += processSms(sms.id)
        }
        return results
    }

    /**
     * پیامک‌هایی که منتظر قالب مانده‌اند را دوباره پردازش می‌کند.
     *
     * بعد از اینکه کاربر یک قالب را آموزش داد، دیگر نباید برای پیامک‌های
     * هم‌شکلِ در صف، دوباره همان پرسش‌ها تکرار شود.
     */
    suspend fun reprocessPending(): Int {
        var fixed = 0
        for (sms in smsDao.listByStatus(listOf(SmsStatus.NEEDS_TEMPLATE))) {
            val accountId = sms.matchedAccountId ?: continue
            val outcome = processWithAccount(sms, accountId)
            if (outcome is ProcessOutcome.DraftReady) fixed++
        }
        return fixed
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

    /**
     * انتقال بین دو حساب خود کاربر؛ هر دو سمت در یک تراکنش دیتابیس ساخته می‌شوند.
     * سمت مبدأ برداشت و سمت مقصد واریز است و هر دو یک transferGroupId مشترک دارند.
     */
    suspend fun addInternalTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amountRial: Long,
        description: String,
        occurredAt: Long
    ): Pair<Long, Long> {
        require(fromAccountId != toAccountId) { "مبدأ و مقصد انتقال یکسان است" }
        require(amountRial > 0) { "مبلغ انتقال باید مثبت باشد" }
        var outgoingId = 0L
        var incomingId = 0L
        db.withTransaction {
            val fromTitle = accountDao.byId(fromAccountId)?.title ?: "مبدأ"
            val toTitle = accountDao.byId(toAccountId)?.title ?: "مقصد"
            val reason = description.trim().takeIf { it.isNotBlank() }?.let { " — بابت $it" } ?: ""
            val outgoingDescription = "انتقال وجه به حساب $toTitle$reason"
            val incomingDescription = "انتقال وجه از حساب $fromTitle$reason"
            val groupId = transferDao.insert(
                TransferGroupEntity(createdAt = now(), incomplete = false, note = "انتقال دستی بین حساب‌های کاربر")
            )
            val common = TransactionEntity(
                accountId = fromAccountId,
                amountRial = amountRial,
                direction = TxDirection.WITHDRAW,
                nature = TxNature.TRANSFER,
                categoryId = null,
                description = outgoingDescription,
                occurredAt = occurredAt,
                recordedAt = now(),
                source = TxSource.MANUAL,
                status = TxStatus.CONFIRMED,
                transferGroupId = groupId,
                userEdited = true
            )
            outgoingId = txDao.insert(common.copy(counterparty = "حساب دیگر من"))
            incomingId = txDao.insert(
                common.copy(
                    id = 0,
                    accountId = toAccountId,
                    direction = TxDirection.DEPOSIT,
                    description = incomingDescription,
                    counterparty = "حساب دیگر من"
                )
            )
        }
        return outgoingId to incomingId
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

    /** @param accountId اگر داده شود، خلاصه فقط برای همان حساب محاسبه می‌شود. */
    suspend fun summary(from: Long, to: Long, accountId: Long? = null): Summary {
        // منطق جمع‌زدن در TxSummarizer است تا صفحه خانه و ویجت دقیقاً یک محاسبه داشته باشند.
        val s = TxSummarizer.summarize(txDao.listRange(from, to), accountId)
        return Summary(s.incomeRial, s.expenseRial, s.pendingCount, s.pendingIncomeRial, s.pendingExpenseRial)
    }

    /** بازه ماه شمسی جاری. */
    fun currentPersianMonthRange(): Pair<Long, Long> {
        val today = PersianDate.today()
        val first = today.firstOfMonth()
        return first.startOfDayMillis() to today.lastOfMonth().endOfDayMillisExclusive()
    }

    private fun now() = System.currentTimeMillis()
}
