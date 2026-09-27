package ir.kharjyar.app.data.db

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notes", indices = [Index("updatedAt")])
@Serializable
data class NoteEntity(@PrimaryKey(autoGenerate = true) val id:Long=0,val title:String,val body:String,val pinned:Boolean=false,val createdAt:Long,val updatedAt:Long)

object LoanKind { const val BORROWED=0; const val LENT=1 }
@Entity(tableName="loans",indices=[Index("nextDueAt")])
@Serializable
data class LoanEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val party:String,val kind:Int,val principalRial:Long,val installmentAmountRial:Long,val installmentCount:Int,val startAt:Long,val nextDueAt:Long,val reminderDaysBefore:Int=1,val note:String="",val closed:Boolean=false)
@Entity(tableName="loan_installments",indices=[Index("loanId"),Index("dueAt"),Index("paid")])
@Serializable
data class LoanInstallmentEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val loanId:Long,val number:Int,val amountRial:Long,val dueAt:Long,val paid:Boolean=false,val paidAt:Long?=null)
