package ir.kharjyar.app.core.identity

/** قواعد پایدار نام کاربری برای نمایش و همگام‌سازی احتمالی آینده. */
object Username {
    private val valid = Regex("^[a-z0-9_]{3,30}$")

    /** فقط حروف لاتین، رقم انگلیسی و زیرخط را نگه می‌دارد. */
    fun sanitize(input: String): String = input
        .lowercase()
        .filter { it in 'a'..'z' || it in '0'..'9' || it == '_' }
        .take(30)

    fun isValid(value: String): Boolean = valid.matches(value)
}
