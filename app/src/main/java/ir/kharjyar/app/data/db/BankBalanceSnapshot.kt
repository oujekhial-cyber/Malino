package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bank_balance_snapshots")
data class BankBalanceSnapshotEntity(@PrimaryKey val accountId: Long, val balanceRial: Long, val messageAt: Long, val smsSystemId: Long)

@Dao interface BankBalanceSnapshotDao {
    @Query("SELECT * FROM bank_balance_snapshots") fun observeAll(): Flow<List<BankBalanceSnapshotEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: BankBalanceSnapshotEntity)
}
