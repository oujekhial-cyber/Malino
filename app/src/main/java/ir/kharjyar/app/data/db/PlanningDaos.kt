package ir.kharjyar.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface NoteDao {
 @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC") fun observeAll():Flow<List<NoteEntity>>
 @Insert suspend fun insert(v:NoteEntity):Long
 @Update suspend fun update(v:NoteEntity)
 @Query("DELETE FROM notes WHERE id=:id") suspend fun delete(id:Long)
}
@Dao interface LoanDao {
 @Query("SELECT * FROM loans ORDER BY closed, nextDueAt") fun observeLoans():Flow<List<LoanEntity>>
 @Query("SELECT * FROM loan_installments ORDER BY dueAt") fun observeInstallments():Flow<List<LoanInstallmentEntity>>
 @Insert suspend fun insertLoan(v:LoanEntity):Long
 @Insert suspend fun insertInstallments(v:List<LoanInstallmentEntity>)
 @Update suspend fun updateLoan(v:LoanEntity)
 @Update suspend fun updateInstallment(v:LoanInstallmentEntity)
 @Query("SELECT * FROM loan_installments WHERE paid=0 AND dueAt<=:until") suspend fun due(until:Long):List<LoanInstallmentEntity>
}
