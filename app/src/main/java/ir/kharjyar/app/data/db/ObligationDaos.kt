package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface DebtDao {
    @Query("SELECT * FROM debt_people") suspend fun allPeopleOnce():List<DebtPersonEntity>
    @Query("SELECT * FROM debts") suspend fun allDebtsOnce():List<DebtEntity>
    @Query("SELECT * FROM debt_payments") suspend fun allPaymentsOnce():List<DebtPaymentEntity>
    @Query("SELECT * FROM debt_people ORDER BY name") fun observePeople(): Flow<List<DebtPersonEntity>>
    @Query("SELECT * FROM debts ORDER BY settled, dueAt") fun observeDebts(): Flow<List<DebtEntity>>
    @Query("SELECT * FROM debt_payments ORDER BY paidAt DESC") fun observePayments(): Flow<List<DebtPaymentEntity>>
    @Insert suspend fun insertPerson(v: DebtPersonEntity): Long
    @Insert suspend fun insertDebt(v: DebtEntity): Long
    @Insert suspend fun insertPayment(v: DebtPaymentEntity): Long
    @Update suspend fun updateDebt(v: DebtEntity)
    @Query("DELETE FROM debt_payments WHERE debtId = :debtId") suspend fun deletePaymentsOf(debtId: Long)
    @Delete suspend fun deleteDebt(v: DebtEntity)
    @Query("SELECT * FROM debts WHERE settled = 0 AND reminderAt IS NOT NULL AND reminderAt <= :until") suspend fun dueReminders(until: Long): List<DebtEntity>
}
@Dao interface CheckDao {
    @Query("SELECT * FROM checks") suspend fun allOnce():List<CheckEntity>
    @Query("SELECT * FROM checks ORDER BY status, dueAt") fun observeAll(): Flow<List<CheckEntity>>
    @Insert suspend fun insert(v: CheckEntity): Long
    @Update suspend fun update(v: CheckEntity)
    @Delete suspend fun delete(v: CheckEntity)
    @Query("SELECT * FROM checks WHERE status = 0 AND reminderAt IS NOT NULL AND reminderAt <= :until") suspend fun dueReminders(until: Long): List<CheckEntity>
}
