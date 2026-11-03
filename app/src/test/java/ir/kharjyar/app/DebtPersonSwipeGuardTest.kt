package ir.kharjyar.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class DebtPersonSwipeGuardTest {
    private val screen = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
    private val dao = File("src/main/java/ir/kharjyar/app/data/db/ObligationDaos.kt").readText()

    @Test fun `person cards support swipe actions and retain detail navigation`() {
        assertTrue(screen.contains("key(person.id)"))
        assertTrue(screen.contains("SwipeActionRow(onEdit = { editingPerson = person }, onDelete = { deletingPerson = person }"))
        assertTrue(screen.contains(".clickable { selectedPersonId = person.id }"))
        assertTrue(screen.contains("updatePerson(person.copy(name = name.trim(), phone = phone.trim(), note = note.trim()))"))
    }

    @Test fun `deletion confirms full impact and removes records atomically without financial transactions`() {
        assertTrue(screen.contains("debtIdsOfPerson(person.id)"))
        assertTrue(screen.contains("حتی موارد خارج از فیلتر فعلی"))
        assertTrue(screen.contains("trashPerson(person.id, System.currentTimeMillis())"))
        assertTrue(screen.contains("Notifier.cancelDebtReminder(context, it)"))
        assertTrue(dao.contains("@Transaction suspend fun deletePersonWithDebts"))
        assertTrue(dao.contains("DELETE FROM debt_payments WHERE debtId IN (SELECT id FROM debts WHERE personId = :personId)"))
        assertTrue(dao.contains("DELETE FROM debts WHERE personId = :personId"))
        assertFalse(dao.contains("DELETE FROM transactions"))
    }
}
