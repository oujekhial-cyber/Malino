package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Entity(tableName = "recurring_transactions")
@Serializable
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long, val amountRial: Long, val direction: Int, val nature: Int,
    val categoryId: Long? = null, val description: String = "",
    val nextAt: Long, val anchorDay: Int, val enabled: Boolean = true
)

@Entity(tableName = "recurring_occurrences", indices = [Index(value = ["recurringId", "dueAt"], unique = true)])
@Serializable
data class RecurringOccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recurringId: Long, val dueAt: Long, val transactionId: Long
)

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY enabled DESC, nextAt") fun observeAll(): Flow<List<RecurringTransactionEntity>>
    @Query("SELECT * FROM recurring_transactions") suspend fun allOnce(): List<RecurringTransactionEntity>
    @Query("SELECT * FROM recurring_occurrences") suspend fun allOccurrencesOnce(): List<RecurringOccurrenceEntity>
    @Query("SELECT * FROM recurring_occurrences ORDER BY dueAt DESC") fun observeOccurrences(): Flow<List<RecurringOccurrenceEntity>>
    @Query("SELECT * FROM recurring_transactions WHERE enabled = 1 AND nextAt <= :now ORDER BY nextAt") suspend fun due(now: Long): List<RecurringTransactionEntity>
    @Query("SELECT * FROM recurring_transactions WHERE id=:id") suspend fun byId(id: Long): RecurringTransactionEntity?
    @Query("SELECT EXISTS(SELECT 1 FROM recurring_occurrences WHERE recurringId=:id AND dueAt=:at)") suspend fun hasOccurrence(id: Long, at: Long): Boolean
    @Insert suspend fun insert(item: RecurringTransactionEntity): Long
    @Insert suspend fun insertOccurrence(item: RecurringOccurrenceEntity): Long
    @Update suspend fun update(item: RecurringTransactionEntity)
}
