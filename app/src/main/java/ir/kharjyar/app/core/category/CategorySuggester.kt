package ir.kharjyar.app.core.category

/** قانون دسته‌بندی برای موتور پیشنهاد. */
data class Rule(
    val id: Long,
    val keyword: String,
    val categoryId: Long,
    val priority: Int,
    val createdByUser: Boolean
)

/**
 * پیشنهاد دسته‌بندی محلی و قانون‌محور.
 * قوانین صریح کاربر (priority بالاتر) بر پیشنهادهای عمومی مقدم‌اند.
 * از روی مبلغ به‌تنهایی هیچ حدسی زده نمی‌شود.
 */
object CategorySuggester {

    fun suggest(text: String, counterparty: String, rules: List<Rule>): Long? {
        val haystack = (text + " " + counterparty).lowercase()
        val matched = rules
            .filter { it.keyword.isNotBlank() && haystack.contains(it.keyword.lowercase()) }
            .sortedWith(
                compareByDescending<Rule> { it.createdByUser }
                    .thenByDescending { it.priority }
                    .thenByDescending { it.keyword.length }
            )
        return matched.firstOrNull()?.categoryId
    }
}
