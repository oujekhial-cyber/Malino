package ir.kharjyar.app.ui.components

import android.content.Context
import android.widget.Toast

/** بازخورد یکسان پس از ذخیره موفق؛ پیام پس از خروج از فرم نیز دیده می‌شود. */
fun showSavedMessage(context: Context, subject: String = "اطلاعات") {
    Toast.makeText(context, "$subject با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
}
