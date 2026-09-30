package ir.kharjyar.app.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

object StockSide { const val BUY=0; const val SELL=1 }
object StockDraftStatus { const val PENDING=0; const val CONFIRMED=1; const val IGNORED=2 }

/** معامله استخراج‌شده از پیامک کارگزاری؛ تا تأیید کاربر روی سبد اثر ندارد. */
@Serializable
@Entity(tableName="stock_sms_drafts",indices=[Index(value=["fingerprint"],unique=true),Index("status"),Index("occurredAt")])
data class StockSmsDraftEntity(
 @PrimaryKey(autoGenerate=true) val id:Long=0,
 val fingerprint:String,
 val sender:String,
 val broker:String,
 val symbol:String,
 val side:Int,
 val quantity:Long,
 val unitPriceRial:Long,
 val totalRial:Long,
 val occurredAt:Long,
 val status:Int=StockDraftStatus.PENDING,
 val createdAt:Long=System.currentTimeMillis()
)

@Dao interface StockDao {
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertDraft(v:StockSmsDraftEntity):Long
 @Update suspend fun updateDraft(v:StockSmsDraftEntity)
 @Query("SELECT * FROM stock_sms_drafts WHERE status=0 ORDER BY occurredAt DESC") fun observePending():Flow<List<StockSmsDraftEntity>>
 @Query("SELECT * FROM stock_sms_drafts ORDER BY occurredAt DESC") fun observeAll():Flow<List<StockSmsDraftEntity>>
 @Query("SELECT * FROM stock_sms_drafts") suspend fun allOnce():List<StockSmsDraftEntity>
}
