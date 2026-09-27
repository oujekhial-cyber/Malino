package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

object CivicMessageKind { const val TRAFFIC_FINE=0; const val UTILITY_BILL=1; const val INSURANCE=2; const val ADLIRAN=3 }

@Entity(tableName="user_profiles",indices=[Index(value=["username"],unique=true)])
data class UserProfileEntity(@PrimaryKey val id:Long=1,val username:String,val displayName:String="",val imagePath:String="",val remoteId:String?=null,val syncPending:Boolean=true,val updatedAt:Long=System.currentTimeMillis())

@Entity(tableName="covered_people",indices=[Index("name")])
data class CoveredPersonEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val relation:String="",val nationalId:String="",val insuranceProvider:String="",val policyNumber:String="",val policyExpiresAt:Long?=null,val note:String="")

@Entity(tableName="vehicles",indices=[Index("ownerId"),Index(value=["plate"],unique=true)])
data class VehicleEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val ownerId:Long?=null,val title:String,val plate:String)

@Entity(tableName="civic_messages",indices=[Index(value=["fingerprint"],unique=true),Index("kind"),Index("receivedAt"),Index("personId"),Index("vehicleId")])
data class CivicMessageEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val kind:Int,val sender:String,val body:String,val receivedAt:Long,val fingerprint:String,val personId:Long?=null,val vehicleId:Long?=null,val read:Boolean=false)

@Dao interface CivicDao {
 @Query("SELECT * FROM civic_messages ORDER BY receivedAt DESC") fun observeMessages():Flow<List<CivicMessageEntity>>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertMessage(v:CivicMessageEntity):Long
 @Update suspend fun updateMessage(v:CivicMessageEntity)
 @Query("DELETE FROM civic_messages WHERE id=:id") suspend fun deleteMessage(id:Long)
 @Query("SELECT * FROM covered_people ORDER BY name") fun observePeople():Flow<List<CoveredPersonEntity>>
 @Insert suspend fun insertPerson(v:CoveredPersonEntity):Long
 @Update suspend fun updatePerson(v:CoveredPersonEntity)
 @Query("DELETE FROM covered_people WHERE id=:id") suspend fun deletePerson(id:Long)
 @Query("SELECT * FROM vehicles ORDER BY title") fun observeVehicles():Flow<List<VehicleEntity>>
 @Insert suspend fun insertVehicle(v:VehicleEntity):Long
 @Query("DELETE FROM vehicles WHERE id=:id") suspend fun deleteVehicle(id:Long)
 @Query("SELECT * FROM user_profiles WHERE id=1") fun observeProfile():Flow<UserProfileEntity?>
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun saveProfile(v:UserProfileEntity)
 @Query("SELECT COUNT(*) FROM user_profiles WHERE username=:username AND id!=:exceptId") suspend fun usernameCount(username:String,exceptId:Long=1):Int
}
