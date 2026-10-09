package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NotificationTransactionEditRouteGuardTest {
    @Test fun `phone and in app notifications expose transaction editing`() {
        val notifier = File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
        val review = File("src/main/java/ir/kharjyar/app/ui/screens/ReviewScreen.kt").readText()
        val activity = File("src/main/java/ir/kharjyar/app/MainActivity.kt").readText()
        val editor = File("src/main/java/ir/kharjyar/app/ui/screens/TransactionEditScreen.kt").readText()

        assertTrue(notifier.contains(".addAction(android.R.drawable.ic_menu_edit, \"ویرایش و تکمیل\", pi)"))
        assertTrue(review.contains("if (sms.status == SmsStatus.DRAFT_READY)"))
        assertTrue(review.contains("TextButton(onClick = open) { Text(\"ویرایش و تکمیل\") }"))
        assertTrue(activity.contains("Notifier.DEST_CONFIRM_TX -> if (txId > 0) \"tx/${'$'}txId\""))
        assertTrue(editor.contains("AccountPicker("))
        assertTrue(editor.contains("AmountTextField("))
        assertTrue(editor.contains("NaturePicker("))
        assertTrue(editor.contains("CategoryPicker("))
        assertTrue(editor.contains("DatePickerRow("))
    }
}
