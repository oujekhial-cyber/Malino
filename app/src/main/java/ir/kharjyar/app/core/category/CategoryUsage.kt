package ir.kharjyar.app.core.category

import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxStatus

/** Rank active categories by current, confirmed transaction use; stable original order breaks ties. */
fun categoriesByUsage(categories: List<CategoryEntity>, transactions: List<TransactionEntity>): List<CategoryEntity> {
    val counts = transactions.asSequence()
        .filter { it.deletedAt == null && it.status == TxStatus.CONFIRMED }
        .mapNotNull { it.categoryId }
        .groupingBy { it }.eachCount()
    return categories.sortedByDescending { counts[it.id] ?: 0 }
}
