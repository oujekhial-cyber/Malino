package ir.kharjyar.app

import ir.kharjyar.app.core.category.categoriesByUsage
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryUsageTest {
    @Test fun `confirmed active transactions rank categories and ties retain original order`() {
        val categories = listOf(CategoryEntity(id=1, name="الف", colorArgb=0), CategoryEntity(id=2, name="ب", colorArgb=0), CategoryEntity(id=3, name="ج", colorArgb=0))
        fun tx(id: Long, cat: Long, status: Int = TxStatus.CONFIRMED, deletedAt: Long? = null) = TransactionEntity(
            id=id, accountId=1, amountRial=100, direction=TxDirection.WITHDRAW,
            occurredAt=100, recordedAt=100, categoryId=cat, status=status, deletedAt=deletedAt)
        val transactions = listOf(tx(1,2), tx(2,2), tx(3,1), tx(4,3,TxStatus.PENDING), tx(5,3,deletedAt=200))
        assertEquals(listOf(2L,1L,3L), categoriesByUsage(categories,transactions).map { it.id })
        assertEquals(listOf(1L,2L,3L), categoriesByUsage(categories,emptyList()).map { it.id })
    }
}
