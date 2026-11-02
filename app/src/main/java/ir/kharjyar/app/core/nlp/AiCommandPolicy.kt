package ir.kharjyar.app.core.nlp

/**
 * مرز اختیار دستیار: خواندن/مسیریابی سریع است، اما هر تغییر پایدار باید در UI
 * پیش‌نمایش شود و یک تأیید نهایی انسانی داشته باشد.
 */
object AiCommandPolicy {
    val immediateActions = setOf("navigate", "transaction_query", "report_query", "open_record")
    val confirmRequiredActions = setOf(
        "transaction", "loan_borrowed", "loan_lent", "debt_receivable", "debt_payable",
        "asset_record", "reminder_create", "check_issued", "check_received", "note_create",
        "vehicle_record", "installment_payment", "civic_record",
        // مدل فقط هدف را پیشنهاد می‌دهد؛ اجرا همیشه پس از تطبیق محلی و تأیید صریح است.
        "record_delete", "record_edit"
    )
    val forbiddenActions = setOf("delete", "bulk_delete", "change_balance", "execute_code", "export_secret")

    fun canExecuteImmediately(action:String)=action in immediateActions
    fun requiresConfirmation(action:String)=action in confirmRequiredActions
    fun isAllowed(action:String)=canExecuteImmediately(action)||requiresConfirmation(action)
}
