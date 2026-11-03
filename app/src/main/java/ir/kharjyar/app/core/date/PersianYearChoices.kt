package ir.kharjyar.app.core.date

/** Years around today are available even without transactions; historical data remains selectable. */
fun persianYearChoices(currentYear: Int, dataYears: Collection<Int>, selectedYear: Int? = null): List<Int> {
    val minimum = (currentYear - 30).coerceAtLeast(1)
    val maximum = (currentYear + 10).coerceAtMost(3177)
    return ((minimum..maximum).toList() + dataYears + listOfNotNull(selectedYear))
        .filter { it in 1..3177 }.distinct().sortedDescending()
}
