package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface DebtDao {
    @Query("SELECT * FROM debt_people") suspend fun allPeopleOnce():List<DebtPersonEntity>
    @Query("SELECT * FROM debts") suspend fun allDebtsOnce():List<DebtEntity>
    @Query("SELECT * FROM debt_payments") suspend fun allPaymentsOnce():List<DebtPaymentEntity>
    @Query("SELECT * FROM debt_people WHERE deletedAt IS NULL ORDER BY name") fun observePeople(): Flow<List<DebtPersonEntity>>
    @Query("SELECT * FROM debts WHERE deletedAt IS NULL AND personId IN (SELECT id FROM debt_people WHERE deletedAt IS NULL) ORDER BY settled, dueAt") fun observeDebts(): Flow<List<DebtEntity>>
    @Query("SELECT * FROM debt_payments WHERE debtId IN (SELECT d.id FROM debts d JOIN debt_people p ON p.id=d.personId WHERE d.deletedAt IS NULL AND p.deletedAt IS NULL) ORDER BY paidAt DESC") fun observePayments(): Flow<List<DebtPaymentEntity>>
    @Insert suspend fun insertPerson(v: DebtPersonEntity): Long
    @Update suspend fun updatePerson(v: DebtPersonEntity)
    @Query("SELECT id FROM debts WHERE personId = :personId") suspend fun debtIdsOfPerson(personId: Long): List<Long>
    @Query("DELETE FROM debt_payments WHERE debtId IN (SELECT id FROM debts WHERE personId = :personId)") suspend fun deletePaymentsOfPerson(personId: Long)
    @Query("DELETE FROM debts WHERE personId = :personId") suspend fun deleteDebtsOfPerson(personId: Long)
    @Query("DELETE FROM debt_people WHERE id = :personId") suspend fun deletePerson(personId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM debt_people WHERE id=:id AND deletedAt IS NOT NULL)") suspend fun isPersonTrashed(id: Long): Boolean
    @Transaction suspend fun deletePersonWithDebts(personId: Long): List<Long> {
        if (!isPersonTrashed(personId)) return emptyList()
        val ids = debtIdsOfPerson(personId)
        deletePaymentsOfPerson(personId)
        deleteDebtsOfPerson(personId)
        deletePerson(personId)
        return ids
    }
    @Query("UPDATE debt_people SET deletedAt=:at WHERE id=:id AND deletedAt IS NULL") suspend fun trashPerson(id: Long, at: Long)
    @Query("UPDATE debt_people SET deletedAt=NULL WHERE id=:id") suspend fun restorePerson(id: Long)
    @Query("SELECT * FROM debt_people WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC") fun observeTrashPeople(): Flow<List<DebtPersonEntity>>
    @Query("UPDATE debts SET deletedAt=:at WHERE id=:id AND deletedAt IS NULL") suspend fun trashDebt(id: Long, at: Long)
    @Query("UPDATE debts SET deletedAt=NULL WHERE id=:id") suspend fun restoreDebt(id: Long)
    @Query("SELECT * FROM debts WHERE deletedAt IS NOT NULL AND personId IN (SELECT id FROM debt_people WHERE deletedAt IS NULL) ORDER BY deletedAt DESC") fun observeTrashDebts(): Flow<List<DebtEntity>>
    @Query("SELECT * FROM debts WHERE deletedAt IS NOT NULL AND deletedAt<=:cutoff") suspend fun expiredDebts(cutoff: Long): List<DebtEntity>
    @Query("SELECT * FROM debt_people WHERE deletedAt IS NOT NULL AND deletedAt<=:cutoff") suspend fun expiredPeople(cutoff: Long): List<DebtPersonEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM debts WHERE id=:id AND deletedAt IS NOT NULL)") suspend fun isDebtTrashed(id: Long): Boolean
    @Transaction suspend fun permanentlyDeleteDebt(debt: DebtEntity) { if (isDebtTrashed(debt.id)) { deletePaymentsOf(debt.id); deleteDebt(debt) } }
    @Insert suspend fun insertDebt(v: DebtEntity): Long
    @Insert suspend fun insertPayment(v: DebtPaymentEntity): Long
    @Update suspend fun updateDebt(v: DebtEntity)
    @Query("DELETE FROM debt_payments WHERE debtId = :debtId") suspend fun deletePaymentsOf(debtId: Long)
    @Delete suspend fun deleteDebt(v: DebtEntity)
    @Query("SELECT * FROM debts WHERE deletedAt IS NULL AND personId IN (SELECT id FROM debt_people WHERE deletedAt IS NULL) AND settled = 0 AND reminderAt IS NOT NULL AND reminderAt <= :until") suspend fun dueReminders(until: Long): List<DebtEntity>
}
@Dao interface CheckDao {
    @Query("SELECT * FROM checks") suspend fun allOnce():List<CheckEntity>
    @Query("SELECT * FROM checks WHERE deletedAt IS NULL ORDER BY status, dueAt") fun observeAll(): Flow<List<CheckEntity>>
    @Query("UPDATE checks SET deletedAt=:at WHERE id=:id AND deletedAt IS NULL") suspend fun trash(id: Long, at: Long)
    @Query("UPDATE checks SET deletedAt=NULL WHERE id=:id AND deletedAt IS NOT NULL") suspend fun restore(id: Long): Int
    @Query("SELECT * FROM checks WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC") fun observeTrash(): Flow<List<CheckEntity>>
    @Query("SELECT * FROM checks WHERE deletedAt IS NOT NULL AND deletedAt<=:cutoff") suspend fun expired(cutoff: Long): List<CheckEntity>
    @Query("DELETE FROM checks WHERE id=:id AND deletedAt IS NOT NULL") suspend fun permanentlyDelete(id: Long)
    @Insert suspend fun insert(v: CheckEntity): Long
    @Update suspend fun update(v: CheckEntity)
    @Delete suspend fun delete(v: CheckEntity)
    @Query("SELECT * FROM checks WHERE deletedAt IS NULL AND status = 0 AND reminderAt IS NOT NULL AND reminderAt <= :until") suspend fun dueReminders(until: Long): List<CheckEntity>
}
