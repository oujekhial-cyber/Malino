package ir.kharjyar.app.data.db

import kotlinx.serialization.Serializable
import androidx.room.*
import kotlinx.coroutines.flow.Flow

object AssetKind { const val GOLD=0; const val VEHICLE=1; const val PROPERTY=2; const val OTHER=3 }
@Entity(tableName="assets",indices=[Index("kind"),Index("active")])
@Serializable
data class AssetEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val kind:Int,val title:String,val quantity:Double=1.0,val purchasePriceRial:Long,val currentValueRial:Long,val purchasedAt:Long,val note:String="",val active:Boolean=true)
@Entity(tableName="asset_trades",indices=[Index("assetId"),Index("tradedAt")])
@Serializable
data class AssetTradeEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val assetId:Long,val isSale:Boolean,val quantity:Double,val amountRial:Long,val tradedAt:Long)
@Entity(tableName="transaction_attachments",indices=[Index("transactionId")])
@Serializable
data class TransactionAttachmentEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val transactionId:Long,val imagePath:String,val createdAt:Long)

@Dao interface AssetDao { @Query("SELECT * FROM assets") suspend fun allAssetsOnce():List<AssetEntity>;@Query("SELECT * FROM asset_trades") suspend fun allTradesOnce():List<AssetTradeEntity>; @Query("SELECT * FROM assets ORDER BY active DESC,purchasedAt DESC")fun observeAssets():Flow<List<AssetEntity>>;@Query("SELECT * FROM asset_trades ORDER BY tradedAt DESC")fun observeTrades():Flow<List<AssetTradeEntity>>;@Insert suspend fun insert(v:AssetEntity):Long;@Update suspend fun update(v:AssetEntity);@Insert suspend fun insertTrade(v:AssetTradeEntity):Long }
@Dao interface TransactionAttachmentDao { @Query("SELECT * FROM transaction_attachments") suspend fun allOnce():List<TransactionAttachmentEntity>; @Query("SELECT * FROM transaction_attachments WHERE transactionId=:txId")fun observeFor(txId:Long):Flow<List<TransactionAttachmentEntity>>;@Insert suspend fun insert(v:TransactionAttachmentEntity):Long;@Query("DELETE FROM transaction_attachments WHERE id=:id")suspend fun delete(id:Long) }
