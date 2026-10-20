package ir.kharjyar.app

import ir.kharjyar.app.data.db.ReminderEntity
import ir.kharjyar.app.ui.screens.reminderDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReminderOpenSwipeGuardTest {
    private fun reminder(category:String,title:String="موعد",note:String="")=ReminderEntity(title=title,category=category,note=note,nextAt=1)

    @Test fun `reminders open their related app section`() {
        assertEquals("checks",reminderDestination(reminder("چک")))
        assertEquals("vehicles",reminderDestination(reminder("تعویض روغن خودرو")))
        assertEquals("loans",reminderDestination(reminder("قسط وام")))
        assertEquals("debts",reminderDestination(reminder("بدهی")))
        assertEquals("civicCenter",reminderDestination(reminder("قبض برق")))
        assertEquals("tx/42",reminderDestination(reminder("تراکنش",note="تراکنش #42")))
        assertEquals("reminders",reminderDestination(reminder("سایر")))
    }

    @Test fun `cards expose swipe edit and confirmed delete`() {
        val source=File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText()
        assertTrue(source.contains("SwipeActionRow(onDelete={pendingDelete=r},onEdit={editingReminder=r"))
        assertTrue(source.contains("title={Text(\"حذف یادآور\")}"))
        assertTrue(source.contains("if(existing==null)vm.repo.db.reminderDao().insert(value)else"))
        assertTrue(source.contains("ذخیره تغییرات یادآور"))
    }
}
