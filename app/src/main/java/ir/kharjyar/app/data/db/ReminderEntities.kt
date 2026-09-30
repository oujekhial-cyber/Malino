package ir.kharjyar.app.data.db

import kotlinx.serialization.Serializable
import androidx.room.*
import kotlinx.coroutines.flow.Flow

object ReminderRepeat {
    const val ONCE = 0
    const val DAILY = 1
    const val WEEKLY = 2
    const val MONTHLY = 3
    const val YEARLY = 4
    const val CUSTOM_DAYS = 5
}

@Entity(tableName = "reminders", indices = [Index("nextAt"), Index("enabled")])
@Serializable
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val note: String = "",
    val nextAt: Long,
    val repeatType: Int = ReminderRepeat.ONCE,
    val repeatInterval: Int = 1,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastNotifiedAt: Long? = null
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders") suspend fun allOnce():List<ReminderEntity>
    @Query("SELECT * FROM reminders ORDER BY enabled DESC, nextAt") fun observeAll(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id=:id") suspend fun get(id: Long): ReminderEntity?
    @Query("SELECT * FROM reminders WHERE enabled=1 AND nextAt<=:until") suspend fun due(until: Long): List<ReminderEntity>
    @Insert suspend fun insert(value: ReminderEntity): Long
    @Update suspend fun update(value: ReminderEntity)
    @Query("DELETE FROM reminders WHERE id=:id") suspend fun delete(id: Long)
}
