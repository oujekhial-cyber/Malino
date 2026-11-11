package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface NoteDao {
 @Query("SELECT * FROM notes") suspend fun allOnce():List<NoteEntity>
 @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC") fun observeAll():Flow<List<NoteEntity>>
 @Insert suspend fun insert(v:NoteEntity):Long
 @Update suspend fun update(v:NoteEntity)
 @Query("DELETE FROM notes WHERE id=:id") suspend fun delete(id:Long)
}
@Dao interface LoanDao {
 @Query("SELECT * FROM loans") suspend fun allLoansOnce():List<LoanEntity>
 @Query("SELECT * FROM loan_installments") suspend fun allInstallmentsOnce():List<LoanInstallmentEntity>
 @Query("SELECT * FROM loans WHERE deletedAt IS NULL ORDER BY closed, nextDueAt") fun observeLoans():Flow<List<LoanEntity>>
 @Query("SELECT * FROM loan_installments WHERE loanId IN (SELECT id FROM loans WHERE deletedAt IS NULL) ORDER BY dueAt") fun observeInstallments():Flow<List<LoanInstallmentEntity>>
 @Query("UPDATE loans SET deletedAt=:at WHERE id=:id AND deletedAt IS NULL") suspend fun trash(id:Long,at:Long)
 @Query("UPDATE loans SET deletedAt=NULL WHERE id=:id") suspend fun restore(id:Long)
 @Query("SELECT * FROM loans WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC") fun observeTrash():Flow<List<LoanEntity>>
 @Query("SELECT * FROM loans WHERE deletedAt IS NOT NULL AND deletedAt<=:cutoff") suspend fun expired(cutoff:Long):List<LoanEntity>
 @Query("SELECT EXISTS(SELECT 1 FROM loans WHERE id=:id AND deletedAt IS NOT NULL)") suspend fun isTrashed(id:Long):Boolean
 @Transaction suspend fun permanentlyDelete(id:Long) { if(isTrashed(id)) { deleteInstallments(id); deleteLoan(id) } }
 @Insert suspend fun insertLoan(v:LoanEntity):Long
 @Insert suspend fun insertInstallments(v:List<LoanInstallmentEntity>)
 @Update suspend fun updateLoan(v:LoanEntity)
 @Update suspend fun updateInstallment(v:LoanInstallmentEntity)
 @Query("DELETE FROM loan_installments WHERE loanId=:loanId") suspend fun deleteInstallments(loanId:Long)
 @Query("DELETE FROM loan_installments WHERE loanId=:loanId AND paid=0") suspend fun deleteUnpaidInstallments(loanId:Long)
 @Query("DELETE FROM loans WHERE id=:id") suspend fun deleteLoan(id:Long)
 @Query("SELECT * FROM loan_installments WHERE loanId IN (SELECT id FROM loans WHERE deletedAt IS NULL) AND paid=0 AND dueAt<=:until") suspend fun due(until:Long):List<LoanInstallmentEntity>
}
