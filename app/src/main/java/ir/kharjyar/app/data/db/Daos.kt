package ir.kharjyar.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY archived, createdAt")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts")
    suspend fun allOnce(): List<AccountEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun transactionCount(accountId: Long): Int

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert
    suspend fun insertSender(sender: AccountSenderEntity): Long

    @Query("SELECT * FROM account_senders WHERE accountId = :accountId")
    suspend fun sendersOf(accountId: Long): List<AccountSenderEntity>

    @Query("SELECT * FROM account_senders")
    suspend fun allSenders(): List<AccountSenderEntity>

    @Query("DELETE FROM account_senders WHERE id = :id")
    suspend fun deleteSender(id: Long)
}

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(sms: SmsCandidateEntity): Long

    @Update
    suspend fun update(sms: SmsCandidateEntity)

    @Query("SELECT * FROM sms_candidates WHERE id = :id")
    suspend fun byId(id: Long): SmsCandidateEntity?

    @Query("SELECT * FROM sms_candidates WHERE status IN (:statuses) ORDER BY receivedAt DESC")
    fun observeByStatus(statuses: List<Int>): Flow<List<SmsCandidateEntity>>

    @Query("SELECT COUNT(*) FROM sms_candidates WHERE status IN (:statuses)")
    fun observeCountByStatus(statuses: List<Int>): Flow<Int>

    @Query("SELECT * FROM sms_candidates WHERE status IN (:statuses) ORDER BY receivedAt DESC")
    suspend fun listByStatus(statuses: List<Int>): List<SmsCandidateEntity>

    @Query("DELETE FROM sms_candidates WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM sms_candidates")
    suspend fun allOnce(): List<SmsCandidateEntity>
}

@Dao
interface TemplateDao {
    @Insert
    suspend fun insert(template: SmsTemplateEntity): Long

    @Update
    suspend fun update(template: SmsTemplateEntity)

    @Query("SELECT * FROM sms_templates ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SmsTemplateEntity>>

    @Query("SELECT * FROM sms_templates WHERE enabled = 1 AND sender = :sender")
    suspend fun enabledForSender(sender: String): List<SmsTemplateEntity>

    @Query("SELECT * FROM sms_templates WHERE enabled = 1")
    suspend fun allEnabled(): List<SmsTemplateEntity>

    @Query("SELECT * FROM sms_templates WHERE id = :id")
    suspend fun byId(id: Long): SmsTemplateEntity?

    @Query("DELETE FROM sms_templates WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM sms_templates")
    suspend fun allOnce(): List<SmsTemplateEntity>
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE smsId = :smsId LIMIT 1")
    suspend fun bySmsId(smsId: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """SELECT * FROM transactions
           WHERE occurredAt >= :from AND occurredAt < :to
           ORDER BY occurredAt DESC"""
    )
    fun observeRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query(
        """SELECT * FROM transactions
           WHERE occurredAt >= :from AND occurredAt < :to
           ORDER BY occurredAt ASC"""
    )
    suspend fun listRange(from: Long, to: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 0")
    fun observePendingCount(): Flow<Int>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """SELECT * FROM transactions
           WHERE transferGroupId IS NULL AND nature = 3
             AND amountRial = :amount AND direction = :direction
             AND accountId != :excludeAccountId
             AND occurredAt BETWEEN :from AND :to"""
    )
    suspend fun findTransferCounterpart(
        amount: Long,
        direction: Int,
        excludeAccountId: Long,
        from: Long,
        to: Long
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    suspend fun allOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND balanceAfterRial IS NOT NULL ORDER BY occurredAt DESC LIMIT 1")
    suspend fun latestWithBalance(accountId: Long): TransactionEntity?
}

@Dao
interface TransferDao {
    @Insert
    suspend fun insert(group: TransferGroupEntity): Long

    @Update
    suspend fun update(group: TransferGroupEntity)

    @Query("SELECT * FROM transfer_groups WHERE id = :id")
    suspend fun byId(id: Long): TransferGroupEntity?

    @Query("SELECT * FROM transfer_groups")
    suspend fun allOnce(): List<TransferGroupEntity>
}

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY name")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY archived, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun byId(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories")
    suspend fun allOnce(): List<CategoryEntity>

    @Insert
    suspend fun insertRule(rule: CategoryRuleEntity): Long

    @Update
    suspend fun updateRule(rule: CategoryRuleEntity)

    @Query("SELECT * FROM category_rules ORDER BY priority DESC, createdAt DESC")
    fun observeRules(): Flow<List<CategoryRuleEntity>>

    @Query("SELECT * FROM category_rules WHERE enabled = 1 ORDER BY priority DESC")
    suspend fun enabledRules(): List<CategoryRuleEntity>

    @Query("DELETE FROM category_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("SELECT * FROM category_rules")
    suspend fun allRulesOnce(): List<CategoryRuleEntity>
}
