package ir.kharjyar.app.core.report

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus

data class MonthSummary(val income: Long, val expense: Long, val categories: Map<Long?, Long>)

/** Months are whole Jalali calendar months in Tehran; drafts, trash, transfers and neutral flows are excluded. */
fun monthSummary(transactions: List<TransactionEntity>, month: PersianDate): MonthSummary {
    val first = month.firstOfMonth().startOfDayMillis()
    val end = month.lastOfMonth().endOfDayMillisExclusive()
    val rows = transactions.filter { it.deletedAt == null && it.status == TxStatus.CONFIRMED && it.occurredAt >= first && it.occurredAt < end }
    val incomes = rows.filter { it.nature == TxNature.INCOME || (it.nature == TxNature.UNKNOWN && it.direction == TxDirection.DEPOSIT) }
    val expenses = rows.filter { it.nature == TxNature.EXPENSE || (it.nature == TxNature.UNKNOWN && it.direction == TxDirection.WITHDRAW) }
    return MonthSummary(incomes.sumOf { it.amountRial }, expenses.sumOf { it.amountRial }, expenses.groupBy { it.categoryId }.mapValues { (_, v) -> v.sumOf { it.amountRial } })
}
