package ir.kharjyar.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object DebtKind { const val RECEIVABLE = 0; const val PAYABLE = 1 }
@Entity(tableName = "debt_people")
data class DebtPersonEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val phone: String = "", val note: String = "", val createdAt: Long)
@Entity(tableName = "debts", indices = [Index("personId"), Index("dueAt")])
data class DebtEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val personId: Long, val kind: Int, val amountRial: Long, val title: String, val createdAt: Long, val dueAt: Long?, val reminderAt: Long?, val settled: Boolean = false)
@Entity(tableName = "debt_payments", indices = [Index("debtId"), Index("paidAt")])
data class DebtPaymentEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val debtId: Long, val amountRial: Long, val paidAt: Long, val note: String = "")

object CheckDirection { const val ISSUED = 0; const val RECEIVED = 1 }
object CheckStatus { const val PENDING = 0; const val CLEARED = 1; const val BOUNCED = 2; const val CANCELLED = 3 }
@Entity(tableName = "checks", indices = [Index("dueAt"), Index("accountId"), Index("status")])
data class CheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: Int, val amountRial: Long, val counterparty: String,
    val bankName: String = "", val sayadId: String = "", val serialNumber: String = "",
    val accountId: Long? = null, val issuedAt: Long, val dueAt: Long, val reminderAt: Long?,
    val status: Int = CheckStatus.PENDING, val imagePath: String = "", val note: String = ""
)
