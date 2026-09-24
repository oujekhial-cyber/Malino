package ir.kharjyar.app.core.backup

import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountSenderEntity
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.CategoryRuleEntity
import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsTemplateEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TransferGroupEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Snapshot منطقی و نسخه‌دار از داده‌ها برای بکاپ.
 * از کپی مستقیم فایل DB (که با WAL ناسازگار است) استفاده نمی‌شود.
 */
@Serializable
data class BackupPayload(
    val formatVersion: Int = 1,
    val createdAt: Long,
    val accounts: List<BAccount> = emptyList(),
    val senders: List<BSender> = emptyList(),
    val categories: List<BCategory> = emptyList(),
    val rules: List<BRule> = emptyList(),
    val templates: List<BTemplate> = emptyList(),
    val transactions: List<BTransaction> = emptyList(),
    val transferGroups: List<BTransferGroup> = emptyList(),
    val smsQueue: List<BSms> = emptyList(),
    val settings: Map<String, String> = emptyMap()
) {
    fun toJson(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        fun fromJson(s: String): BackupPayload = json.decodeFromString(serializer(), s)
    }
}

@Serializable
data class BAccount(
    val id: Long, val title: String, val bankName: String, val colorArgb: Long,
    val icon: String, val maskedNumber: String, val initialBalanceRial: Long?,
    val initialBalanceAt: Long?, val archived: Boolean, val createdAt: Long,
    // فیلدهای کارت بانکی؛ پیش‌فرض خالی تا بکاپ‌های قدیمی هم خوانده شوند
    val accountNumber: String = "", val iban: String = "",
    val cardNumber: String = "", val cardExpiry: String = "", val cardCvv2: String = ""
) {
    fun toEntity() = AccountEntity(
        id = id, title = title, bankName = bankName, colorArgb = colorArgb, icon = icon,
        maskedNumber = maskedNumber, accountNumber = accountNumber, iban = iban,
        cardNumber = cardNumber, cardExpiry = cardExpiry, cardCvv2 = cardCvv2,
        initialBalanceRial = initialBalanceRial, initialBalanceAt = initialBalanceAt,
        archived = archived, createdAt = createdAt
    )
    companion object {
        fun of(e: AccountEntity) = BAccount(
            id = e.id, title = e.title, bankName = e.bankName, colorArgb = e.colorArgb,
            icon = e.icon, maskedNumber = e.maskedNumber,
            initialBalanceRial = e.initialBalanceRial, initialBalanceAt = e.initialBalanceAt,
            archived = e.archived, createdAt = e.createdAt,
            accountNumber = e.accountNumber, iban = e.iban,
            cardNumber = e.cardNumber, cardExpiry = e.cardExpiry, cardCvv2 = e.cardCvv2
        )
    }
}

@Serializable
data class BSender(val id: Long, val accountId: Long, val sender: String, val identifierHint: String) {
    fun toEntity() = AccountSenderEntity(id, accountId, sender, identifierHint)
    companion object { fun of(e: AccountSenderEntity) = BSender(e.id, e.accountId, e.sender, e.identifierHint) }
}

@Serializable
data class BCategory(val id: Long, val name: String, val colorArgb: Long, val kind: Int, val archived: Boolean, val builtin: Boolean) {
    fun toEntity() = CategoryEntity(id, name, colorArgb, kind, archived, builtin)
    companion object { fun of(e: CategoryEntity) = BCategory(e.id, e.name, e.colorArgb, e.kind, e.archived, e.builtin) }
}

@Serializable
data class BRule(val id: Long, val keyword: String, val categoryId: Long, val enabled: Boolean, val priority: Int, val createdByUser: Boolean, val createdAt: Long) {
    fun toEntity() = CategoryRuleEntity(id, keyword, categoryId, enabled, priority, createdByUser, createdAt)
    companion object { fun of(e: CategoryRuleEntity) = BRule(e.id, e.keyword, e.categoryId, e.enabled, e.priority, e.createdByUser, e.createdAt) }
}

@Serializable
data class BTemplate(
    val id: Long, val name: String, val sender: String, val bankName: String, val version: Int,
    val enabled: Boolean, val rulesJson: String, val sampleBody: String, val amountUnit: String, val createdAt: Long
) {
    fun toEntity() = SmsTemplateEntity(id, name, sender, bankName, version, enabled, rulesJson, sampleBody, amountUnit, createdAt)
    companion object { fun of(e: SmsTemplateEntity) = BTemplate(e.id, e.name, e.sender, e.bankName, e.version, e.enabled, e.rulesJson, e.sampleBody, e.amountUnit, e.createdAt) }
}

@Serializable
data class BTransaction(
    val id: Long, val accountId: Long, val amountRial: Long, val direction: Int, val nature: Int,
    val categoryId: Long?, val description: String, val occurredAt: Long, val recordedAt: Long,
    val timeIsApproximate: Boolean, val source: Int, val smsId: Long?, val status: Int,
    val transferGroupId: Long?, val balanceAfterRial: Long?, val counterparty: String,
    val refNumber: String, val userEdited: Boolean
) {
    fun toEntity() = TransactionEntity(id, accountId, amountRial, direction, nature, categoryId, description, occurredAt, recordedAt, timeIsApproximate, source, smsId, status, transferGroupId, balanceAfterRial, counterparty, refNumber, userEdited)
    companion object {
        fun of(e: TransactionEntity) = BTransaction(e.id, e.accountId, e.amountRial, e.direction, e.nature, e.categoryId, e.description, e.occurredAt, e.recordedAt, e.timeIsApproximate, e.source, e.smsId, e.status, e.transferGroupId, e.balanceAfterRial, e.counterparty, e.refNumber, e.userEdited)
    }
}

@Serializable
data class BTransferGroup(val id: Long, val createdAt: Long, val incomplete: Boolean, val note: String) {
    fun toEntity() = TransferGroupEntity(id, createdAt, incomplete, note)
    companion object { fun of(e: TransferGroupEntity) = BTransferGroup(e.id, e.createdAt, e.incomplete, e.note) }
}

@Serializable
data class BSms(
    val id: Long, val sender: String, val body: String, val receivedAt: Long, val fingerprint: String,
    val status: Int, val matchedAccountId: Long?, val matchedTemplateId: Long?, val extractionJson: String?, val updatedAt: Long
) {
    fun toEntity() = SmsCandidateEntity(id, sender, body, receivedAt, fingerprint, status, matchedAccountId, matchedTemplateId, extractionJson, updatedAt)
    companion object { fun of(e: SmsCandidateEntity) = BSms(e.id, e.sender, e.body, e.receivedAt, e.fingerprint, e.status, e.matchedAccountId, e.matchedTemplateId, e.extractionJson, e.updatedAt) }
}
